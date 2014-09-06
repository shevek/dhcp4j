/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

/**
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
