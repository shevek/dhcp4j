/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.dhcp.mina.protocol;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.MDCUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpInterfaceManager;
import org.apache.directory.server.dhcp.io.DhcpInterfaceUtils;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a DHCP protocol handler which delegates the work of
 * generating replys to a DhcpService implementation.
 *
 * @see org.apache.directory.server.dhcp.service.DhcpService
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DhcpProtocolHandler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpProtocolHandler.class);
    /**
     * The DHCP service implementation. The implementation is supposed to be
     * thread-safe.
     */
    private final DhcpService dhcpService;
    private final DhcpInterfaceManager interfaceManager;

    public DhcpProtocolHandler(@Nonnull DhcpService dhcpService, @Nonnull DhcpInterfaceManager interfaceManager) {
        this.dhcpService = dhcpService;
        this.interfaceManager = interfaceManager;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        LOG.debug("{} CREATED", session.getLocalAddress());
        session.getFilterChain().addFirst("codec",
                new ProtocolCodecFilter(DhcpProtocolCodecFactory.getInstance()));
    }

    @Override
    public void sessionOpened(IoSession session) {
        LOG.debug("{} -> {} OPENED", session.getRemoteAddress(), session.getLocalAddress());
    }

    @Override
    public void sessionClosed(IoSession session) {
        LOG.debug("{} -> {} CLOSED", session.getRemoteAddress(), session.getLocalAddress());
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        // ignore
    }

    private static void debug(@Nonnull String event, @Nonnull SocketAddress src, @Nonnull SocketAddress dst, @Nonnull DhcpMessage msg) {
        if (LOG.isDebugEnabled())
            LOG.debug("{} {} -> {} {}", event, src, dst, msg);
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        if (LOG.isDebugEnabled())
            LOG.debug("{} -> {} RCVD: {}", session.getRemoteAddress(), session.getLocalAddress(), message);
        DhcpMessage request = (DhcpMessage) message;
        InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();

        // This doesn't work in practice. Pass the InterfaceAddress to the constructor.
        // InetSocketAddress localSocketAddress = (InetSocketAddress) session.getLocalAddress();
        // InterfaceAddress localAddress = new InterfaceAddress(localSocketAddress.getAddress(), 0);
        DhcpRequestContext context = interfaceManager.newRequestContext(
                (InetSocketAddress) session.getServiceAddress(),
                (InetSocketAddress) session.getLocalAddress(),
                (InetSocketAddress) session.getRemoteAddress(),
                request);
        if (context == null) {
            debug("IGNQUERY", session.getRemoteAddress(), session.getLocalAddress(), request);
            return;
        }

        MDCUtils.init(context, request);
        try {
            DhcpMessage reply = dhcpService.getReplyFor(context, request);
            if (reply == null) {
                debug("NOREPLY", session.getRemoteAddress(), session.getLocalAddress(), request);
                return;
            }

            InterfaceAddress localAddress = interfaceManager.getResponseInterface(
                    request.getRelayAgentAddress(),
                    request.getCurrentClientAddress(),
                    session.getRemoteAddress(),
                    reply
            );
            if (localAddress == null) {
                debug("NOIFACE", session.getRemoteAddress(), session.getLocalAddress(), reply);
                return;
            }

            InetSocketAddress isa = DhcpInterfaceUtils.determineMessageDestination(
                    request, reply,
                    localAddress, remoteAddress.getPort());
            session.write(reply, isa);
        } finally {
            MDCUtils.fini();
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} -> {} SENT: " + message, session.getRemoteAddress(),
                    session.getLocalAddress());
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        LOG.error("EXCEPTION CAUGHT ", cause);
        cause.printStackTrace(System.out);
        session.close(true);
    }
}
