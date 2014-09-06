/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.messages.DhcpMessage;
import org.anarres.dhcp.v6.options.Dhcp6Option;
import org.anarres.dhcp.v6.options.DhcpOptions;
import org.anarres.dhcp.v6.options.DhcpOptionsRegistry;
import org.anarres.dhcp.v6.options.UnrecognizedOption;
import org.apache.directory.server.dhcp.DhcpException;

/**
 *
 * @author shevek
 */
public class DhcpMessageDecoder {

    private final DhcpOptionsRegistry registry = DhcpOptionsRegistry.getInstance();

    public DhcpMessage decode(@Nonnull ByteBuffer buffer) throws DhcpException, IOException {
        DhcpMessage message = new DhcpMessage();

        DhcpOptions options = decodeOptions(buffer);
        message.setOptions(options);

        return message;
    }

    /**
     * @param buffer
     * @param len
     * @return
     */
    @Nonnull
    private static byte[] decodeBytes(@Nonnull ByteBuffer buffer, @Nonnegative int len) {
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }

    @Nonnull
    private DhcpOptions decodeOptions(@Nonnull ByteBuffer message) throws DhcpException {
        DhcpOptions options = new DhcpOptions();

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
        Dhcp6Option option = (type != null) ? DhcpOptionsRegistry.newInstance(type) : new UnrecognizedOption(tag);
        option.setData(value);
        return option;
    }
}
