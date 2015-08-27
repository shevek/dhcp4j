package org.anarres.dhcp.v6.service;

import com.google.common.base.Optional;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.options.ClientIdOption;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.DuidOption;
import org.anarres.dhcp.v6.options.IaAddressOption;
import org.anarres.dhcp.v6.options.IaNaOption;
import org.anarres.dhcp.v6.options.IaTaOption;
import org.anarres.dhcp.v6.options.StatusCodeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePooledDhcp6LeaseManager implements Dhcp6LeaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(SimplePooledDhcp6LeaseManager.class);

    private final ClientBindingRegistry iaNaRegistry = new ClientBindingRegistry();

    private final InetAddress startingAddress;
    private final InetAddress endingAddress;

    // TODO extract abstract lease manager for further reuse

    public SimplePooledDhcp6LeaseManager(InetAddress startingAddress, InetAddress endingAddress) {
        this.startingAddress = startingAddress;
        this.endingAddress = endingAddress;
    }

    @Override public Dhcp6Message lease(@Nonnull final Dhcp6Message incomingMsg, @Nonnull final Dhcp6Message reply) {
        final Iterable<IaNaOption> iaNaOptions = incomingMsg.getOptions().getAll(IaNaOption.class);
        leaseNa(incomingMsg, reply, iaNaOptions);

        final Iterable<IaTaOption> iaTaOptions = incomingMsg.getOptions().getAll(IaTaOption.class);
        leaseTa(incomingMsg, reply, iaTaOptions);

        return reply;
    }

    private void leaseTa(final Dhcp6Message incomingMsg, final Dhcp6Message reply, final Iterable<IaTaOption> iaTaOption) {
        // FIXME Implement
    }

    private void leaseNa(final Dhcp6Message incomingMsg, final @Nonnull Dhcp6Message reply, final Iterable<IaNaOption> incomingIaNaOptions) {

        // TODO consider IaAddressOptions
        // TODO consider T1 and T2

        final DuidOption.Duid clientId = getClientId(incomingMsg);
        for (IaNaOption incomingIaNaOption : incomingIaNaOptions) {
            LOG.debug("Client {} requested IaNa {}, {}", clientId, incomingIaNaOption.getIAID(), incomingIaNaOption);
            final Dhcp6Options iaNaResponseOptions = new Dhcp6Options();
            final IaAddressOption option;

            final InetAddress ip;
            if(iaNaRegistry.contains(clientId, incomingIaNaOption.getIAID())) {
                ip = iaNaRegistry.get(clientId, incomingIaNaOption.getIAID()).getIp();
            } else {
                ip = getIp(clientId, incomingIaNaOption.getIAID());
            }

            option = IaAddressOption.create(ip, getPreferredLifetime(), getValidaLifetime(),
            Optional.<Dhcp6Options>absent());

            iaNaRegistry.add(clientId, incomingIaNaOption.getIAID(), ip);
            LOG.debug("Client {} leased: {} for IaNa {}", clientId, ip, incomingIaNaOption.getIAID());

            iaNaResponseOptions.add(option);
            reply.getOptions().add(IaNaOption.create(incomingIaNaOption.getIAID(), incomingIaNaOption.getT1(),
                incomingIaNaOption.getT2(), Optional.of(iaNaResponseOptions)));
        }

    }

    private DuidOption.Duid getClientId(final Dhcp6Message incomingMsg) {
        return incomingMsg.getOptions().get(ClientIdOption.class).getDuid();
    }

    private InetAddress getIp(final DuidOption.Duid clientId, final int iaid) {
        // Warning, this is highly inefficient
        InetAddress current = startingAddress;
        while (true) {
            if(current.equals(endingAddress)) {
                throw new IllegalStateException("IP pool exhausted");
            }

            // TODO check also IATA
            if(!iaNaRegistry.containsIp(current)) {
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
    @Override public Dhcp6Message release(final Dhcp6Message incomingMsg, final Dhcp6Message reply) {
        final Iterable<IaNaOption> iaNaOptions = incomingMsg.getOptions().getAll(IaNaOption.class);
        releaseNa(incomingMsg, reply, iaNaOptions);

        final Iterable<IaTaOption> iaTaOptions = incomingMsg.getOptions().getAll(IaTaOption.class);
        releaseTa(incomingMsg, reply, iaNaOptions);

        return reply;
    }

    private void releaseTa(final Dhcp6Message incomingMsg, final Dhcp6Message reply, final Iterable<IaNaOption> iaTaOption) {
        // FIXME implement
    }

    private void releaseNa(final Dhcp6Message incomingMsg, final Dhcp6Message reply,
        final Iterable<IaNaOption> incomingIaNaOptions) {
        final DuidOption.Duid clientId = getClientId(incomingMsg);

        for (IaNaOption incomingIaNaOption : incomingIaNaOptions) {
            LOG.debug("Client {} is releasing IaNa {}, {}", clientId, incomingIaNaOption.getIAID(), incomingIaNaOption);

            if (iaNaRegistry.contains(clientId, incomingIaNaOption.getIAID())) {
                // TODO we should check if the IPs are the same and warn if they are not
                iaNaRegistry.remove(clientId, incomingIaNaOption.getIAID());
                LOG.debug("Client {} released IaNa successfully {}", clientId, incomingIaNaOption.getIAID());
            } else {
                LOG.warn("Client {} tried to release unknown IaNa: {}", clientId, incomingIaNaOption.getIAID());
                // TODO make this options a constant
                final Dhcp6Options unknownIaNaOptions = new Dhcp6Options();
                unknownIaNaOptions.add(StatusCodeOption.create(StatusCodeOption.NO_BINDING));

                reply.getOptions().add(IaNaOption.create(incomingIaNaOption.getIAID(), incomingIaNaOption.getT1(), incomingIaNaOption.getT2(), Optional.fromNullable(
                    unknownIaNaOptions)));
            }
        }

        reply.getOptions().add(StatusCodeOption.create(StatusCodeOption.SUCCESS, "Release"));
    }
}
