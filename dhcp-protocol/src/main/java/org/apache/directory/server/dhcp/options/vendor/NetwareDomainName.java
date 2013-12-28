/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.vendor;

import org.apache.directory.server.dhcp.options.DhcpOption;

/**
 *
 * @author shevek
 */
public class NetwareDomainName extends DhcpOption {

    @Override
    public byte getTag() {
        return 62;
    }

}
