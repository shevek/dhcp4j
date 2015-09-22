package org.anarres.dhcp.v6.options;

import javax.annotation.Nonnull;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.15
 */
public class UserClassOption extends Dhcp6Option {
    private static final short TAG = 15;

    @Override public short getTag() {
        return TAG;
    }

    @Nonnull
    public static UserClassOption create(@Nonnull final byte[] data) {
        final UserClassOption relayMessageOption = new UserClassOption();
        relayMessageOption.setData(data);
        return relayMessageOption;
    }

}
