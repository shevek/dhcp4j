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
}
