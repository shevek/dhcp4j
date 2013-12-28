package org.apache.directory.server.dhcp.options.misc;

import org.apache.directory.server.dhcp.options.DhcpOption;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author shevek
 */
public class UserClass extends DhcpOption {

    @Override
    public byte getTag() {
        return 77;
    }

}
