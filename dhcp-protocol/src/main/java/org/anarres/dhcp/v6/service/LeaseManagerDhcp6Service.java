package org.anarres.dhcp.v6.service;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.HashSet;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.DHcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.OptionRequestOption;
import org.anarres.dhcp.v6.options.ServerIdOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaseManagerDhcp6Service implements Dhcp6Service {

    private static final Logger LOG = LoggerFactory.getLogger(LeaseManagerDhcp6Service.class);

    private final Dhcp6LeaseManager leaseManager;
    private final ServerIdOption serverId;

    public LeaseManagerDhcp6Service(final Dhcp6LeaseManager leaseManager, final DuidOption.Duid serverId) {
        this.leaseManager = leaseManager;
        this.serverId = new ServerIdOption();
        this.serverId.setDuid(serverId);
    }

    @Override public Optional<Dhcp6Message> getReplyFor(@Nonnull DHcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg) {
        Dhcp6Message reply = null;
        try {
            switch (incomingMsg.getMessageType()) {
            case DHCP_SOLICIT: {
                // TODO handle rapid commits
                // TODO handle accept reconfigure option
                // TODO add unicast option if possible
                Dhcp6Exception.InvalidMsgException.checkSolicit(incomingMsg);
                reply = advertise(incomingMsg);
                break;
            }
            case DHCP_REQUEST: {
//                if(!requestContext.getClientAddress().isMulticastAddress()) {
//                    reply = rejectUnicast(requestContext, incomingMsg);
//                } else {
                Dhcp6Exception.InvalidMsgException.checkRequest(incomingMsg, getServerId().getDuid());
                // TODO handle rapid commits
                // TODO handle accept reconfigure option
                // TODO handle unicast vs multicast https://tools.ietf.org/html/rfc3315#section-18.2.1
                reply = reply(incomingMsg);
//                }
                break;
            }
            case DHCP_RELEASE: {
//                if(!requestContext.getClientAddress().isMulticastAddress()) {
//                    reply = rejectUnicast(requestContext, incomingMsg);
//                } else {
                // TODO handle rapid commits
                // TODO handle accept reconfigure option
                // TODO handle unicast vs multicast https://tools.ietf.org/html/rfc3315#section-18.2.6
                Dhcp6Exception.InvalidMsgException.checkRelease(incomingMsg, getServerId().getDuid());
                reply = release(incomingMsg);
//                }
                break;
            }
            // TODO handle relay agents

            // fall-through intentional, following messages are unexpected to be received on server side
            // https://tools.ietf.org/html/rfc3315#section-15.10
            case DHCP_REPLY: {
            }
            // https://tools.ietf.org/html/rfc3315#section-15.11
            case DHCP_RECONFIGURE: {
            }
            // https://tools.ietf.org/html/rfc3315#section-15.3
            case DHCP_ADVERTISE: {
                throw new Dhcp6Exception.InvalidMsgException(incomingMsg.getMessageType());
            }
            default: {
                throw new IllegalStateException("Unhandled DHCP message " + incomingMsg);
            }
            }
        } catch (Dhcp6Exception.InvalidMsgException e) {
            LOG.warn("Invalid DHCP message detected: {}. Ignoring", incomingMsg, e);
        }

        final OptionRequestOption optionRequestOption = incomingMsg.getOptions().get(OptionRequestOption.class);
        if(reply != null && optionRequestOption != null) {
            stripOptions(reply, optionRequestOption);
        }

        return Optional.fromNullable(reply);
    }

    private static void stripOptions(final Dhcp6Message reply, final OptionRequestOption optionRequestOption) {
        final HashSet<Short> requestedOptions = Sets.newHashSet(optionRequestOption.getRequestedOptions());

        final Dhcp6Options filtered = new Dhcp6Options();

        filtered.addAll(Iterables.filter(reply.getOptions(), new Predicate<Dhcp6Option>() {
            @Override public boolean apply(final Dhcp6Option input) {
                return requestedOptions.contains(input.getTag());
            }
        }));

        reply.setOptions(filtered);
    }

    private Dhcp6Message rejectUnicast(final DHcp6RequestContext requestContext, final Dhcp6Message incomingMsg) {
        final ClientIdOption option = incomingMsg.getOptions().get(ClientIdOption.class);

        LOG.warn("Rejecting message: {} from unicast: {} by client: {}", incomingMsg, requestContext.getClientAddress(),
            option.getDuid());

        Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();

        options.add(option);
        options.add(getServerId());
        options.add(StatusCodeOption.create(StatusCodeOption.USE_MULTICAST));
        reply.setOptions(options);
        return reply;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.6
     */
    private Dhcp6Message release(final Dhcp6Message incomingMsg) {
        final Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        // Release IANA IATA
        final Dhcp6Options options = new Dhcp6Options();
        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        leaseManager.release(incomingMsg, reply);
        return reply;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-17.2.3
     */
    private Dhcp6Message reply(final Dhcp6Message incomingMsg) {
        // TODO reuse this from advertise
        Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();

        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        // TODO Reconfigure Accept option

        // Lease manager might decide to respond with a different message
        reply = leaseManager.lease(incomingMsg, reply);
        return reply;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-17.2.2
     */
    private Dhcp6Message advertise(final Dhcp6Message incomingMsg) {
        Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_ADVERTISE);

        final Dhcp6Options options = new Dhcp6Options();

        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        // TODO add configurable preference (or maybe let pluggable lease manager/dhcpservice worry)
        // TODO Reconfigure Accept option

        // Lease manager might decide to respond with a different message
        reply = leaseManager.lease(incomingMsg, reply);
        return reply;
    }

    protected ServerIdOption getServerId() {
        return serverId;
    }
}
