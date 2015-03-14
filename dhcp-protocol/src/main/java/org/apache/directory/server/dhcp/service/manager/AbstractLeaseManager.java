/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import java.net.InetAddress;
import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.service.AbstractDhcpService;

/**
 *
 * @author shevek
 */
public abstract class AbstractLeaseManager implements LeaseManager {

    public static class LeaseTimeRange {

        @Nonnegative
        public long minLeaseTime = 60;
        @Nonnegative
        public long defaultLeaseTime = 60;
        @Nonnegative
        public long maxLeaseTime = 60;

        public LeaseTimeRange() {
        }

        public LeaseTimeRange(long minLeaseTime, long defaultLeaseTime, long maxLeaseTime) {
            this.minLeaseTime = minLeaseTime;
            this.defaultLeaseTime = defaultLeaseTime;
            this.maxLeaseTime = maxLeaseTime;
        }
    }
    public final LeaseTimeRange TTL_OFFER = new LeaseTimeRange(60, 600, 600);
    public final LeaseTimeRange TTL_LEASE = new LeaseTimeRange(60, 3600, 36000);

    @Nonnegative
    public static long getLeaseTime(@Nonnull LeaseTimeRange leaseTimeSecs, @CheckForSigned long requestedLeaseTimeSecs) {
        if (requestedLeaseTimeSecs < 0)
            return leaseTimeSecs.defaultLeaseTime;
        if (requestedLeaseTimeSecs <= leaseTimeSecs.minLeaseTime)
            return leaseTimeSecs.minLeaseTime;
        if (requestedLeaseTimeSecs >= leaseTimeSecs.maxLeaseTime)
            return leaseTimeSecs.maxLeaseTime;
        return requestedLeaseTimeSecs;
    }

    @Nonnull
    public static DhcpMessage newReply(
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type) {
        return AbstractDhcpService.newReply(request, type);
    }

    @Nonnull
    public static DhcpMessage newReplyAck(
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type,
            @CheckForNull InetAddress assignedClientAddress,
            @Nonnegative long leaseTimeSecs) {
        return AbstractDhcpService.newReplyAck(request, type, assignedClientAddress, leaseTimeSecs);
    }

    public static void setBootParameters(
            @Nonnull DhcpMessage reply,
            @CheckForNull InetAddress nextServerAddress,
            @CheckForNull String bootFileName) {
        AbstractDhcpService.setBootParameters(reply, nextServerAddress, bootFileName);
    }

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