/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.server.pcap;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
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
import org.apache.directory.server.dhcp.service.DhcpService;
import org.apache.directory.server.dhcp.service.manager.LeaseManager;
import org.apache.directory.server.dhcp.service.manager.LeaseManagerDhcpService;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Don't use this yet.
 *
 * @author shevek
 */
public class DhcpServer {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpServer.class);
    private final DhcpService service;
    private final int port;
    private Executor executor = MoreExecutors.directExecutor();

    @Nonnull
    private static InterfaceAddress toInterfaceAddress(@Nonnull PcapAddress address) {
        Preconditions.checkNotNull(address, "PcapAddress was null.");
        int netmask = AddressUtils.toNetmask(address.getNetmask());
        return new InterfaceAddress(address.getAddress(), netmask);
    }

    @Nonnull
    private static InterfaceAddress[] toInterfaceAddresses(@Nonnull PcapNetworkInterface iface) {
        Preconditions.checkNotNull(iface, "PcapNetworkInterface was null.");
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
        for (PcapNetworkInterface iface : Pcaps.findAllDevs())
            LOG.info(iface.getName() + " : " + iface);
        PcapNetworkInterface iface = Pcaps.getDevByName("eth0");
        LOG.info("Using " + iface.getName() + " : " + iface);

        InterfaceAddress[] interfaceAddresses = toInterfaceAddresses(iface);
        LOG.info("Addresses are " + Arrays.toString(interfaceAddresses));

        PcapHandle handle = iface.openLive(4096, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 0);
        handle.loop(10, new DhcpPacketListener(service, interfaceAddresses));
        // handle.setFilter("udp port " + port, BpfProgram.BpfCompileMode.OPTIMIZE);
        // handle.breakLoop();
        // handle.close();
    }
}
