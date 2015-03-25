/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.dhcp;

import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.messages.SystemArchitecture;
import org.apache.directory.server.dhcp.options.ShortOption;

/**
 * See RFC4578.
 *
 * @author shevek
 */
public class ClientSystemArchitecture extends ShortOption {

    public static final int TAG = 93;

    @Override
    public byte getTag() {
        return TAG;
    }

    @Nonnull
    public SystemArchitecture getSystemArchitecture() {
        return SystemArchitecture.forTypeCode(getShortValue());
    }
}
