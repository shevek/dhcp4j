/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.dhcp.BootfileName;
import org.apache.directory.server.dhcp.options.dhcp.IpAddressLeaseTime;
import org.apache.directory.server.dhcp.options.dhcp.ServerIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.TftpServerName;

/**
 *
 * @author shevek
 */
public abstract class AbstractDhcpReplyFactory {

    public static class LeaseTimeRange {

        @Nonnegative
        public long minLeaseTime = 60;
        @Nonnegative
        public long defaultLeaseTime = 60;
        @Nonnegative
        public long maxLeaseTime = 60;

        public LeaseTimeRange() {
        }

        public LeaseTimeRange(long minLeaseTime, long defaultLeaseTime, long maxLeaseTime) {
            this.minLeaseTime = minLeaseTime;
            this.defaultLeaseTime = defaultLeaseTime;
            this.maxLeaseTime = maxLeaseTime;
        }
    }
    public final LeaseTimeRange TTL_OFFER = new LeaseTimeRange(60, 600, 600);
    public final LeaseTimeRange TTL_LEASE = new LeaseTimeRange(60, 3600, 36000);

    @Nonnegative
    protected static long getLeaseTime(@Nonnull LeaseTimeRange leaseTimeSecs, @CheckForSigned long requestedLeaseTimeSecs) {
        if (requestedLeaseTimeSecs < 0)
            return leaseTimeSecs.defaultLeaseTime;
        if (requestedLeaseTimeSecs <= leaseTimeSecs.minLeaseTime)
            return leaseTimeSecs.minLeaseTime;
        if (requestedLeaseTimeSecs >= leaseTimeSecs.maxLeaseTime)
            return leaseTimeSecs.maxLeaseTime;
        return requestedLeaseTimeSecs;
    }

    /**
     * Initializes a general DHCP reply message.
     *
     * Sets:
     * <ul>
     * <li>op=BOOTREPLY
     * <li>htype, hlen, xid, flags, giaddr, chaddr like in request message
     * <li>hops, secs to 0.
     * <li>server hostname to the hostname appropriate for the interface the
     * request was received on
     * <li>the server identifier set to the address of the interface the request
     * was received on
     * </ul>
     *
     * @param request
     * @return DhcpMessage
     */
    @Nonnull
    protected static DhcpMessage newReply(
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type) {
        DhcpMessage reply = new DhcpMessage();

        reply.setOp(DhcpMessage.OP_BOOTREPLY);
        reply.setMessageType(type);

        reply.setHardwareAddress(request.getHardwareAddress());
        reply.setTransactionId(request.getTransactionId());
        reply.setFlags(request.getFlags());
        reply.setRelayAgentAddress(request.getRelayAgentAddress());

        /* I think these are forbidden in a reply, which seems odd, as they
         * are useful for disambiguation.

         byte[] clientIdentifier = request.getOptions().getOption(ClientIdentifier.class);
         if (clientIdentifier != null)
         reply.getOptions().setOption(ClientIdentifier.class, clientIdentifier);
         byte[] uuidClientIdentifier = request.getOptions().getOption(UUIDClientIdentifier.class);
         if (uuidClientIdentifier != null)
         reply.getOptions().setOption(UUIDClientIdentifier.class, uuidClientIdentifier);
         */
        return reply;
    }

    /** Utility: Constructs a new NAK reply. */
    @Nonnull
    protected static DhcpMessage newReplyNak(
            @Nonnull DhcpMessage request) {
        DhcpMessage reply = newReply(request, MessageType.DHCPNAK);
        reply.setMessageType(MessageType.DHCPNAK);
        reply.setCurrentClientAddress(null);
        reply.setAssignedClientAddress(null);
        reply.setNextServerAddress(null);
        return reply;
    }

    /** Utility: Constructs a new ACK reply. */
    @Nonnull
    protected static DhcpMessage newReplyAck(
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type,
            @CheckForNull InetAddress assignedClientAddress,
            @CheckForSigned long leaseTimeSecs) {
        DhcpMessage reply = newReply(request, type);
        if (leaseTimeSecs > 0)
            reply.getOptions().setIntOption(IpAddressLeaseTime.class, leaseTimeSecs);
        if (assignedClientAddress != null)
            reply.setAssignedClientAddress(assignedClientAddress);
        return reply;
    }

    /** Utility: Sets the ServerIdentifier option in the reply. */
    protected static void setServerIdentifier(
            @Nonnull DhcpMessage reply,
            @Nonnull InetAddress localAddress) {
        if (!AddressUtils.isZeroAddress(localAddress)) {
            reply.setServerHostname(InetAddresses.toAddrString(localAddress));
            reply.getOptions().add(new ServerIdentifier(localAddress));
        }
    }

    /** Utility: Sets the ServerIdentifier option in the reply. */
    protected static void setServerIdentifier(
            @Nonnull DhcpMessage reply,
            @Nonnull InterfaceAddress localAddress) {
        setServerIdentifier(reply, localAddress.getAddress());
    }

    /** Utility: calls {@link DhcpRequestContext#setClientAddress(InetAddress)} if required. */
    protected static void setClientAddress(
            @Nonnull DhcpRequestContext context,
            @CheckForNull InetAddress address) {
        if (!AddressUtils.isZeroAddress(context.getClientAddress()))
            return;
        if (AddressUtils.isZeroAddress(address))
            return;
        context.setClientAddress(address);
    }

    /** Utility: Makes best effort to set the ServerIdentifier option in the reply. */
    protected static void setServerIdentifier(
            @Nonnull DhcpRequestContext context,
            @Nonnull DhcpMessage reply) {
        setClientAddress(context, reply.getAssignedClientAddress());
        setServerIdentifier(reply, context.getInterfaceAddress());
    }

    /** Utility: Sets the given boot parameter fields in the reply. */
    protected static void setBootParameters(
            @Nonnull DhcpMessage reply,
            @CheckForNull InetAddress nextServerAddress,
            @CheckForNull String bootFileName) {
        if (nextServerAddress != null) {
            reply.setNextServerAddress(nextServerAddress);
            reply.getOptions().setStringOption(TftpServerName.class, InetAddresses.toAddrString(nextServerAddress));
        }
        if (bootFileName != null) {
            reply.setBootFileName(bootFileName);
            reply.getOptions().setStringOption(BootfileName.class, bootFileName);
        }
    }
}
