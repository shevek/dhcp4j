/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.server.pcap;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpMessageDecoder;
import org.apache.directory.server.dhcp.io.DhcpMessageEncoder;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.pcap4j.core.PacketListener;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class DhcpPacketListener implements PacketListener {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpPacketListener.class);
    private final DhcpMessageDecoder decoder = new DhcpMessageDecoder();
    private final DhcpMessageEncoder encoder = new DhcpMessageEncoder();
    private final DhcpService service;
    private final InterfaceAddress[] interfaceAddresses;

    public DhcpPacketListener(@Nonnull DhcpService service, @Nonnull InterfaceAddress[] interfaceAddresses) {
        this.service = service;
        this.interfaceAddresses = interfaceAddresses;
    }

    public void gotPacket(Packet rawPacket) {
        try {
            LOG.info("Read raw " + rawPacket);
            IpV4Packet ipPacket = rawPacket.get(IpV4Packet.class);
            UdpPacket udpPacket = rawPacket.get(UdpPacket.class);
            byte[] dhcpData = udpPacket.getPayload().getRawData();
            InetSocketAddress remoteAddress = new InetSocketAddress(ipPacket.getHeader().getSrcAddr(), udpPacket.getHeader().getSrcPort().valueAsInt());
            InetSocketAddress localAddress = new InetSocketAddress(ipPacket.getHeader().getDstAddr(), udpPacket.getHeader().getDstPort().valueAsInt());
            DhcpRequestContext context = new DhcpRequestContext(interfaceAddresses, remoteAddress, localAddress);
            DhcpMessage request = decoder.decode(ByteBuffer.wrap(dhcpData));
            LOG.info("Read DHCP " + request);
            DhcpMessage reply = service.getReplyFor(context, request);
            if (reply == null)
                return;
            byte[] replyData = new byte[1536];
            ByteBuffer buffer = ByteBuffer.wrap(replyData);
            encoder.encode(buffer, reply);
            replyData = Arrays.copyOf(replyData, buffer.position());    // Truncate array to writer position.
            UnknownPacket.Builder dhcpBuilder = new UnknownPacket.Builder()
                    .rawData(replyData);
            UdpPacket.Builder udpBuilder = new UdpPacket.Builder()
                    .payloadBuilder(dhcpBuilder)
                    .srcPort(udpPacket.getHeader().getDstPort())
                    .dstPort(udpPacket.getHeader().getSrcPort())
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true);
            IpV4Packet.Builder ipBuilder = new IpV4Packet.Builder()
                    .payloadBuilder(udpBuilder)
                    .srcAddr(null) // TODO
                    .dstAddr(null) // TODO
                    .protocol(IpNumber.UDP)
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .paddingAtBuild(true);
            EthernetPacket.Builder ethernetBuilder = new EthernetPacket.Builder()
                    .payloadBuilder(ipBuilder)
                    .type(EtherType.IPV4)
                    .paddingAtBuild(true);
            Packet replyPacket = ethernetBuilder.build();
            // handle.sendPacket(replyPacket);
        } catch (Exception e) {
            LOG.error("DHCP failed", e);
        }
    }

}
