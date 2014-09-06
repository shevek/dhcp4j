/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.service.store;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.directory.server.dhcp.DhcpException;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.messages.MessageType;
import org.apache.directory.server.dhcp.options.vendor.HostName;
import org.apache.directory.server.dhcp.service.manager.AbstractLeaseManager;

/**
 *
 * @author shevek
 */
public class LdapStoreLeaseManager extends AbstractLeaseManager {

    private final LeaseTimeRange TTL_OFFER = new LeaseTimeRange(60, 600, 600);
    private final LeaseTimeRange TTL_LEASE = new LeaseTimeRange(60, 3600, 36000);
    private final InitialDirContext context;

    public LdapStoreLeaseManager() throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // env.put(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, "ldap://localhost:389/dc=tcat,dc=test");
        this.context = new InitialDirContext(env);
    }

    /**
     * @param hardwareAddress
     * @return Host
     * @throws DhcpException
     */
    @CheckForNull
    protected DhcpConfigHost getHost(@Nonnull HardwareAddress hardwareAddress) throws DhcpException {
        try {
            String filter = "(&(objectclass=ipHost)(objectclass=ieee802Device)(macaddress={0}))";
            SearchControls sc = new SearchControls();
            sc.setCountLimit(1);
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> ne = context.search("", filter, new Object[]{hardwareAddress.toString()}, sc);

            if (ne.hasMoreElements()) {
                SearchResult sr = ne.next();
                Attributes att = sr.getAttributes();
                Attribute ipHostNumberAttribute = att.get("iphostnumber");

                if (ipHostNumberAttribute != null) {
                    InetAddress clientAddress = InetAddress.getByName((String) ipHostNumberAttribute.get());
                    Attribute cnAttribute = att.get("cn");

                    return new DhcpConfigHost(
                            cnAttribute != null ? (String) cnAttribute.get() : "unknown",
                            hardwareAddress,
                            clientAddress);
                }
            }
        } catch (NamingException e) {
            throw new DhcpException("Can't lookup lease", e);
        } catch (UnknownHostException e) {
            throw new DhcpException("Can't lookup lease", e);
        }

        return null;
    }

    @Nonnull
    protected DhcpMessage newReply(
            @Nonnull InterfaceAddress localAddress,
            @Nonnull DhcpMessage request,
            @Nonnull MessageType type,
            @Nonnull DhcpConfigHost host,
            @Nonnegative long leaseTimeSecs
    ) {
        DhcpMessage reply = newReplyAck(localAddress, request, type, host.getClientAddress(), leaseTimeSecs);
        reply.getOptions().add(new HostName(host.getName()));
        reply.getOptions().addAll(host.getOptions());
        return reply;
    }

    @Override
    public DhcpMessage leaseOffer(InterfaceAddress localAddress, DhcpMessage request, InetAddress remoteAddress, InetAddress clientRequestedAddress, long clientRequestedExpirySecs) throws DhcpException {
        DhcpConfigHost host = getHost(request.getHardwareAddress());
        if (host == null)
            return null;
        if (AddressUtils.isZeroAddress(host.getClientAddress()))
            return null;
        long leaseTimeSecs = getLeaseTime(TTL_OFFER, clientRequestedExpirySecs);
        return newReply(localAddress, request, MessageType.DHCPACK, host, leaseTimeSecs);
    }

    @Override
    public DhcpMessage leaseRequest(InterfaceAddress localAddress, DhcpMessage request, InetAddress clientRequestedAddress, long clientRequestedExpirySecs) throws DhcpException {
        DhcpConfigHost host = getHost(request.getHardwareAddress());
        if (host == null)
            return null;
        if (AddressUtils.isZeroAddress(host.getClientAddress()))
            return null;
        long leaseTimeSecs = getLeaseTime(TTL_LEASE, clientRequestedExpirySecs);
        return newReply(localAddress, request, MessageType.DHCPACK, host, leaseTimeSecs);
    }

    @Override
    public boolean leaseDecline(InterfaceAddress localAddress, DhcpMessage request, InetAddress clientAddress) throws DhcpException {
        return true;
    }

    @Override
    public boolean leaseRelease(InterfaceAddress localAddress, DhcpMessage request, InetAddress clientAddress) throws DhcpException {
        return false;
    }

}
