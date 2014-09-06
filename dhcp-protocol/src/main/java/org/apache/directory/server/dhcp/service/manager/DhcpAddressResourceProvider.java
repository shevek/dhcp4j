/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.anarres.jallocator.ResourceProvider;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.Subnet;

/**
 *
 * @author shevek
 */
public class DhcpAddressResourceProvider implements ResourceProvider<InetAddress> {

    private final byte[] rangeStartBytes;
    private final long rangeSize;

    // private final byte[] addressBytes = new byte[rangeStartBytes.length];
    public DhcpAddressResourceProvider(@Nonnull Subnet subnet) {
        Preconditions.checkNotNull(subnet, "Subnet was null.");
        rangeStartBytes = subnet.getRangeStart().getAddress();
        rangeSize = subnet.getRangeSize();
    }

    @Override
    public long getResourceCount() {
        return rangeSize;
    }

    @Override
    public InetAddress getResource(long index) {
        byte[] addressBytes = Arrays.copyOf(rangeStartBytes, rangeStartBytes.length);
        addressBytes = AddressUtils.add(addressBytes, index);
        return AddressUtils.toInetAddress(addressBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rangeStartBytes) ^ (int) rangeSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DhcpAddressResourceProvider other = (DhcpAddressResourceProvider) obj;
        return Arrays.equals(rangeStartBytes, other.rangeStartBytes) && rangeSize == other.rangeSize;
    }

    @Override
    public String toString() {
        InetAddress rangeStart = AddressUtils.toInetAddress(rangeStartBytes);
        InetAddress rangeEnd = getResource(getResourceCount() - 1);
        return Objects.toStringHelper(this)
                .add("rangeStartBytes", Arrays.toString(rangeStartBytes))
                .add("rangeStart", InetAddresses.toAddrString(rangeStart))
                .add("rangeEnd", InetAddresses.toAddrString(rangeEnd))
                .add("rangeSize", rangeSize)
                .toString();
    }
}
