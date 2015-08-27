/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.8
 *
 * @author marosmars
 */
public abstract class PreferenceOption extends Dhcp6Option {

    public byte getPreference() {
        return getData()[0];
    }

    public void setPreference(byte preference) {
        setData(new byte[]{preference});
    }
}
