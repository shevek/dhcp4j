/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import org.apache.directory.server.dhcp.options.BaseDhcpOption;

/**
 *
 * @author shevek
 */
public abstract class Dhcp6Option extends BaseDhcpOption {

    public abstract short getTag();

    @Override
    protected int getTagAsInt() {
        return getTag() & 0xFFFF;
    }
}
