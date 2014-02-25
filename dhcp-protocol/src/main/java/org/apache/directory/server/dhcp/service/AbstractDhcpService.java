/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package org.apache.directory.server.dhcp.service;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.address.AddressUtils;
import org.apache.directory.server.dhcp.address.InterfaceAddress;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.options.dhcp.ClientIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.IpAddressLeaseTime;
import org.apache.directory.server.dhcp.options.dhcp.MaximumDhcpMessageSize;
import org.apache.directory.server.dhcp.options.dhcp.ParameterRequestList;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.options.dhcp.ServerIdentifier;

/**
 * Abstract implementation of the server-side DHCP protocol. This class just
 * provides some utility methods and dispatches server-bound messages to handler
 * methods which can be overridden to provide the functionality.
 * <p>
 * Client-bound messages and BOOTP messages are ignored.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * 
 */
public abstract class AbstractDhcpService implements DhcpService {

    private static final Log LOG = LogFactory.getLog(AbstractDhcpService.class);

    @Override
    public DhcpMessage getReplyFor(
            InterfaceAddress localAddress,
            InetSocketAddress clientAddress,
            DhcpMessage request)
            throws DhcpException {
        Preconditions.checkNotNull(localAddress, "LocalAddress was null.");

        // ignore messages with an op != REQUEST/REPLY
        if ((request.getOp() != DhcpMessage.OP_BOOTREQUEST)
                && (request.getOp() != DhcpMessage.OP_BOOTREPLY)) {
            LOG.warn("Request operator is not BOOTREQUEST or BOOTREPLY: " + request);
            return null;
        }

        // message type option MUST be set - we don't support plain BOOTP.
        if (request.getMessageType() == null) {
            LOG.warn("Request is missing message type - plain BOOTP not supported: " + request);
            return null;
        }

        if (request.getHardwareAddress() == null) {
            LOG.warn("Request is missing hardware address: " + request);
            return null;
        }

        // From StoreBasedDhcpService
        InetAddress serverAddress = request.getOptions().getAddressOption(ServerIdentifier.class);
        if (!AddressUtils.isZeroAddress(serverAddress) && !AddressUtils.isZeroAddress(localAddress.getAddress())) {
            if (!Objects.equal(localAddress.getAddress(), serverAddress)) {
                LOG.debug("Request is not to this server: " + request);
                return null;
            }
        }

        // dispatch based on the message type
        switch (request.getMessageType()) {
            case DHCPDISCOVER:
                return handleDISCOVER(localAddress, clientAddress, request);

            case DHCPOFFER:
                return handleOFFER(localAddress, clientAddress, request);

            case DHCPREQUEST:
                return handleREQUEST(localAddress, clientAddress, request);

            case DHCPDECLINE:
                return handleDECLINE(localAddress, clientAddress, request);

            case DHCPRELEASE:
                return handleRELEASE(localAddress, clientAddress, request);

            case DHCPINFORM:
                return handleINFORM(localAddress, clientAddress, request);

            case DHCPACK:
            case DHCPNAK:
                return null; // just ignore them

            default:
                return handleUnknownMessage(localAddress, clientAddress, request);
        }
    }

    /**
     * Handle DHCPDISCOVER message. The default implementation just ignores it.
     * 
     * @param localAddress
     * @param clientAddress
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     *         it.
     * @throws DhcpException
     */
    @CheckForNull
    protected DhcpMessage handleDISCOVER(@Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress clientAddress, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got DISCOVER message: " + request + " from " + clientAddress);
        return null;
    }

    /**
     * Handle DHCPOFFER message. The default implementation just ignores it.
     * 
     * @param localAddress
     * @param clientAddress
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     *         it.
     * @throws DhcpException
     */
    @CheckForNull
    protected DhcpMessage handleOFFER(@Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress clientAddress, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got OFFER message: " + request + " from " + clientAddress);
        return null;
    }

    /**
     * Handle DHCPREQUEST message. The default implementation just ignores it.
     * 
     * @param localAddress
     * @param clientAddress
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     *         it.
     */
    @CheckForNull
    protected DhcpMessage handleREQUEST(@Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress clientAddress, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got REQUEST message: " + request + " from " + clientAddress);
        return null;
    }

    /**
     * Handle DHCPDECLINE message. The default implementation just ignores it.
     * 
     * @param localAddress
     * @param clientAddress
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     *         it.
     */
    @CheckForNull
    protected DhcpMessage handleDECLINE(@Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress clientAddress, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got DECLINE message: " + request + " from " + clientAddress);
        return null;
    }

    /**
     * Handle DHCPRELEASE message. The default implementation just ignores it.
     * 
     * @param localAddress
     * @param clientAddress
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     *         it.
     */
    @CheckForNull
    protected DhcpMessage handleRELEASE(@Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress clientAddress, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got RELEASE message: " + request + " from " + clientAddress);
        return null;
    }

    /**
     * Handle DHCPINFORM message. The default implementation just ignores it.
     * 
     * @param localAddress
     * @param clientAddress
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     *         it.
     */
    @CheckForNull
    protected DhcpMessage handleINFORM(@Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress clientAddress, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got INFORM message: " + request + " from " + clientAddress);
        return null;
    }

    /**
     * Handle unknown DHCP message. The default implementation just logs and
     * ignores it.
     * 
     * @param clientAddress
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     *         it.
     */
    @CheckForNull
    protected DhcpMessage handleUnknownMessage(@Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress clientAddress, @Nonnull DhcpMessage request) {
        if (LOG.isWarnEnabled())
            LOG.warn("Got unknkown DHCP message: " + request + " from " + clientAddress);
        return null;
    }

    /**
     * Determine address on which to base selection. If the relay agent address is
     * set, we use the relay agent's address, otherwise we use the address we
     * received the request from.
     * 
     * @param clientAddress
     * @param request
     * @return InetAddress
     */
    @Nonnull
    protected InetAddress getRemoteAddress(
            @Nonnull InterfaceAddress localAddress,
            @Nonnull DhcpMessage request,
            @CheckForNull InetSocketAddress clientAddress) {
        // FIXME: do we know
        // a) the interface address over which we received a message (!)
        // b) the client address (if specified)
        // c) the relay agent address?

        // if the relay agent address is set, we use it as the selection base
        if (!AddressUtils.isZeroAddress(request.getRelayAgentAddress()))
            return request.getRelayAgentAddress();
        if (clientAddress != null)
            return clientAddress.getAddress();
        return localAddress.getAddress();
    }

    /**
     * Initialize a general DHCP reply message. Sets:
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
     * @param localAddress
     * @param request
     * @return DhcpMessage
     */
    @Nonnull
    public static DhcpMessage newReply(
            @Nonnull InterfaceAddress localAddress,
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
        // set server hostname
        // set server identifier based on the IF on which we received the packet
        InetAddress serverAddress = localAddress.getAddress();
        if (!AddressUtils.isZeroAddress(serverAddress)) {
            reply.setServerHostname(InetAddresses.toAddrString(serverAddress));
            reply.getOptions().add(new ServerIdentifier(serverAddress));
        }

        return reply;
    }

    @Nonnull
    public static DhcpMessage newReplyNak(
            @Nonnull InterfaceAddress localAddress,
            @Nonnull DhcpMessage request) {
        DhcpMessage reply = newReply(localAddress, request, MessageType.DHCPNAK);
        reply.setMessageType(MessageType.DHCPNAK);
        reply.setCurrentClientAddress(null);
        reply.setAssignedClientAddress(null);
        reply.setNextServerAddress(null);
        return reply;
    }

    @Nonnull
    public static DhcpMessage newReply(
            @Nonnull InterfaceAddress localAddress,
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type,
            @CheckForSigned long leaseTimeSecs,
            @CheckForNull InetAddress assignedClientAddress,
            @CheckForNull InetAddress nextServerAddress,
            @CheckForNull String bootFileName) {
        DhcpMessage reply = newReply(localAddress, request, type);
        if (leaseTimeSecs > 0)
            reply.getOptions().setIntOption(IpAddressLeaseTime.class, leaseTimeSecs);
        if (assignedClientAddress != null)
            reply.setAssignedClientAddress(assignedClientAddress);
        if (nextServerAddress != null)
            reply.setNextServerAddress(nextServerAddress);
        if (bootFileName != null)
            reply.setBootFileName(bootFileName);
        return reply;
    }

    /**
     * Strip options that the client doesn't want, if the ParameterRequestList
     * option is present.
     * 
     * @param request
     * @param options
     */
    protected static void stripOptions(@Nonnull DhcpMessage request, @Nonnull OptionsField options) {
        // these options must not be present
        options.remove(RequestedIpAddress.class);
        options.remove(ParameterRequestList.class);
        options.remove(ClientIdentifier.class);
        options.remove(MaximumDhcpMessageSize.class);

        ParameterRequestList prl = request.getOptions().get(ParameterRequestList.class);
        if (prl == null)
            return;

        byte[] tags = prl.getData();
        Arrays.sort(tags);

        for (Iterator<DhcpOption> i = options.iterator(); i.hasNext();) {
            DhcpOption o = i.next();
            if (o instanceof ServerIdentifier)
                continue;
            if (Arrays.binarySearch(tags, o.getTag()) >= 0)
                continue;
            i.remove();
        }
    }
}
