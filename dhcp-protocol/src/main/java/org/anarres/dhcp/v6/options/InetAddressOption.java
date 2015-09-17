package org.anarres.dhcp.v6.options;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Base option for InetAddress based options
 */
public abstract class InetAddressOption extends Dhcp6Option {

    public InetAddress getAddress() {
        try {
            return InetAddress.getByAddress(getData());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to parse address from: " + Arrays.toString(getData()), e);
        }
    }

    public void setAddress(final InetAddress address) {
        setData(address.getAddress());
    }

    public void setAddress(final String address) {
        try {
            setData(InetAddress.getByName(address).getAddress());
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unable to parse as InetAddress: " + address, e);
        }
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + getAddress();
    }
}
