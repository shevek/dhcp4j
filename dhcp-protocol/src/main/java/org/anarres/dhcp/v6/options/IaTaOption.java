/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import org.apache.directory.server.dhcp.DhcpException;

/**
 * http://tools.ietf.org/html/rfc3315#section-22.5
 *
 * @author marosmars
 */
public class IaTaOption extends IaOption {

    private static final short TAG = 4;
    private static final int HEADER_LENGTH = 12;

    @Override
    public short getTag() {
        return TAG;
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

    @Override
    public int getHeaderSize() {
        return HEADER_LENGTH;
    }
}
