/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.io;

import com.google.common.primitives.Shorts;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.messages.DhcpMessage;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.DhcpOptions;

/**
 *
 * @author shevek
 */
public class DhcpMessageEncoder {

    private static class Inner {

        private static final DhcpMessageEncoder INSTANCE = new DhcpMessageEncoder();
    }

    @Nonnull
    public static DhcpMessageEncoder getInstance() {
        return Inner.INSTANCE;
    }

    public void encode(ByteBuffer byteBuffer, DhcpMessage message) {
        byteBuffer.put(message.getMessageType().getCode());
        int transactionId = message.getTransactionId();
        byteBuffer.put((byte) ((transactionId >> 16) & 0xFF));
        byteBuffer.put((byte) ((transactionId >> 8) & 0xFF));
        byteBuffer.put((byte) (transactionId & 0xFF));
        encode(byteBuffer, message.getOptions());
    }

    public void encode(@Nonnull ByteBuffer byteBuffer, @Nonnull DhcpOptions options) {
        for (Dhcp6Option option : options) {
            // Option continuation per RFC3396
            short tag = option.getTag();
            byte[] data = option.getData();
            byteBuffer.putShort(tag);
            byteBuffer.putShort(Shorts.checkedCast(data.length));
            byteBuffer.put(data);
        }
    }
}
