/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.dhcp;

import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.options.DhcpOption;

/**
 * See RFC4578.
 *
 * @author shevek
 */
public class ClientNetworkInterface extends DhcpOption {

    public static final int TAG = 94;

    @Override
    public byte getTag() {
        return TAG;
    }

    public int getType() {
        return getData()[0] & 0xFF; // Always 1: UNDI.
    }

    public int getRevisionMajor() {
        return getData()[1] & 0xFF;
    }

    public int getRevisionMinor() {
        return getData()[2] & 0xFF;
    }

    @Override
    public void validate() throws DhcpException {
        super.validate();
        validateLength(3);
    }

    @Override
    protected String toStringData() {
        return getRevisionMajor() + "." + getRevisionMinor();
    }
}
