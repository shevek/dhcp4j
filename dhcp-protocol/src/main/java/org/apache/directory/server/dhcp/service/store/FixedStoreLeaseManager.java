/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.store;

import com.google.common.base.Preconditions;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.options.perinterface.BroadcastAddress;
import org.apache.directory.server.dhcp.options.vendor.SubnetMask;
import org.apache.directory.server.dhcp.service.manager.AbstractLeaseManager;

/**
 *
 * @author shevek
 */
public class FixedStoreLeaseManager extends AbstractLeaseManager {

    private final ConcurrentMap<HardwareAddress, Lease> leases = new ConcurrentHashMap<HardwareAddress, Lease>();

    @Nonnull
    public ConcurrentMap<HardwareAddress, Lease> getLeases() {
        return leases;
    }

    @CheckForNull
    public Lease getLease(@Nonnull HardwareAddress hardwareAddress) {
        Preconditions.checkNotNull(hardwareAddress, "HardwareAddress was null.");
        return leases.get(hardwareAddress);
    }

    @Nonnull
    public Lease addLease(@Nonnull HardwareAddress hardwareAddress, @Nonnull Lease lease) {
        leases.put(hardwareAddress, lease);
        return lease;
    }

    @Nonnull
    public Lease addLease(@Nonnull HardwareAddress hardwareAddress, @Nonnull InetAddress clientAddress) {
        Lease lease = new Lease();
        lease.setClientAddress(clientAddress);
        return addLease(hardwareAddress, lease);
    }

    @Nonnull
    public Lease addLease(@Nonnull HardwareAddress hardwareAddress, @Nonnull InterfaceAddress interfaceAddress) {
        Lease lease = addLease(hardwareAddress, interfaceAddress.getAddress());
        OptionsField options = lease.getOptions();
        options.setAddressOption(SubnetMask.class, interfaceAddress.getNetmaskAddress());
        options.setAddressOption(BroadcastAddress.class, interfaceAddress.getBroadcastAddress());
        return lease;
    }

    @Override
    public DhcpMessage leaseOffer(InterfaceAddress[] localAddresses,
            DhcpMessage request,
            InetAddress clientRequestedAddress, long clientRequestedExpirySecs) throws DhcpException {
        Lease lease = getLease(request.getHardwareAddress());
        if (lease == null)
            return null;
        lease.setState(Lease.LeaseState.OFFERED);
        lease.setExpires(System.currentTimeMillis() / 1000 + 3600);
        return SimpleStoreLeaseManager.newReply(request, MessageType.DHCPOFFER, lease);
    }

    @Override
    public DhcpMessage leaseRequest(InterfaceAddress[] localAddresses,
            DhcpMessage request,
            InetAddress clientRequestedAddress, long clientRequestedExpirySecs) throws DhcpException {
        Lease lease = getLease(request.getHardwareAddress());
        if (lease == null)
            return null;
        lease.setState(Lease.LeaseState.ACTIVE);
        lease.setExpires(System.currentTimeMillis() / 1000 + 3600);
        return SimpleStoreLeaseManager.newReply(request, MessageType.DHCPACK, lease);
    }

}
