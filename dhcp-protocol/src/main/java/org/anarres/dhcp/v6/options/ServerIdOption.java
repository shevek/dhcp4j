/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

/**
 * http://tools.ietf.org/html/rfc3315#section-22.3
 *
 * @author shevek
 */
public class ServerIdOption extends DuidOption {

    private static final short TAG = 2;

    @Override
    public short getTag() {
        return TAG;
    }
}
