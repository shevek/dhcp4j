package org.anarres.dhcp.v6.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import org.anarres.dhcp.v6.options.DuidOption;
import org.junit.Before;
import org.junit.Test;

public class ClientBindingRegistryTest {

    private ClientBindingRegistry registry;
    private final int iaid = 1;
    private DuidOption.Duid duid = new DuidOption.Duid(new byte[] { 1, 2, 3, 4 });
    private InetAddress localHost;

    @Before public void setUp() throws Exception {
        registry = new ClientBindingRegistry("test");
        localHost = InetAddress.getLocalHost();
    }

    @Test public void test() throws Exception {
        assertNull(registry.get(duid, iaid));
        assertFalse(registry.contains(duid, iaid));
        assertNull(registry.remove(duid, iaid));
        assertFalse(registry.containsIp(localHost));

        registry.add(duid, iaid, localHost);
        assertNotNull(registry.get(duid, iaid));
        final ClientBindingRegistry.ClientBinding clientBinding = registry.get(duid, iaid);
        assertEquals(iaid, clientBinding.getIaId());
        assertEquals(localHost, clientBinding.getIp());

        assertTrue(registry.contains(duid, iaid));
        assertTrue(registry.containsIp(localHost));

        assertEquals(clientBinding, registry.remove(duid, iaid));
    }
}