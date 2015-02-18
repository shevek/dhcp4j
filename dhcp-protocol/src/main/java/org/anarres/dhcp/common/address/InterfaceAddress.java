/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import java.net.InetAddress;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * An interface address: An address and a netmask.
 *
 * The address may be any address, and the lowest and highest directly
 * accessible addresses on the network may be retrieved.
 *
 *
 * @author shevek
 */
public class InterfaceAddress extends AbstractMaskedAddress {

    public InterfaceAddress(@Nonnull InetAddress address, @Nonnegative int netmask) {
        super(address, netmask);
    }

    public InterfaceAddress(@Nonnull java.net.InterfaceAddress address) {
        this(address.getAddress(), address.getNetworkPrefixLength());
    }

    /**
     * Returns true iff this interface address "should" be able to reach the given InetAddress.
     *
     * @see NetworkAddress#contains(InetAddress)
     * @see AddressUtils#isLocal(AbstractMaskedAddress, InetAddress)
     */
    public boolean isLocal(@Nonnull InetAddress address) {
        return AddressUtils.isLocal(this, address);
    }

    /**
     * Constructs a new {@link NetworkAddress} by canonicalizing the
     * There isn't much computational reason to do this, as all of the
     * properties of the returned {@link NetworkAddress} will agree with this
     * InterfaceAddress except for the fundamental {@link #getAddress() address}.
     */
    @Nonnull
    public NetworkAddress toNetworkAddress() {
        return new NetworkAddress(getAddress(), getNetmask());
    }
}
