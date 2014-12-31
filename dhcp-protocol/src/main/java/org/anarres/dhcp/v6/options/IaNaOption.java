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

    public int getT2() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(8);
    }

    @Nonnull
    public Dhcp6Options getOptions() throws DhcpException {
        byte[] data = getData();
        ByteBuffer buf = ByteBuffer.wrap(data, 12, data.length - 12);
        Dhcp6MessageDecoder decoder = new Dhcp6MessageDecoder();
        return decoder.decodeOptions(buf);
    }
}
