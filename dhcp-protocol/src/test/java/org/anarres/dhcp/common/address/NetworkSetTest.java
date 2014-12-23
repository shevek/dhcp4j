/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.net.InetAddresses;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class NetworkSetTest {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkSetTest.class);

    private static void addRange(@Nonnull NetworkSet set, @Nonnull String a, @Nonnull String b) {
        InetAddressRange range = new InetAddressRange(
                InetAddresses.forString(a),
                InetAddresses.forString(b));
        LOG.info("Add (inclusive) " + range);
        set.addRange(range);
    }

    private static void assertSize(@Nonnull NetworkSet set, int size) {
        List<NetworkAddress> networks = set.toNetworkList();
        LOG.info(String.valueOf(networks));
        assertEquals(size, networks.size());

        List<InetAddressRange> ranges = set.toAddressRangeList();
        LOG.info(String.valueOf(ranges));
    }

    @Test
    public void testNetworkSetAdd() throws Exception {
        NetworkSet set = new NetworkSet();
        addRange(set, "1.2.3.4", "1.3.5.6");
        assertSize(set, 17);
    }

    @Test
    public void testNetworkSetRemove() throws Exception {
        NetworkSet set = new NetworkSet();
        addRange(set, "0.0.0.0", "255.255.255.255");
        set.removeAddress(InetAddresses.forString("1.2.3.4"));
        set.removeNetwork(NetworkAddress.forString("123.45.6.7/25"));
        assertSize(set, 54);
    }
}
