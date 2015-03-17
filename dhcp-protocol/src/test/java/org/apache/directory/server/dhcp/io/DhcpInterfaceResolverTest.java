/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.io;

import org.junit.Test;

/**
 *
 * @author shevek
 */
public class DhcpInterfaceResolverTest {

    // Test requires human inspection to determine whether it detected eth0:0.
    @Test
    public void testInterfaceName() throws Exception{
        DhcpInterfaceManager resolver = new DhcpInterfaceManager();
        resolver.addInterfaces(new DhcpInterfaceManager.NamedPredicate("eth0:0"));
    }
}
