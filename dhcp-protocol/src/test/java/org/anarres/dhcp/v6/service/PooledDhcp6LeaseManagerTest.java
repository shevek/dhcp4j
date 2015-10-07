package org.anarres.dhcp.v6.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.anarres.dhcp.v6.service.AbstractDhcp6LeaseManager.Lifetimes;

/**
 *
 * @author marekgr
 *
 */
public class PooledDhcp6LeaseManagerTest extends AbstractDhcp6LeaseManagerTest {

    protected Dhcp6LeaseManager getLeaseManagerInstance() throws UnknownHostException {
        InetAddress startingAddress =  InetAddress.getByName("FF02::1");
        InetAddress endingAddress = InetAddress.getByName("FF02::10");
        Lifetimes lifetimes = new Lifetimes(1, 2, 3, 4);

        return new PooledDhcp6LeaseManager(startingAddress, endingAddress, lifetimes);
    }

}
