/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.net.InetAddresses;
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

    /**
     * Constructs an InterfaceAddress from a String of the form 1.2.3.4/25.
     *
     * @throws IllegalArgumentException if the argument was duff.
     */
    @Nonnull
    public static InterfaceAddress forString(@Nonnull String addressString) {
        String netmaskString = null;
        int idx = addressString.indexOf('/');
        if (idx != -1) {
            netmaskString = addressString.substring(idx + 1);
            addressString = addressString.substring(0, idx);
        }
        InetAddress address = InetAddresses.forString(addressString);
        int netmask;
        if (netmaskString != null) {
            try {
                netmask = Integer.parseInt(netmaskString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot parse netmask from " + netmaskString, e);
            }
        } else {
            netmask = address.getAddress().length * Byte.SIZE;
        }
        return new InterfaceAddress(address, netmask);
    }

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
