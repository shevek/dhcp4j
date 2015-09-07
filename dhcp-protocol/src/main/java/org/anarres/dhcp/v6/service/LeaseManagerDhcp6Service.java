package org.anarres.dhcp.v6.service;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.net.InetAddress;
import java.util.HashSet;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.messages.Dhcp6RelayMessage;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.OptionRequestOption;
import org.anarres.dhcp.v6.options.RelayMessageOption;
import org.anarres.dhcp.v6.options.ServerIdOption;
import org.anarres.dhcp.v6.options.ServerUnicastOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaseManagerDhcp6Service implements Dhcp6Service {

    private static final Logger LOG = LoggerFactory.getLogger(LeaseManagerDhcp6Service.class);

    private final Dhcp6LeaseManager leaseManager;
    private final ServerIdOption serverId;
    private Optional<ServerUnicastOption> serverUnicastOption;

    public LeaseManagerDhcp6Service(@Nonnull final Dhcp6LeaseManager leaseManager, @Nonnull final DuidOption.Duid serverId) {
        this.leaseManager = leaseManager;
        this.serverId = new ServerIdOption();
        this.serverId.setDuid(serverId);
        serverUnicastOption = Optional.absent();
    }

    // TODO add preference option

    /**
     *
     * @param leaseManager lease managing delegate
     * @param serverId ServerIdOption
     * @param serverUnicastAddress this server's address to be placed into server unicast option to
     *                             allow unicast messages from clients
     */
    public LeaseManagerDhcp6Service(@Nonnull final Dhcp6LeaseManager leaseManager, @Nonnull final DuidOption.Duid serverId, @Nonnull final
        InetAddress serverUnicastAddress) {
        this.leaseManager = leaseManager;
        this.serverId = new ServerIdOption();
        this.serverId.setDuid(serverId);
        serverUnicastOption = Optional.of(ServerUnicastOption.create(serverUnicastAddress));
    }

    @Override
    public Optional<Dhcp6Message> getReplyFor(@Nonnull Dhcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = null;
        try {
            switch (incomingMsg.getMessageType()) {
            case DHCP_SOLICIT: {
                // TODO handle rapid commits
                // TODO handle accept reconfigure option
                // TODO add unicast option if possible (make configurable to receive unicasts or not)
                Dhcp6Exception.InvalidMsgException.checkSolicit(incomingMsg);
                reply = advertise(requestContext, incomingMsg);

                // Add serverUnicastOption if configured
                if(unicastAllowed()) {
                    reply.getOptions().add(serverUnicastOption.get());
                }
                break;
            }
            case DHCP_REQUEST: {
                // TODO is this check correct ? if we dont allow unicasts, we check if we received from link local addr
                // https://tools.ietf.org/html/rfc3315#section-18.2.1 first paragraph
                if (!unicastAllowed() && !isLinkLocal(requestContext)) {
                    reply = rejectUnicast(requestContext, incomingMsg);
                } else {
                    Dhcp6Exception.InvalidMsgException.checkRequest(incomingMsg, getServerId().getDuid());
                    // TODO handle rapid commits
                    // TODO handle accept reconfigure option
                    reply = reply(requestContext, incomingMsg);
                }
                break;
            }
            case DHCP_RENEW: {
                // TODO is this check correct ? if we dont allow unicasts, we check if we received from link local addr
                if (!unicastAllowed() && !isLinkLocal(requestContext)) {
                    reply = rejectUnicast(requestContext, incomingMsg);
                } else {
                    Dhcp6Exception.InvalidMsgException.checkRenew(incomingMsg, getServerId().getDuid());
                    // TODO handle rapid commits
                    // TODO handle accept reconfigure option
                    reply = renew(requestContext, incomingMsg);
                }
                break;
            }
            case DHCP_REBIND: {
                Dhcp6Exception.InvalidMsgException.checkRebind(incomingMsg, getServerId().getDuid());
                reply = rebind(requestContext, incomingMsg);
                break;
            }
            case DHCP_RELEASE: {
                // TODO is this check correct ? if we dont allow unicasts, we check if we received from link local addr
                if (!unicastAllowed() && !isLinkLocal(requestContext)) {
                    reply = rejectUnicast(requestContext, incomingMsg);
                } else {
                    // TODO handle rapid commits
                    // TODO handle accept reconfigure option
                    Dhcp6Exception.InvalidMsgException.checkRelease(incomingMsg, getServerId().getDuid());
                    reply = release(requestContext, incomingMsg);
                }
                break;
            }
            case DHCP_CONFIRM: {
                Dhcp6Exception.InvalidMsgException.checkConfirm(incomingMsg);
                reply = confirm(requestContext, incomingMsg);
                if(reply == null) {
                    LOG.warn("Unable to confirm request: {}", incomingMsg);
                }
                break;
            }
            case DHCP_DECLINE: {
                // TODO is this check correct ? if we dont allow unicasts, we check if we received from link local addr
                if (!unicastAllowed() && !isLinkLocal(requestContext)) {
                    reply = rejectUnicast(requestContext, incomingMsg);
                } else {
                    Dhcp6Exception.InvalidMsgException.checkDecline(incomingMsg, getServerId().getDuid());
                    reply = decline(requestContext, incomingMsg);
                }
                break;
            }
            case DHCP_INFORMATION_REQUEST: {
                Dhcp6Exception.InvalidMsgException.checkInformationRequest(incomingMsg, getServerId().getDuid());
                reply = requestInformation(requestContext, incomingMsg);
                break;
            }
            case DHCP_RELAY_FORW: {
                Dhcp6Exception.InvalidMsgException.checkRelayForward(incomingMsg);
                RelayMessageOption innerMsg = incomingMsg.getOptions().get(RelayMessageOption.class);
                requestContext = new Dhcp6RequestContext(((Dhcp6RelayMessage) incomingMsg).getLinkAddress(), requestContext.getClientAddress(), incomingMsg.getOptions());
                final Optional<Dhcp6Message> nestedReply = getReplyFor(requestContext, innerMsg.getRelayedMessage());

                // TODO forward relay options to the lease manager
                // TODO copy Interface-id option in case of a solicit message

                if(nestedReply.isPresent()) {
                    reply = wrapAsRelayReply((Dhcp6RelayMessage) incomingMsg, nestedReply);
                } else {
                    reply = nestedReply.orNull();
                }
                break;
            }

            // https://tools.ietf.org/html/rfc3315#section-15.10
            case DHCP_REPLY: {}
            // https://tools.ietf.org/html/rfc3315#section-15.11
            case DHCP_RECONFIGURE: {}
            // https://tools.ietf.org/html/rfc3315#section-15.13
            case DHCP_RELAY_REPL: {}
            // https://tools.ietf.org/html/rfc3315#section-15.3
            case DHCP_ADVERTISE: {
                // fall-through intentional, following messages are unexpected to be received on server side
                throw new Dhcp6Exception.InvalidMsgException(incomingMsg.getMessageType());
            }
            default: {
                reply = handle(requestContext, incomingMsg);
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

    /**
     * Handle message with no specific handler implemented
     */
    protected Dhcp6Message handle(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg) throws Dhcp6Exception {
        throw new Dhcp6Exception("Unhandled DHCP message type " + incomingMsg);
    }

    private static boolean isLinkLocal(final @Nonnull Dhcp6RequestContext requestContext) {
        return requestContext.getClientAddress().isLinkLocalAddress();
    }

    private boolean unicastAllowed() {
        return serverUnicastOption.isPresent();
    }

    private Dhcp6RelayMessage wrapAsRelayReply(final @Nonnull Dhcp6RelayMessage incomingMsg,
        final Optional<Dhcp6Message> nestedReply) {
        final Dhcp6RelayMessage dhcp6RelayReply = new Dhcp6RelayMessage();
        dhcp6RelayReply.setMessageType(Dhcp6MessageType.DHCP_RELAY_REPL);
        dhcp6RelayReply.setHopCount(incomingMsg.getHopCount());
        dhcp6RelayReply.setLinkAddress(incomingMsg.getLinkAddress());
        dhcp6RelayReply.setPeerAddress(incomingMsg.getPeerAddress());
        final Dhcp6Options options = new Dhcp6Options();
        options.add(RelayMessageOption.create(nestedReply.get()));
        dhcp6RelayReply.setOptions(options);
        return dhcp6RelayReply;
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

    /**
     * Construct a reply message with status USE_MULTICAST
     */
    private Dhcp6Message rejectUnicast(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg) {
        final ClientIdOption option = incomingMsg.getOptions().get(ClientIdOption.class);

        LOG.warn("Rejecting message: {} from UNICAST: {} by client: {}", incomingMsg, requestContext.getClientAddress(),
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
    private Dhcp6Message release(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        final Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();
        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        return leaseManager.release(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.7
     */
    private Dhcp6Message decline(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        final Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();
        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        return leaseManager.decline(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-17.2.3
     */
    private Dhcp6Message reply(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();

        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        // TODO Reconfigure Accept option

        return leaseManager.lease(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.3
     */
    private Dhcp6Message renew(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();

        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        return  leaseManager.renew(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.5
     */
    private Dhcp6Message requestInformation(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();

        if(incomingMsg.getOptions().contains(ClientIdOption.class)) {
            options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        }
        options.add(getServerId());
        reply.setOptions(options);

        return leaseManager.requestInformation(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-17.2.2
     */
    private Dhcp6Message advertise(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
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
        return leaseManager.lease(requestContext, incomingMsg, reply);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.2
     */
    private Dhcp6Message confirm(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(Dhcp6MessageType.DHCP_REPLY);

        final Dhcp6Options options = new Dhcp6Options();

        options.add(incomingMsg.getOptions().get(ClientIdOption.class));
        options.add(getServerId());
        reply.setOptions(options);

        return leaseManager.confirm(requestContext, incomingMsg, reply);
    }

    protected ServerIdOption getServerId() {
        return serverId;
    }
}
