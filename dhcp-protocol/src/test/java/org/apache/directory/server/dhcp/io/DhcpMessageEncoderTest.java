/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.io;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.messages.HardwareAddressType;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.dhcp.BootfileName;
import org.apache.directory.server.dhcp.options.dhcp.IpAddressLeaseTime;
import org.apache.directory.server.dhcp.options.dhcp.ServerIdentifier;
import org.apache.directory.server.dhcp.options.dhcp.TftpServerName;
import org.apache.directory.server.dhcp.options.perinterface.BroadcastAddress;
import org.apache.directory.server.dhcp.options.vendor.SubnetMask;
import org.junit.Test;

/**
 *
 * @author shevek
 */
public class DhcpMessageEncoderTest {

    @Test
    public void testEncoder() throws Exception {

        DhcpMessage message = new DhcpMessage();
        message.setMessageType(MessageType.DHCPACK);
        message.setOp(DhcpMessage.OP_BOOTREPLY);
        message.setHardwareAddress(new HardwareAddress(HardwareAddressType.Ethernet, new byte[]{1, 2, 3, 4, 5, 6}));

        InetAddress address = InetAddress.getByAddress(new byte[]{8, 9, 10, 11});

        message.setAssignedClientAddress(address);
        message.setCurrentClientAddress(address);
        message.getOptions().setIntOption(IpAddressLeaseTime.class, 600);
        message.getOptions().setAddressOption(SubnetMask.class, "255.255.255.0");
        message.getOptions().setAddressOption(ServerIdentifier.class, address);
        message.getOptions().setStringOption(TftpServerName.class, "123.45.67.89");
        message.getOptions().setAddressOption(BroadcastAddress.class, address);
        message.getOptions().setStringOption(BootfileName.class, "http://10.253.0.1:5180/ipxe/config/8c666e12-7cc8-1031-8001-828dcf47821b/52:54:00:12:34:56");

        DhcpMessageEncoder encoder = new DhcpMessageEncoder();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        encoder.encode(buf, message);
    }
}