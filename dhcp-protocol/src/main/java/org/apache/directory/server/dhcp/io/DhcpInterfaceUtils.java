/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.io;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
/* pp */ class DhcpInterfaceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpInterfaceUtils.class);

    /** If this returns an address, it is NOT a zero address. */
    @CheckForNull
    private static InetAddress toInetAddress(@Nonnull DhcpMessage message) throws DhcpException {
        // From the packet:
        InetAddress assignedClientAddress = message.getAssignedClientAddress();
        if (!AddressUtils.isZeroAddress(assignedClientAddress))
            return assignedClientAddress;
        InetAddress currentClientAddress = message.getCurrentClientAddress();
        if (!AddressUtils.isZeroAddress(currentClientAddress))
            return currentClientAddress;
        // TODO: Should this catch locally?
        InetAddress requestedClientAddress = message.getOptions().getAddressOption(RequestedIpAddress.class);
        if (!AddressUtils.isZeroAddress(requestedClientAddress))
            return requestedClientAddress;
        InetAddress relayAgentAddress = message.getRelayAgentAddress();
        if (!AddressUtils.isZeroAddress(relayAgentAddress))
            return relayAgentAddress;
        return null;
    }

    /** If this returns an address, it is NOT a zero address. */
    @CheckForNull
    private static InetAddress toInetAddress(@Nonnull SocketAddress sa) {
        if (!(sa instanceof InetSocketAddress))
            return null;
        InetSocketAddress isa = (InetSocketAddress) sa;
        InetAddress ia = isa.getAddress();
        if (!AddressUtils.isZeroAddress(ia))
            return ia;
        return null;
    }

    /** If this returns an address, it is NOT a zero address. */
    @CheckForNull
    public static InetAddress toInetAddress(@Nonnull Object... objects) throws DhcpException {
        for (Object object : objects) {
            if (object == null)
                continue;
            if (object instanceof InetAddress) {
                InetAddress a = (InetAddress) object;
                if (!AddressUtils.isZeroAddress(a))
                    return a;
            } else if (object instanceof SocketAddress) {
                InetAddress a = toInetAddress((SocketAddress) object);
                if (a != null)
                    return a;
            } else if (object instanceof DhcpMessage) {
                InetAddress a = toInetAddress((DhcpMessage) object);
                if (a != null)
                    return a;
            } else {
                throw new IllegalArgumentException("Cannot derive an InetAddress from " + object.getClass());
            }
        }
        LOG.debug("Failed to determine message address from " + Arrays.toString(objects));
        return null;
    }

}
