/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.DhcpUtils;
import org.anarres.dhcp.common.LogUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpInterfaceResolver;
import org.apache.directory.server.dhcp.io.DhcpMessageDecoder;
import org.apache.directory.server.dhcp.io.DhcpMessageEncoder;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author shevek
 */
@ChannelHandler.Sharable
public class DhcpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpHandler.class);
    private final DhcpService dhcpService;
    private final DhcpInterfaceResolver interfaceResolver;
    private final DhcpMessageDecoder decoder = new DhcpMessageDecoder();
    private final DhcpMessageEncoder encoder = new DhcpMessageEncoder();

    public DhcpHandler(@Nonnull DhcpService dhcpService, @Nonnull DhcpInterfaceResolver resolver) {
        this.dhcpService = dhcpService;
        this.interfaceResolver = resolver;
    }

    private static void debug(@Nonnull String event, @Nonnull SocketAddress src, @Nonnull SocketAddress dst, @Nonnull DhcpMessage msg) {
        if (LOG.isDebugEnabled())
            LOG.debug("{} -> {} {} {}", src, dst, event, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        DhcpMessage request = decoder.decode(msg.content().nioBuffer());

        InterfaceAddress interfaceAddress = interfaceResolver.getQueryInterface(
                msg.recipient().getAddress(),
                ctx.channel().localAddress(),
                request,
                msg.sender().getAddress()
        );
        if (interfaceAddress == null) {
            debug("IGNQUERY", msg.sender(), msg.recipient(), request);
            return;
        }
        // debug("READ", msg.sender(), msg.recipient(), request);

        MDC.put(LogUtils.MDC_DHCP_CLIENT_HARDWARE_ADDRESS, String.valueOf(request.getHardwareAddress()));
        MDC.put(LogUtils.MDC_DHCP_SERVER_INTERFACE_ADDRESS, String.valueOf(interfaceAddress));
        try {
            DhcpMessage reply = dhcpService.getReplyFor(
                    interfaceAddress,
                    msg.sender(), request);
            if (reply == null) {
                debug("NOREPLY", msg.sender(), msg.recipient(), request);
                return;
            }

            interfaceAddress = interfaceResolver.getResponseInterface(
                    request.getRelayAgentAddress(),
                    request.getCurrentClientAddress(),
                    msg.sender().getAddress(),
                    reply
            );
            if (interfaceAddress == null) {
                debug("NOIFACE", msg.recipient(), msg.sender(), reply);
                return;
            }

            InetSocketAddress isa = DhcpUtils.determineMessageDestination(
                    request, reply,
                    interfaceAddress, msg.sender().getPort());

            ByteBuf buf = ctx.alloc().buffer(1024);
            ByteBuffer buffer = buf.nioBuffer(buf.writerIndex(), buf.writableBytes());
            encoder.encode(buffer, reply);
            buffer.flip();
            buf.writerIndex(buf.writerIndex() + buffer.remaining());
            DatagramPacket packet = new DatagramPacket(buf, isa);
            debug("WRITE", packet.sender(), packet.recipient(), reply);
            ctx.write(packet, ctx.voidPromise());
        } finally {
            MDC.remove(LogUtils.MDC_DHCP_SERVER_INTERFACE_ADDRESS);
            MDC.remove(LogUtils.MDC_DHCP_CLIENT_HARDWARE_ADDRESS);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("Error on channel: " + cause, cause);
        // ctx.close();
    }
}
