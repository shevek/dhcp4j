/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.address;

import java.net.InetAddress;

/**
 *
 * @author shevek
 */
public class InterfaceAddress extends AbstractMaskedAddress {

    public InterfaceAddress(InetAddress address, int netmask) {
        super(address, netmask);
    }

}
