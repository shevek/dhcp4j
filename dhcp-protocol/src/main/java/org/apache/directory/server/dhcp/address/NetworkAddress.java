/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.address;

import java.net.InetAddress;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public class NetworkAddress extends AbstractMaskedAddress {

    @Nonnull
    private static InetAddress toNetworkAddress(@Nonnull InetAddress address, @Nonnegative int netmask) {
        byte[] data = address.getAddress();
        AddressUtils.mask(data, netmask);
        return AddressUtils.toAddress(data);
    }

    public NetworkAddress(@Nonnull InetAddress address, @Nonnegative int netmask) {
        super(toNetworkAddress(address, netmask), netmask);
    }

}
