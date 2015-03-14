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

    private static void checkReplyType(DhcpMessage request, DhcpMessage reply, MessageType...types) {
        for (MessageType type : types)
            if (type.equals(reply.getMessageType()))
                return;
        throw new IllegalStateException("Illegal response type " + reply.getMessageType() + " to request type " + request.getMessageType());
    }

    @Override
    protected DhcpMessage handleDISCOVER(InterfaceAddress[] localAddresses, InetSocketAddress remoteAddress, DhcpMessage request) throws DhcpException {
        // InetAddress networkAddress = getRemoteAddress(localAddresses, request, remoteAddress);
        InetAddress requestedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        long requestedExpirySecs = request.getOptions().getIntOption(IpAddressLeaseTime.class);
        DhcpMessage reply = getLeaseManager().leaseOffer(localAddresses, request, requestedAddress, requestedExpirySecs);
        if (reply == null)
            return null;
        checkReplyType(request, reply, MessageType.DHCPOFFER, MessageType.DHCPNAK);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleREQUEST(InterfaceAddress[] localAddresses, InetSocketAddress remoteAddress, DhcpMessage request) throws DhcpException {
        InetAddress requestedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (requestedAddress == null)
            return newReplyNak(request);
        long requestedExpirySecs = request.getOptions().getIntOption(IpAddressLeaseTime.class);
        DhcpMessage reply = getLeaseManager().leaseRequest(localAddresses, request, requestedAddress, requestedExpirySecs);
        if (reply == null)
            return newReplyNak(request);
        checkReplyType(request, reply, MessageType.DHCPACK, MessageType.DHCPNAK);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleDECLINE(InterfaceAddress[] localAddresses, InetSocketAddress clientAddress, DhcpMessage request) throws DhcpException {
        InetAddress declinedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (declinedAddress == null)
            return newReplyNak(request);
        boolean result = getLeaseManager().leaseDecline(localAddresses, request, declinedAddress);
        if (!result)
            return newReplyNak(request);
        DhcpMessage reply = newReplyAck(request, MessageType.DHCPACK, declinedAddress, -1);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleRELEASE(InterfaceAddress[] localAddresses, InetSocketAddress clientAddress, DhcpMessage request) throws DhcpException {
        InetAddress releasedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (AddressUtils.isZeroAddress(releasedAddress))
            releasedAddress = clientAddress.getAddress();
        if (releasedAddress == null)
            return newReplyNak(request);

        boolean result = getLeaseManager().leaseRelease(localAddresses, request, releasedAddress);
        if (!result)
            return newReplyNak(request);
        DhcpMessage reply = newReplyAck(request, MessageType.DHCPACK, releasedAddress, -1);
        stripOptions(request, reply.getOptions());
        return reply;
    }

}
