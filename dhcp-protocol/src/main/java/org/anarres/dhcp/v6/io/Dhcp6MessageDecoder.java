/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.io;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.Dhcp6Exception;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.messages.Dhcp6MessageType;
import org.anarres.dhcp.v6.messages.Dhcp6RelayMessage;
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

    private static final Logger LOG = LoggerFactory.getLogger(Dhcp6MessageDecoder.class);

    private static class Inner {
        private static final Dhcp6MessageDecoder INSTANCE = new Dhcp6MessageDecoder(Dhcp6OptionsRegistry.getInstance());
    }

    @Nonnull
    public static Dhcp6MessageDecoder getInstance() {
        return Inner.INSTANCE;
    }

    private final Dhcp6OptionsRegistry registry;

    public Dhcp6MessageDecoder(final Dhcp6OptionsRegistry registry) {
        this.registry = registry;
    }

    // TODO make this extensible, right now we fail for every undefined DHCP message
    // TODO extract option decoder and use just the decoder for VendorSpecificInformationOption like options

    /**
     * https://tools.ietf.org/html/rfc3315#section-6
     * https://tools.ietf.org/html/rfc3315#section-7
     */
    public Dhcp6Message decode(@Nonnull ByteBuffer buffer) throws Dhcp6Exception, IOException {
        LOG.debug("Decoding Dhcp6 message: {}", buffer);

        final Dhcp6MessageType msgType = getMsgType(buffer);
        LOG.debug("Message type: {}", msgType);

        Dhcp6Message message;
        if(isRelayedMessage(msgType)) {
            LOG.debug("Message relay");

            final Dhcp6RelayMessage relayMsg = new Dhcp6RelayMessage();
            message = relayMsg;

            relayMsg.setHopCount(getByte(buffer));
            LOG.debug("Hop count: {}", message.getTransactionId());

            relayMsg.setLinkAddress(getInetAddress(buffer, (byte) 16));
            LOG.debug("Link address: {}", message.getTransactionId());

            relayMsg.setPeerAddress(getInetAddress(buffer, (byte) 16));
            LOG.debug("Peer address: {}", message.getTransactionId());
        } else {
            message = new Dhcp6Message();
            message.setMessageType(msgType);

            message.setTransactionId(getTxId(buffer));
            LOG.debug("Transaction ID: {}", message.getTransactionId());

            Dhcp6Options options = decodeOptions(buffer);
            message.setOptions(options);
        }

        LOG.debug("Dhcp6 message decoded: {}", message);
        message.setMessageType(msgType);
        return message;

    }

    private InetAddress getInetAddress(final ByteBuffer bufferm, final byte size) throws Dhcp6Exception {
        final byte[] ipBytes = new byte[size];
        bufferm.get(ipBytes, bufferm.position(), size);
        try {
            return InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            throw new Dhcp6Exception("Unable to parse inet address from: " + BaseEncoding.base16().encode(ipBytes), e);
        }
    }

    static boolean isRelayedMessage(final Dhcp6MessageType type) {
        return type == Dhcp6MessageType.DHCP_RELAY_FORW || type == Dhcp6MessageType.DHCP_RELAY_REPL;
    }

    /**
     * https://tools.ietf.org/html/rfc3315#section-15.1
     */
    private static int getTxId(final ByteBuffer buffer) {
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
        final byte type = getByte(buffer);
        final Dhcp6MessageType dhcp6MessageType = Dhcp6MessageType.forTypeCode(type);
        Dhcp6Exception.UnknownMsgException.check(dhcp6MessageType, type);
        return dhcp6MessageType;
    }

    private static byte getByte(final ByteBuffer buffer) {
        return buffer.get();
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
            LOG.trace("Option: {}", option);
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
