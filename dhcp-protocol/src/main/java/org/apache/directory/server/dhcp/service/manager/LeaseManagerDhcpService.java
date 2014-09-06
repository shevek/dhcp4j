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
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
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
    protected DhcpMessage handleDISCOVER(InterfaceAddress localAddress, InetSocketAddress remoteAddress, DhcpMessage request) throws DhcpException {
        InetAddress networkAddress = getRemoteAddress(localAddress, request, remoteAddress);
        InetAddress requestedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        long requestedExpirySecs = request.getOptions().getIntOption(IpAddressLeaseTime.class);
        DhcpMessage reply = getLeaseManager().leaseOffer(localAddress, request, networkAddress, requestedAddress, requestedExpirySecs);
        if (reply == null)
            return null;
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleREQUEST(InterfaceAddress localAddress, InetSocketAddress remoteAddress, DhcpMessage request) throws DhcpException {
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
        DhcpMessage reply = newReplyAck(localAddress, request, MessageType.DHCPACK, declinedAddress, -1);
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
        DhcpMessage reply = newReplyAck(localAddress, request, MessageType.DHCPACK, releasedAddress, -1);
        stripOptions(request, reply.getOptions());
        return reply;
    }

}
