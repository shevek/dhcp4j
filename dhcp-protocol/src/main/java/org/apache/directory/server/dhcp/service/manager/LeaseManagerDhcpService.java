/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import java.net.InetAddress;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.dhcp.IpAddressLeaseTime;
import org.apache.directory.server.dhcp.options.dhcp.RequestedIpAddress;
import org.apache.directory.server.dhcp.service.AbstractDhcpService;
import org.apache.directory.server.dhcp.service.DhcpService;

/**
 * Wraps a {@link LeaseManager} as a {@link DhcpService}.
 *
 * This gives slightly more convenience and automatic options management at
 * the expense of very little loss of flexibility. This approach is generally
 * recommended.
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

    private static void checkReplyType(DhcpMessage request, DhcpMessage reply, MessageType... types) {
        for (MessageType type : types)
            if (type.equals(reply.getMessageType()))
                return;
        throw new IllegalStateException("Illegal response type " + reply.getMessageType() + " to request type " + request.getMessageType());
    }

    @Override
    protected DhcpMessage handleDISCOVER(DhcpRequestContext context, DhcpMessage request) throws DhcpException {
        // InetAddress networkAddress = getRemoteAddress(localAddresses, request, remoteAddress);
        InetAddress requestedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        long requestedExpirySecs = request.getOptions().getIntOption(IpAddressLeaseTime.class);
        DhcpMessage reply = getLeaseManager().leaseOffer(context, request, requestedAddress, requestedExpirySecs);
        if (reply == null)
            return null;
        checkReplyType(request, reply, MessageType.DHCPOFFER, MessageType.DHCPNAK);
        setServerIdentifier(context, reply);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleREQUEST(DhcpRequestContext context, DhcpMessage request) throws DhcpException {
        InetAddress requestedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (requestedAddress == null)
            return newReplyNak(request);
        long requestedExpirySecs = request.getOptions().getIntOption(IpAddressLeaseTime.class);
        DhcpMessage reply = getLeaseManager().leaseRequest(context, request, requestedAddress, requestedExpirySecs);
        if (reply == null)
            return newReplyNak(request);
        checkReplyType(request, reply, MessageType.DHCPACK, MessageType.DHCPNAK);
        setServerIdentifier(context, reply);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleDECLINE(DhcpRequestContext context, DhcpMessage request) throws DhcpException {
        InetAddress declinedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (declinedAddress == null)
            return newReplyNak(request);
        boolean result = getLeaseManager().leaseDecline(context, request, declinedAddress);
        if (!result)
            return newReplyNak(request);
        DhcpMessage reply = newReplyAck(request, MessageType.DHCPACK, declinedAddress, -1);
        setServerIdentifier(context, reply);
        stripOptions(request, reply.getOptions());
        return reply;
    }

    @Override
    protected DhcpMessage handleRELEASE(DhcpRequestContext context, DhcpMessage request) throws DhcpException {
        InetAddress releasedAddress = request.getOptions().getAddressOption(RequestedIpAddress.class);
        if (AddressUtils.isZeroAddress(releasedAddress))
            releasedAddress = context.getRemoteAddress();
        if (AddressUtils.isZeroAddress(releasedAddress))
            return newReplyNak(request);

        boolean result = getLeaseManager().leaseRelease(context, request, releasedAddress);
        if (!result)
            return newReplyNak(request);
        DhcpMessage reply = newReplyAck(request, MessageType.DHCPACK, releasedAddress, -1);
        setServerIdentifier(context, reply);
        stripOptions(request, reply.getOptions());
        return reply;
    }

}
