package org.anarres.dhcp.v6.service;

import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.IaOption;

/**
 * Basic implementation of a lease manager providing IPs from an IP pool
 */
@Beta
public class PooledDhcp6LeaseManager extends AbstractDhcp6LeaseManager {

    private final InetAddress startingAddress;
    private final InetAddress endingAddress;

    // TODO should we use utilities from package org.anarres.dhcp.common.address here ??

    public PooledDhcp6LeaseManager(@Nonnull final InetAddress startingAddress, @Nonnull final InetAddress endingAddress,
        @Nonnull final Lifetimes lifetimes) {
        super(lifetimes, ClientBindingRegistry.createForIaNa(), ClientBindingRegistry.createForIaTa());
        this.startingAddress = startingAddress;
        this.endingAddress = endingAddress;
    }

    /**
     * Invoked during renew, rebind and confirm. Each address needs to be checked if still appropriate
     *
     * @throws org.anarres.dhcp.v6.Dhcp6Exception.UnableToAnswerException in case the test can not be performed
     */
    @Override protected boolean isAppropriate(final Dhcp6RequestContext requestContext, final DuidOption.Duid clientId, final int iaid,
        final InetAddress ip) throws Dhcp6Exception.UnableToAnswerException {
        return true;
    }

    /**
     * @return next available IP
     */
    @Override protected InetAddress newIp(final Dhcp6RequestContext requestContext, final DuidOption.Duid clientId, final IaOption iaOption)
        throws Dhcp6Exception {
        // Client provided address hints
        final Iterable<InetAddress> requestedAddresses = getAddressesFromIa(iaOption);

        // Warning, this is highly inefficient
        InetAddress current = startingAddress;
        while (true) {
            if(current.equals(endingAddress)) {
                throw new IllegalStateException("IP pool exhausted");
            }

            if(!getIaNaRegistry().containsIp(current) && !getIaTaRegistry().containsIp(current)) {
                return current;
            }

            current = InetAddresses.increment(current);
        }
    }

}
