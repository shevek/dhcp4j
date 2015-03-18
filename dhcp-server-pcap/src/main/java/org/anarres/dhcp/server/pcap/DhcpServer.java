/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.server.pcap;

import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.io.DhcpMessageDecoder;
import org.apache.directory.server.dhcp.io.DhcpMessageEncoder;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.directory.server.dhcp.service.manager.LeaseManagerDhcpService;
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
    private final int port;
    private Executor executor = MoreExecutors.directExecutor();

    @Nonnull
    private static InterfaceAddress toInterfaceAddress(@Nonnull PcapAddress address) {
        int netmask = AddressUtils.toNetmask(address.getNetmask());
        return new InterfaceAddress(address.getAddress(), netmask);
    }

    @Nonnull
    private static InterfaceAddress[] toInterfaceAddresses(@Nonnull PcapNetworkInterface iface) {
        List<InterfaceAddress> out = new ArrayList<InterfaceAddress>();
        for (PcapAddress address : iface.getAddresses())
            out.add(toInterfaceAddress(address));
        return out.toArray(new InterfaceAddress[out.size()]);
    }

    public DhcpServer(@Nonnull DhcpService service, @Nonnegative int port) {
        this.service = service;
        this.port = port;
    }

    public DhcpServer(@Nonnull DhcpService service) {
        this(service, DhcpService.SERVER_PORT);
    }

    public DhcpServer(@Nonnull LeaseManager manager, @Nonnegative int port) {
        this(new LeaseManagerDhcpService(manager), port);
    }

    public DhcpServer(@Nonnull LeaseManager manager) {
        this(new LeaseManagerDhcpService(manager));
    }

    @PostConstruct
    public void start() throws IOException, InterruptedException {
    }

    @PreDestroy
    public void stop() throws IOException, InterruptedException {
    }

    public void run() throws Exception {
        PcapNetworkInterface iface = null;

        InterfaceAddress[] localAddresses = toInterfaceAddresses(iface);

        PcapHandle handle = iface.openLive(4096, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1024);
        handle.setFilter("udp port " + port, BpfProgram.BpfCompileMode.OPTIMIZE);
        for (;;) {
            Packet rawPacket = handle.getNextPacketEx();
            IpV4Packet ipPacket = rawPacket.get(IpV4Packet.class);
            UdpPacket udpPacket = rawPacket.get(UdpPacket.class);
            byte[] dhcpData = udpPacket.getPayload().getRawData();
            InetSocketAddress remoteAddress = new InetSocketAddress(ipPacket.getHeader().getSrcAddr(), udpPacket.getHeader().getSrcPort().valueAsInt());
            InetSocketAddress localAddress = new InetSocketAddress(ipPacket.getHeader().getDstAddr(), udpPacket.getHeader().getDstPort().valueAsInt());
            DhcpRequestContext context = new DhcpRequestContext(localAddresses, remoteAddress, localAddress);
            DhcpMessage request = decoder.decode(ByteBuffer.wrap(dhcpData));
            DhcpMessage reply = service.getReplyFor(context, request);
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
            handle.sendPacket(replyPacket);
        }
    }
}
