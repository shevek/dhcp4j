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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.directory.server.dhcp.address.AddressUtils;
import org.apache.directory.server.dhcp.mina.protocol.DhcpProtocolCodecFactory;
import org.apache.directory.server.dhcp.mina.protocol.DhcpProtocolHandler;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

/**
 *
 * @author shevek
 */
public class DhcpServer {

    private final DhcpService service;
    private final int port;
    private final ProtocolCodecFilter codec = new ProtocolCodecFilter(DhcpProtocolCodecFactory.getInstance());
    private final List<NioDatagramAcceptor> acceptors = new ArrayList<NioDatagramAcceptor>();

    public DhcpServer(@Nonnull DhcpService service, int port) {
        this.service = service;
        this.port = port;
    }

    public DhcpServer(@Nonnull DhcpService service) {
        this(service, DhcpProtocolHandler.SERVER_PORT);
    }

    private void bind(Set<InetAddress> addresses, InetAddress address) throws IOException {
        if (AddressUtils.isZeroAddress(address))
            return;
        if (!addresses.add(address))
            return;
        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        acceptor.bind(new InetSocketAddress(address, port));
        acceptor.getFilterChain().addLast("dhcp-wire", new LoggingFilter("dhcp-wire"));
        acceptor.getFilterChain().addLast("dhcp-codec", codec);
        acceptor.getFilterChain().addLast("dhcp-packet", new LoggingFilter("dhcp-packet"));
        acceptor.setHandler(new DhcpProtocolHandler(service));
        acceptors.add(acceptor);
    }

    @PostConstruct
    public void start() throws IOException {
        Set<InetAddress> addresses = new HashSet<InetAddress>();
        for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                bind(addresses, addr.getAddress());
                bind(addresses, addr.getBroadcast());
            }
        }
    }

    @PreDestroy
    public void stop() throws IOException {
        for (IoAcceptor acceptor : acceptors)
            acceptor.unbind();
    }
}
