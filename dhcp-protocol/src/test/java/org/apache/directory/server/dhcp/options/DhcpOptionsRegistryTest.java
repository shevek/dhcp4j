/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

/**
 *
 * @author shevek
 */
public class DhcpOptionsRegistryTest {

    private static final Log LOG = LogFactory.getLog(DhcpOptionsRegistryTest.class);

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
