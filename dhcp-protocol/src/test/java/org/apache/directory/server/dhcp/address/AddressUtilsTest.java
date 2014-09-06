/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.address;

import org.anarres.dhcp.common.address.AddressUtils;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.SignedBytes;
import com.google.common.primitives.UnsignedBytes;
import java.util.Arrays;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Byte array operations compatible with Cassandra.
 * 
 * Cassandra uses FastByteComparisons which is an adaptation of {@link UnsignedBytes}.
 * Therefore, this treats bytes as unsigned.
 *
 * @author shevek
 */
public class AddressUtilsTest {

    private static final Log LOG = LogFactory.getLog(AddressUtilsTest.class);

    @Nonnull
    public static byte[] A(int... c) {
        byte[] buf = new byte[c.length];
        for (int i = 0; i < c.length; i++)
            buf[i] = (byte) c[i];
        return buf;
    }

    @Nonnull
    public static String S(byte[] s) {
        return "[" + UnsignedBytes.join(" ", s) + "]";
    }

    public static byte[] C(byte[] c) {
        return Arrays.copyOf(c, c.length);
    }

    public void test(byte[] expect, byte[] actual) {
        assertEquals("Arrays are not same length", expect.length, actual.length);
        for (int i = 0; i < expect.length; i++) {
            if (expect[i] != actual[i]) {
                fail("Mismatch at " + i + ": Expected " + (int) expect[i] + " but got " + (int) actual[i]);
            }
        }
    }

    private void testIncrementNumeric(byte[] expect, byte[] input) {
        LOG.info(S(input) + " + 1 =? " + S(expect));
        byte[] output = AddressUtils.increment(C(input));
        LOG.info(S(input) + " + 1 == " + S(output));
        assertArrayEquals(expect, output);
    }

    @Test
    public void testIncrementNumeric() {
        LOG.info("IncrementNumeric");
        testIncrementNumeric(A(1), A(0));
        testIncrementNumeric(A(2), A(1));
        testIncrementNumeric(A(UnsignedBytes.MAX_VALUE), A(UnsignedBytes.MAX_VALUE - 1));
        testIncrementNumeric(A(1, 0, 4), A(1, 0, 3));
        // Wraps, rather than extending with the 1.
        testIncrementNumeric(A(0), A(UnsignedBytes.MAX_VALUE));
        testIncrementNumeric(A(0, 0), A(UnsignedBytes.MAX_VALUE, UnsignedBytes.MAX_VALUE));
        testIncrementNumeric(A(UnsignedBytes.MAX_VALUE, 4, 0), A(UnsignedBytes.MAX_VALUE, 3, UnsignedBytes.MAX_VALUE));
    }

    private void testDecrementNumeric(byte[] expect, byte[] input) {
        LOG.info(S(input) + " - 1 =? " + S(expect) + " (expected)");
        byte[] output = AddressUtils.decrement(C(input));
        LOG.info(S(input) + " - 1 == " + S(output) + " (actual)");
        assertArrayEquals(expect, output);
    }

    @Test
    public void testDecrementNumeric() {
        LOG.info("DecrementNumeric");
        testDecrementNumeric(A(0), A(1));
        testDecrementNumeric(A(1), A(2));
        testDecrementNumeric(A(UnsignedBytes.MAX_VALUE - 1), A(UnsignedBytes.MAX_VALUE));
        testDecrementNumeric(A(0, 2), A(0, 3));
        testDecrementNumeric(A(2, 1), A(2, 2));
        testDecrementNumeric(A(1, UnsignedBytes.MAX_VALUE), A(2, 0));
        testDecrementNumeric(A(1, UnsignedBytes.MAX_VALUE, UnsignedBytes.MAX_VALUE), A(2, 0, 0));
    }

    @Test
    public void testComparison() {
        byte[] b = new byte[]{0};
        for (int i = 0; i < 254; i++) {
            byte[] c = AddressUtils.increment(C(b));
            LOG.info("Compare " + S(b) + " to " + S(c));
            assertTrue(UnsignedBytes.lexicographicalComparator().compare(b, c) < 0);
            b = c;
        }
    }

    @Test
    public void testAdd() {
        assertArrayEquals(A(1, 2), AddressUtils.add(A(0, 4), 0xFEL));
        assertArrayEquals(A(0, 0), AddressUtils.add(A(UnsignedBytes.MAX_VALUE, UnsignedBytes.MAX_VALUE), 0x1L));
    }

    private void testToNetworkAddress(@Nonnull byte[] expect, @Nonnull byte[] in, @Nonnegative int mask) {
        byte[] out = AddressUtils.toNetworkAddress(C(in), mask);
        LOG.info(Arrays.toString(in) + "/" + mask + " -> " + Arrays.toString(out)
                + " (expected " + Arrays.toString(expect) + ")");
        assertArrayEquals(expect, out);
    }

    @Test
    public void testToNetworkAddress() {
        testToNetworkAddress(A(0, 0, 0, 0), A(1, 2, 3, 4), 1);
        testToNetworkAddress(A(1, 0, 0, 0), A(1, 2, 3, 4), 8);
        testToNetworkAddress(A(1, 2, 0, 0), A(1, 2, 3, 4), 16);
        testToNetworkAddress(A(1, 2, 2, 0), A(1, 2, 3, 4), 23);
        testToNetworkAddress(A(1, 2, 3, 0), A(1, 2, 3, 4), 24);
        testToNetworkAddress(A(1, 2, 3, 4), A(1, 2, 3, 4), 31);
        testToNetworkAddress(A(1, 2, 3, 4), A(1, 2, 3, 4), 32);

        // JLS 15.19 says you can't shift left by 64 to get 0. Fuck.
        testToNetworkAddress(Longs.toByteArray(0L), Longs.toByteArray(-1L), 0);
        for (int i = 1; i <= 64; i++) {
            long value = -1L << (Long.SIZE - i);
            testToNetworkAddress(Longs.toByteArray(value), Longs.toByteArray(-1L), i);
        }
    }

    public void testToBroadcastAddress(@Nonnull byte[] expect, @Nonnull byte[] in, @Nonnegative int mask) {
        byte[] out = AddressUtils.toBroadcastAddress(C(in), mask);
        LOG.info(Arrays.toString(in) + "/" + mask + " -> " + Arrays.toString(out)
                + " (expected " + Arrays.toString(expect) + ")");
        assertArrayEquals(expect, out);
    }

    @Test
    public void testToBroadcastAddress() {
        testToBroadcastAddress(A(127, 255, 255, 255), A(1, 2, 3, 4), 1);
        testToBroadcastAddress(A(1, 255, 255, 255), A(1, 2, 3, 4), 8);
        testToBroadcastAddress(A(1, 2, 255, 255), A(1, 2, 3, 4), 16);
        testToBroadcastAddress(A(1, 2, 3, 255), A(1, 2, 3, 4), 23);
        testToBroadcastAddress(A(1, 2, 3, 255), A(1, 2, 3, 4), 24);
        testToBroadcastAddress(A(1, 2, 3, 5), A(1, 2, 3, 4), 31);
        testToBroadcastAddress(A(1, 2, 3, 4), A(1, 2, 3, 4), 32);

        for (int i = 0; i < 64; i++) {
            long value = -1L >>> i;
            testToBroadcastAddress(Longs.toByteArray(value), Longs.toByteArray(0L), i);
        }
        // JLS 15.19 says you can't shift left by 64 to get 0. Fuck.
        testToBroadcastAddress(Longs.toByteArray(0L), Longs.toByteArray(0L), 64);
    }

    public void testToNetworkMask(@Nonnull byte[] expect, @Nonnegative int mask) {
        LOG.info(mask + " =? " + S(expect));
        byte[] out = AddressUtils.toNetworkMask(4, mask);
        LOG.info(mask + " == " + S(out));
        assertArrayEquals(expect, out);
    }

    @Test
    public void testToNetworkMask() {
        long i32 = 0xFFffFFffL;
        for (int i = 0; i < 32; i++) {
            long value = (i32 << (32 - i)) & i32;
            testToNetworkMask(Ints.toByteArray((int) value), i);
        }
    }
}