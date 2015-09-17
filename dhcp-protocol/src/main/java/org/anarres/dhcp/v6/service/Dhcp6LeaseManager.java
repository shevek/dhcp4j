package org.anarres.dhcp.v6.service;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;

/**
 * A delegate handler for LeaseManagerDhcp6Service. Responsible for leasing IPs and providing additional information to clients
 *
 * https://tools.ietf.org/html/rfc3315#section-18.2
 */
@Beta
public interface Dhcp6LeaseManager {

    /**
     * Lease manager can decide whether to serve a specific request or not
     */
    @Nullable Dhcp6Message lease(final Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, Dhcp6Message reply)
        throws Dhcp6Exception;

    /**
     * Release all leased addresses from incomingMsg
     */
    @Nonnull Dhcp6Message release(final Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, Dhcp6Message reply)
        throws Dhcp6Exception;

    /**
     * Provide additional information (options) to the client. This is called as a separate request, but also during:
     * lease, rebind, renew
     */
    @Nonnull Dhcp6Message requestInformation(final Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, Dhcp6Message reply)
        throws Dhcp6Exception;

    /**
     * Lease manager needs to add a status code to the reply Success/NotOnLink. If there is not enough information to
     * confirm the request. Lease manager should return null.
     */
    @Nullable Dhcp6Message confirm(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, Dhcp6Message reply)
        throws Dhcp6Exception;

    /**
     * Renew the addresses leased to client. This should be called from the client at time T1.
     */
    @Nonnull Dhcp6Message renew(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, Dhcp6Message reply)
        throws Dhcp6Exception;

    /**
     * Rebind the addresses leased to client. This should be called from the client at time T2.
     */
    @Nullable Dhcp6Message rebind(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, Dhcp6Message reply)
        throws Dhcp6Exception;

    /**
     * Handle declined IPs by a client. IPs are probably already in use and should not be leased.
     */
    @Nonnull Dhcp6Message decline(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, Dhcp6Message reply)
        throws Dhcp6Exception;

    /**
     * Handle unknown/undefined message. If a DHCPv6 message does not fall into any of the above methods, this one will be invoked.
     * The implementations of LeaseManager might support additional DHCPv6 message types.
     */
    @Nullable Dhcp6Message handle(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg) throws Dhcp6Exception;

}
