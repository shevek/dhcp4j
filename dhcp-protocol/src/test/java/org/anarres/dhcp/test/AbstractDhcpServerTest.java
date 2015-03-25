/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.test;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.NetworkAddress;
import org.apache.directory.server.dhcp.io.DhcpInterfaceManager;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.service.store.FixedStoreLeaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public abstract class AbstractDhcpServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDhcpServerTest.class);
    public static final String INTERFACE_NAME = "br0";

    private static void print(@Nonnull NetworkInterface iface, int depth) throws SocketException {
        LOG.info(Strings.repeat(" ", depth * 4) + iface);
        print(iface.getSubInterfaces(), depth + 1);
    }

    private static void print(@Nonnull Enumeration<NetworkInterface> ifaces, int depth) throws SocketException {
        for (NetworkInterface subiface : Collections.list(ifaces)) {
            print(subiface, depth);
        }
    }

    @CheckForNull
    private static NetworkInterface getNetworkInterfaceByName(@Nonnull String name, @Nonnull Enumeration<NetworkInterface> ifaces) throws SocketException {
        for (NetworkInterface iface : Collections.list(ifaces)) {
            if (name.equals(iface.getName()))
                return iface;
            NetworkInterface subiface = getNetworkInterfaceByName(name, iface.getSubInterfaces());
            if (subiface != null)
                return subiface;
        }
        return null;
    }

    @CheckForNull
    public static NetworkInterface getNetworkInterfaceByName(@Nonnull String name) throws SocketException {
        return getNetworkInterfaceByName(name, NetworkInterface.getNetworkInterfaces());
    }

    /**
     * ifconfig br0 10.27.0.1
     * sudo dhcping -h 08:00:20:c0:ff:ee -s 10.27.0.1
     * OR
     * sudo dhclient -d eth0 -n
     */
    @Nonnull
    public FixedStoreLeaseManager newLeaseManager(@Nonnull String interfaceName) throws Exception {
        NetworkInterface iface = getNetworkInterfaceByName(interfaceName);
        if (iface == null) {
            print(NetworkInterface.getNetworkInterfaces(), 0);
            assertNotNull("No such interface " + interfaceName, iface);
        }
        InterfaceAddress address = Iterables.find(iface.getInterfaceAddresses(), new Predicate<InterfaceAddress>() {
            public boolean apply(InterfaceAddress input) {
                return input.getAddress() instanceof Inet4Address;
            }
        });
        NetworkAddress network = new NetworkAddress(address);
        FixedStoreLeaseManager manager = new FixedStoreLeaseManager();
        manager.addLease(HardwareAddress.fromString("08:00:20:c0:ff:ee"), network.getMachineAddress(42));
        return manager;
    }

    public void assertKosher(@Nonnull DhcpInterfaceManager resolver) {
        assertFalse(resolver.getInterfaces().isEmpty());
    }
}
