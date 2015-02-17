/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.directory.server.dhcp.service.manager.LeaseManagerDhcpService;

/**
 *
 * @author shevek
 */
public class DhcpServer {

    private final DhcpService service;
    private final SocketAddress address;
    private Channel channel;

    public DhcpServer(@Nonnull DhcpService service, @Nonnull SocketAddress address) {
        this.service = service;
        this.address = address;
    }

    public DhcpServer(@Nonnull DhcpService service) {
        this(service, new InetSocketAddress(DhcpService.SERVER_PORT));
    }

    public DhcpServer(@Nonnull LeaseManager manager, @Nonnull SocketAddress address) {
        this(new LeaseManagerDhcpService(manager), address);
    }

    public DhcpServer(@Nonnull LeaseManager manager) {
        this(new LeaseManagerDhcpService(manager));
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        ThreadFactory factory = new DefaultThreadFactory("dhcp-server");
        EventLoopGroup group = new NioEventLoopGroup(0, factory);

        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioDatagramChannel.class);
        b.option(ChannelOption.SO_BROADCAST, true);
        b.handler(new DhcpHandler(service));
        channel = b.bind(address).sync().channel();
    }

    @PreDestroy
    public void stop() throws IOException, InterruptedException {
        EventLoop loop = channel.eventLoop();
        channel.close().sync();
        loop.shutdownGracefully();
    }
}
