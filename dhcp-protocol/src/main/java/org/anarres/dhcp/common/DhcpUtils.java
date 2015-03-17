/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.options.dhcp.BootfileName;
import org.apache.directory.server.dhcp.options.dhcp.ServerIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.TftpServerName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class DhcpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpUtils.class);

    public static void setServerIdentifier(
            @Nonnull DhcpMessage reply,
            @Nonnull InetAddress localAddress) {
        if (!AddressUtils.isZeroAddress(localAddress)) {
            reply.setServerHostname(InetAddresses.toAddrString(localAddress));
            reply.getOptions().add(new ServerIdentifier(localAddress));
        }
    }

    public static void setBootParameters(
            @Nonnull DhcpMessage reply,
            @CheckForNull InetAddress nextServerAddress,
            @CheckForNull String bootFileName) {
        if (nextServerAddress != null) {
            reply.setNextServerAddress(nextServerAddress);
            reply.getOptions().setStringOption(TftpServerName.class, InetAddresses.toAddrString(nextServerAddress));
        }
        if (bootFileName != null) {
            reply.setBootFileName(bootFileName);
            reply.getOptions().setStringOption(BootfileName.class, bootFileName);
        }
    }
}
