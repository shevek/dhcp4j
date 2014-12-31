/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public abstract class DuidOption extends Dhcp6Option {

    @Nonnull
    public byte[] getDuid() {
        return getData();
    }

    public void setDuid(@Nonnull byte[] duid) {
        setData(duid);
    }
}
