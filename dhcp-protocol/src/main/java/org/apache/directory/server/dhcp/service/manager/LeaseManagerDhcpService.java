/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.address.AddressUtils;
import org.apache.directory.server.dhcp.address.InterfaceAddress;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.dhcp.IpAddressLeaseTime;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.service.AbstractDhcpService;

/**
 *
 * @author shevek
 */
public class LeaseManagerDhcpService extends AbstractDhcpService {

    @Nonnull
    private final LeaseManager leaseManager;

    public LeaseManagerDhcpService(@Nonnull LeaseManager leaseManager) {
        this.leaseManager = leaseManager;
    }

    @Nonnull
    public LeaseManager getLeaseManager() {
        return leaseManager;
    }

    @Override
    protected DhcpMessage handleDISCOVER(InterfaceAddress localAddress, InetSocketAddress clientAddress, DhcpMessage request) throws DhcpException {
        InetAddress remoteAddress = getRemoteAddress(localAddress, request, clientAddress);
        InetAddress requestedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        long requestedExpirySecs = request.getOptions().getIntOption(IpAddressLeaseTime.class);
        DhcpMessage reply = getLeaseManager().leaseOffer(localAddress, request, remoteAddress, requestedAddress, requestedExpirySecs);
        if (reply == null)
            return null;
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleREQUEST(InterfaceAddress localAddress, InetSocketAddress clientAddress, DhcpMessage request) throws DhcpException {
        InetAddress requestedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (requestedAddress == null)
            return newReplyNak(localAddress, request);
        long requestedExpirySecs = request.getOptions().getIntOption(IpAddressLeaseTime.class);
        DhcpMessage reply = getLeaseManager().leaseRequest(localAddress, request, requestedAddress, requestedExpirySecs);
        if (reply == null)
            return newReplyNak(localAddress, request);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleDECLINE(InterfaceAddress localAddress, InetSocketAddress clientAddress, DhcpMessage request) throws DhcpException {
        InetAddress declinedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (declinedAddress == null)
            return newReplyNak(localAddress, request);
        boolean result = getLeaseManager().leaseDecline(localAddress, request, declinedAddress);
        if (!result)
            return newReplyNak(localAddress, request);
        DhcpMessage reply = newReply(localAddress, request, MessageType.DHCPACK, -1, declinedAddress, null, null);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleRELEASE(InterfaceAddress localAddress, InetSocketAddress clientAddress, DhcpMessage request) throws DhcpException {
        InetAddress releasedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (AddressUtils.isZeroAddress(releasedAddress))
            releasedAddress = clientAddress.getAddress();
        if (releasedAddress == null)
            return newReplyNak(localAddress, request);

        boolean result = getLeaseManager().leaseRelease(localAddress, request, releasedAddress);
        if (!result)
            return newReplyNak(localAddress, request);
        DhcpMessage reply = newReply(localAddress, request, MessageType.DHCPACK, -1, releasedAddress, null, null);
        stripOptions(request, reply.getOptions());
        return reply;
    }

}
