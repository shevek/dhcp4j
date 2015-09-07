/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.io;

import com.google.common.primitives.Shorts;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6RelayMessage;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.Dhcp6Options;

/**
 * http://tools.ietf.org/html/rfc3315#section-22.1
 *
 * @author shevek, marosmars
 */
public class Dhcp6MessageEncoder {

    private static class Inner {

        private static final Dhcp6MessageEncoder INSTANCE = new Dhcp6MessageEncoder();
    }

    @Nonnull
    public static Dhcp6MessageEncoder getInstance() {
        return Inner.INSTANCE;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-6
     * https://tools.ietf.org/html/rfc3315#section-7
     */
    public void encode(ByteBuffer byteBuffer, Dhcp6Message message) {
        byteBuffer.put(message.getMessageType().getCode());
        if(Dhcp6MessageDecoder.isRelayedMessage(message.getMessageType())) {
            final Dhcp6RelayMessage relayedMessage = (Dhcp6RelayMessage) message;
            byteBuffer.put(relayedMessage.getHopCount());
            byteBuffer.put(relayedMessage.getLinkAddress().getAddress());
            byteBuffer.put(relayedMessage.getPeerAddress().getAddress());
        } else {
            int transactionId = message.getTransactionId();
            byteBuffer.put((byte) ((transactionId >> 16) & 0xFF));
            byteBuffer.put((byte) ((transactionId >> 8) & 0xFF));
            byteBuffer.put((byte) (transactionId & 0xFF));
        }
        encode(byteBuffer, message.getOptions());
    }

    public void encode(@Nonnull ByteBuffer byteBuffer, @Nonnull Dhcp6Options options) {
        for (Dhcp6Option option : options) {
            // Option continuation per RFC3396
            encode(byteBuffer, option);
        }
    }

    public ByteBuffer encode(@Nonnull Dhcp6Options options) {
        ByteBuffer allOptions = ByteBuffer.allocate(0);

        for (Dhcp6Option option : options) {
            // Option continuation per RFC3396
            final ByteBuffer allocate = ByteBuffer.allocate(option.getData().length + 4);
            encode(allocate, option);
            allocate.flip();
            allOptions.flip();
            allOptions = ByteBuffer.allocate(allOptions.limit() + allocate.limit()).put(allOptions).put(allocate);
        }

        return allOptions;
    }

    public void encode(final @Nonnull ByteBuffer byteBuffer, @Nonnull final Dhcp6Option option) {
        short tag = option.getTag();
        byte[] data = option.getData();
        byteBuffer.putShort(tag);
        byteBuffer.putShort(Shorts.checkedCast(data.length));
        byteBuffer.put(data);
    }
}
