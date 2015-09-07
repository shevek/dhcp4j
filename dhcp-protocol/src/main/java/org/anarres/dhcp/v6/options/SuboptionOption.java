package org.anarres.dhcp.v6.options;

import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;

/**
 * Base class for options that can posses suboptions e.g. IaNa option
 */
public abstract class SuboptionOption extends Dhcp6Option {

    @Nonnull
    public Dhcp6Options getOptions() throws Dhcp6Exception {
        Dhcp6MessageDecoder decoder = Dhcp6MessageDecoder.getInstance();
        return decoder.decodeOptions(getOptionsRaw());
    }

    public void setOptions(@Nonnull final ByteBuffer options) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.position(getHeaderSize());
        buf.put(options);
    }

    protected abstract int getHeaderSize();

    public void setOptions(@Nonnull final Dhcp6Options options) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.position(getHeaderSize());
        Dhcp6MessageEncoder.getInstance().encode(buf, options);
    }

    @Nonnull protected ByteBuffer getOptionsRaw() {
        byte[] data = getData();
        return ByteBuffer.wrap(data, getHeaderSize(), data.length - getHeaderSize());
    }
}
