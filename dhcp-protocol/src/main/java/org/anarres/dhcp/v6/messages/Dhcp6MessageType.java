/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.messages;

import com.google.common.primitives.UnsignedBytes;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * https://www.iana.org/assignments/dhcpv6-parameters/dhcpv6-parameters.xml
 *
 * @author shevek
 */
public enum Dhcp6MessageType {

    DHCP_UNRECOGNIZED(0),
    /** RFC3315 */
    DHCP_SOLICIT(1),
    /** RFC3315 */
    DHCP_ADVERTISE(2),
    /** RFC3315 */
    DHCP_REQUEST(3),
    /** RFC3315 */
    DHCP_CONFIRM(4),
    /** RFC3315 */
    DHCP_RENEW(5),
    /** RFC3315 */
    DHCP_REBIND(6),
    /** RFC3315 */
    DHCP_REPLY(7),
    /** RFC3315 */
    DHCP_RELEASE(8),
    /** RFC3315 */
    DHCP_DECLINE(9),
    /** RFC3315 */
    DHCP_RECONFIGURE(10),
    /** RFC3315 */
    DHCP_INFORMATION_REQUEST(11),
    /** RFC3315 */
    DHCP_RELAY_FORW(12),
    /** RFC3315 */
    DHCP_RELAY_REPL(13),
    /** RFC5007 */
    LEASEQUERY(14),
    /** RFC5007 */
    LEASEQUERY_REPLY(15),
    /** RFC5460 */
    LEASEQUERY_DONE(16),
    /** RFC5460 */
    LEASEQUERY_DATA(17),
    /** RFC6977 */
    RECONFIGURE_REQUEST(18),
    /** RFC6977 */
    RECONFIGURE_REPLY(19),
    /** RFC7341 */
    DHCPV4_QUERY(20),
    /** RFC7341 */
    DHCPV4_RESPONSE(21);
    private final byte code;

    private Dhcp6MessageType(@Nonnegative int code) {
        this.code = UnsignedBytes.checkedCast(code);
    }

    public byte getCode() {
        return code;
    }

    @Nonnull
    public static Dhcp6MessageType forTypeCode(byte type) {
        for (Dhcp6MessageType mt : Dhcp6MessageType.values())
            if (type == mt.getCode())
                return mt;
        return DHCP_UNRECOGNIZED;
    }
}
