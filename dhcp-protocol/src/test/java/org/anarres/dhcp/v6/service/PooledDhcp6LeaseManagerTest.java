package org.anarres.dhcp.v6.service;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.IaAddressOption;
import org.anarres.dhcp.v6.options.IaNaOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.anarres.dhcp.v6.service.AbstractDhcp6LeaseManager.Lifetimes;
import org.junit.Test;

/**
 *
 * @author marekgr
 *
 */
public class PooledDhcp6LeaseManagerTest extends AbstractDhcp6LeaseManagerTest {

    protected Dhcp6LeaseManager getLeaseManagerInstance() throws UnknownHostException {
        return getLeaseManagerInstance("FF02::1", "FF02::10");
    }

    protected Dhcp6LeaseManager getLeaseManagerInstance(String startingAddressStr, String endingAddressStr)
                    throws UnknownHostException {
        InetAddress startingAddress = InetAddress.getByName(startingAddressStr);
        InetAddress endingAddress = InetAddress.getByName(endingAddressStr);
        Lifetimes lifetimes = new Lifetimes(1, 2, 3, 4);
        return new PooledDhcp6LeaseManager(startingAddress, endingAddress, lifetimes);
    }

    @Test
    public void testRequestNoAddressLeased() throws Dhcp6Exception, UnknownHostException {
        final Dhcp6LeaseManager leaseManager = getLeaseManagerInstance("FF02::1", "FF02::2"); // pool with one address
        IaNaOption iaNa1 = IaNaOption.create(1, 2, 3, Optional.of(new Dhcp6Options()));
        IaNaOption iaNa2 = IaNaOption.create(2, 2, 3, Optional.of(new Dhcp6Options()));
        Dhcp6Message request = createDhcp6Message(Dhcp6MessageType.DHCP_REQUEST, clientId, iaNa1, iaNa2);

        Dhcp6Message reply = leaseManager.lease(requestContext, request, new Dhcp6Message());
        final Dhcp6Options responseOptions = reply.getOptions();

        assertAppearedNTimes(responseOptions, IaNaOption.class, 2);
        int numberOfAddressesLeased = 0;
        for (IaNaOption option : responseOptions.getAll(IaNaOption.class)) {
            final Dhcp6Options options = option.getOptions();
            if (options.contains(IaAddressOption.class)) {
                numberOfAddressesLeased++;
            } else {
                assertStatusEquals(options, StatusCodeOption.NO_ADDRS_AVAIL);
            }
        }
        assertEquals(1, numberOfAddressesLeased);
    }

}
