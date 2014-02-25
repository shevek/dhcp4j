/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.io.IOException;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.directory.server.dhcp.service.DhcpService;

/**
 *
 * @author shevek
 */
public class DhcpServer {

    private final DhcpService service;
    private final int port;
    private EventLoopGroup group;
    private Channel channel;

    public DhcpServer(@Nonnull DhcpService service, @Nonnegative int port) {
        this.service = service;
        this.port = port;
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioDatagramChannel.class);
        b.option(ChannelOption.SO_BROADCAST, true);
        b.handler(new DhcpHandler(service));
        channel = b.bind(port).sync().channel();
    }

    @PreDestroy
    public void stop() throws IOException, InterruptedException {
        channel.close().sync();
        group.shutdownGracefully();
    }
}
