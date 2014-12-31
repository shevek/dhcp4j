/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.messages;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author shevek
 */
public enum Dhcp6Parameters {

    SOL_MAX_DELAY(1, TimeUnit.SECONDS, "Max delay of first Solicit"),
    SOL_TIMEOUT(1, TimeUnit.SECONDS, "Initial Solicit timeout"),
    SOL_MAX_RT(120, TimeUnit.SECONDS, "Max Solicit timeout value"),
    REQ_TIMEOUT(1, TimeUnit.SECONDS, "Initial Request timeout"),
    REQ_MAX_RT(30, TimeUnit.SECONDS, "Max Request timeout value"),
    REQ_MAX_RC(10, "Max Request retry attempts"),
    CNF_MAX_DELAY(1, TimeUnit.SECONDS, "Max delay of first Confirm"),
    CNF_TIMEOUT(1, TimeUnit.SECONDS, "Initial Confirm timeout"),
    CNF_MAX_RT(4, TimeUnit.SECONDS, "Max Confirm timeout"),
    CNF_MAX_RD(10, TimeUnit.SECONDS, "Max Confirm duration"),
    REN_TIMEOUT(10, TimeUnit.SECONDS, "Initial Renew timeout"),
    REN_MAX_RT(600, TimeUnit.SECONDS, "Max Renew timeout value"),
    REB_TIMEOUT(10, TimeUnit.SECONDS, "Initial Rebind timeout"),
    REB_MAX_RT(600, TimeUnit.SECONDS, "Max Rebind timeout value"),
    INF_MAX_DELAY(1, TimeUnit.SECONDS, "Max delay of first Information-request"),
    INF_TIMEOUT(1, TimeUnit.SECONDS, "Initial Information-request timeout"),
    INF_MAX_RT(120, TimeUnit.SECONDS, "Max Information-request timeout value"),
    REL_TIMEOUT(1, TimeUnit.SECONDS, "Initial Release timeout"),
    REL_MAX_RC(5, "Max Release attempts"),
    DEC_TIMEOUT(1, TimeUnit.SECONDS, "Initial Decline timeout"),
    DEC_MAX_RC(5, "Max Decline attempts"),
    REC_TIMEOUT(2, TimeUnit.SECONDS, "Initial Reconfigure timeout"),
    REC_MAX_RC(8, "Max Reconfigure attempts"),
    HOP_COUNT_LIMIT(32, "Max hop count in a Relay-forward message");
    private final long value;
    private final String description;

    private Dhcp6Parameters(int value, TimeUnit unit, String description) {
        this(unit.toMillis(value), description);
    }

    private Dhcp6Parameters(long value, String description) {
        this.value = value;
        this.description = description;
    }

    public long getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + "(" + getValue() + ", " + getDescription() + ")";
    }
}
