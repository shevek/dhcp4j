/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.address;

import java.net.InetAddress;
import javax.annotation.CheckForNull;

/**
 *
 * @author shevek
 */
public class AddressUtils {

    public static boolean isZeroAddress(@CheckForNull byte[] address) {
        if (address == null)
            return true;
        for (byte b : address)
            if (b != 0)
                return false;
        return true;
    }

    public static boolean isZeroAddress(@CheckForNull InetAddress address) {
        if (address == null)
            return true;
        return isZeroAddress(address.getAddress());
    }
}
