/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.UnsignedBytes;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.messages.HardwareAddress;

/**
 * General purpose address manipulation routines.
 *
 * All methods are big-endian (network-endian) and modify their arguments.
 *
 * @see InetAddresses
 * @author shevek
 */
public class AddressUtils {

    /**
     * Performs an arbitrary-precision increment of a byte array.
     *
     * @param in The array to increment.
     * @return The same input array.
     */
    @Nonnull
    public static byte[] increment(@Nonnull byte[] in) {
        for (int i = in.length - 1; i >= 0; i--) {
            if (UnsignedBytes.toInt(in[i]) < 255) {
                in[i]++;
                break;
            }
            in[i] = 0;
        }

        return in;
    }

    /**
     * Increments an InetAddress.
     */
    @Nonnull
    public static InetAddress increment(@Nonnull InetAddress in) {
        return toInetAddress(increment(in.getAddress()));
    }

    /**
     * Performs an arbitrary-precision decrement of a byte array.
     *
     * @param in The array to decrement.
     * @return The same input array.
     */
    @Nonnull
    public static byte[] decrement(@Nonnull byte[] in) {
        for (int i = in.length - 1; i >= 0; i--) {
            if (UnsignedBytes.toInt(in[i]) > 0) {
                in[i]--;
                break;
            }
            in[i] = UnsignedBytes.MAX_VALUE;
        }

        return in;
    }

    /**
     * Decrements an InetAddress.
     */
    @Nonnull
    public static InetAddress decrement(@Nonnull InetAddress in) {
        return toInetAddress(decrement(in.getAddress()));
    }

    private static void _add(@Nonnull byte[] in, @Nonnegative long value) {
        for (int i = in.length - 1; i >= 0; i--) {
            if (value == 0)
                break;
            value += UnsignedBytes.toInt(in[i]);
            in[i] = (byte) (value & 0xFF);
            value >>>= Byte.SIZE;
        }
        // Preconditions.checkArgument(value == 0, "Carry overflow after arithmetic.");
    }

    /** Big-endian. Treats value as unsigned. */
    @Nonnull
    public static byte[] add(@Nonnull byte[] in, @Nonnegative long value) {
        Preconditions.checkArgument(Long.SIZE - Long.numberOfLeadingZeros(value) <= in.length * Byte.SIZE,
                "Illegal addend %s for array of length %s", value, in.length);
        _add(in, value);
        return in;
    }

    /** Treats value as unsigned. */
    @Nonnull
    public static InetAddress add(@Nonnull InetAddress in, @Nonnegative long value) {
        return toInetAddress(add(in.getAddress(), value));
    }

    /** Big-endian. */
    @Nonnull
    public static byte[] add(@Nonnull byte[] in, @Nonnull byte[] value) {
        Preconditions.checkArgument(in.length == value.length,
                "Illegal addend of length %s for array of length %s", value.length, in.length);
        // return new BigInteger(in).add(new BigInteger(Longs.toByteArray(value))).toByteArray();
        int carry = 0;
        for (int i = in.length - 1; i >= 0; i--) {
            int sum = UnsignedBytes.toInt(in[i]) + UnsignedBytes.toInt(value[i]) + carry;
            in[i] = (byte) (sum & 0xFF);
            carry = sum >> Byte.SIZE;
        }

        // Preconditions.checkArgument(carry == 0, "Carry overflow after addition.");
        return in;
    }

    /** Big-endian. */
    @Nonnull
    public static byte[] subtract(@Nonnull byte[] in, @Nonnegative long value) {
        Preconditions.checkArgument(Long.SIZE - Long.numberOfLeadingZeros(value) <= in.length * Byte.SIZE,
                "Illegal subtrahend %s for array of length %s", value, in.length);
        _add(in, -value);
        return in;
    }

    @Nonnull
    public static InetAddress subtract(@Nonnull InetAddress in, @Nonnegative long value) {
        return toInetAddress(subtract(in.getAddress(), value));
    }

    /** Big-endian. */
    @Nonnull
    public static byte[] subtract(@Nonnull byte[] in, @Nonnull byte[] value) {
        Preconditions.checkArgument(in.length == value.length,
                "Illegal subtrahend of length %s for array of length %s", value.length, in.length);

        int carry = 0;
        for (int i = in.length - 1; i >= 0; i--) {
            int sum = UnsignedBytes.toInt(in[i]) - UnsignedBytes.toInt(value[i]) + carry;
            in[i] = (byte) (sum & 0xFF);
            carry = sum >> Byte.SIZE;
        }

        // Preconditions.checkArgument(carry == 0, "Carry overflow after subtraction.");
        return in;
    }

    /**
     * Determines whether the given address is null or
     * "0.0.0.0" (or the IPv6 equivalent).
     */
    public static boolean isZeroAddress(@CheckForNull byte[] address) {
        if (address == null)
            return true;
        for (byte b : address)
            if (b != 0)
                return false;
        return true;
    }

    /**
     * Determines whether the given address is null or
     * "0.0.0.0" (or the IPv6 equivalent).
     */
    public static boolean isZeroAddress(@CheckForNull InetAddress address) {
        if (address == null)
            return true;
        return isZeroAddress(address.getAddress());
    }

    /**
     * Determines whether the InetAddress contained within the given address is null or
     * "0.0.0.0" (or the IPv6 equivalent).
     */
    public static boolean isZeroAddress(@CheckForNull InetSocketAddress address) {
        if (address == null)
            return true;
        return isZeroAddress(address.getAddress());
    }

    /**
     * Determines whether the given address is null or
     * 00:00:00:00:00:00.
     */
    public static boolean isZeroAddress(@CheckForNull HardwareAddress address) {
        if (address == null)
            return true;
        return isZeroAddress(address.getAddress());
    }

    /**
     * Determines whether the given address is a "useful" unicast address.
     *
     * Site local is allowed. Loopback is not.
     */
    public static boolean isUnicastAddress(@CheckForNull InetAddress address) {
        return (address != null)
                && !address.isAnyLocalAddress()
                && !address.isLoopbackAddress()
                && !address.isMulticastAddress();
    }

    /**
     * Constructs an InetAddress from the given byte array.
     * This is equivalent to {@link InetAddress#getByAddress(byte[])}
     * but throws only unchecked exceptions.
     *
     * @throws RuntimeException if the underlying routine throws {@link UnknownHostException}.
     */
    @Nonnull
    public static InetAddress toInetAddress(@Nonnull byte[] data) {
        try {
            return InetAddress.getByAddress(data);
        } catch (UnknownHostException e) {
            throw Throwables.propagate(e);
        }
    }

    private static int toByteMask(@Nonnegative int netmask) {
        // If netmask%8 is the number of bits to keep, then
        // 8 - netmask%8 is the number of bits to remove.
        // The & 0xff on the end is informational.
        // Compute the number of bits which ought to be removed.
        return (-1 << (Byte.SIZE - (netmask % Byte.SIZE))) & 0xff;
    }

    /**
     * Zeros out all but the first <code>netmask</code> bits of
     * <code>in</code>.
     * This routine modifies its argument and returns it.
     */
    @Nonnull
    public static byte[] toNetworkAddress(@Nonnull byte[] in, @Nonnegative int netmask) {
        int idx = netmask / Byte.SIZE;
        if (idx < in.length) {
            int mask = toByteMask(netmask);
            in[idx] &= mask;
            Arrays.fill(in, idx + 1, in.length, (byte) 0);
        }
        return in;
    }

    @Nonnull
    public static InetAddress toNetworkAddress(@Nonnull InetAddress in, @Nonnegative int netmask) {
        return toInetAddress(toNetworkAddress(in.getAddress(), netmask));
    }

    @Nonnull
    public static byte[] toBroadcastAddress(@Nonnull byte[] in, @Nonnegative int netmask) {
        int idx = netmask / Byte.SIZE;
        if (idx < in.length) {
            int mask = toByteMask(netmask);
            in[idx] &= mask;
            in[idx] |= ~mask;
            Arrays.fill(in, idx + 1, in.length, UnsignedBytes.MAX_VALUE);
        }
        return in;
    }

    @Nonnull
    public static InetAddress toBroadcastAddress(@Nonnull InetAddress in, @Nonnegative int netmask) {
        return toInetAddress(toBroadcastAddress(in.getAddress(), netmask));
    }

    /**
     * Constructs an address suitable for use as a mask.
     *
     * The return value is a big-endian byte array of the specified length.
     * The high <code>netmask</code> bits will be 1 and the remaining low bits will be 0.
     * For example, the address may be of the form 255.255.240.0 or ff:ff:fc:....
     */
    @Nonnull
    public static byte[] toNetworkMask(@Nonnegative int addressLength, @Nonnegative int netmask) {
        byte[] out = new byte[addressLength];
        int idx = netmask / Byte.SIZE;
        if (idx < addressLength) {
            Arrays.fill(out, 0, idx, UnsignedBytes.MAX_VALUE);
            out[idx] = (byte) toByteMask(netmask);
        } else {
            Arrays.fill(out, UnsignedBytes.MAX_VALUE);
        }
        return out;
    }

    @Nonnull
    public static InetAddress toNetworkMaskAddress(@Nonnegative int addressLength, @Nonnegative int netmask) {
        return toInetAddress(toNetworkMask(addressLength, netmask));
    }

    /**
     * Constructs a companion netmask address to the given InetAddress.
     *
     * @see #toNetworkMask(int, int)
     */
    @Nonnull
    public static InetAddress toNetworkMaskAddress(@Nonnull InetAddress in, @Nonnegative int netmask) {
        return toNetworkMaskAddress(in.getAddress().length, netmask);
    }

    /**
     * Converts an address of the form 255.255.240.0 into an CIDR netmask.
     */
    @Nonnegative
    public static int toNetmask(@Nonnull byte[] netmaskAddress) {
        for (int i = netmaskAddress.length - 1; i >= 0; i--) {
            // Find the last nonzero byte.
            if (netmaskAddress[i] == 0)
                continue;
            // We have a nonzero byte.
            int byteValue = UnsignedBytes.toInt(netmaskAddress[i]);
            int ntz = Integer.numberOfTrailingZeros(byteValue);
            return (i + 1) * Byte.SIZE - ntz;
        }
        return 0;
    }

    /**
     * Converts an address of the form 255.255.240.0 into an CIDR netmask.
     */
    @Nonnegative
    public static int toNetmask(@Nonnull InetAddress netmaskAddress) {
        return toNetmask(netmaskAddress.getAddress());
    }

    public static long toLong(@Nonnull byte[] data) {
        long out = 0;
        for (byte b : data)
            out = (out << Byte.SIZE) + UnsignedBytes.toInt(b);
        return out;
    }

    /**
     * Returns whether a given target address is hypothetically reachable from the given AbstractMaskedAddress.
     */
    public static boolean isLocal(@Nonnull AbstractMaskedAddress self, @Nonnull InetAddress target) {
        Preconditions.checkNotNull(self, "Self (AbstractMaskedAddress) was null.");
        Preconditions.checkNotNull(target, "Target (InetAddress) was null.");
        if (!self.getAddress().getClass().equals(target.getClass()))
            return false;
        byte[] selfBytes = toNetworkAddress(self.getAddress().getAddress(), self.getNetmask());
        byte[] targetBytes = toNetworkAddress(target.getAddress(), self.getNetmask());
        return Arrays.equals(selfBytes, targetBytes);
    }

    /** Like {@link InetAddresses#toAddrString(InetAddress)} but accepts nulls. */
    @CheckForNull
    public static String toAddrString(@CheckForNull InetAddress address) {
        if (address == null)
            return null;
        return InetAddresses.toAddrString(address);
    }

    private AddressUtils() {
    }
}
