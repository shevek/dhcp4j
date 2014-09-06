/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.messages;

import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public enum DhcpMessageType {

    DHCP_UNRECOGNIZED(-1),
    DHCP_SOLICIT(1),
    DHCP_ADVERTISE(2),
    DHCP_REQUEST(3),
    DHCP_CONFIRM(4),
    DHCP_RENEW(5),
    DHCP_REBIND(6),
    DHCP_REPLY(7),
    DHCP_RELEASE(8),
    DHCP_DECLINE(9),
    DHCP_RECONFIGURE(10),
    DHCP_INFORMATION_REQUEST(11),
    DHCP_RELAY_FORW(12),
    DHCP_RELAY_REPL(13);
    private final byte ordinal;

    private DhcpMessageType(int ordinal) {
        this.ordinal = (byte) ordinal;
    }

    public byte getCode() {
        return ordinal;
    }

    @Nonnull
    public static DhcpMessageType forTypeCode(byte type) {
        for (DhcpMessageType mt : DhcpMessageType.values())
            if (type == mt.getCode())
                return mt;
        return DHCP_UNRECOGNIZED;
    }
}
