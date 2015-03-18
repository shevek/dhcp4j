/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.directory.server.dhcp.io;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.AddressUtils;
import org.anarres.dhcp.common.address.InterfaceAddress;
import org.apache.directory.server.dhcp.DhcpException;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Works around the weakness with broadcast UDP in Java.
 *
 * @author shevek
 */
public class DhcpInterfaceManager {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpInterfaceManager.class);

    public static class ValidPredicate implements Predicate<NetworkInterface> {

        @Override
        public boolean apply(NetworkInterface iface) {
            try {
                if (iface == null) {
                    LOG.debug("Ignoring NetworkInterface: null!");
                    return false;
                }
                // Bridges claim to be down when no attached interfaces are up. :-(
                // if (false && !iface.isUp()) { LOG.debug("Ignoring NetworkInterface: Down: {}", iface); return false; }
                if (iface.isLoopback()) {
                    LOG.debug("Ignoring NetworkInterface: Loopback: {}", iface);
                    return false;
                }
                if (iface.isPointToPoint()) {
                    LOG.debug("Ignoring NetworkInterface: PointToPoint: {}", iface);
                    return false;
                }
                return true;
            } catch (SocketException e) {
                LOG.error("Failed to query " + iface, e);
                return false;
            }
        }
    }

    public static class NamedPredicate extends ValidPredicate {

        private final List<? extends String> names;

        public NamedPredicate(@Nonnull List<? extends String> names) {
            this.names = names;
        }

        public NamedPredicate(@Nonnull Iterable<? extends String> names) {
            this(Lists.newArrayList(names));
        }

        public NamedPredicate(@Nonnull String... names) {
            this(Arrays.asList(names));
        }

        @Override
        public boolean apply(NetworkInterface iface) {
            String name = iface.getName();
            if (!names.contains(name)) {
                LOG.debug("Ignoring NetworkInterface: Not included by name: {} in {}", name, names);
                return false;
            }
            return super.apply(iface);
        }
    }

    private static final InterfaceAddress[] EMPTY_INTERFACE_ADDRESS_ARRAY = new InterfaceAddress[0];

    private static class Dummy {

        private static final Dummy INSTANCE = new Dummy();

        private Dummy() {
        }
    }
    private final ConcurrentMap<InterfaceAddress, Dummy> interfaces = new ConcurrentHashMap<InterfaceAddress, Dummy>();
    // private final Multimap<NetworkInterface, InterfaceAddress> interfaces = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    @Nonnull
    public Set<? extends InterfaceAddress> getInterfaces() {
        return interfaces.keySet();
    }

    /**
     * If this returns an address, it is NOT the global address.
     */
    @CheckForNull
    public InterfaceAddress getResponseInterface(@Nonnull Object... objects) throws DhcpException {
        InetAddress address = DhcpInterfaceUtils.toInetAddress(objects);
        if (address != null) {
            InterfaceAddress iface = getInterface(address);
            if (iface != null)
                return iface;
        }
        return null;
    }

    /**
     * Returns an object which may be used to later decide on a specific server interface.
     */
    @CheckForNull
    public DhcpRequestContext newRequestContext(
            @Nonnull InetSocketAddress serviceAddress,
            @Nonnull InetSocketAddress remoteAddress,
            @Nonnull InetSocketAddress localAddress,
            @Nonnull DhcpMessage request) throws DhcpException {
        InetAddress address = DhcpInterfaceUtils.toInetAddress(request.getRelayAgentAddress(), localAddress, serviceAddress, remoteAddress, request);
        if (address == null)
            // We don't know the remote system's address. Let's see if getting a lease helps us at all.
            return new DhcpRequestContext(interfaces.keySet(), remoteAddress, localAddress);
        InterfaceAddress interfaceAddress = getInterface(address);
        if (interfaceAddress == null)
            // We know the remote system's address but we don't think we can talk to it.
            return null;
        // We know the remote system's address. The lease had better match.
        return new DhcpRequestContext(interfaceAddress, remoteAddress, localAddress);
    }

    @CheckForNull
    public InterfaceAddress getInterface(@Nonnull InetAddress address) {
        Preconditions.checkNotNull(address, "Address was null.");
        for (InterfaceAddress iface : interfaces.keySet()) {
            if (iface.isLocal(address))
                return iface;
        }
        return null;
    }

    public void addInterface(@Nonnull InterfaceAddress address) throws IOException, InterruptedException {
        LOG.debug("Adding InterfaceAddress: {}", address);
        interfaces.put(address, Dummy.INSTANCE);
    }

    public void addInterface(@Nonnull NetworkInterface iface) throws IOException, InterruptedException {
        for (java.net.InterfaceAddress address : iface.getInterfaceAddresses()) {
            if (!AddressUtils.isUnicastAddress(address.getAddress())) {
                LOG.debug("Ignoring InterfaceAddress: Not unicast: {}", address);
                continue;
            }
            if (!(address.getAddress() instanceof Inet4Address)) {
                LOG.debug("Ignoring InterfaceAddress: Not IPv4: {}", address);
                continue;
            }

            addInterface(new InterfaceAddress(address.getAddress(), address.getNetworkPrefixLength()));
        }
    }

    private void addInterfaces(@Nonnull Enumeration<? extends NetworkInterface> ifaces, @Nonnull Predicate<? super NetworkInterface> predicate) throws IOException, InterruptedException {
        for (NetworkInterface iface : Collections.list(ifaces)) {
            if (predicate.apply(iface)) {
                LOG.debug("Adding NetworkInterface: {}", iface);
                addInterface(iface);
            }
            addInterfaces(iface.getSubInterfaces(), predicate);
        }
    }

    public void addInterfaces(@Nonnull Predicate<? super NetworkInterface> predicate) throws IOException, InterruptedException {
        addInterfaces(NetworkInterface.getNetworkInterfaces(), predicate);
    }

    public void addDefaultInterfaces() throws IOException, InterruptedException {
        addInterfaces(new ValidPredicate());
    }

    public void start() throws IOException, InterruptedException {
        /*
         if (interfaces.isEmpty()) {
         LOG.warn("No interfaces configured; adding defaults.");
         addDefaultInterfaces();
         }
         */
    }

    public void stop() throws IOException, InterruptedException {
        // interfaces.clear();
    }
}
