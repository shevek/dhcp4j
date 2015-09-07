package org.anarres.dhcp.v6.options;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.7
 */
public class OptionRequestOption extends Dhcp6Option {

    private static final short TAG = 6;

    @Override
    public short getTag() {
        return TAG;
    }

    public Iterable<Short> getRequestedOptions() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        List<Short> options = Lists.newArrayListWithCapacity(getData().length / 2);

        while(buf.remaining()!=0) {
            options.add(buf.getShort());
        }

        return options;
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        try {
            values.append("requestedOptions:[");
            values.append(Iterables.toString(getRequestedOptions()));
            values.append("]");
        } catch (RuntimeException e) {
            values.append("requestedOptions:[");
            values.append(toStringDataFallback(getData()));
            values.append("]");
        }

        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }
}
