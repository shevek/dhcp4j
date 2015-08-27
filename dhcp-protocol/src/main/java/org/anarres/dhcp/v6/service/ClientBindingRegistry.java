package org.anarres.dhcp.v6.service;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.anarres.dhcp.v6.options.DuidOption;

@ThreadSafe
public class ClientBindingRegistry {

    @GuardedBy("this")
    private final Map<DuidOption.Duid, ClientBindings> bindings = new HashMap<>();
    @GuardedBy("this")
    private final Set<InetAddress> allIps = new HashSet<>();

    public synchronized void add(final DuidOption.Duid clientId, final int iaid, final InetAddress ip) {
        if(!bindings.containsKey(clientId)) {
            bindings.put(clientId, new ClientBindings());
        }

        final ClientBindings clientBindings = bindings.get(clientId);
        clientBindings.add(new ClientBinding(iaid, ip));
        allIps.add(ip);
    }

    public synchronized void remove(final DuidOption.Duid clientId, final int iaid) {
        if(bindings.containsKey(clientId)) {
            final ClientBinding removed = bindings.get(clientId).remove(iaid);
            allIps.remove(removed.getIp());
        }
    }

    public boolean containsIp(final InetAddress ip) {
        return allIps.contains(ip);
    }

    public boolean contains(final DuidOption.Duid clientId, final int iaid) {
        return bindings.containsKey(clientId) && bindings.get(clientId).contains(iaid);
    }

    @Nullable
    public ClientBinding get(final DuidOption.Duid clientId, final int iaid) {
        return bindings.get(clientId) == null ? null : bindings.get(clientId).get(iaid);
    }



    public static class ClientBinding {

        private final int iaId;
        private InetAddress ip;

        public ClientBinding(final int iaId, @Nonnull final InetAddress ip) {
            this.iaId = iaId;
            this.ip = ip;
        }

        public int getIaId() {
            return iaId;
        }

        public InetAddress getIp() {
            return ip;
        }

        @Override public String toString() {
            return "ClientBinding{" +
                "iaId=" + iaId +
                ", ip=" + ip +
                '}';
        }

        @Override public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final ClientBinding that = (ClientBinding) o;
            return Objects.equals(iaId, that.iaId);
        }

        @Override public int hashCode() {
            return Objects.hash(iaId);
        }
    }

    private static class ClientBindings {
        private final Map<Integer, ClientBinding> bindings;

        private ClientBindings() {
            this.bindings = new HashMap<>();
        }

        ClientBinding get(int iaid) {
            return bindings.get(iaid);
        }

        void add(ClientBinding binding) {
            bindings.put(binding.iaId, binding);
        }

        ClientBinding remove(int iaid) {
            return bindings.remove(iaid);
        }

        boolean contains(final int iaid) {
            return bindings.containsKey(iaid);
        }

    }
}
