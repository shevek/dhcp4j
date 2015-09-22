package org.anarres.dhcp.v6.options;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.20
 */
public class ReconfigureAcceptOption extends Dhcp6Option {

    private static final short TAG = 20;

    @Override public short getTag() {
        return TAG;
    }

    // TODO ensure data is empty
}
