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
     *
     * @throws IllegalArgumentException if the argument was duff.
     */
    @Nonnull
    public static NetworkAddress forString(@Nonnull String addressString) {
        return InterfaceAddress.forString(addressString).toNetworkAddress();
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
     * Returns the <code>index</code>th machine address in this network.
     *
     * The 0th address is the network address. The first usable address is index 1.
     * If your network is larger than 2^63 machines, you are out of luck
     * with this method.
     *
     * @see #getMachineInterfaceAddress(long)
     * @see AddressUtils#add(InetAddress, long)
     */
    @Nonnull
    public InetAddress getMachineAddress(@Nonnegative long index) {
        return AddressUtils.add(getNetworkAddress(), index);
    }

    /**
     * Returns the <code>index</code>th machine address in this network.
     *
     * The 0th address is the network address. The first usable address is index 1.
     * If your network is larger than 2^63 machines, you are out of luck
     * with this method.
     *
     * The netmask in the returned {@link InterfaceAddress} is that of this
     * NetworkAddress.
     *
     * @see #getMachineAddress(long)
     * @see AddressUtils#add(InetAddress, long)
     */
    @Nonnull
    public InterfaceAddress getMachineInterfaceAddress(@Nonnegative long index) {
        return new InterfaceAddress(getMachineAddress(index), getNetmask());
    }

    /**
     * Returns true iff this network contains the given InetAddress.
     *
     * @see InterfaceAddress#isLocal(InetAddress)
     * @see AddressUtils#isLocal(AbstractMaskedAddress, InetAddress)
     */
    public boolean contains(@Nonnull InetAddress address) {
        return AddressUtils.isLocal(this, address);
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
