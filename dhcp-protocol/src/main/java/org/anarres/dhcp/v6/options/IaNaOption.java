/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.base.Optional;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * http://tools.ietf.org/html/rfc3315#section-22.4
 *
 * @author shevek
 */
public class IaNaOption extends Dhcp6Option {

    private static final short TAG = 3;

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

    public int getT1() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(4);
    }

    public void setT1(int t1) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(4, t1);
    }

    public void setT2(int t2) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(8, t2);
    }

    public int getT2() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(8);
    }

    // FIXME extrace base abstract class for option based options e.g. IANA IATA

    @Nonnull
    public Dhcp6Options getOptions() throws DhcpException {
        // FIXME DhcpOptions allow only a single instance of option per message
        // However the IaAddressOption can appear more than once
        Dhcp6MessageDecoder decoder = Dhcp6MessageDecoder.getInstance();
        return decoder.decodeOptions(getOptionsRaw());
    }

    public void setOptions(@Nonnull final ByteBuffer options) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        options.position(0);
        buf.position(12);
        buf.put(options);
    }

    public void setOptions(@Nonnull final Dhcp6Options options) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.position(12);
        Dhcp6MessageEncoder.getInstance().encode(buf, options);
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
        values.append(", T1:");
        values.append(getT1());
        values.append(", T2:");
        values.append(getT2());
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

    public static IaNaOption create(int IaId, int t1, int t2, Optional<Dhcp6Options> options) {
        final IaNaOption iaNaOption = new IaNaOption();
        int length = 12;

        ByteBuffer encodedOptions = null;
        if(options.isPresent() && !options.get().isEmpty()) {
            encodedOptions = Dhcp6MessageEncoder.getInstance().encode(options.get());
            length += encodedOptions.limit();
        }

        iaNaOption.setData(new byte[length]);
        iaNaOption.setT1(t1);
        iaNaOption.setT2(t2);
        iaNaOption.setIAID(IaId);
        if (encodedOptions != null) {
            iaNaOption.setOptions(encodedOptions);
        }
        return iaNaOption;
    }
}
