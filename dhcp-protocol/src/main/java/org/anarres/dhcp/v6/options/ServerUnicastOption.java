/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.base.Preconditions;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.12
 *
 * @author marosmars
 */
public class ServerUnicastOption extends Dhcp6Option {

    private static final short TAG = 12;

    @Override
    public short getTag() {
        return TAG;
    }

    @Nonnull
    public InetAddress getIp() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        final byte[] dst = new byte[16];
        buf.get(dst, 0, 16);

        try {
            return Inet6Address.getByAddress(dst);
        } catch (UnknownHostException e) {
            // This can happen only if the IP byte array is of illegal length
            throw new IllegalStateException("Illegal IP address", e);
        }
    }

    public void setIp(@Nonnull final InetAddress address) {
        Preconditions.checkArgument(address instanceof Inet6Address, "Ipv4 detected: %s", address);
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.put(address.getAddress());
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        values.append("IP:");
        values.append(getIp());
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }

    public static ServerUnicastOption create(@Nonnull final InetAddress ip) {
        final ServerUnicastOption iaNaOption = new ServerUnicastOption();
        iaNaOption.setData(new byte[16]);
        iaNaOption.setIp(ip);
        return iaNaOption;
    }
}
