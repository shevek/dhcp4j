/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.io;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.options.dhcp.ServerIdentifier;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class DhcpInterfaceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpInterfaceUtils.class);

    /** If this returns an address, it is NOT a zero address. */
    @CheckForNull
    private static InetAddress toInetAddress(@Nonnull DhcpMessage message) throws DhcpException {
        // From the packet:
        InetAddress assignedClientAddress = message.getAssignedClientAddress();
        if (!AddressUtils.isZeroAddress(assignedClientAddress))
            return assignedClientAddress;
        InetAddress currentClientAddress = message.getCurrentClientAddress();
        if (!AddressUtils.isZeroAddress(currentClientAddress))
            return currentClientAddress;
        // TODO: Should this catch locally?
        InetAddress requestedClientAddress = message.getOptions().getAddressOption(RequestedIpAddress.class);
        if (!AddressUtils.isZeroAddress(requestedClientAddress))
            return requestedClientAddress;
        InetAddress serverAddress = message.getOptions().getAddressOption(ServerIdentifier.class);
        if (!AddressUtils.isZeroAddress(serverAddress))
            return serverAddress;
        InetAddress relayAgentAddress = message.getRelayAgentAddress();
        if (!AddressUtils.isZeroAddress(relayAgentAddress))
            return relayAgentAddress;
        return null;
    }

    /** If this returns an address, it is NOT a zero address. */
    @CheckForNull
    private static InetAddress toInetAddress(@Nonnull SocketAddress sa) {
        if (!(sa instanceof InetSocketAddress))
            return null;
        InetSocketAddress isa = (InetSocketAddress) sa;
        InetAddress ia = isa.getAddress();
        if (!AddressUtils.isZeroAddress(ia))
            return ia;
        return null;
    }

    /** If this returns an address, it is NOT a zero address. */
    @CheckForNull
    public static InetAddress toInetAddress(@Nonnull Object... objects) throws DhcpException {
        for (Object object : objects) {
            if (object == null)
                continue;
            if (object instanceof InetAddress) {
                InetAddress a = (InetAddress) object;
                if (!AddressUtils.isZeroAddress(a))
                    return a;
            } else if (object instanceof SocketAddress) {
                InetAddress a = toInetAddress((SocketAddress) object);
                if (a != null)
                    return a;
            } else if (object instanceof DhcpMessage) {
                InetAddress a = toInetAddress((DhcpMessage) object);
                if (a != null)
                    return a;
            } else {
                throw new IllegalArgumentException("Cannot derive an InetAddress from " + object.getClass());
            }
        }
        // LOG.debug("Failed to determine message address from " + Arrays.toString(objects));
        return null;
    }

    /**
     * Determine where to send the message: <br>
     * If the 'giaddr' field in a DHCP message from a client is non-zero, the
     * server sends any return messages to the 'DHCP server' port on the BOOTP
     * relay agent whose address appears in 'giaddr'. If the 'giaddr' field is
     * zero and the 'ciaddr' field is nonzero, then the server unicasts DHCPOFFER
     * and DHCPACK messages to the address in 'ciaddr'. If 'giaddr' is zero and
     * 'ciaddr' is zero, and the broadcast bit is set, then the server broadcasts
     * DHCPOFFER and DHCPACK messages to 0xffffffff. If the broadcast bit is not
     * set and 'giaddr' is zero and 'ciaddr' is zero, then the server unicasts
     * DHCPOFFER and DHCPACK messages to the client's hardware address and
     * 'yiaddr' address. In all cases, when 'giaddr' is zero, the server
     * broadcasts any DHCPNAK messages to 0xffffffff.
     */
    @Nonnull
    //This will suppress PMD.AvoidUsingHardCodedIP warnings in this class
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    public static InetSocketAddress determineMessageDestination(
            @Nonnull DhcpMessage request,
            @Nonnull DhcpMessage reply,
            @Nonnull InterfaceAddress localAddress,
            @Nonnegative int remotePort
    ) {
        if (!AddressUtils.isZeroAddress(request.getRelayAgentAddress())) {
            // send to agent, if received via agent.
            return new InetSocketAddress(request.getRelayAgentAddress(), DhcpService.SERVER_PORT);
        } else if (reply.getMessageType() == MessageType.DHCPNAK) {
            // force broadcast for DHCPNAKs
            return new InetSocketAddress(localAddress.getBroadcastAddress(), remotePort);
        } else if (!AddressUtils.isZeroAddress(request.getCurrentClientAddress())) {
            // have a current address? unicast to it.
            return new InetSocketAddress(request.getCurrentClientAddress(), remotePort);
        } else {
            // not a NAK...
            return new InetSocketAddress(localAddress.getBroadcastAddress(), remotePort);
        }
    }

    private DhcpInterfaceUtils() {
    }
}
