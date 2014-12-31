/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.Dhcp6Options;
import org.anarres.dhcp.v6.options.Dhcp6OptionsRegistry;
import org.anarres.dhcp.v6.options.UnrecognizedOption;
import org.apache.directory.server.dhcp.DhcpException;

/**
 *
 * @author shevek
 */
public class Dhcp6MessageDecoder {

    private final Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();

    public Dhcp6Message decode(@Nonnull ByteBuffer buffer) throws DhcpException, IOException {
        Dhcp6Message message = new Dhcp6Message();

        Dhcp6Options options = decodeOptions(buffer);
        message.setOptions(options);

        return message;
    }

    @Nonnull
    private static byte[] decodeBytes(@Nonnull ByteBuffer buffer, @Nonnegative int len) {
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }

    @Nonnull
    public Dhcp6Options decodeOptions(@Nonnull ByteBuffer message) throws DhcpException {
        Dhcp6Options options = new Dhcp6Options();

        while (message.hasRemaining()) {
            short tag = message.getShort();
            short length = message.getShort();
            byte[] value = decodeBytes(message, length);
            options.add(newOptionInstance(tag, value));
        }

        return options;
    }

    @Nonnull
    private Dhcp6Option newOptionInstance(@Nonnegative short tag, @Nonnull byte[] value) throws DhcpException {
        Class<? extends Dhcp6Option> type = registry.getOptionType(tag);
        Dhcp6Option option = (type != null) ? Dhcp6OptionsRegistry.newInstance(type) : new UnrecognizedOption(tag);
        option.setData(value);
        return option;
    }
}
