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

import com.google.common.primitives.UnsignedBytes;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * The Dynamic Host Configuration Protocol (DHCP) provides a framework
 * for passing configuration information to hosts on a TCP/IP network.  
 * Configuration parameters and other control information are carried in
 * tagged data items that are stored in the 'options' field of the DHCP
 * message.  The data items themselves are also called "options."
 *
 * https://www.iana.org/assignments/bootp-dhcp-parameters/bootp-dhcp-parameters.xhtml
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class DhcpOption {

    /**
     * The default data array used for simple (unparsed) options.
     */
    private byte[] data;

    /**
     * Get the option's code tag.
     * 
     * @return byte
     */
    public abstract byte getTag();

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

    public final void writeTo(@Nonnull ByteBuffer out) {
        // Option continuation per RFC3396
        byte tag = getTag();
        byte data[] = getData();
        for (int offset = 0; offset < data.length || offset == 0; offset += UnsignedBytes.MAX_VALUE) {
            int length = Math.min(data.length - offset, UnsignedBytes.MAX_VALUE);
            out.put(tag);
            out.put((byte) length);
            out.put(data, offset, length);
        }
    }

    @Nonnull
    protected String toStringData() throws DhcpException {
        return Arrays.toString(getData());
    }

    @Override
    public String toString() {
        String text;
        try {
            text = toStringData();
        } catch (Exception e) {
            text = Arrays.toString(getData());
        }
        return getClass().getSimpleName() + "[" + (getTag() & 0xFF) + "]: " + text;
    }
}
