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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Iterator;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.options.dhcp.ClientIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.IpAddressLeaseTime;
import org.apache.directory.server.dhcp.options.dhcp.MaximumDhcpMessageSize;
import org.apache.directory.server.dhcp.options.dhcp.ParameterRequestList;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.options.dhcp.ServerIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public abstract class AbstractDhcpService extends AbstractDhcpReplyFactory implements DhcpService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDhcpService.class);

    @Override
    public DhcpMessage getReplyFor(
            DhcpRequestContext context,
            DhcpMessage request)
            throws DhcpException {
        Preconditions.checkNotNull(context, "DhcpRequestContext was null.");

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
        if (!AddressUtils.isZeroAddress(serverAddress)) {
            FIND:
            {
                for (InterfaceAddress localAddress : context.getInterfaceAddresses())
                    if (Objects.equal(localAddress.getAddress(), serverAddress))
                        break FIND;
                LOG.debug("Request directed to " + serverAddress + ", not to this server: " + request);
                return null;
            }
        }

        DhcpMessage reply;
        // dispatch based on the message type
        switch (request.getMessageType()) {
            case DHCPDISCOVER:
                reply = handleDISCOVER(context, request);
                break;

            case DHCPOFFER:
                reply = handleOFFER(context, request);
                break;

            case DHCPREQUEST:
                reply = handleREQUEST(context, request);
                break;

            case DHCPDECLINE:
                reply = handleDECLINE(context, request);
                break;

            case DHCPRELEASE:
                reply = handleRELEASE(context, request);
                break;

            case DHCPINFORM:
                reply = handleINFORM(context, request);
                break;

            case DHCPACK:
            case DHCPNAK:
                reply = null; // just ignore them
                break;

            default:
                reply = handleUnknownMessage(context, request);
                break;
        }

        return reply;
    }

    /**
     * Handle DHCPDISCOVER message. The default implementation just ignores it.
     *
     * @param context
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     * it.
     * @throws DhcpException
     */
    @CheckForNull
    protected DhcpMessage handleDISCOVER(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got DISCOVER message: " + request + " in " + context);
        return null;
    }

    /**
     * Handle DHCPOFFER message. The default implementation just ignores it.
     *
     * @param context
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     * it.
     * @throws DhcpException
     */
    @CheckForNull
    protected DhcpMessage handleOFFER(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got OFFER message: " + request + " in " + context);
        return null;
    }

    /**
     * Handle DHCPREQUEST message. The default implementation just ignores it.
     *
     * @param context
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     * it.
     */
    @CheckForNull
    protected DhcpMessage handleREQUEST(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got REQUEST message: " + request + " in " + context);
        return null;
    }

    /**
     * Handle DHCPDECLINE message. The default implementation just ignores it.
     *
     * @param context
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     * it.
     */
    @CheckForNull
    protected DhcpMessage handleDECLINE(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got DECLINE message: " + request + " in " + context);
        return null;
    }

    /**
     * Handle DHCPRELEASE message. The default implementation just ignores it.
     *
     * @param context
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     * it.
     */
    @CheckForNull
    protected DhcpMessage handleRELEASE(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got RELEASE message: " + request + " in " + context);
        return null;
    }

    /**
     * Handle DHCPINFORM message. The default implementation just ignores it.
     *
     * @param context
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     * it.
     */
    @CheckForNull
    protected DhcpMessage handleINFORM(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request)
            throws DhcpException {
        if (LOG.isDebugEnabled())
            LOG.debug("Got INFORM message: " + request + " in " + context);
        return null;
    }

    /**
     * Handle unknown DHCP message. The default implementation just logs and
     * ignores it.
     *
     * @param context
     * @param request the request message
     * @return DhcpMessage response message or <code>null</code> to ignore (don't reply to)
     * it.
     */
    @CheckForNull
    protected DhcpMessage handleUnknownMessage(@Nonnull DhcpRequestContext context, @Nonnull DhcpMessage request) {
        if (LOG.isWarnEnabled())
            LOG.warn("Got unknkown DHCP message: " + request + " in " + context);
        return null;
    }

    /**
     * Determine address on which to base selection. If the relay agent address is
     * set, we use the relay agent's address, otherwise we use the address we
     * received the request from.
     *
     * @return InetAddress
     */
    @Nonnull
    @Deprecated // This isn't used any more.
    public static InetAddress getRemoteAddress(
            @Nonnull InterfaceAddress localAddress,
            @CheckForNull InetSocketAddress remoteAddress,
            @Nonnull DhcpMessage request)
            throws DhcpException {
        // FIXME: do we know
        // a) the interface address over which we received a message (!)
        // b) the client address (if specified)
        // c) the relay agent address?

        InetAddress assignedClientAddress = request.getAssignedClientAddress();
        if (!AddressUtils.isZeroAddress(assignedClientAddress))
            return assignedClientAddress;
        InetAddress currentClientAddress = request.getCurrentClientAddress();
        if (!AddressUtils.isZeroAddress(currentClientAddress))
            return currentClientAddress;
        InetAddress requestedClientAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (!AddressUtils.isZeroAddress(requestedClientAddress))
            return requestedClientAddress;

        // if the relay agent address is set, we use it as the selection base
        if (!AddressUtils.isZeroAddress(request.getRelayAgentAddress()))
            return request.getRelayAgentAddress();
        if (remoteAddress != null)
            return remoteAddress.getAddress();
        return localAddress.getAddress();
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
            if (o instanceof IpAddressLeaseTime)
                continue;
            if (Arrays.binarySearch(tags, o.getTag()) >= 0)
                continue;
            i.remove();
        }
    }
}
