/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.address;

import org.anarres.dhcp.common.address.NetworkAddress;
import org.anarres.dhcp.common.address.Subnet;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class SubnetTest {

    private static final Log LOG = LogFactory.getLog(SubnetTest.class);

    @Test
    public void testSubnet() {
        InetAddress address = InetAddresses.forString("1.2.3.4");
        NetworkAddress network = new NetworkAddress(address, 24);
        Subnet subnet = new Subnet(network, null, null);
        LOG.info("Using subnet " + subnet);
        assertEquals(InetAddresses.forString("1.2.3.0"), subnet.getNetworkAddress().getAddress());
        assertEquals(InetAddresses.forString("1.2.3.1"), subnet.getRangeStart());
        assertEquals(InetAddresses.forString("1.2.3.254"), subnet.getRangeEnd());
        assertEquals(254, subnet.getRangeSize());
        assertFalse(subnet.rangeContains(InetAddresses.forString("1.2.2.15")));
        assertFalse(subnet.rangeContains(InetAddresses.forString("1.2.3.0")));
        assertTrue(subnet.rangeContains(InetAddresses.forString("1.2.3.1")));
        assertTrue(subnet.rangeContains(InetAddresses.forString("1.2.3.2")));
        assertTrue(subnet.rangeContains(InetAddresses.forString("1.2.3.13")));
        assertTrue(subnet.rangeContains(InetAddresses.forString("1.2.3.252")));
        assertFalse(subnet.rangeContains(InetAddresses.forString("1.2.3.255")));
        assertFalse(subnet.rangeContains(InetAddresses.forString("2.2.3.13")));
    }
}