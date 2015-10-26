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
package org.apache.directory.server.dhcp.messages;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.beans.PropertyEditorSupport;
import javax.annotation.Nonnull;

/**
 * A representation of a DHCP hardware address.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class HardwareAddress {

    public static class PropertyEditor extends PropertyEditorSupport {

        @Override
        public String getJavaInitializationString() {
            String text = getAsText();
            if (text == null)
                return "null";
            else
                return "HardwareAddress.fromString(\"" + text + "\")";
        }

        @Override
        public void setAsText(String text) {
            if (text == null)
                setValue(null);
            else
                setValue(HardwareAddress.fromString(text));
        }
    }
    /**
     * [htype] Hardware address type, see ARP section in "Assigned Numbers" RFC;
     * e.g., '1' = 10mb ethernet.
     */
    private final short type;
    /**
     * [hlen] Hardware address length (e.g. '6' for 10mb ethernet).
     */
    private final short length;
    /**
     * [chaddr] Client hardware address.
     */
    private final byte[] address;

    /**
     * @param type
     * @param length
     * @param address
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public HardwareAddress(short type, short length, @Nonnull byte[] address) {
        this.type = type;
        this.length = length;
        this.address = address;
    }

    public HardwareAddress(@Nonnull HardwareAddressType type, short length, @Nonnull byte[] address) {
        this(type.getCode(), length, address);
    }

    public HardwareAddress(@Nonnull HardwareAddressType type, @Nonnull byte[] address) {
        this(type.getCode(), (short) address.length, address);
    }

    @Nonnull
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public byte[] getAddress() {
        return address;
    }

    public short getType() {
        return type;
    }

    public short getLength() {
        return length;
    }

    /**
     * @see java.lang.Object#hashCode()
     * @return the instance's hash code
     */
    @Override
    public int hashCode() {
        int hashCode = 98643532 ^ type ^ length;

        for (int i = 0; i < length; i++) {
            hashCode ^= address[i];
        }

        return hashCode;
    }


    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (!getClass().equals(obj.getClass()))
            return false;

        HardwareAddress hw = (HardwareAddress) obj;

        if (type != hw.type)
            return false;
        if (length != hw.length)
            return false;
        for (int i = 0; i < length; i++)
            if (address[i] != hw.address[i])
                return false;
        return true;
    }

    /**
     * Create the string representation of the hardware address native to the
     * corresponding address type. This method currently supports only type
     * 1==ethernet with the representation <code>a1:a2:a3:a4:a5:a6</code>.<br>
     * For all other types, this method falls back to the representation created
     * by toString().
     *
     * @see java.lang.Object#toString()
     */
    @Nonnull
    public String getNativeRepresentation() {
        StringBuilder sb = new StringBuilder();

        switch (type) {
            case 1:
                for (int i = 0; i < length; i++) {
                    if (i > 0) {
                        sb.append(":");
                    }

                    String hex = Integer.toHexString(address[i] & 0xff);

                    if (hex.length() < 2) {
                        sb.append('0');
                    }

                    sb.append(hex);
                }

                break;

            default:
                sb.append(toString());
        }

        return sb.toString();
    }

    /**
     * Create a string representation of the hardware address. The string
     * representation is in the format<br>
     * <code>t/a1:a2:a3...</code><br>
     * Where <code>t</code> represents the address type (decimal) and
     * <code>a<sub>n</sub></code> represent the address bytes (hexadecimal).
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append("/");

        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(":");
            }

            String hex = Integer.toHexString(address[i] & 0xff);

            if (hex.length() < 2) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString();
    }

    /**
     * Parses a string representation of a hardware address.
     * Valid: 1/11:22:33:44:55:66 (toString())
     * Valid: Ethernet/11:22:33:44:55:66
     * Valid: 11:22:33:44:55:66 (defaults to Ethernet)
     *
     * @param text
     * @return HardwareAddress
     */
    @Nonnull
    public static HardwareAddress fromString(@Nonnull String text) {
        int idx = text.indexOf('/');
        HardwareAddressType hardwareAddressType = HardwareAddressType.Ethernet;
        if (idx != -1) {
            String hardwareAddressTypeText = text.substring(0, idx);
            try {
                int hardwareAddressTypeCode = Integer.parseInt(hardwareAddressTypeText);
                hardwareAddressType = HardwareAddressType.forCode(hardwareAddressTypeCode);
            } catch (NumberFormatException e) {
                // This will throw IllegalArgumentException, which is roughly what we want.
                hardwareAddressType = HardwareAddressType.valueOf(hardwareAddressTypeText);
            }
            text = text.substring(idx + 1);
        }

        CharMatcher separator = CharMatcher.BREAKING_WHITESPACE.or(CharMatcher.anyOf(":-"));
        Iterable<String> parts = Splitter.on(separator).omitEmptyStrings().trimResults().split(text);
        int i = 0;
        byte[] out = new byte[Iterables.size(parts)];
        for (String part : parts)
            out[i++] = (byte) Integer.parseInt(part, 16);
        return new HardwareAddress(hardwareAddressType.getCode(), (short) out.length, out);
    }
}
