/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Arrays;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A network address: A base address and a netmask representing a contiguous
 * range of addresses.
 * 
 * The base address is canonicalized by the constructor to be the
 * first address in the permissible range. Thus {@link #getAddress()}
 * will NOT necessarily return the same {@link InetAddress} as was passed
 * to the constructor.
 * 
 * @see NetworkSet
 *
 * @author shevek
 */
public class NetworkAddress extends AbstractMaskedAddress {

    /**
     * Constructs a NetworkAddress from a String of the form 1.2.3.4/25.
     * @throws IllegalArgumentException if the argument was duff.
     */
    @Nonnull
    public static NetworkAddress forString(@Nonnull String addressString) {
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
        return new NetworkAddress(address, netmask);
    }

    /**
     * This constructor canonicalizes the fundamental address to be the network address.
     */
    public NetworkAddress(@Nonnull InetAddress address, @Nonnegative int netmask) {
        super(AddressUtils.toNetworkAddress(address, netmask), netmask);
    }

    @Override
    public InetAddress getNetworkAddress() {
        return getAddress();
    }

    /**
     * Returns true iff this network contains the given InetAddress.
     */
    public boolean contains(@Nonnull InetAddress address) {
        if (!getAddress().getClass().equals(address.getClass()))
            return false;
        byte[] network = AddressUtils.toNetworkAddress(address.getAddress(), getNetmask());
        return Arrays.equals(getAddress().getAddress(), network);
    }

    /**
     * Returns true iff this network contains the whole of the given network.
     * That is to say, the given network represents a sub-network of this network.
     */
    public boolean contains(@Nonnull NetworkAddress networkAddress) {
        return contains(networkAddress.getNetworkAddress())
                && contains(networkAddress.getBroadcastAddress());
    }

    /**
     * Returns an InetAddressRange representation of this network's address range.
     * 
     * The InetAddressRange isn't as useful for computation, but can be
     * useful for display or configuring other tools.
     */
    @Nonnull
    public InetAddressRange toRange() {
        return new InetAddressRange(getNetworkAddress(), getBroadcastAddress());
    }
}
