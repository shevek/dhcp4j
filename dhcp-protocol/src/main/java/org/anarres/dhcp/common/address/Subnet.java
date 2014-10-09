/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.base.Objects;
import com.google.common.primitives.UnsignedBytes;
import java.net.InetAddress;
import java.util.Comparator;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class Subnet {

    private static final Logger LOG = LoggerFactory.getLogger(Subnet.class);
    @Nonnull
    private NetworkAddress networkAddress;
    @Nonnull
    private InetAddress rangeStart;
    @Nonnull
    private InetAddress rangeEnd;
    @Nonnegative
    private transient long rangeSize = -1;

    public Subnet() {
    }

    public Subnet(@Nonnull NetworkAddress networkAddress, @CheckForNull InetAddress rangeStart, @CheckForNull InetAddress rangeEnd) {
        this.networkAddress = networkAddress;
        _setRangeStart(rangeStart);
        _setRangeEnd(rangeEnd);
        setRangeSize();
    }

    @Nonnull
    public NetworkAddress getNetworkAddress() {
        return networkAddress;
    }

    @Nonnull
    public InetAddress getRangeStart() {
        return rangeStart;
    }

    // @Attribute
    protected void _setRangeStart(InetAddress rangeStart) {
        if (rangeStart == null)
            rangeStart = AddressUtils.increment(networkAddress.getAddress());
        if (!networkAddress.contains(rangeStart))
            throw new IllegalArgumentException("Range startpoint " + rangeStart + " not in network " + networkAddress);
        this.rangeStart = rangeStart;
    }

    public void setRangeStart(InetAddress rangeStart) {
        _setRangeStart(rangeStart);
        setRangeSize();
    }

    @Nonnull
    public InetAddress getRangeEnd() {
        return rangeEnd;
    }

    // @Attribute
    protected void _setRangeEnd(InetAddress rangeEnd) {
        if (rangeEnd == null)
            rangeEnd = AddressUtils.decrement(networkAddress.getBroadcastAddress());
        if (!networkAddress.contains(rangeEnd))
            throw new IllegalArgumentException("Range endpoint " + rangeEnd + " not in network " + networkAddress);
        this.rangeEnd = rangeEnd;
    }

    public void setRangeEnd(InetAddress rangeEnd) {
        _setRangeEnd(rangeEnd);
        setRangeSize();
    }

    @Nonnegative
    public long getRangeSize() {
        return rangeSize;
    }

    // @Commit
    protected void setRangeSize() {
        InetAddress start = getRangeStart();
        InetAddress end = getRangeEnd();
        byte[] range = AddressUtils.subtract(end.getAddress(), start.getAddress());
        this.rangeSize = AddressUtils.toLong(range) + 1;
    }

    public boolean rangeContains(@Nonnull byte[] address) {
        Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();
        return (comparator.compare(address, getRangeStart().getAddress()) >= 0)
                && (comparator.compare(address, getRangeEnd().getAddress()) <= 0);
    }

    public boolean rangeContains(@Nonnull InetAddress address) {
        return rangeContains(address.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getNetworkAddress(), getRangeStart(), getRangeEnd());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!getClass().equals(obj.getClass()))
            return false;
        Subnet other = (Subnet) obj;
        return Objects.equal(getNetworkAddress(), other.getNetworkAddress())
                && Objects.equal(getRangeStart(), other.getRangeStart())
                && Objects.equal(getRangeEnd(), other.getRangeEnd());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("networkAddress", getNetworkAddress())
                .add("rangeStart", getRangeStart())
                .add("rangeEnd", getRangeEnd())
                .add("rangeSize", getRangeSize())
                .toString();
    }
}
