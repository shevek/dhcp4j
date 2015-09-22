/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.anarres.dhcp.common.address.NetworkAddress;
import org.anarres.dhcp.common.address.Subnet;
import org.anarres.jallocator.ResourceAllocator;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for lease managers which allocate addresses from a pool.
 *
 * @author shevek
 */
public abstract class AbstractDynamicLeaseManager extends AbstractLeaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDynamicLeaseManager.class);
    public static final int TTL_EXPIRE = 600;
    private final Cache<NetworkAddress, ResourceAllocator<InetAddress>> allocators = CacheBuilder.newBuilder()
            .expireAfterAccess(TTL_LEASE.maxLeaseTime * 4, TimeUnit.SECONDS)
            .recordStats()
            .build();

    @CheckForNull
    protected abstract InetAddress getFixedAddressFor(@Nonnull HardwareAddress hardwareAddress) throws DhcpException;

    @CheckForNull
    protected abstract Subnet getSubnetFor(@Nonnull NetworkAddress networkAddress) throws DhcpException;

    @CheckForNull
    private ResourceAllocator<InetAddress> getAllocatorFor(@Nonnull NetworkAddress networkAddress) throws DhcpException {
        final Subnet subnet = getSubnetFor(networkAddress);
        if (subnet == null)
            return null;
        NetworkAddress network = subnet.getNetworkAddress();
        ResourceAllocator<InetAddress> allocator = allocators.getIfPresent(network);
        // TODO: Update the resource allocator if it's out of date.
        if (allocator != null)
            return allocator;
        try {
            return allocators.get(network, new Callable<ResourceAllocator<InetAddress>>() {
                @Override
                public ResourceAllocator<InetAddress> call() throws Exception {
                    DhcpAddressResourceProvider resourceProvider = new DhcpAddressResourceProvider(subnet);
                    return new ResourceAllocator<InetAddress>(resourceProvider);
                }
            });
        } catch (ExecutionException e) {
            throw new DhcpException("Failed to load ResourceAllocator for " + subnet, e);
        }
    }

    /**
     * Leases an InetAddress for the given HardwareAddress.
     *
     * Lock, retrieve current mapping from store.
     * If the InetAddress is unallocated OR allocated to the given HardwareAddress, return it.
     * Else return null.
     */
    @CheckForNull
    protected abstract boolean leaseIp(
            @Nonnull InetAddress address,
            @Nonnull HardwareAddress hardwareAddress,
            @Nonnegative long ttl) throws Exception;

    @CheckForNull
    protected InetAddress leaseMac(
            @Nonnull DhcpRequestContext context,
            @Nonnull HardwareAddress hardwareAddress,
            @CheckForNull InetAddress currentAddress, @CheckForNull InetAddress requestedAddress,
            @Nonnegative long ttl)
            throws Exception {

        // Is it a singular system?
        // If it's singular, perhaps it deliberately gets no DHCP.
        // What network should the system address come from?
        // String hardwareId = InventoryUtils.hardwareAddress(hardwareAddress);
        // UUID systemId = new InventoryManager.GetSystemIdCommand(inventoryManager, hardwareId).execute();
        // BareMetalBootParameters bootParameters = new BareMetalManager.GetBootParametersCommand(bareMetalManager, systemId).execute();
        // Fixed address from configuration.
        FIXED:
        {
            LOG.debug("Looking for fixed address.");
            InetAddress fixedAddress = getFixedAddressFor(hardwareAddress);
            if (fixedAddress != null) {
                if (!leaseIp(fixedAddress, hardwareAddress, ttl * 2))
                    LOG.error("Client configured with fixed-address " + fixedAddress + " but lease failed.");
                else if (LOG.isDebugEnabled())
                    LOG.debug("Using fixed address " + fixedAddress);
                return fixedAddress;
            }
        }

        // Current or previous lease address, if available.
        EXISTING:
        {
            LOG.debug("Looking for pre-existing address.");
            if (currentAddress != null) {
                if (leaseIp(currentAddress, hardwareAddress, ttl * 2)) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Using pre-existing address " + currentAddress);
                    return currentAddress;
                }
            }
        }

        REQUESTED:
        {
            LOG.debug("Looking for client-requested address.");
            // Requested address, if available.
            if (requestedAddress != null) {
                if (leaseIp(requestedAddress, hardwareAddress, ttl * 2)) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Using client-requested address {}", requestedAddress);
                    return requestedAddress;
                }
            }
        }

        CREATED:
        for (InterfaceAddress interfaceAddress : context.getInterfaceAddresses()) {
            // New address from the server's pool
            ResourceAllocator<InetAddress> allocator = getAllocatorFor(interfaceAddress.toNetworkAddress());
            if (allocator == null) {
                if (LOG.isDebugEnabled())
                    LOG.debug("No address allocator for {}", interfaceAddress);
                continue CREATED;
            }
            for (InetAddress allocatedAddress : allocator) {
                if (leaseIp(allocatedAddress, hardwareAddress, ttl * 2)) {
                    if (LOG.isDebugEnabled())
                        LOG.debug("Using server-generated address {}", allocatedAddress);
                    return allocatedAddress;
                }
            }
        }
        LOG.warn("Failed to generate a lease for {}", hardwareAddress);

        return null;
    }

    /**
     * Leases an InetAddress for the given HardwareAddress.
     *
     * Lock, retrieve current mapping from store.
     * Call {@link #leaseMac(DhcpRequestContext, HardwareAddress, InetAddress, InetAddress, long)}
     * with the current mapping.
     * Store the returned address (if given) as the new current mapping and release the lock.
     */
    @CheckForNull
    protected abstract InetAddress leaseMac(
            @Nonnull DhcpRequestContext context,
            @Nonnull DhcpMessage request,
            @CheckForNull InetAddress clientRequestedAddress, @Nonnegative long ttl)
            throws Exception;

    @Override
    public DhcpMessage leaseOffer(
            DhcpRequestContext context,
            DhcpMessage request,
            InetAddress clientRequestedAddress, long clientRequestedExpirySecs)
            throws DhcpException {
        // LOG.info("OFFER: interfaceAddress=" + interfaceAddress + ", networkAddress=" + networkAddress + ", request=" + request + ", hardwareAddress=" + hardwareAddress + ", requestedAddress=" + requestedAddress + ", requestedLeaseTimeSecs=" + requestedLeaseTimeSecs);
        try {
            long leaseTimeSecs = getLeaseTime(TTL_OFFER, clientRequestedExpirySecs);
            InetAddress clientAddress = leaseMac(context, request, clientRequestedAddress, leaseTimeSecs);
            if (clientAddress == null)
                return null;
            return newReplyAck(request, MessageType.DHCPOFFER, clientAddress, leaseTimeSecs);
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, DhcpException.class);
            throw new DhcpException("Failed to lease for MAC " + request.getHardwareAddress() + ": " + e, e);
        }
    }

    @Override
    public DhcpMessage leaseRequest(
            DhcpRequestContext context,
            DhcpMessage request,
            InetAddress clientRequestedAddress, long clientRequestedExpirySecs)
            throws DhcpException {
        try {
            long leaseTimeSecs = getLeaseTime(TTL_LEASE, clientRequestedExpirySecs);
            if (clientRequestedAddress == null) {
                LOG.warn("REQUEST from " + request.getHardwareAddress() + " did not request an address.");
                return null;
            }
            InetAddress clientAddress = leaseMac(context, request, clientRequestedAddress, leaseTimeSecs);
            if (clientAddress == null)
                return null;
            return newReplyAck(request, MessageType.DHCPACK, clientAddress, leaseTimeSecs);
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, DhcpException.class);
            throw new DhcpException("Failed to lease for MAC " + request.getHardwareAddress() + ": " + e, e);
        }
    }
}
