package org.anarres.dhcp.v6.options;

import java.nio.ByteBuffer;

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
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getShort(0);
    }
}
