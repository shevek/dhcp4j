/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.options;

import com.google.common.primitives.Bytes;
import java.util.Arrays;

/**
 *
 * @author shevek
 */
public abstract class NulTerminatedStringOption extends StringOption {

    @Override
    protected byte[] getStringData() {
        byte[] data = super.getStringData();
        int length = Bytes.indexOf(data, (byte) 0);
        if (length >= 0)
            return Arrays.copyOf(data, length);
        return data;
    }

    @Override
    protected void setStringData(byte[] data) {
        super.setStringData(Arrays.copyOf(data, data.length + 1));
    }
}
