/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.address;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.primitives.UnsignedBytes;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public class AddressUtils {

    public static void increment(@Nonnull byte[] in) {
        for (int i = in.length - 1; i >= 0; i--) {
            if (UnsignedBytes.toInt(in[i]) < 255) {
                in[i]++;
                break;
            }
            in[i] = 0;
        }
    }

    public static void decrement(@Nonnull byte[] in) {
        for (int i = in.length - 1; i >= 0; i--) {
            if (UnsignedBytes.toInt(in[i]) > 0) {
                in[i]--;
                break;
            }
            in[i] = (byte) 255;
        }
    }

    public static void mask(@Nonnull byte[] in, int nbits) {
        Preconditions.checkArgument(nbits >> (in.length * Byte.SIZE) == 0,
                "Illegal mask %s for array of length %s", nbits, in.length);
        for (int i = in.length - 1; i >= 0; i--) {
            if (nbits < Byte.SIZE) {
                int mask = (-1 << (Byte.SIZE - nbits));
                in[i] &= mask;
                break;
            }
            in[i] = 0;
            nbits -= Byte.SIZE;
        }
    }

    public static void add(@Nonnull byte[] in, long value) {
        Preconditions.checkArgument(Long.SIZE - Long.numberOfLeadingZeros(value) <= in.length * Byte.SIZE,
                "Illegal addend %s for array of length %s", value, in.length);
        // return new BigInteger(in).add(new BigInteger(Longs.toByteArray(value))).toByteArray();
        for (int i = in.length - 1; i >= 0; i--) {
            if (value == 0)
                break;
            value += UnsignedBytes.toInt(in[i]);
            in[i] = (byte) (value & 0xFF);
            value >>>= Byte.SIZE;
        }
    }

    public static void add(@Nonnull byte[] in, @Nonnull byte[] value) {
        Preconditions.checkArgument(in.length >= value.length,
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
    }

    public static long subtract(@Nonnull byte[] left, @Nonnull byte[] right) {
        Preconditions.checkArgument(left.length >= right.length,
                "Illegal subtrahend of length %s for array of length %s", left.length, right.length);
        // TODO: Convert to local byte arithmetic.
        BigInteger result = new BigInteger(left).subtract(new BigInteger(right));
        Preconditions.checkState(result.bitCount() < Long.SIZE,
                "Overflow in conversion of result to long.");
        return result.longValue();
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
    public static InetAddress toAddress(@Nonnull byte[] data) {
        try {
            return InetAddress.getByAddress(data);
        } catch (UnknownHostException e) {
            throw Throwables.propagate(e);
        }
    }
}
