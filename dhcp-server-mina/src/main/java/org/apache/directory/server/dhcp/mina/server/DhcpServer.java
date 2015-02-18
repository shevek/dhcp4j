/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.directory.server.dhcp.io.DhcpInterfaceResolver;
import org.apache.directory.server.dhcp.mina.protocol.DhcpProtocolCodecFactory;
import org.apache.directory.server.dhcp.mina.protocol.DhcpProtocolHandler;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.directory.server.dhcp.service.manager.LeaseManagerDhcpService;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

/**
 *
 * @author shevek
 */
public class DhcpServer extends DhcpInterfaceResolver {

    private final LoggingFilter logger_wire = new LoggingFilter("dhcp-wire");
    private final ProtocolCodecFilter codec = new ProtocolCodecFilter(DhcpProtocolCodecFactory.getInstance());
    private final LoggingFilter logger_packet = new LoggingFilter("dhcp-packet");
    private final DhcpService service;
    private final int port;
    private NioDatagramAcceptor acceptor;

    public DhcpServer(@Nonnull DhcpService service, @Nonnegative int port) {
        this.service = service;
        this.port = port;
    }

    public DhcpServer(@Nonnull DhcpService service) {
        this(service, DhcpService.SERVER_PORT);
    }

    public DhcpServer(@Nonnull LeaseManager leaseManager) throws SocketException {
        this(new LeaseManagerDhcpService(leaseManager));
    }

    @PostConstruct
    @Override
    public void start() throws IOException, InterruptedException {
        super.start();

        NioDatagramAcceptor a = new NioDatagramAcceptor();
        a.bind(new InetSocketAddress(port));
        a.getFilterChain().addLast("dhcp-wire", logger_wire);
        a.getFilterChain().addLast("dhcp-codec", codec);
        a.getFilterChain().addLast("dhcp-packet", logger_packet);
        a.setHandler(new DhcpProtocolHandler(service, this));
        this.acceptor = a;
    }

    // @NotThreadSafe
    @PreDestroy
    @Override
    public void stop() throws IOException, InterruptedException {
        acceptor.unbind();
        acceptor = null;

        super.stop();
    }
}
