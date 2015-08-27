/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.6
 *
 * @author marosmars
 */
public class IaAddressOption extends Dhcp6Option {

    private static final short TAG = 5;

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
        Preconditions.checkArgument(address instanceof Inet6Address);
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.put(address.getAddress());
    }

    public void setPreferredLifetime(int preferredLifetime) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(16, preferredLifetime);
    }

    public int getPreferredLifetime() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(16);
    }

    public int getValidLifetime() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(20);
    }

    public void setValidLifetime(int validLifetime) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(20, validLifetime);
    }

    public void setOptions(@Nonnull final ByteBuffer options) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        options.position(0);
        buf.position(12);
        buf.put(options);
    }

    public void setOptions(@Nonnull final Dhcp6Options options) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.position(12);
        Dhcp6MessageEncoder.getInstance().encode(buf, options);
    }

    // FIXME extrace base abstract class for option based options e.g. IANA IATA

    @Nonnull
    public Dhcp6Options getOptions() throws DhcpException {
        Dhcp6MessageDecoder decoder = Dhcp6MessageDecoder.getInstance();
        return decoder.decodeOptions(getOptionsRaw());
    }

    @Nonnull
    private ByteBuffer getOptionsRaw() {
        byte[] data = getData();
        return ByteBuffer.wrap(data, 24, data.length - 24);
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        values.append("IP:");
        values.append(getIp());
        values.append(", Preferred-lifetime:");
        values.append(getPreferredLifetime());
        values.append(", Valid-lifetime:");
        values.append(getValidLifetime());
        values.append(", ");
        try {
            values.append(getOptions().toString());
        } catch (DhcpException e) {
            values.append("options:[");
            values.append(toStringDataFallback(getOptionsRaw().array()));
            values.append("]");
        }

        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }

    public static IaAddressOption create(@Nonnull final InetAddress ip, final int preferredLifetime, final int validaLifetime, final Optional<Dhcp6Options> options) {
        final IaAddressOption iaNaOption = new IaAddressOption();
        int length = 24;

        ByteBuffer encodedOptions = null;
        if(options.isPresent() && !options.get().isEmpty()) {
            encodedOptions = Dhcp6MessageEncoder.getInstance().encode(options.get());
            length += encodedOptions.limit();
        }

        iaNaOption.setData(new byte[length]);
        iaNaOption.setIp(ip);
        iaNaOption.setPreferredLifetime(preferredLifetime);
        iaNaOption.setValidLifetime(validaLifetime);
        if (encodedOptions != null) {
            iaNaOption.setOptions(encodedOptions);
        }
        return iaNaOption;
    }
}
