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
import org.anarres.dhcp.v6.options.InterfaceIdOption;
import org.anarres.dhcp.v6.options.OptionRequestOption;
import org.anarres.dhcp.v6.options.RelayMessageOption;
import org.anarres.dhcp.v6.options.ServerIdOption;
import org.anarres.dhcp.v6.options.ServerUnicastOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base DHCPv6 service outlining the standard DHCPv6 communication patterns defined in https://tools.ietf.org/html/rfc3315
 *
 * Created by marosmars.
 */
public abstract class AbstractDhcp6Service implements Dhcp6Service {

    private static final Logger LOG = LoggerFactory.getLogger(LeaseManagerDhcp6Service.class);

    private final Optional<ServerUnicastOption> serverUnicastOption;

    // TODO add preference option
    // TODO add Authentication handling https://tools.ietf.org/html/rfc3315#section-21
    // TODO support server initiated reconfigure https://tools.ietf.org/html/rfc3315#section-19 + reconfigure accept option

    public AbstractDhcp6Service() {
        this.serverUnicastOption = Optional.absent();
    }

    public AbstractDhcp6Service(final InetAddress serverUnicastAddress) {
        this.serverUnicastOption = Optional.of(ServerUnicastOption.create(serverUnicastAddress));;
    }

    @Override
    public Optional<Dhcp6Message> getReplyFor(@Nonnull Dhcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        Dhcp6Message reply;
        try {
            switch (incomingMsg.getMessageType()) {
            // TODO check reply types. Lease manager has the flexibility to change them
            case DHCP_SOLICIT: {
                // TODO handle rapid commits
                // TODO handle accept reconfigure option
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
                    reply = renew(requestContext, incomingMsg);
                }
                break;
            }
            case DHCP_REBIND: {
                Dhcp6Exception.InvalidMsgException.checkRebind(incomingMsg);
                reply = rebind(requestContext, incomingMsg);
                break;
            }
            case DHCP_RELEASE: {
                // TODO is this check correct ? if we dont allow unicasts, we check if we received from link local addr
                if (!unicastAllowed() && !isLinkLocal(requestContext)) {
                    reply = rejectUnicast(requestContext, incomingMsg);
                } else {
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
                // Recursively call this method to get a reply to inner DHCP message. Just add replay msg options to request context
                final Optional<Dhcp6Message> nestedReply = getReplyFor(requestContext.withRelayedOptions(incomingMsg.getOptions()), innerMsg.getRelayedMessage());

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
                reply = handle(requestContext, incomingMsg, incomingMsg.getMessageType().getCode());
            }
            }
        } catch (Dhcp6Exception.UnknownMsgException e) {
            // FIXME Unknown MsgException is never thrown here. Unknown message types need better handling
            reply = handle(requestContext, incomingMsg, e.getMsgType());
        }

        // Strip options to provide only requested
        final OptionRequestOption optionRequestOption = incomingMsg.getOptions().get(OptionRequestOption.class);
        if(reply != null && optionRequestOption != null) {
            stripOptions(reply, optionRequestOption);
        }

        return Optional.fromNullable(reply);
    }

    protected final Dhcp6Message createReply(final Dhcp6Message incomingMsg) {
        final Dhcp6Message reply = createGenericReply(incomingMsg, Dhcp6MessageType.DHCP_REPLY);
        reply.getOptions().add(incomingMsg.getOptions().get(ClientIdOption.class));
        return reply;
    }

    protected final Dhcp6Message createGenericReply(final Dhcp6Message incomingMsg, final Dhcp6MessageType type) {
        final Dhcp6Message reply = new Dhcp6Message();
        reply.setTransactionId(incomingMsg.getTransactionId());
        reply.setMessageType(type);

        final Dhcp6Options options = new Dhcp6Options();
        options.add(getServerId());
        reply.setOptions(options);
        return reply;
    }


    private static boolean isLinkLocal(final @Nonnull Dhcp6RequestContext requestContext) {
        return requestContext.getClientAddress().isLinkLocalAddress();
    }

    private static void stripOptions(final Dhcp6Message reply, final OptionRequestOption optionRequestOption) {
        final HashSet<Short> requestedOptions = Sets.newHashSet(optionRequestOption.getRequestedOptions());

        final Dhcp6Options filtered = new Dhcp6Options();

        filtered.addAll(Iterables.filter(reply.getOptions(), new Predicate<Dhcp6Option>() {
            @Override public boolean apply(final Dhcp6Option input) {
                // keep only requested options + the standard options RFC3315
                return input.getTag() <= 20 || requestedOptions.contains(input.getTag());
            }
        }));

        reply.setOptions(filtered);
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

        // https://tools.ietf.org/html/rfc3315#section-22.18 - Copy interfaceId option if present in request
        if(incomingMsg.getOptions().contains(InterfaceIdOption.class)) {
            options.add(incomingMsg.getOptions().get(InterfaceIdOption.class));
        }

        dhcp6RelayReply.setOptions(options);
        return dhcp6RelayReply;
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

    protected abstract Dhcp6Message release(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message decline(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message reply(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message renew(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message rebind(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message requestInformation(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message advertise(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message confirm(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg)
        throws Dhcp6Exception;

    protected abstract Dhcp6Message handle(Dhcp6RequestContext requestContext, Dhcp6Message incomingMsg, byte msgType) throws Dhcp6Exception;

    protected abstract ServerIdOption getServerId();
}
