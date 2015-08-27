package org.anarres.dhcp.v6.options;

import com.google.common.collect.Lists;
import java.nio.ByteBuffer;
import java.util.List;

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
}
