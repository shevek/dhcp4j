package org.anarres.dhcp.v6.io;

import java.net.InetAddress;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.options.Dhcp6Options;

/**
 * Request context with IP information about the client
 */
public class Dhcp6RequestContext {

    private final InetAddress clientAddress;
    private final InetAddress linkAddress;
    private final Dhcp6Options relayedOptions;

    /**
     * Non-relayed messages
     */
    public Dhcp6RequestContext(@Nonnull final InetAddress clientAddress) {
        this(clientAddress, clientAddress, Dhcp6Options.EMPTY);
    }

    /**
     * Relayed messages
     */
    public Dhcp6RequestContext(@Nonnull final InetAddress linkAddress, @Nonnull final InetAddress clientAddress,
        @Nonnull final Dhcp6Options options) {
        this.linkAddress = linkAddress;
        this.clientAddress = clientAddress;
        this.relayedOptions = options;
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
    public Dhcp6Options getRelayedOptions() {
        return relayedOptions;
    }

    public Dhcp6RequestContext withRelayedOptions(@Nonnull final Dhcp6Options relayedOptions) {
        return new Dhcp6RequestContext(getLinkAddress(), getClientAddress(), relayedOptions);
    }
}
