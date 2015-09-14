package org.anarres.dhcp.v6.options;

/**
 * Created by mmarsale on 14.9.2015.
 */
public class InterfaceIdOption extends Dhcp6Option {

    private static final short TAG = 18;

    @Override public short getTag() {
        return TAG;
    }
}
