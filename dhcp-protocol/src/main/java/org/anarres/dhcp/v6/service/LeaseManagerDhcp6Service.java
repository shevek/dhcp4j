package org.anarres.dhcp.v6.service;

import com.google.common.annotations.Beta;
import java.net.InetAddress;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.ServerIdOption;

/**
 * DHCPv6 Service delegating IA manipulation to a DHCPv6 Lease Manager
 */
@Beta
public class LeaseManagerDhcp6Service extends AbstractDhcp6Service {

    private final Dhcp6LeaseManager leaseManager;
    private final ServerIdOption serverId;

    public LeaseManagerDhcp6Service(@Nonnull final Dhcp6LeaseManager leaseManager, @Nonnull final DuidOption.Duid serverId) {
        this.leaseManager = leaseManager;
        this.serverId = new ServerIdOption();
        this.serverId.setDuid(serverId);
    }

    /**
     *
     * @param leaseManager
     *            lease managing delegate
     * @param serverId
     *            ServerIdOption
     * @param serverUnicastAddress
     *            this server's address to be placed into server unicast option to allow unicast messages from clients
     */
    public LeaseManagerDhcp6Service(@Nonnull final Dhcp6LeaseManager leaseManager, @Nonnull final DuidOption.Duid serverId, @Nonnull final
        InetAddress serverUnicastAddress) {
        super(serverUnicastAddress);
        this.leaseManager = leaseManager;
        this.serverId = new ServerIdOption();
        this.serverId.setDuid(serverId);
    }

    /**
     * Handle message with no specific handler implemented
     */
    @Override @Nullable
    protected Dhcp6Message handle(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg) throws Dhcp6Exception {
        // Delegate full handling of unknown message to lease manager
        return leaseManager.handle(requestContext, incomingMsg);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.6
     */
    @Override protected Dhcp6Message release(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        final Dhcp6Message reply = createReply(incomingMsg);

        return leaseManager.release(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.7
     */
    @Override protected Dhcp6Message decline(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        final Dhcp6Message reply = createReply(incomingMsg);

        return leaseManager.decline(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-17.2.3
     */
    @Override protected Dhcp6Message reply(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = createReply(incomingMsg);

        final Dhcp6Message lease = leaseManager.lease(requestContext, incomingMsg, reply);

        // The server includes other options containing configuration
        // information to be returned to the client as described in section 18.2.
        leaseManager.requestInformation(requestContext, incomingMsg, lease);

        return lease;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.3
     */
    @Override protected Dhcp6Message renew(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = createReply(incomingMsg);

        final Dhcp6Message renew = leaseManager.renew(requestContext, incomingMsg, reply);

        // The server includes other options containing configuration
        // information to be returned to the client as described in section 18.2.
        leaseManager.requestInformation(requestContext, incomingMsg, renew);

        return renew;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.3
     */
    @Override
    protected Dhcp6Message rebind(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = createReply(incomingMsg);

        final Dhcp6Message rebind = leaseManager.rebind(requestContext, incomingMsg, reply);

        // The server includes other options containing configuration
        // information to be returned to the client as described in section 18.2.
        leaseManager.requestInformation(requestContext, incomingMsg, reply);

        return rebind;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.5
     */
    @Override
    protected Dhcp6Message requestInformation(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = createGenericReply(incomingMsg, Dhcp6MessageType.DHCP_REPLY);

        if (incomingMsg.getOptions().contains(ClientIdOption.class)) {
            reply.getOptions().add(incomingMsg.getOptions().get(ClientIdOption.class));
        }

        return leaseManager.requestInformation(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-17.2.2
     */
    @Override
    protected Dhcp6Message advertise(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = createReply(incomingMsg);
        reply.setMessageType(Dhcp6MessageType.DHCP_ADVERTISE);

        final Dhcp6Message lease = leaseManager.lease(requestContext, incomingMsg, reply);

        // TODO https://tools.ietf.org/html/rfc3315#section-17.2.2 paragraph 6
        // If the server will not assign any addresses to any IAs in a
        // SUBSEQUENT Request from the client the server MUST send an Advertise with NoAddrsAvail status.

        leaseManager.requestInformation(requestContext, incomingMsg, reply);

        return lease;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.2
     */
    @Override protected Dhcp6Message confirm(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = createReply(incomingMsg);

        return leaseManager.confirm(requestContext, incomingMsg, reply);
    }

    @Override
    protected ServerIdOption getServerId() {
        return serverId;
    }
}
