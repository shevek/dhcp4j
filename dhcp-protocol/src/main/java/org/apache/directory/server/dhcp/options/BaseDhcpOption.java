/*
 *   Copyright 2005 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.directory.server.dhcp.options;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedBytes;
import java.util.Arrays;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * The Dynamic Host Configuration Protocol (DHCP) provides a framework
 * for passing configuration information to hosts on a TCP/IP network.
 * Configuration parameters and other control information are carried in
 * tagged data items that are stored in the 'options' field of the DHCP
 * message. The data items themselves are also called "options."
 *
 * https://www.iana.org/assignments/bootp-dhcp-parameters/bootp-dhcp-parameters.xhtml
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class BaseDhcpOption {

    /**
     * The default data array used for simple (unparsed) options.
     */
    private byte[] data;

    /**
     * Get the option's code tag.
     *
     * @return byte
     */
    protected abstract int getTagAsInt();

    /**
     * Get the data (wire format) into a byte array. Subclasses must provide an
     * implementation which serializes the parsed data back into a byte array if
     * they override {@link #setData(byte[])}.
     *
     * @return byte[]
     */
    @Nonnull
    public final byte[] getData() {
        return data;
    }

    /**
     * Set the data (wire format) from a byte array. The default implementation
     * just records the data as a byte array. Subclasses may parse the data into
     * something more meaningful.
     *
     * @param data
     */
    public final void setData(@Nonnull byte data[]) {
        this.data = data;
    }

    /**
     * Ensures that the byte array held after deserialization is valid.
     */
    public void validate() throws DhcpException {
    }

    protected void validateLength(@Nonnegative int length) throws DhcpException {
        byte[] b = getData();
        if (b.length != length)
            throw new DhcpException("Expected exactly " + length + " data bytes in " + this + ", not " + b.length);
    }

    /**
     * The last-resort stringifier for DHCP Option data.
     *
     * This probably returns a base 16 representation of the data.
     * Do not machine-parse this. Use {@link #getData()} instead for raw data.
     */
    @Nonnull
    protected final String toStringDataFallback() {
        return BaseEncoding.base16().withSeparator(" ", 8).encode(getData());
    }

    @Nonnull
    protected String toStringData() throws DhcpException {
        return toStringDataFallback();
    }

    @Override
    public String toString() {
        String text;
        try {
            text = toStringData();
        } catch (Exception e) {
            text = toStringDataFallback();
        }
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + text;
    }
}
