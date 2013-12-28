/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 * 
 */
package org.apache.directory.server.dhcp.io;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.DhcpOptionsRegistry;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.options.dhcp.DhcpMessageType;
import org.apache.directory.server.dhcp.options.dhcp.UnrecognizedOption;

/**
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DhcpMessageDecoder {

    private final DhcpOptionsRegistry registry = DhcpOptionsRegistry.getInstance();

    /**
     * Convert a byte buffer into a DhcpMessage.
     * 
     * @return a DhcpMessage.
     * @param buffer ByteBuffer to convert to a DhcpMessage object
     * @throws DhcpException 
     */
    @Nonnull
    public DhcpMessage decode(@Nonnull ByteBuffer buffer) throws DhcpException, IOException {
        byte op = buffer.get();

        short htype = (short) (buffer.get() & 0xff);
        short hlen = (short) (buffer.get() & 0xff);
        short hops = (short) (buffer.get() & 0xff);
        int xid = buffer.getInt();
        int secs = buffer.getShort() & 0xffff;
        short flags = buffer.getShort();

        InetAddress ciaddr = decodeAddress(buffer);
        InetAddress yiaddr = decodeAddress(buffer);
        InetAddress siaddr = decodeAddress(buffer);
        InetAddress giaddr = decodeAddress(buffer);

        byte[] chaddr = decodeBytes(buffer, 16);

        String sname = decodeString(buffer, 64);
        String file = decodeString(buffer, 128);

        OptionsField options = decodeOptions(buffer);

        // message type option: may be null if option isn't set (BOOTP)
        DhcpMessageType mto = options.get(DhcpMessageType.class);

        return new DhcpMessage(null != mto ? mto.getMessageType(): null, op, new HardwareAddress(htype, hlen, chaddr),
                hops, xid, secs, flags, ciaddr, yiaddr, siaddr, giaddr, sname, file, options);
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

    /**
     * @param buffer
     * @return
     */
    @Nonnull
    private static String decodeString(@Nonnull ByteBuffer buffer, @Nonnegative int len)
            throws IOException {
        byte[] bytes = decodeBytes(buffer, len);
        // find zero-terminator
        int slen = Bytes.indexOf(bytes, (byte) 0);
        if (slen < 0)
            slen = bytes.length;
        return new String(bytes, 0, slen, Charsets.ISO_8859_1);
    }

    /**
     * Read a 4-byte inet address from the buffer.
     * 
     * @param buffer
     * @return
     * @throws UnknownHostException
     */
    @CheckForNull
    private static InetAddress decodeAddress(@Nonnull ByteBuffer buffer) {
        try {
            byte[] bytes = decodeBytes(buffer, 4);
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // should not happen
            return null;
        }
    }

    private static final byte[] VENDOR_MAGIC_COOKIE
            = {(byte) 99, (byte) 130, (byte) 83, (byte) 99};

    @Nonnull
    public OptionsField decodeOptions(@Nonnull ByteBuffer message) throws DhcpException {
        byte[] magicCookie = new byte[VENDOR_MAGIC_COOKIE.length];
        message.get(magicCookie);
        if (!Arrays.equals(VENDOR_MAGIC_COOKIE, magicCookie))
            throw new DhcpException("Parse exception.");

        OptionsField options = new OptionsField();

        for (;;) {
            byte tag = message.get();

            if (tag == 0) // pad option
                continue;
            if (tag == -1) // end option
                break;

            int length = message.get() & 0xFF;
            byte[] value = decodeBytes(message, length);
            DhcpOption option = options.get(tag);
            if (option != null) {
                byte[] dataPrev = option.getData();
                byte[] dataCurr = Arrays.copyOf(dataPrev, dataPrev.length + length);
                System.arraycopy(value, 0, dataCurr, dataPrev.length, length);
                option.setData(dataCurr);
            } else {
                options.add(newOptionInstance(tag, value));
            }
        }

        // After concatenation has been resolved, we can do this.
        for (DhcpOption option : options)
            option.validate();

        return options;
    }

    @Nonnull
    private DhcpOption newOptionInstance(@Nonnegative byte tag, @Nonnull byte[] value) throws DhcpException {
        Class<? extends DhcpOption> type = registry.getOptionType(tag);
        DhcpOption option = (type != null) ? DhcpOptionsRegistry.newInstance(type) : new UnrecognizedOption(tag);
        option.setData(value);
        return option;
    }
}
