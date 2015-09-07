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
public class PreferenceOption extends Dhcp6Option {

    private static final short TAG = 7;

    @Override
    public short getTag() {
        return TAG;
    }

    public byte getPreference() {
        return getData()[0];
    }

    public void setPreference(byte preference) {
        setData(new byte[]{preference});
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        values.append("preference:");
        values.append(getPreference());
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }

    public static PreferenceOption create(final byte preference) {
        final PreferenceOption iaNaOption = new PreferenceOption();
        iaNaOption.setData(new byte[1]);
        iaNaOption.setPreference(preference);
        return iaNaOption;
    }
}
