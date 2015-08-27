package org.anarres.dhcp.v6.io;

import java.net.InetAddress;

public class DHcp6RequestContext {

    private final InetAddress clientAddress;

    public DHcp6RequestContext(final InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }
}
