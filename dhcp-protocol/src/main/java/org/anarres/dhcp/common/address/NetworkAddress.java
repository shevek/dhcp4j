/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import java.net.InetAddress;
import java.util.Arrays;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public class NetworkAddress extends AbstractMaskedAddress {

    public NetworkAddress(@Nonnull InetAddress address, @Nonnegative int netmask) {
        super(AddressUtils.toNetworkAddress(address, netmask), netmask);
    }

    public boolean contains(@Nonnull InetAddress address) {
        if (!getAddress().getClass().equals(address.getClass()))
            return false;
        byte[] network = AddressUtils.toNetworkAddress(address.getAddress(), getNetmask());
        return Arrays.equals(getAddress().getAddress(), network);
    }
}
