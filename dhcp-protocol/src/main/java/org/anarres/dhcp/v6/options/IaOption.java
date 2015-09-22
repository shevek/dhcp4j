package org.anarres.dhcp.v6.options;

import java.nio.ByteBuffer;

/**
 * Created by marosmars
 */
public abstract class IaOption extends SuboptionOption {

    public int getIAID() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(0);
    }

    public void setIAID(int IAID) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(0, IAID);
    }
}
