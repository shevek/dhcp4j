/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * http://tools.ietf.org/html/rfc3315#section-22.5
 *
 * @author marosmars
 */
public class IaTaOption extends Dhcp6Option {

    private static final short TAG = 4;

    @Override
    public short getTag() {
        return TAG;
    }

    public int getIAID() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(0);
    }

    public void setIAID(int IAID) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(0, IAID);
    }

    // FIXME extrace base abstract class for option based options e.g. IANA IATA

    @Nonnull
    public Dhcp6Options getOptions() throws DhcpException {
        Dhcp6MessageDecoder decoder = Dhcp6MessageDecoder.getInstance();
        return decoder.decodeOptions(getOptionsRaw());
    }

    @Nonnull
    private ByteBuffer getOptionsRaw() {
        byte[] data = getData();
        return ByteBuffer.wrap(data, 12, data.length - 12);
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        values.append("IAID:");
        values.append(getIAID());
        values.append(", ");
        try {
            values.append(getOptions().toString());
        } catch (DhcpException e) {
            values.append("options:[");
            values.append(toStringDataFallback(getOptionsRaw().array()));
            values.append("]");
        }

        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }
}
