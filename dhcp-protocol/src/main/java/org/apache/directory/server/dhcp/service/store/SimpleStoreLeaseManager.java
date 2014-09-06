/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.dhcp.service.store;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.vendor.SubnetMask;
import org.apache.directory.server.dhcp.service.manager.AbstractLeaseManager;

/**
 * Very simple dummy/proof-of-concept implementation of a DhcpStore.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SimpleStoreLeaseManager extends AbstractLeaseManager {
    // private static final String DEFAULT_INITIAL_CONTEXT_FACTORY =
    // "org.apache.directory.server.core.jndi.CoreContextFactory";

    // a map of current leases
    private final List<DhcpConfigSubnet> subnets = new ArrayList<DhcpConfigSubnet>();
    private final Cache<HardwareAddress, Lease> leases = CacheBuilder.newBuilder()
            .expireAfterAccess((long) (TTL_LEASE.maxLeaseTime * 2), TimeUnit.SECONDS)
            .recordStats()
            .build();

    //This will suppress PMD.AvoidUsingHardCodedIP warnings in this class
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    public SimpleStoreLeaseManager() {
        subnets.add(new DhcpConfigSubnet(
                InetAddresses.forString("192.168.168.0"), InetAddresses.forString("255.255.255.0"),
                InetAddresses.forString("192.168.168.159"), InetAddresses.forString("192.168.168.179")));
    }

    /**
     * Find the subnet for the given client address.
     * 
     * @param clientAddress
     * @return Subnet
     */
    @CheckForNull
    protected DhcpConfigSubnet findSubnet(@Nonnull InetAddress remoteAddress) {
        for (DhcpConfigSubnet subnet : subnets) {
            if (subnet.contains(remoteAddress))
                return subnet;
        }
        return null;
    }

    @Nonnull
    protected DhcpMessage newReply(
            @Nonnull InterfaceAddress localAddress,
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type,
            @Nonnull Lease lease) {
        long leaseTimeSecs = lease.getExpires() - System.currentTimeMillis() / 1000;
        DhcpMessage reply = newReplyAck(localAddress, request, type, lease.getClientAddress(), leaseTimeSecs);
        setBootParameters(reply, lease.getNextServerAddress(), null);
        reply.getOptions().addAll(lease.getOptions());
        return reply;
    }

    @Override
    public DhcpMessage leaseOffer(InterfaceAddress localAddress, DhcpMessage request, InetAddress remoteAddress, InetAddress clientRequestedAddress, long clientRequestedExpirySecs) throws DhcpException {
        HardwareAddress hardwareAddress = request.getHardwareAddress();
        Lease lease = leases.getIfPresent(hardwareAddress);
        if (lease != null)
            return newReply(localAddress, request, MessageType.DHCPOFFER, lease);

        DhcpConfigSubnet subnet = findSubnet(remoteAddress);
        if (subnet == null)
            return null;

        long leaseTimeSecs = getLeaseTime(TTL_OFFER, clientRequestedExpirySecs);

        // TODO: Allocate a new address.
        lease = new Lease();
        lease.setHardwareAddress(hardwareAddress);
        lease.setState(Lease.STATE_OFFERED);
        lease.setClientAddress(clientRequestedAddress);
        lease.setExpires(System.currentTimeMillis() / 1000 + leaseTimeSecs);
        lease.getOptions().setAddressOption(SubnetMask.class, subnet.getNetmask());
        leases.put(hardwareAddress, lease);

        return newReply(localAddress, request, MessageType.DHCPOFFER, lease);
    }

    @Override
    public DhcpMessage leaseRequest(InterfaceAddress localAddress, DhcpMessage request, InetAddress clientRequestedAddress, long clientRequestedExpirySecs) throws DhcpException {
        HardwareAddress hardwareAddress = request.getHardwareAddress();
        Lease lease = leases.getIfPresent(hardwareAddress);
        if (lease == null)
            return null;
        if (!Objects.equal(lease.getClientAddress(), clientRequestedAddress))
            return null;

        lease.setState(Lease.STATE_ACTIVE);
        long leaseTimeSecs = getLeaseTime(TTL_LEASE, clientRequestedExpirySecs);
        lease.setExpires(System.currentTimeMillis() / 1000 + leaseTimeSecs);

        return newReply(localAddress, request, MessageType.DHCPACK, lease);
    }

    @Override
    public boolean leaseDecline(InterfaceAddress localAddress, DhcpMessage request, InetAddress clientAddress) throws DhcpException {
        leases.invalidate(request.getHardwareAddress());
        return true;    // Should check if present.
    }

    @Override
    public boolean leaseRelease(InterfaceAddress localAddress, DhcpMessage request, InetAddress clientAddress) throws DhcpException {
        leases.invalidate(request.getHardwareAddress());
        return true;       // Should check if present.
    }
}
