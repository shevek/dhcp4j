/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpMessageDecoder;
import org.apache.directory.server.dhcp.io.DhcpMessageEncoder;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.service.DhcpService;

/**
 *
 * @author shevek
 */
public class DhcpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Log LOG = LogFactory.getLog(DhcpHandler.class);
    private final DhcpService dhcpService;
    private final DhcpMessageDecoder decoder = new DhcpMessageDecoder();
    private final DhcpMessageEncoder encoder = new DhcpMessageEncoder();

    public DhcpHandler(@Nonnull DhcpService dhcpService) {
        this.dhcpService = dhcpService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        DhcpMessage request = decoder.decode(msg.content().nioBuffer());
        InterfaceAddress localAddress = new InterfaceAddress(msg.recipient().getAddress(), 0);
        DhcpMessage reply = dhcpService.getReplyFor(
                localAddress,
                msg.sender(), request);
        if (reply != null) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            encoder.encode(buffer, reply);
            buffer.flip();
            ByteBuf buf = Unpooled.wrappedBuffer(buffer);
            DatagramPacket packet = new DatagramPacket(buf, msg.sender());
            ctx.write(packet);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause);
        ctx.close();
    }

}
