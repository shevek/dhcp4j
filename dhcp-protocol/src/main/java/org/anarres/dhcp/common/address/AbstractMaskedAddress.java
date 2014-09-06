/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.base.Objects;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public abstract class AbstractMaskedAddress {

    private final InetAddress address;
    private final int netmask;

    public AbstractMaskedAddress(@Nonnull InetAddress address, @Nonnegative int netmask) {
        this.address = address;
        this.netmask = netmask;
    }

    @Nonnull
    public InetAddress getAddress() {
        return address;
    }

    @Nonnegative
    public int getNetmask() {
        return netmask;
    }

    @Nonnull
    public InetAddress getBroadcastAddress() {
        return AddressUtils.toBroadcastAddress(getAddress(), getNetmask());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getAddress()) ^ getNetmask();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!getClass().equals(obj.getClass()))
            return false;
        AbstractMaskedAddress other = (AbstractMaskedAddress) obj;
        return Objects.equal(getAddress(), other.getAddress())
                && getNetmask() == other.getNetmask();
    }

    @Override
    public String toString() {
        return InetAddresses.toAddrString(getAddress()) + "/" + getNetmask();
    }

}
