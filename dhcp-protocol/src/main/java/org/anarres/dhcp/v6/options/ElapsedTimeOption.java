package org.anarres.dhcp.v6.options;

import com.google.common.primitives.Shorts;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.9
 *
 * @author marosmars
 */
public class ElapsedTimeOption extends Dhcp6Option {

    private static final short TAG = 8;

    @Override
    public short getTag() {
        return TAG;
    }

    public short getElapsedTime() {
        return Shorts.fromByteArray(getData());
    }
}
