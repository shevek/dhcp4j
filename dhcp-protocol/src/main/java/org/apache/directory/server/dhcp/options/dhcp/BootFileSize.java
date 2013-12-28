/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options.dhcp;

import org.apache.directory.server.dhcp.options.ShortOption;

/**
 * Size in 512-byte chunks. RFC2132.
 *
 * @author shevek
 */
public class BootFileSize extends ShortOption {

    @Override
    public byte getTag() {
        return 13;
    }

}
