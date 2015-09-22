package org.anarres.dhcp.v6.options;

import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;
import org.anarres.dhcp.v6.messages.Dhcp6Message;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.10
 */
public class RelayMessageOption extends Dhcp6Option {
    private static final short TAG = 9;

    @Override public short getTag() {
        return TAG;
    }

    @Nonnull
    public Dhcp6Message getRelayedMessage() throws Dhcp6Exception {
        try {
            return Dhcp6MessageDecoder.getInstance().decode(ByteBuffer.wrap(getData()));
        } catch (IOException e) {
            throw new Dhcp6Exception("Unable to decode relayed dhcpv6 message: " + BaseEncoding.base16().encode(getData()), e);
        }
    }

    public void setRelayedMessage(@Nonnull final Dhcp6Message message) {
        Dhcp6MessageEncoder.getInstance().encode(ByteBuffer.wrap(getData()), message);
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        try {
            values.append("requestedOptions:[");
            values.append(getRelayedMessage().toString());
            values.append("]");
        } catch (Dhcp6Exception e) {
            values.append("requestedOptions:[");
            values.append(toStringDataFallback(getData()));
            values.append("]");
        }

        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }

    @Nonnull
    public static RelayMessageOption create(@Nonnull final Dhcp6Message relayedMessage) {
        final byte[] data = new byte[relayedMessage.getLength()];
        final RelayMessageOption relayMessageOption = new RelayMessageOption();
        relayMessageOption.setData(data);
        relayMessageOption.setRelayedMessage(relayedMessage);
        return relayMessageOption;
    }

}
