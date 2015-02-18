/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.netty;

import com.google.common.net.InetAddresses;
import java.net.NetworkInterface;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.service.store.FixedStoreLeaseManager;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author shevek
 */
public class DhcpServerTest {

    /**
     * ifconfig br0 10.27.0.1
     * sudo dhcping -h 08:00:20:c0:ff:ee -s 10.27.0.1
     * OR
     * sudo dhclient -d -d eth0 -n
     */
    @Ignore
    @Test
    public void testServer() throws Exception {
        FixedStoreLeaseManager manager = new FixedStoreLeaseManager();
        manager.addLease(HardwareAddress.fromString("08:00:20:c0:ff:ee"), InetAddresses.forString("10.27.0.42"));
        DhcpServer server = new DhcpServer(manager);
        server.start();
        server.addInterface(NetworkInterface.getByName("br0"));
        Thread.sleep(200000);
        server.stop();
    }

}
