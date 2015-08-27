/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.io;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.Dhcp6OptionsRegistry;
import org.anarres.dhcp.v6.options.UnrecognizedOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek, marosmars
 */
public class Dhcp6MessageDecoder {

    private static class Inner {

        private static final Dhcp6MessageDecoder INSTANCE = new Dhcp6MessageDecoder();
    }

    @Nonnull
    public static Dhcp6MessageDecoder getInstance() {
        return Inner.INSTANCE;
    }

    private Dhcp6MessageDecoder() {}

    private static final Logger LOG = LoggerFactory.getLogger(Dhcp6MessageDecoder.class);

    private final Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();

    /**
     * https://tools.ietf.org/html/rfc3315#section-6
     */
    public Dhcp6Message decode(@Nonnull ByteBuffer buffer) throws Dhcp6Exception, IOException {
        LOG.debug("Decoding Dhcp6 message: {}", buffer);

        Dhcp6Message message = new Dhcp6Message();

        message.setMessageType(getMsgType(buffer));
        LOG.debug("Message type: {}", message.getMessageType());

        message.setTransactionId(getTxId(buffer));
        LOG.debug("Transaction ID: {}", message.getTransactionId());

        Dhcp6Options options = decodeOptions(buffer);
        message.setOptions(options);

        LOG.debug("Dhcp6 message decoded: {}", message);
        return message;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-15.1
     */
    private static int getTxId(final ByteBuffer buffer) {
        Preconditions.checkArgument(buffer.remaining() > 3);
        final byte[] txBytes = new byte[4];
        buffer.get(txBytes, 1, 3);
        txBytes[0] = 0;
        return Ints.fromByteArray(txBytes);
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-5.3
     */
    @Nonnull
    private static Dhcp6MessageType getMsgType(final ByteBuffer buffer) throws Dhcp6Exception.UnknownMsgException {
        Preconditions.checkArgument(buffer.remaining() > 1);
        final byte type = buffer.get();
        final Dhcp6MessageType dhcp6MessageType = Dhcp6MessageType.forTypeCode(type);
        Dhcp6Exception.UnknownMsgException.check(dhcp6MessageType, type);
        return dhcp6MessageType;
    }

    @Nonnull
    private static byte[] decodeBytes(@Nonnull ByteBuffer buffer, @Nonnegative int len) {
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-5.3
     */
    @Nonnull
    public Dhcp6Options decodeOptions(@Nonnull ByteBuffer message) throws Dhcp6Exception {
        Dhcp6Options options = new Dhcp6Options();

        while (message.hasRemaining()) {
            short tag = message.getShort();
            LOG.trace("Option type: {}", tag);
            short length = message.getShort();
            LOG.trace("Option len: {}", length);
            byte[] value = decodeBytes(message, length);
            final Dhcp6Option option = newOptionInstance(tag, value);
            LOG.debug("Option: {}", option);
            options.add(option);
        }

        return options;
    }

    @Nonnull
    private Dhcp6Option newOptionInstance(@Nonnegative short tag, @Nonnull byte[] value) throws Dhcp6Exception {
        Class<? extends Dhcp6Option> type = registry.getOptionType(tag);
        Dhcp6Option option = (type != null) ? Dhcp6OptionsRegistry.newInstance(type) : new UnrecognizedOption(tag);
        option.setData(value);
        return option;
    }
}
