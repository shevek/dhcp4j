/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.dhcp;

import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.options.DhcpOption;

/**
 *
 * @see RFC4578
 * @author shevek
 */
public class ClientNetworkInterface extends DhcpOption {

    public static final int TAG = 94;

    @Override
    public byte getTag() {
        return TAG;
    }

    public int getRevisionMajor() {
        return getData()[0] & 0xFF;
    }

    public int getRevisionMinor() {
        return getData()[1] & 0xFF;
    }

    @Override
    public void validate() throws DhcpException {
        super.validate();
        if (getData().length != 2)
            throw new DhcpException("Expected exactly 2 data bytes in " + this);
    }

    @Override
    protected String toStringData() {
        return getRevisionMajor() + "." + getRevisionMinor();
    }
}
