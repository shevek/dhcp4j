/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.io;

import com.google.common.base.Preconditions;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;

/**
 *
 * @author shevek
 */
@NotThreadSafe
public class DhcpRequestContext {

    private final Iterable<? extends InterfaceAddress> interfaceAddresses;
    private final InetSocketAddress remoteSocketAddress;
    private final InetSocketAddress localSocketAddress;
    private InterfaceAddress interfaceAddress = null;
    private InetAddress clientAddress = null;

    public DhcpRequestContext(
            @Nonnull Iterable<? extends InterfaceAddress> interfaceAddresses,
            @Nonnull InetSocketAddress remoteSocketAddress,
            @Nonnull InetSocketAddress localSocketAddress
    ) {
        this.interfaceAddresses = interfaceAddresses;
        this.remoteSocketAddress = remoteSocketAddress;
        this.localSocketAddress = localSocketAddress;

        for (InterfaceAddress interfaceAddress : interfaceAddresses)
            if (AddressUtils.isZeroAddress(interfaceAddress.getAddress()))
                throw new IllegalArgumentException("Illegal InterfaceAddress " + interfaceAddress);
    }

    public DhcpRequestContext(
            @Nonnull InterfaceAddress[] interfaceAddresses,
            @Nonnull InetSocketAddress remoteSocketAddress,
            @Nonnull InetSocketAddress localSocketAddress) {
        this(Arrays.asList(interfaceAddresses), remoteSocketAddress, localSocketAddress);
    }

    public DhcpRequestContext(
            @Nonnull InterfaceAddress interfaceAddress,
            @Nonnull InetSocketAddress remoteSocketAddress,
            @Nonnull InetSocketAddress localSocketAddress) {
        this(Collections.singletonList(interfaceAddress), remoteSocketAddress, localSocketAddress);
        this.interfaceAddress = interfaceAddress;
    }

    @Nonnull
    public Iterable<? extends InterfaceAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    /** May be the zero address. */
    @Nonnull
    public InetSocketAddress getRemoteSocketAddress() {
        return remoteSocketAddress;
    }

    /** May be the zero address. */
    @Nonnull
    public InetAddress getRemoteAddress() {
        return Preconditions.checkNotNull(getRemoteSocketAddress().getAddress(), "Remote InetSocketAddress was null.");
    }

    /** May be the zero address. */
    @Nonnull
    public InetSocketAddress getLocalSocketAddress() {
        return localSocketAddress;
    }

    /** May be the zero address. */
    @Nonnull
    public InetAddress getLocalAddress() {
        return Preconditions.checkNotNull(getLocalSocketAddress().getAddress(), "Local InetAddress was null.");
    }

    public void setRelayAgentAddress(@CheckForNull InetAddress relayAgentAddress) {
    }

    public void setClientAddress(@Nonnull InetAddress clientAddress) {
        Preconditions.checkArgument(!AddressUtils.isZeroAddress(clientAddress), "Client address was null or zero.");
        Preconditions.checkState(this.clientAddress == null, "Already have a client address.");
        this.clientAddress = clientAddress;
        IFACE:
        if (interfaceAddress == null) {
            for (InterfaceAddress interfaceAddress : getInterfaceAddresses()) {
                if (interfaceAddress.isLocal(clientAddress)) {
                    this.interfaceAddress = interfaceAddress;
                    break IFACE;
                }
            }
            throw new IllegalArgumentException("No interface can reach client address " + clientAddress + ": " + getInterfaceAddresses());
        }
    }

    @Nonnull
    public InterfaceAddress getInterfaceAddress() {
        return Preconditions.checkNotNull(interfaceAddress, "InterfaceAddress not set - did you call setClientAddress()?");
    }

}
