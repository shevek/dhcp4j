/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.service.Dhcp6Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author marosmars
 */
@ChannelHandler.Sharable
public class Dhcp6Handler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger LOG = LoggerFactory.getLogger(Dhcp6Handler.class);

    private final Dhcp6Service dhcpService;

    private final Dhcp6MessageDecoder dhcp6MessageDecoder;
    private final Dhcp6MessageEncoder dhcp6MessageEncoder;

    public Dhcp6Handler(@Nonnull Dhcp6Service dhcpService, @Nonnull final byte[] serverDuid) {
        this.dhcpService = dhcpService;
        Preconditions.checkArgument(serverDuid.length == 2); // https://tools.ietf.org/html/rfc3315#section-9.1

        dhcp6MessageDecoder = Dhcp6MessageDecoder.getInstance();
        dhcp6MessageEncoder = Dhcp6MessageEncoder.getInstance();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Incomming DHCP : {}, from: {}", ByteBufUtil.hexDump(msg.content()), msg.sender());
        }

        final Dhcp6Message incommingMsg;
        try {
            incommingMsg = dhcp6MessageDecoder.decode(msg.content().nioBuffer());
        } catch (final Dhcp6Exception.UnknownMsgException e) {
            LOG.warn("Unknown DHCP message type: {}. Ignoring", ByteBufUtil.hexDump(msg.content()), e);
            return;
        }

        final Optional<Dhcp6Message> reply = dhcpService
            .getReplyFor(new Dhcp6RequestContext(msg.sender().getAddress()), incommingMsg);

        if(reply.isPresent()) {
            LOG.debug("Responding with message: {}", reply.get());

            // TODO what size to allocate the buffer to ?
            ByteBuf buf = ctx.alloc().buffer(1024);
            ByteBuffer buffer = buf.nioBuffer(buf.writerIndex(), buf.writableBytes());
            dhcp6MessageEncoder.encode(buffer, reply.get());
            buffer.flip();
            buf.writerIndex(buf.writerIndex() + buffer.remaining());
            DatagramPacket packet = new DatagramPacket(buf, msg.sender());
            ctx.write(packet);
        } else {
            LOG.warn("No response from DHCP service received for: {}. Ignoring.", incommingMsg);
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
