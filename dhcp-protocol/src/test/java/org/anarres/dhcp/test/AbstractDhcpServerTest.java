/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.NetworkAddress;
import org.apache.directory.server.dhcp.io.DhcpInterfaceManager;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.service.store.FixedStoreLeaseManager;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public abstract class AbstractDhcpServerTest {

    public static final String INTERFACE_NAME = "br0";

    /**
     * ifconfig br0 10.27.0.1
     * sudo dhcping -h 08:00:20:c0:ff:ee -s 10.27.0.1
     * OR
     * sudo dhclient -d eth0 -n
     */
    @Nonnull
    public FixedStoreLeaseManager newLeaseManager(@Nonnull String interfaceName) throws Exception {
        NetworkInterface iface = NetworkInterface.getByName(interfaceName);
        assertNotNull("No such interface " + interfaceName, iface);
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
