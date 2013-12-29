/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import java.net.InetAddress;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.address.InterfaceAddress;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.service.AbstractDhcpService;

/**
 *
 * @author shevek
 */
public abstract class AbstractLeaseManager implements LeaseManager {

    public static class LeaseTimeRange {

        public long minLeaseTime = 60;
        public long defaultLeaseTime = 60;
        public long maxLeaseTime = 60;

        public LeaseTimeRange() {
        }

        public LeaseTimeRange(long minLeaseTime, long defaultLeaseTime, long maxLeaseTime) {
            this.minLeaseTime = minLeaseTime;
            this.defaultLeaseTime = defaultLeaseTime;
            this.maxLeaseTime = maxLeaseTime;
        }

    }

    public static long getLeaseTime(@Nonnull LeaseTimeRange leaseTimeSecs, long requestedLeaseTimeSecs) {
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
            @Nonnull InterfaceAddress localAddress,
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type) {
        return AbstractDhcpService.newReply(localAddress, request, type);
    }

    @Nonnull
    public static DhcpMessage newReply(
            @Nonnull InterfaceAddress localAddress,
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type,
            @Nonnegative long leaseTimeSecs,
            @CheckForNull InetAddress assignedClientAddress,
            @CheckForNull InetAddress nextServerAddress,
            @CheckForNull String bootFileName) {
        return AbstractDhcpService.newReply(localAddress, request, type, leaseTimeSecs, assignedClientAddress, nextServerAddress, bootFileName);
    }

}
