/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.primitives.UnsignedBytes;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * All methods are big-endian (network-endian) and modify their arguments.
 *
 * @author shevek
 */
public class AddressUtils {

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

    @Nonnull
    public static InetAddress increment(@Nonnull InetAddress in) {
        return toInetAddress(increment(in.getAddress()));
    }

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

    @Nonnull
    public static InetAddress decrement(@Nonnull InetAddress in) {
        return toInetAddress(decrement(in.getAddress()));
    }

    /** Big-endian. */
    public static byte[] add(@Nonnull byte[] in, long value) {
        Preconditions.checkArgument(Long.SIZE - Long.numberOfLeadingZeros(value) <= in.length * Byte.SIZE,
                "Illegal addend %s for array of length %s", value, in.length);

        for (int i = in.length - 1; i >= 0; i--) {
            if (value == 0)
                break;
            value += UnsignedBytes.toInt(in[i]);
            in[i] = (byte) (value & 0xFF);
            value >>>= Byte.SIZE;
        }

        return in;
    }

    /** Big-endian. */
    public static byte[] add(@Nonnull byte[] in, @Nonnull byte[] value) {
        Preconditions.checkArgument(in.length == value.length,
                "Illegal addend of length %s for array of length %s", value.length, in.length);
        // return new BigInteger(in).add(new BigInteger(Longs.toByteArray(value))).toByteArray();
        int carry = 0;
        for (int i = in.length - 1; i >= 0; i--) {
            if (i >= value.length)
                break;
            int sum = UnsignedBytes.toInt(in[i]) + UnsignedBytes.toInt(value[i]) + carry;
            in[i] = (byte) (sum & 0xFF);
            carry = sum >> Byte.SIZE;
        }

        Preconditions.checkArgument(carry == 0, "Carry overflow after addition.");

        return in;
    }

    /** Big-endian. */
    public static byte[] subtract(@Nonnull byte[] in, @Nonnull byte[] value) {
        Preconditions.checkArgument(in.length == value.length,
                "Illegal subtrahend of length %s for array of length %s", value.length, in.length);

        int carry = 0;
        for (int i = in.length - 1; i >= 0; i--) {
            int sum = UnsignedBytes.toInt(in[i]) - UnsignedBytes.toInt(value[i]) + carry;
            in[i] = (byte) (sum & 0xFF);
            carry = sum >> Byte.SIZE;
        }

        Preconditions.checkArgument(carry == 0, "Carry overflow after subtraction.");

        return in;
    }

    public static boolean isZeroAddress(@CheckForNull byte[] address) {
        if (address == null)
            return true;
        for (byte b : address)
            if (b != 0)
                return false;
        return true;
    }

    public static boolean isZeroAddress(@CheckForNull InetAddress address) {
        if (address == null)
            return true;
        return isZeroAddress(address.getAddress());
    }

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

    @Nonnull
    public static byte[] toNetworkMask(@Nonnull int addressLength, @Nonnegative int netmask) {
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

    public static long toLong(@Nonnull byte[] data) {
        long out = 0;
        for (byte b : data)
            out = (out << Byte.SIZE) + UnsignedBytes.toInt(b);
        return out;
    }
}
