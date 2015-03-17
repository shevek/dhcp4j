/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import java.net.InetAddress;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.service.AbstractDhcpReplyFactory;

/**
 *
 * @author shevek
 */
public abstract class AbstractLeaseManager extends AbstractDhcpReplyFactory implements LeaseManager {

    @Override
    public boolean leaseDecline(
            InterfaceAddress[] localAddresses,
            DhcpMessage request,
            InetAddress clientAddress) throws DhcpException {
        return false;
    }

    @Override
    public boolean leaseRelease(
            InterfaceAddress[] localAddresses,
            DhcpMessage request,
            InetAddress clientAddress) throws DhcpException {
        return false;
    }
}
