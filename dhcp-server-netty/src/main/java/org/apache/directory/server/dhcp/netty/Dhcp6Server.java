/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.service.SimplePooledDhcp6LeaseManager;
import org.anarres.dhcp.v6.service.Dhcp6LeaseManager;
import org.anarres.dhcp.v6.service.Dhcp6Service;
import org.anarres.dhcp.v6.service.LeaseManagerDhcp6Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marosmars
 */
public class Dhcp6Server {

    private static final Logger LOG = LoggerFactory.getLogger(Dhcp6Server.class);

    private final Dhcp6Service service;
    private final int port;
    private Channel channel;
    private static final byte[] SERVER_ID = new byte[] { 0, 1 };

    public static void main(String[] args) {
        final InetAddress startingAddress;
        final InetAddress endingAddress;

        try {
            startingAddress = Inet6Address.getByName("fe80::a00:27ff:fe4f:7b7e");
            endingAddress = Inet6Address.getByName("fe80::a00:27ff:fe4f:7b7f");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        final Dhcp6Server dhcp6Server = new Dhcp6Server(new SimplePooledDhcp6LeaseManager(startingAddress,
            endingAddress), Dhcp6Service.SERVER_PORT, new DuidOption.Duid(new byte[]{1,2}));
        try {
            dhcp6Server.start();
            Thread.sleep(50000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Dhcp6Server(@Nonnull Dhcp6Service service, @Nonnegative int port) {
        this.service = service;
        this.port = port;
    }

    public Dhcp6Server(@Nonnull Dhcp6Service service) {
        this(service, Dhcp6Service.SERVER_PORT);
    }

    public Dhcp6Server(@Nonnull Dhcp6LeaseManager manager, @Nonnegative int port, final DuidOption.Duid serverId) {
        this(new LeaseManagerDhcp6Service(manager, serverId), port);
    }

    public Dhcp6Server(@Nonnull Dhcp6LeaseManager manager, final DuidOption.Duid serverId) {
        this(new LeaseManagerDhcp6Service(manager, serverId));
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        ThreadFactory factory = new DefaultThreadFactory("dhcp-server");
        EventLoopGroup group = new NioEventLoopGroup(0, factory);

        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioDatagramChannel.class);
        b.handler(new Dhcp6Handler(service, SERVER_ID));
        channel = b.bind(port).sync().channel();
        LOG.info("DHCPv6 server started on : {}, with id: {}", channel.localAddress(), SERVER_ID);
    }

    @PreDestroy
    public void stop() throws IOException, InterruptedException {
        EventLoop loop = channel.eventLoop();
        channel.close().sync();
        channel = null;
        loop.shutdownGracefully();
    }
}
