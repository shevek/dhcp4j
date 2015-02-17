/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.mina.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.anarres.dhcp.common.address.AddressUtils;
import org.apache.directory.server.dhcp.mina.protocol.DhcpProtocolCodecFactory;
import org.apache.directory.server.dhcp.mina.protocol.DhcpProtocolHandler;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.directory.server.dhcp.service.manager.LeaseManagerDhcpService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

/**
 *
 * @author shevek
 */
public class DhcpServer {

    private final int port;
    private final Iterable<NetworkInterface> interfaces;
    private final LoggingFilter logger_wire = new LoggingFilter("dhcp-wire");
    private final ProtocolCodecFilter codec = new ProtocolCodecFilter(DhcpProtocolCodecFactory.getInstance());
    private final LoggingFilter logger_packet = new LoggingFilter("dhcp-packet");
    private final DhcpProtocolHandler handler;
    private final Map<InetAddress, NioDatagramAcceptor> acceptors = new HashMap<InetAddress, NioDatagramAcceptor>();

    public DhcpServer(@Nonnull DhcpService service, @Nonnegative int port, @Nonnull Iterable<NetworkInterface> interfaces) {
        this.port = port;
        this.interfaces = interfaces;
        this.handler = new DhcpProtocolHandler(service);
    }

    public DhcpServer(@Nonnull DhcpService service) throws SocketException {
        this(service, DhcpService.SERVER_PORT, Collections.list(NetworkInterface.getNetworkInterfaces()));
    }

    public DhcpServer(@Nonnull LeaseManager leaseManager) throws SocketException {
        this(new LeaseManagerDhcpService(leaseManager));
    }

    private void bind(@Nonnull InetAddress address) throws IOException {
        if (AddressUtils.isZeroAddress(address))
            return;
        if (acceptors.containsKey(address))
            return;
        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        acceptor.bind(new InetSocketAddress(address, port));
        acceptor.getFilterChain().addLast("dhcp-wire", logger_wire);
        acceptor.getFilterChain().addLast("dhcp-codec", codec);
        acceptor.getFilterChain().addLast("dhcp-packet", logger_packet);
        acceptor.setHandler(handler);
        acceptors.put(address, acceptor);
    }

    @PostConstruct
    public void start() throws IOException {
        for (NetworkInterface iface : interfaces) {
            for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                bind(addr.getAddress());
                bind(addr.getBroadcast());
            }
        }
    }

    @PreDestroy
    public void stop() throws IOException {
        for (IoAcceptor acceptor : acceptors.values())
            acceptor.unbind();
        acceptors.clear();
    }
}
