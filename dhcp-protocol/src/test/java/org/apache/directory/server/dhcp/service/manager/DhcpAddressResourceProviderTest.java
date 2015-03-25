/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.anarres.dhcp.common.address.NetworkAddress;
import org.anarres.dhcp.common.address.Subnet;
import org.anarres.jallocator.ResourceAllocator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class DhcpAddressResourceProviderTest {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpAddressResourceProviderTest.class);

    @Test
    public void testAllocator() {
        final InetAddress address = InetAddresses.forString("1.2.3.4");

        for (int netmask = 17; netmask < 31; netmask++) {
            final NetworkAddress network = new NetworkAddress(address, netmask);
            Subnet subnet = new Subnet(network, null, null);
            LOG.info("Using subnet " + subnet);

            DhcpAddressResourceProvider resourceProvider = new DhcpAddressResourceProvider(subnet);
            ResourceAllocator<InetAddress> resourceAllocator = new ResourceAllocator<InetAddress>(resourceProvider);

            for (int i = 0; i < 3; i++) {
                Set<InetAddress> out = new HashSet<InetAddress>();
                for (InetAddress allocated : resourceAllocator) {
                    assertNotNull("Failed to allocate", allocated);
                    // LOG.info("Allocated " + allocated);
                    assertTrue("Address not in subnet.", subnet.rangeContains(allocated));
                    out.add(allocated);
                }
                LOG.info("Allocated " + out.size() + " addresses.");
                assertEquals(subnet.getRangeSize(), out.size());
            }
        }
    }
}