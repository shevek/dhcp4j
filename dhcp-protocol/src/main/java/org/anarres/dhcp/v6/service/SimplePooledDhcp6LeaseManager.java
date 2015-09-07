package org.anarres.dhcp.v6.service;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.net.InetAddresses;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePooledDhcp6LeaseManager implements Dhcp6LeaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(SimplePooledDhcp6LeaseManager.class);

    private final ClientBindingRegistry iaNaRegistry = new ClientBindingRegistry("IaNa");
    private final ClientBindingRegistry iaTaRegistry = new ClientBindingRegistry("IaTa");

    private final InetAddress startingAddress;
    private final InetAddress endingAddress;

    // TODO extract abstract lease manager for further reuse

    public SimplePooledDhcp6LeaseManager(@Nonnull final InetAddress startingAddress, @Nonnull final InetAddress endingAddress) {
        this.startingAddress = startingAddress;
        this.endingAddress = endingAddress;
    }

    @Nonnull
    @Override
    public Dhcp6Message lease(final Dhcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg, @Nonnull final Dhcp6Message reply) {
        leaseIA(requestContext, incomingMsg, reply, IaNaOption.class, iaNaRegistry);
        leaseIA(requestContext, incomingMsg, reply, IaTaOption.class, iaTaRegistry);

        return reply;
    }

    private <T extends IaOption> void leaseIA(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
        final @Nonnull Dhcp6Message reply, Class<T> iaType, final ClientBindingRegistry iaRegistry) {

        // TODO consider IaAddressOptions as suggestion
        // TODO consider T1 and T2

        final DuidOption.Duid clientId = getClientId(incomingMsg);
        for (T incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {
            LOG.debug("Client {} requested IA:{} for {}. IA option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);
            final Dhcp6Options iaNaResponseOptions = new Dhcp6Options();
            final IaAddressOption option;

            option = wrapIp(clientId, incomingIaOption, iaRegistry,
                getIp(requestContext, clientId, incomingIaOption, iaRegistry));

            iaNaResponseOptions.add(option);
            final T copy = Dhcp6OptionsRegistry.copy(iaType, incomingIaOption);
            // TODO consider T1 and T2
            copy.setOptions(iaNaResponseOptions);

            reply.getOptions().add(copy);
        }
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-18.2.3
     */
    @Nonnull
    @Override
    public Dhcp6Message renew(final Dhcp6RequestContext requestContext, @Nonnull final Dhcp6Message incomingMsg, @Nonnull final Dhcp6Message reply) {
        renewIA(requestContext, incomingMsg, reply, IaNaOption.class, iaNaRegistry);
        renewIA(requestContext, incomingMsg, reply, IaTaOption.class, iaTaRegistry);

        return reply;
    }

    private <T extends IaOption> void renewIA(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
        final Dhcp6Message reply, final Class<T> iaType, final ClientBindingRegistry iaRegistry) {

        final DuidOption.Duid clientId = getClientId(incomingMsg);
        for (T incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {
            LOG.debug("Client {} renews IA:{} for {}. IA option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);

            final Dhcp6Options iaNaResponseOptions = new Dhcp6Options();
            final Dhcp6Option option;

            if(iaRegistry.contains(clientId, incomingIaOption.getIAID())) {
                final InetAddress ip = iaRegistry.get(clientId, incomingIaOption.getIAID()).getIp();
                option = wrapIp(clientId, incomingIaOption, iaRegistry, ip);
                if(!isAppropriate(requestContext, clientId, incomingIaOption.getIAID(), ip)) {
                    ((IaAddressOption) option).setPreferredLifetime(0);
                    ((IaAddressOption) option).setValidLifetime(0);
                }
            } else {
                option = StatusCodeOption.create(StatusCodeOption.NO_BINDING);
            }

            iaNaResponseOptions.add(option);
            final T copy = Dhcp6OptionsRegistry.copy(iaType, incomingIaOption);
            copy.setOptions(iaNaResponseOptions);

            reply.getOptions().add(copy);
        }
    }

    /**
     * Invoked during renew. Each address needs to be checked if still appropriate
     */
    protected boolean isAppropriate(final Dhcp6RequestContext requestContext, final DuidOption.Duid clientId, final int iaid,
        final InetAddress ip) {
        return true;
    }

    private IaAddressOption wrapIp(final DuidOption.Duid clientId, final IaOption incomingIaOption,
        final ClientBindingRegistry iaRegistry, final InetAddress ip) {

        final IaAddressOption option = IaAddressOption
            .create(ip, getPreferredLifetime(), getValidaLifetime(), Optional.<Dhcp6Options>absent());

        iaRegistry.add(clientId, incomingIaOption.getIAID(), ip);
        LOG.debug("Client {} leased: {} for IaNa {}", clientId, ip, incomingIaOption.getIAID());
        return option;
    }

    private InetAddress getIp(final Dhcp6RequestContext requestContext, final DuidOption.Duid clientId, final IaOption incomingIaNaOption,
        final ClientBindingRegistry iaRegistry) {
        final InetAddress ip;
        if(iaRegistry.contains(clientId, incomingIaNaOption.getIAID())) {
            ip = iaRegistry.get(clientId, incomingIaNaOption.getIAID()).getIp();
        } else {
            ip = newIp(requestContext, clientId, incomingIaNaOption.getIAID());
        }
        return ip;
    }

    private DuidOption.Duid getClientId(final Dhcp6Message incomingMsg) {
        return incomingMsg.getOptions().get(ClientIdOption.class).getDuid();
    }

    private InetAddress newIp(final Dhcp6RequestContext requestContext, final DuidOption.Duid clientId, final int iaid) {
        // Warning, this is highly inefficient
        InetAddress current = startingAddress;
        while (true) {
            if(current.equals(endingAddress)) {
                throw new IllegalStateException("IP pool exhausted");
            }

            if(!iaNaRegistry.containsIp(current) && !iaTaRegistry.containsIp(current)) {
                return current;
            }

            current = InetAddresses.increment(current);
        }
    }

    private int getValidaLifetime() {
        // FIXME
        return 45;
    }

    private int getPreferredLifetime() {
        // FIXME
        return 55;
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

    private <T extends IaOption> List<ClientBindingRegistry.ClientBinding> releaseIa(final Dhcp6Message incomingMsg,
        final Dhcp6Message reply, final Class<T> iaType, final ClientBindingRegistry iaRegistry) {
        final DuidOption.Duid clientId = getClientId(incomingMsg);

        List<ClientBindingRegistry.ClientBinding> removedBindings = new ArrayList<>();

        for (T incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {
            LOG.debug("Client {} trying to release IA:{} for {}. Ia option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);

            if (iaRegistry.contains(clientId, incomingIaOption.getIAID())) {
                // TODO we should check if the IPs are the same and warn if they are not
                removedBindings.add(iaRegistry.remove(clientId, incomingIaOption.getIAID()));
                LOG.debug("Client {} released IA:{} successfully {}", clientId, iaRegistry, incomingIaOption.getIAID());
            } else {
                LOG.warn("Client {} tried to release unknown IA:{}: {}", clientId, iaRegistry, incomingIaOption.getIAID());
                // TODO make this options a constant
                final Dhcp6Options unknownIaNaOptions = new Dhcp6Options();
                unknownIaNaOptions.add(StatusCodeOption.create(StatusCodeOption.NO_BINDING));

                final T copy = Dhcp6OptionsRegistry.copy(iaType, incomingIaOption);
                copy.setOptions(unknownIaNaOptions);

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
        final Dhcp6Message incomingMsg, final Dhcp6Message reply) {
        declineIa(incomingMsg, reply, IaNaOption.class, iaNaRegistry);
        declineIa(incomingMsg, reply, IaTaOption.class, iaTaRegistry);

        requestInformation(requestContext, incomingMsg, reply);
        return reply;
    }

    private <T extends IaOption> void declineIa(final Dhcp6Message incomingMsg, final Dhcp6Message reply,
        final Class<T> iaType, final ClientBindingRegistry iaRegistry) {
        final List<ClientBindingRegistry.ClientBinding> clientBindings = releaseIa(incomingMsg, reply, iaType, iaRegistry);
        // FIXME put the declined bindings into a quarantine as they are already in use somewhere and we should not provide them
    }

    @Nonnull
    @Override
    public Dhcp6Message requestInformation(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg, final Dhcp6Message reply) {
        // Add some additional options
        return reply;
    }

    @Nullable
    @Override
    public Dhcp6Message confirm(final Dhcp6RequestContext requestContext,
        final Dhcp6Message incomingMsg, final Dhcp6Message reply) throws Dhcp6Exception {
        confirmIa(requestContext, incomingMsg, IaNaOption.class, iaNaRegistry);
        confirmIa(requestContext, incomingMsg, IaTaOption.class, iaTaRegistry);

        requestInformation(requestContext, incomingMsg, reply);
        return reply;
    }

    private <T extends IaOption> boolean confirmIa(final Dhcp6RequestContext requestContext, final Dhcp6Message incomingMsg,
       final Class<T> iaType, final ClientBindingRegistry iaRegistry) throws Dhcp6Exception {

        final DuidOption.Duid clientId = getClientId(incomingMsg);

        boolean confirmed = false;

        for (IaOption incomingIaOption : incomingMsg.getOptions().getAll(iaType)) {
            LOG.debug("Client {} confirming IA:{} for {}. IA option: {}", clientId, iaRegistry, incomingIaOption.getIAID(), incomingIaOption);

            if(iaRegistry.contains(clientId, incomingIaOption.getIAID())) {
                final Iterable<InetAddress> addresses = getAddressesFromIa(incomingIaOption);
                for (InetAddress address : addresses) {
                    confirmed = isAppropriate(requestContext, clientId, incomingIaOption.getIAID(), address);
                }
            }

        }

        return confirmed;
    }

    private Iterable<InetAddress> getAddressesFromIa(final SuboptionOption incomingIaNaOption) throws Dhcp6Exception {
        return Iterables.transform(incomingIaNaOption.getOptions().getAll(IaAddressOption.class), new Function<IaAddressOption, InetAddress>() {
            @Override public InetAddress apply(final IaAddressOption input) {
                return input.getIp();
            }
        });
    }

}
