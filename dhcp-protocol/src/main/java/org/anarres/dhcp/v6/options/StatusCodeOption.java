/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.base.Charsets;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.13
 * https://tools.ietf.org/html/rfc3315#section-24.4
 *
 * @author marosmars
 */
public class StatusCodeOption extends Dhcp6Option {

    private static final short TAG = 13;

    /**
     * Success
     */
    public static final short SUCCESS = 0;

    /**
     * Failure, reason unspecified; this status code is sent by either a client or a server
     * to indicate a failure not explicitly specified in this document.
     */
    public static final short UNSPEC_FAIL = 1;

    /**
     * Server has no addresses available to assign to the IA(s).
     */
    public static final short NO_ADDRS_AVAIL = 2;

    /**
     * Client record (binding) unavailable.
     */
    public static final short NO_BINDING = 3;

    /**
     * The prefix for the address is not appropriate for the link to which the client is attached.
     */
    public static final short NOT_ON_LINK = 4;

    /**
     * Sent by a server to a client to force the client to send messages to the server.
     * using the All_DHCP_Relay_Agents_and_Servers address.
     */
    public static final short USE_MULTICAST = 5;


    @Override
    public short getTag() {
        return TAG;
    }

    public short getStatusCode() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getShort();
    }

    public void setStatusCode(short statusCode) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putShort(0, statusCode);
    }

    @Nonnull public String getMessage() {
        return new String(getData(), 2, getData().length - 2, Charsets.UTF_8);
    }

    public void setMessage(@Nonnull String message) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.position(2);
        buf.put(message.getBytes(Charsets.UTF_8));
    }

    public void setPreference(byte preference) {
        setData(new byte[]{preference});
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        values.append("Code:");
        values.append(getStatusCode());
        values.append(", Message:");
        values.append(getMessage());
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }

    @Nonnull
    public static StatusCodeOption create(final short statusCode, @Nonnull final String message) {
        final StatusCodeOption option = new StatusCodeOption();
        option.setData(new byte[2 + message.getBytes(Charsets.UTF_8).length]);
        option.setStatusCode(statusCode);
        option.setMessage(message);
        return option;
    }

    @Nonnull
    public static Dhcp6Option create(final short statusCode) {
        final StatusCodeOption option = new StatusCodeOption();
        option.setData(new byte[2]);
        option.setStatusCode(statusCode);
        return option;
    }
}
