package org.anarres.dhcp.v6.io;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.net.InetAddress;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.anarres.dhcp.v6.options.Dhcp6Options;

/**
 * Request context with IP information about the client
 */
public class Dhcp6RequestContext {

    private final InetAddress clientAddress;
    private final InetAddress linkAddress;
    private final Optional<Dhcp6Options> relayedOptions;

    /**
     * Non-relayed messages
     */
    public Dhcp6RequestContext(@Nonnull final InetAddress clientAddress) {
        this(clientAddress, clientAddress, null);
    }

    /**
     * Relayed messages
     */
    public Dhcp6RequestContext(@Nonnull final InetAddress linkAddress, @Nonnull final InetAddress clientAddress,
        @Nullable final Dhcp6Options options) {
        this.linkAddress = linkAddress;
        this.clientAddress = clientAddress;
        this.relayedOptions = Optional.fromNullable(options);
    }

    /**
     * Unicast address of the client
     */
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-7.1
     */
    public InetAddress getLinkAddress() {
        return linkAddress;
    }

    /**
     * Options added by the relay agents
     */
    public Optional<Dhcp6Options> getRelayedOptions() {
        return relayedOptions;
    }

    public Dhcp6RequestContext withRelayedOptions(Dhcp6Options relayedOptions) {
        Preconditions.checkState(!getRelayedOptions().isPresent(), "Relayed options already set");
        return new Dhcp6RequestContext(getLinkAddress(), getClientAddress(), relayedOptions);
    }
}
