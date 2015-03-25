/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.manager;

import java.net.InetAddress;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.io.DhcpRequestContext;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.service.AbstractDhcpReplyFactory;
import org.apache.directory.server.dhcp.service.store.Lease;

/**
 * A base class for lease managers which allocate addresses from a pool.
 *
 * @author shevek
 */
public abstract class AbstractLeaseManager extends AbstractDhcpReplyFactory implements LeaseManager {

    /** Ignores DECLINE messages by default. */
    @Override
    public boolean leaseDecline(
            DhcpRequestContext context,
            DhcpMessage request,
            InetAddress clientAddress) throws DhcpException {
        return false;
    }

    /** Ignores RELEASE messages by default. */
    @Override
    public boolean leaseRelease(
            DhcpRequestContext context,
            DhcpMessage request,
            InetAddress clientAddress) throws DhcpException {
        return false;
    }

    /** A factory method for a new reply message. */
    @Nonnull
    public static DhcpMessage newReply(
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type,
            @Nonnull Lease lease) {
        long leaseTimeSecs = lease.getExpires() - System.currentTimeMillis() / 1000;
        DhcpMessage reply = newReplyAck(request, type, lease.getClientAddress(), leaseTimeSecs);
        setBootParameters(reply, lease.getNextServerAddress(), null);
        reply.getOptions().addAll(lease.getOptions());
        return reply;
    }
}
