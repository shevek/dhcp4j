/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.vendor;

import org.apache.directory.server.dhcp.options.DhcpOption;

/**
 * http://tools.ietf.org/html/rfc4702
 *
 * @author shevek
 */
public class ClientFQDN extends DhcpOption {

    @Override
    public byte getTag() {
        return 81;
    }
}
