/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class DhcpOptionsRegistryTest {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpOptionsRegistryTest.class);

    @Test
    public void testRegistry() {
        DhcpOptionsRegistry registry = DhcpOptionsRegistry.getInstance();
        LOG.info("DhcpOptionsRegistry is " + registry);
        for (int i = 0; i < 0xFF; i++) {
            Class<?> type = registry.getOptionType((byte) i);
            LOG.info(i + " -> " + type);
        }
    }

}
