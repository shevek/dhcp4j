/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

/**
 * http://tools.ietf.org/html/rfc3315#section-22.2
 *
 * @author shevek
 */
public class ClientIdOption extends DuidOption {

    private static final short TAG = 1;

    @Override
    public short getTag() {
        return TAG;
    }

}
