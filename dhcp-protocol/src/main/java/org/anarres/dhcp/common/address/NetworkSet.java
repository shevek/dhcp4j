/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.primitives.UnsignedBytes;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A NetworkSet: An optimal representation for a noncontiguous set of addresses.
 *
 * @author shevek
 */
public class NetworkSet {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkSet.class);

    @Nonnull
    private static byte[] extend(@Nonnull byte[] b) {
        byte[] c = new byte[b.length + 1];
        System.arraycopy(b, 0, c, 1, b.length);
        return c;
    }

    @Nonnull
    private static byte[] truncate(@Nonnull byte[] b) {
        byte[] c = new byte[b.length - 1];
        System.arraycopy(b, 1, c, 0, c.length);
        return c;
    }

    /** Like InetAddress but without copying data in the getter. */
    private static class Address implements Comparable<Address> {

        private final byte[] data;

        public Address(@Nonnull byte[] data) {
            this.data = data;
        }

        @Nonnull
        public byte[] getData() {
            return data;
        }

        @Override
        public int compareTo(@Nonnull Address o) {
            return UnsignedBytes.lexicographicalComparator().compare(getData(), o.getData());
        }

        @Nonnull
        public InetAddress toInetAddress() {
            return AddressUtils.toInetAddress(truncate(data));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (null == obj)
                return false;
            if (!getClass().equals(obj.getClass()))
                return false;
            return Arrays.equals(data, ((Address) obj).data);
        }

        @Override
        public String toString() {
            return UnsignedBytes.join(" ", data);
        }
    }

    private static class Count {

        @CheckForSigned
        private int value;

        public Count(@CheckForSigned int value) {
            this.value = value;
        }

        public boolean add(@CheckForSigned int delta) {
            value += delta;
            return value == 0;
        }
    }
    private final SortedMap<Address, Count> data = new TreeMap<Address, Count>();

    /**
     * @param start The inclusive start-point.
     * @param end The exclusive end-point.
     */
    private void addRange(@Nonnull byte[] start, @Nonnull byte[] end, int delta) {
        START:
        {
            Address key = new Address(start);
            Count count = data.get(key);
            if (count == null)
                data.put(key, new Count(delta));
            else if (count.add(delta))
                data.remove(key);
        }

        END:
        {
            Address key = new Address(end);
            Count count = data.get(key);
            if (count == null)
                data.put(key, new Count(-delta));
            else if (count.add(-delta))
                data.remove(key);
        }
    }

    /**
     * Inclusive endpoints.
     */
    public void addRange(@Nonnull InetAddress start, @Nonnull InetAddress end) {
        addRange(extend(start.getAddress()), AddressUtils.increment(extend(end.getAddress())), 1);
    }

    /**
     * Inclusive endpoints.
     */
    public void removeRange(@Nonnull InetAddress start, @Nonnull InetAddress end) {
        addRange(extend(start.getAddress()), AddressUtils.increment(extend(end.getAddress())), -1);
    }

    public void addRange(@Nonnull InetAddressRange range) {
        addRange(range.getStart(), range.getEnd());
    }

    public void removeRange(@Nonnull InetAddressRange range) {
        removeRange(range.getStart(), range.getEnd());
    }

    /**
     * Inclusive endpoints.
     */
    public void addNetwork(@Nonnull NetworkAddress network) {
        addRange(network.getNetworkAddress(), network.getBroadcastAddress());
    }

    public void removeNetwork(@Nonnull NetworkAddress network) {
        removeRange(network.getNetworkAddress(), network.getBroadcastAddress());
    }

    public void addAddress(@Nonnull InetAddress address) {
        addRange(address, address);
    }

    public void removeAddress(@Nonnull InetAddress address) {
        removeRange(address, address);
    }

    private static void _add_bit(@Nonnull byte[] data, @Nonnegative int bitIndex) {
        // LOG.info("  +: Add " + bitIndex + " to " + UnsignedBytes.join(" ", data));
        // LOG.info(bitIndex + " -> " + (bitIndex / 8) + "[" + (bitIndex % 8) + "] & " + byteValue);

        int byteValue = 1 << (7 - (bitIndex % 8));
        // This is actually an arbitrary precision arithmetic routine computing
        // data + (1 e bitIndex)
        for (int byteIndex = bitIndex / 8; byteIndex >= 0; byteIndex--) {
            if (byteValue == 0)
                break;
            byteValue += UnsignedBytes.toInt(data[byteIndex]);
            data[byteIndex] = (byte) (byteValue & 0xFF);
            byteValue >>= Byte.SIZE;
        }
        // LOG.info("  +: Result is " + UnsignedBytes.join(" ", data));
    }

    /** endAddress is EXCLUSIVE. */
    private static void toNetworkList(@Nonnull List<NetworkAddress> out, @Nonnull Address startAddress, @Nonnull Address endAddress) {
        while (startAddress.compareTo(endAddress) < 0) {
            // LOG.info("Reducing " + startAddress + " - " + endAddress);
            byte[] data = startAddress.getData();  // Remember, this is one byte overlength.
            int startNetmask = Math.max(AddressUtils.toNetmask(data) - 8, 0);

            byte[] tmp = new byte[data.length];
            // Search down from the first set bit for the highest bit we can add without overflowing endAddress.
            SEARCH:
            {
                // Subtract 1 to hit the first set bit.
                for (int netmask = startNetmask - 1; netmask < (data.length - 1) * Byte.SIZE; netmask++) {
                    System.arraycopy(data, 0, tmp, 0, data.length);
                    // Add Byte.SIZE to correct for the overlength
                    _add_bit(tmp, netmask + Byte.SIZE);
                    // LOG.info("   [" + netmask + "] Compare tmp=" + UnsignedBytes.join(" ", tmp) + " end=" + UnsignedBytes.join(" ", endAddress.getData()));
                    if (UnsignedBytes.lexicographicalComparator().compare(tmp, endAddress.getData()) <= 0) {
                        NetworkAddress networkAddress = new NetworkAddress(AddressUtils.toInetAddress(truncate(data)), netmask);
                        // LOG.info("Adding " + networkAddress.toRange());
                        out.add(networkAddress);
                        break SEARCH;
                    }
                }
                throw new IllegalStateException("WAT?");
            }

            startAddress = new Address(tmp);    // now we'd better make a new tmp.
        }
    }

    @Nonnull
    public List<NetworkAddress> toNetworkList() {
        List<NetworkAddress> out = new ArrayList<NetworkAddress>();
        Address startAddress = null;
        int total = 0;
        for (Map.Entry<Address, Count> e : data.entrySet()) {
            NetworkSet.Address address = e.getKey();
            NetworkSet.Count count = e.getValue();

            if (total == 0)
                startAddress = address;
            total += count.value;
            if (total == 0)
                toNetworkList(out, startAddress, address);
        }
        return out;
    }

    @Nonnull
    private static InetAddressRange toRange(@Nonnull Address start, @Nonnull Address end) {
        // This routine does two copies: One here and one in truncate().
        byte[] tmp = Arrays.copyOf(end.data, end.data.length);
        AddressUtils.decrement(tmp);
        return new InetAddressRange(start.toInetAddress(), AddressUtils.toInetAddress(truncate(tmp)));
    }

    @Nonnull
    public List<InetAddressRange> toAddressRangeList() {
        List<InetAddressRange> out = new ArrayList<InetAddressRange>();
        Address startAddress = null;
        int total = 0;
        for (Map.Entry<Address, Count> e : data.entrySet()) {
            NetworkSet.Address address = e.getKey();
            NetworkSet.Count count = e.getValue();

            if (total == 0)
                startAddress = address;
            total += count.value;
            if (total == 0)
                out.add(toRange(startAddress, address));
        }
        return out;
    }
}
