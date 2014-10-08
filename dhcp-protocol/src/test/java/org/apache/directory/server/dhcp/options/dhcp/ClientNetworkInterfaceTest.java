/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.dhcp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class ClientNetworkInterfaceTest {

    @Test
    public void testToString() {
        ClientNetworkInterface option = new ClientNetworkInterface();
        option.setData(new byte[]{3, 45});
        assertEquals("3.45", option.toStringData());
    }
}