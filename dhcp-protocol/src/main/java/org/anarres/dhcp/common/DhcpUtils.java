/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.service.DhcpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shevek
 */
public class DhcpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpUtils.class);

    /**
     * Determine where to send the message: <br>
     * If the 'giaddr' field in a DHCP message from a client is non-zero, the
     * server sends any return messages to the 'DHCP server' port on the BOOTP
     * relay agent whose address appears in 'giaddr'. If the 'giaddr' field is
     * zero and the 'ciaddr' field is nonzero, then the server unicasts DHCPOFFER
     * and DHCPACK messages to the address in 'ciaddr'. If 'giaddr' is zero and
     * 'ciaddr' is zero, and the broadcast bit is set, then the server broadcasts
     * DHCPOFFER and DHCPACK messages to 0xffffffff. If the broadcast bit is not
     * set and 'giaddr' is zero and 'ciaddr' is zero, then the server unicasts
     * DHCPOFFER and DHCPACK messages to the client's hardware address and
     * 'yiaddr' address. In all cases, when 'giaddr' is zero, the server
     * broadcasts any DHCPNAK messages to 0xffffffff.
     */
    @Nonnull
    //This will suppress PMD.AvoidUsingHardCodedIP warnings in this class
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    public static InetSocketAddress determineMessageDestination(
            @Nonnull DhcpMessage request,
            @Nonnull DhcpMessage reply,
            @Nonnull InterfaceAddress localAddress,
            @Nonnegative int remotePort
    ) {
        if (!AddressUtils.isZeroAddress(request.getRelayAgentAddress())) {
            // send to agent, if received via agent.
            return new InetSocketAddress(request.getRelayAgentAddress(), DhcpService.SERVER_PORT);
        } else if (reply.getMessageType() == MessageType.DHCPNAK) {
            // force broadcast for DHCPNAKs
            return new InetSocketAddress(localAddress.getBroadcastAddress(), remotePort);
        } else if (!AddressUtils.isZeroAddress(request.getCurrentClientAddress())) {
            // have a current address? unicast to it.
            return new InetSocketAddress(request.getCurrentClientAddress(), remotePort);
        } else {
            // not a NAK...
            return new InetSocketAddress(localAddress.getBroadcastAddress(), remotePort);
        }
    }
}
