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
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpMessageDecoder;
import org.apache.directory.server.dhcp.io.DhcpMessageEncoder;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpNumber;

/**
 * Don't use this yet.
 *
 * @author shevek
 */
public class DhcpServer {

    private final DhcpMessageDecoder decoder = new DhcpMessageDecoder();
    private final DhcpMessageEncoder encoder = new DhcpMessageEncoder();
    private final DhcpService service;

    @Nonnull
    private InterfaceAddress toInterfaceAddress(@Nonnull PcapAddress address) {
        int netmask = AddressUtils.toNetmask(address.getNetmask());
        return new InterfaceAddress(address.getAddress(), netmask);
    }

    public DhcpServer(DhcpService service) {
        this.service = service;
    }

    public void run() throws Exception {
        PcapNetworkInterface iface = null;

        InterfaceAddress localAddress = toInterfaceAddress(iface.getAddresses().get(0));

        PcapHandle handle = iface.openLive(4096, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1024);
        handle.setFilter("udp port 67", BpfProgram.BpfCompileMode.OPTIMIZE);
        for (;;) {
            Packet rawPacket = handle.getNextPacketEx();
            IpV4Packet ipPacket = rawPacket.get(IpV4Packet.class);
            UdpPacket udpPacket = rawPacket.get(UdpPacket.class);
            byte[] dhcpData = udpPacket.getPayload().getRawData();
            DhcpMessage message = decoder.decode(ByteBuffer.wrap(dhcpData));
            InetSocketAddress remoteAddress = new InetSocketAddress(ipPacket.getHeader().getSrcAddr(), udpPacket.getHeader().getSrcPort().valueAsInt());
            DhcpMessage reply = service.getReplyFor(localAddress, remoteAddress, message);
            if (reply == null)
                continue;
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
                    .srcAddr(null)  // TODO
                    .dstAddr(null)  // TODO
                    .protocol(IpNumber.UDP)
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .paddingAtBuild(true);
            EthernetPacket.Builder ethernetBuilder = new EthernetPacket.Builder()
                    .payloadBuilder(ipBuilder)
                    .type(EtherType.IPV4)
                    .paddingAtBuild(true);
            Packet replyPacket = ethernetBuilder.build();
            handle.sendPacket(replyPacket);
        }
    }
}
