/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.base.Optional;
import java.nio.ByteBuffer;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * http://tools.ietf.org/html/rfc3315#section-22.4
 *
 * @author shevek, marosmars
 */
public class IaNaOption extends IaOption {

    private static final short TAG = 3;
    private static final int HEADER_LENGTH = 12;

    @Override
    public short getTag() {
        return TAG;
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
        int length = HEADER_LENGTH;

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

    @Override protected int getHeaderSize() {
        return HEADER_LENGTH;
    }
}
