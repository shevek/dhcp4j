/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.mina;

import com.google.common.net.InetAddresses;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpInterfaceManager;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.messages.HardwareAddressType;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.mina.protocol.DhcpProtocolHandler;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.directory.server.dhcp.service.manager.LeaseManagerDhcpService;
import org.apache.directory.server.dhcp.service.store.SimpleStoreLeaseManager;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.DummySession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class DhcpProtocolHandlerTest {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpProtocolHandlerTest.class);
    private final HardwareAddress hardwareAddress = new HardwareAddress(HardwareAddressType.Ethernet, new byte[]{1, 2, 3, 4, 5, 6});

    @Nonnull
    private DhcpMessage newRequest(@Nonnull MessageType type) {
        DhcpMessage request = new DhcpMessage();
        request.setOp(DhcpMessage.OP_BOOTREQUEST);
        request.setMessageType(type);
        request.setHardwareAddress(hardwareAddress);
        return request;
    }

    @Test
    public void testProtocolHandler() throws Exception {
        DhcpMessage request = newRequest(MessageType.DHCPDISCOVER);
        request.getOptions().setAddressOption(RequestedIpAddress.class, "0.0.0.0");
        DummySession session = new DummySession() {
            {
                setLocalAddress(new InetSocketAddress("0.0.0.0", DhcpService.SERVER_PORT));
                setRemoteAddress(new InetSocketAddress("0.0.0.0", DhcpService.CLIENT_PORT));
            }

            @Override
            public WriteFuture write(Object message, SocketAddress remoteAddress) {
                WriteFuture future = new DefaultWriteFuture(this);
                WriteRequest request = new DefaultWriteRequest(message, future, remoteAddress);
                increaseWrittenMessages(request, System.currentTimeMillis());

                // IoFilterChain filterChain = getFilterChain();
                // filterChain.fireFilterWrite(request);
                LOG.info("Message " + getWrittenMessages() + " is " + message);
                return future;
            }
        };

        LeaseManager manager = new SimpleStoreLeaseManager();
        DhcpService service = new LeaseManagerDhcpService(manager);
        InterfaceAddress address = new InterfaceAddress(InetAddresses.forString("10.1.2.3"), 24);
        DhcpProtocolHandler protocolHandler = new DhcpProtocolHandler(service, new DhcpInterfaceManager());
        protocolHandler.messageReceived(session, request);
        // Right now, this isn't configured, so it doesn't respond.
        // assertEquals(1, session.getWrittenMessages());
    }
}
