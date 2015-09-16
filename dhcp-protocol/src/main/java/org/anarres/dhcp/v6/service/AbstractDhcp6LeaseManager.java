package org.anarres.dhcp.v6.service;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6RequestContext;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.Dhcp6OptionsRegistry;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.IaAddressOption;
import org.anarres.dhcp.v6.options.IaNaOption;
import org.anarres.dhcp.v6.options.IaOption;
import org.anarres.dhcp.v6.options.IaTaOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.anarres.dhcp.v6.options.SuboptionOption;
import org.anarres.dhcp.v6.options.TimedOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base DHCPv6 Lease manager managing IaNA and IaTA.
 *
 * Created by marosmars
 */
@Beta
public abstract class AbstractDhcp6LeaseManager implements Dhcp6LeaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(PooledDhcp6LeaseManager.class);

    private final ClientBindingRegistry iaNaRegistry = new ClientBindingRegistry("IaNa");
    private final ClientBindingRegistry iaTaRegistry = new ClientBindingRegistry("IaTa");
    private final Lifetimes lifetimes;

    public AbstractDhcp6LeaseManager(@Nonnull final Lifetimes lifetimes) {
        this.lifetimes = lifetimes;
    }

    // TODO add a timer to monitor valid lifetimes of leased addresses. After the valid lifetime has passed, make the address available again.

    protected ClientBindingRegistry getIaNaRegistry() {
        return iaNaRegistry;
    }

    protected ClientBindingRegistry getIaTaRegistry() {
        return iaTaRegistry;
    }

    protected Lifetimes getLifetimes() {
        return lifetimes;
    }

    @Nonnull
    @Override
    public Dhcp6Message lease(final Dhcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg, @Nonnull final Dhcp6Message reply)
        throws Dhcp6Exception {
        leaseIA(requestContext, incomingMsg, reply, IaNaOption.class, iaNaRegistry);
        leaseIA(requestContext, incomingMsg, reply, IaTaOption.class, iaTaRegistry);

        return reply;
    }

    private <T extends IaOption> void leaseIA(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
        final @Nonnull Dhcp6Message reply, Class<T> iaType, final ClientBindingRegistry iaRegistry)
        throws Dhcp6Exception {

        final DuidOption.Duid clientId = getClientId(incomingMsg);
        for (T incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {
            LOG.debug("Client {} requested IA:{} for {}. IA option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);
            final Dhcp6Options iaResponseOptions = new Dhcp6Options();
            final IaAddressOption option;

            option = wrapIp(clientId, incomingIaOption, iaRegistry,
                getIp(requestContext, clientId, incomingIaOption, iaRegistry));

            iaResponseOptions.add(option);
            final T copy = createIaOption(iaType, incomingIaOption, iaResponseOptions);

            reply.getOptions().add(copy);
        }
    }

    private <T extends IaOption> T createIaOption(final Class<T> iaType, final T incomingIaOption, final Dhcp6Options iaResponseOptions) {
        final T copy = Dhcp6OptionsRegistry.newInstance(iaType);
        copy.setData(new byte[copy.getHeaderSize() + iaResponseOptions.getLength()]);
        setTimeParameters(copy);
        copy.setIAID(incomingIaOption.getIAID());
        copy.setOptions(iaResponseOptions);
        return copy;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.3
     */
    @Nonnull
    @Override
    public Dhcp6Message renew(final Dhcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg, @Nonnull final Dhcp6Message reply)
        throws Dhcp6Exception {
        renewIA(requestContext, incomingMsg, reply, IaNaOption.class, iaNaRegistry);
        renewIA(requestContext, incomingMsg, reply, IaTaOption.class, iaTaRegistry);

        return reply;
    }

    protected final <T extends IaOption> void renewIA(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
        final Dhcp6Message reply, final Class<T> iaType, final ClientBindingRegistry iaRegistry)
        throws Dhcp6Exception {

        final DuidOption.Duid clientId = getClientId(incomingMsg);
        for (T incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {

            final Dhcp6Options iaResponseOptions = new Dhcp6Options();
            final Dhcp6Option option;

            if(iaRegistry.contains(clientId, incomingIaOption.getIAID())) {
                option = renewIAInstance(requestContext, iaRegistry, clientId, incomingIaOption, "renews");
            } else {
                LOG.debug("Client {} renews UNKNOWN IA:{} for {}. IA option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);
                option = StatusCodeOption.create(StatusCodeOption.NO_BINDING);
            }

            iaResponseOptions.add(option);
            final T copy = createIaOption(iaType, incomingIaOption, iaResponseOptions);

            reply.getOptions().add(copy);
        }
    }

    private <T extends IaOption> void setTimeParameters(final T copy) {
        if(copy instanceof TimedOption) {
            ((TimedOption) copy).setT1(lifetimes.getT1());
            ((TimedOption) copy).setT2(lifetimes.getT2());
        }
    }

    private <T extends IaOption> Dhcp6Option renewIAInstance(final Dhcp6RequestContext requestContext,
        final ClientBindingRegistry iaRegistry, final DuidOption.Duid clientId, final T incomingIaOption, final String type)
        throws Dhcp6Exception {

        final Dhcp6Option option;
        LOG.debug("Client {} {} IA:{} for {}. IA option: {}", clientId, type, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);
        // FIXME what if the address from client does not match address stored for the client on server ?
        final InetAddress ip = iaRegistry.get(clientId, incomingIaOption.getIAID()).getIp();

        final Iterable<InetAddress> addressesFromIa = getAddressesFromIa(incomingIaOption);

        // Check whether client provided at least one address for IA
        if(Iterables.isEmpty(addressesFromIa)) {
            LOG.warn("Client {} {} IA:{} for {}. IA option: {}. No address present", clientId, type, iaRegistry,
                incomingIaOption.getIAID(), incomingIaOption);
        }

        // FIXME can the client request renew with a single IaNa option containing multiple addresses ? according to RFC this is possible
        // but it is real ? how would client have multiple addresses for a single Ia ?

        // Check whether client provided the same address as is assigned to it
        final InetAddress ipFromClient = Iterables.getFirst(addressesFromIa, null);
        if(!ipFromClient.equals(ip)) {
            LOG.warn("Client {} {} UNKNOWN address:{} vs {}, IA:{} for {}.", clientId, type, ipFromClient, ip, iaRegistry, incomingIaOption.getIAID());
        }

        option = wrapIp(clientId, incomingIaOption, iaRegistry, ip);

        if(!isAppropriate(requestContext, clientId, incomingIaOption.getIAID(), ip)) {
            LOG.debug("Client {} IA:{} for {}. Inappropriate address detected: {}", clientId, iaRegistry, incomingIaOption.getIAID(), ip);
            ((IaAddressOption) option).setPreferredLifetime(0);
            ((IaAddressOption) option).setValidLifetime(0);
        }
        return option;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.3
     */
    @Nullable
    @Override
    public Dhcp6Message rebind(final Dhcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg, @Nonnull final Dhcp6Message reply)
        throws Dhcp6Exception {
        try {
            rebindIA(requestContext, incomingMsg, reply, IaNaOption.class, iaNaRegistry);
            rebindIA(requestContext, incomingMsg, reply, IaTaOption.class, iaTaRegistry);
        } catch (Dhcp6Exception.UnableToAnswerException e) {
            LOG.warn("Skipping request {}, unable to rebind", incomingMsg, e);
            return null;
        }

        // TODO check if reply not null + consider return values from above rebindIA
        requestInformation(requestContext, incomingMsg, reply);
        return reply;
    }

    protected final <T extends IaOption> void rebindIA(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
        final Dhcp6Message reply, final Class<T> iaType, final ClientBindingRegistry iaRegistry) throws Dhcp6Exception {
        final DuidOption.Duid clientId = getClientId(incomingMsg);
        for (T incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {

            final Dhcp6Options iaResponseOptions = new Dhcp6Options();
            final Dhcp6Option option;

            if(iaRegistry.contains(clientId, incomingIaOption.getIAID())) {
                option = renewIAInstance(requestContext, iaRegistry, clientId, incomingIaOption, "rebinds");
            } else {
                LOG.debug("Client {} rebinds UNKNOWN IA:{} for {}. IA option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);
                throw new Dhcp6Exception.UnableToAnswerException("No IA found for " + incomingIaOption.getIAID());
            }

            iaResponseOptions.add(option);
            final T copy = createIaOption(iaType, incomingIaOption, iaResponseOptions);

            reply.getOptions().add(copy);
        }
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.6
     */
    @Nonnull
    @Override
    public Dhcp6Message release(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
        final Dhcp6Message reply) {
        releaseIa(incomingMsg, reply, IaNaOption.class, iaNaRegistry);
        releaseIa(incomingMsg, reply, IaTaOption.class, iaTaRegistry);

        return reply;
    }

    protected final <T extends IaOption> List<ClientBindingRegistry.ClientBinding> releaseIa(final Dhcp6Message incomingMsg,
        final Dhcp6Message reply, final Class<T> iaType, final ClientBindingRegistry iaRegistry) {
        final DuidOption.Duid clientId = getClientId(incomingMsg);

        List<ClientBindingRegistry.ClientBinding> removedBindings = new ArrayList<>();

        for (T incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {
            LOG.debug("Client {} trying to release IA:{} for {}. Ia option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);

            if (iaRegistry.contains(clientId, incomingIaOption.getIAID())) {
                removedBindings.add(iaRegistry.remove(clientId, incomingIaOption.getIAID()));
                LOG.debug("Client {} released IA:{} successfully {}", clientId, iaRegistry, incomingIaOption.getIAID());
            } else {
                LOG.warn("Client {} tried to release unknown IA:{}: {}", clientId, iaRegistry, incomingIaOption.getIAID());
                // TODO make this options a constant
                final Dhcp6Options unknownIaOptions = new Dhcp6Options();
                unknownIaOptions.add(StatusCodeOption.create(StatusCodeOption.NO_BINDING));

                final T copy = createIaOption(iaType, incomingIaOption, unknownIaOptions);

                reply.getOptions().add(copy);
            }
        }

        reply.getOptions().add(StatusCodeOption.create(StatusCodeOption.SUCCESS));

        return removedBindings;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.7
     */
    @Nonnull
    @Override
    public Dhcp6Message decline(final Dhcp6RequestContext requestContext,
        final Dhcp6Message incomingMsg, final Dhcp6Message reply) throws Dhcp6Exception {
        declineIa(incomingMsg, reply, IaNaOption.class, iaNaRegistry);
        declineIa(incomingMsg, reply, IaTaOption.class, iaTaRegistry);

        requestInformation(requestContext, incomingMsg, reply);
        return reply;
    }

    @Nullable
    @Override
    public Dhcp6Message handle(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
        final byte msgType) throws Dhcp6Exception {
        LOG.warn("Unknown message type detected: {}, Ignoring: {}", msgType, incomingMsg);
        throw new Dhcp6Exception.UnknownMsgException(incomingMsg.getMessageType().getCode());
    }

    protected final <T extends IaOption> void declineIa(final Dhcp6Message incomingMsg, final Dhcp6Message reply,
        final Class<T> iaType, final ClientBindingRegistry iaRegistry) {
        final List<ClientBindingRegistry.ClientBinding> clientBindings = releaseIa(incomingMsg, reply, iaType, iaRegistry);
        // FIXME put the declined bindings into a quarantine as they are already in use somewhere and we should not provide them
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.5
     */
    @Nonnull
    @Override
    public Dhcp6Message requestInformation(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg, final Dhcp6Message reply)
        throws Dhcp6Exception {
        // Add some additional options
        return reply;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.2
     */
    @Nullable
    @Override
    public Dhcp6Message confirm(final Dhcp6RequestContext requestContext,
        final Dhcp6Message incomingMsg, final Dhcp6Message reply) throws Dhcp6Exception {
        try {
            final short confirmedStatus = confirmIAs(requestContext, incomingMsg) ?
                StatusCodeOption.SUCCESS :
                StatusCodeOption.NOT_ON_LINK;

            reply.getOptions().add(StatusCodeOption.create(confirmedStatus));
        } catch (Dhcp6Exception.UnableToAnswerException e) {
            LOG.warn("Skipping request {}, unable to confirm", incomingMsg, e);
            return null;
        }

        requestInformation(requestContext, incomingMsg, reply);
        return reply;
    }

    /**
     * Confirm all IA groups in the confirm message. Base implementation confirms IaNa and IaTa.
     */
    protected boolean confirmIAs(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg)
        throws Dhcp6Exception {
        boolean confirmed = confirmIa(requestContext, incomingMsg, IaNaOption.class, iaNaRegistry);
        confirmed &= confirmIa(requestContext, incomingMsg, IaTaOption.class, iaTaRegistry);
        return confirmed;
    }

    protected final <T extends IaOption> boolean confirmIa(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
       final Class<T> iaType, final ClientBindingRegistry iaRegistry) throws Dhcp6Exception {

        final DuidOption.Duid clientId = getClientId(incomingMsg);

        for (IaOption incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {
            LOG.debug("Client {} confirming IA:{} for {}. IA option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);

            if(iaRegistry.contains(clientId, incomingIaOption.getIAID())) {
                final Iterable<InetAddress> addresses = getAddressesFromIa(incomingIaOption);

                //or there were no addresses in any of the IAs sent by the client, the server MUST NOT send a reply to the client.
                if(Iterables.isEmpty(addresses)) {
                    LOG.warn("Client {} confirming IA:{} for {} with no address. Unable to answer", clientId, iaRegistry, incomingIaOption.getIAID());
                    throw new Dhcp6Exception.UnableToAnswerException("No addresses for " + incomingIaOption.getIAID());
                }

                for (InetAddress address : addresses) {
                    final boolean appropriate = isAppropriate(requestContext, clientId, incomingIaOption.getIAID(), address);
                    if(!appropriate) {
                        LOG.debug("Client {} inappropriate address detected for IA: {}, IA optiona: {}", clientId, incomingIaOption.getIAID(), incomingIaOption);
                        return false;
                    }
                }
            }

        }

        return true;
    }

    public static Iterable<InetAddress> getAddressesFromIa(final SuboptionOption incomingIaNaOption) throws Dhcp6Exception {
        return Iterables.transform(incomingIaNaOption.getOptions().getAll(IaAddressOption.class), new Function<IaAddressOption, InetAddress>() {
            @Override public InetAddress apply(final IaAddressOption input) {
                return input.getIp();
            }
        });
    }


    protected abstract boolean isAppropriate(Dhcp6RequestContext requestContext, DuidOption.Duid clientId, int iaid,
        InetAddress ip) throws Dhcp6Exception.UnableToAnswerException;

    private IaAddressOption wrapIp(final DuidOption.Duid clientId, final IaOption incomingIaOption,
        final ClientBindingRegistry iaRegistry, final InetAddress ip) {

        final IaAddressOption option = IaAddressOption
            .create(ip, lifetimes.getPreferredLt(), lifetimes.getValidLt(), Optional.<Dhcp6Options>absent());

        iaRegistry.add(clientId, incomingIaOption.getIAID(), ip);
        LOG.debug("Client {} leased: {} for IaNa {}", clientId, ip, incomingIaOption.getIAID());
        return option;
    }

    private InetAddress getIp(final Dhcp6RequestContext requestContext, final DuidOption.Duid clientId, final IaOption incomingIaNaOption,
        final ClientBindingRegistry iaRegistry) throws Dhcp6Exception {
        final InetAddress ip;
        if(iaRegistry.contains(clientId, incomingIaNaOption.getIAID())) {
            ip = iaRegistry.get(clientId, incomingIaNaOption.getIAID()).getIp();
        } else {
            ip = newIp(requestContext, clientId, incomingIaNaOption);
        }
        return ip;
    }

    private DuidOption.Duid getClientId(final Dhcp6Message incomingMsg) {
        return incomingMsg.getOptions().get(ClientIdOption.class).getDuid();
    }

    protected abstract InetAddress newIp(Dhcp6RequestContext requestContext, DuidOption.Duid clientId,
        IaOption iaOption)
        throws Dhcp6Exception;

    /**
     * Static lifetimes container
     */
    public static final class Lifetimes {

        private final int t1, t2;
        private final int preferredLt, validLt;

        public Lifetimes(final int t1, final int t2, final int preferredLt, final int validLt) {
            this.t1 = t1;
            this.t2 = t2;
            this.preferredLt = preferredLt;
            this.validLt = validLt;
            validate();
        }

        private void validate() {
            Preconditions.checkArgument(getPreferredLt() < getValidLt(), "Preferred lifetime < Valid lifetime");
            Preconditions.checkArgument(t1 < t2, "T1 < T2");
            Preconditions.checkArgument(t1 < getPreferredLt(), "T1 < Preferred lifetime");
            Preconditions.checkArgument(t2 < getPreferredLt(), "T2 < Preferred lifetime");
        }

        public int getPreferredLt() {
            return preferredLt;
        }

        public int getT1() {
            return t1;
        }

        public int getT2() {
            return t2;
        }

        public int getValidLt() {
            return validLt;
        }
    }
}
