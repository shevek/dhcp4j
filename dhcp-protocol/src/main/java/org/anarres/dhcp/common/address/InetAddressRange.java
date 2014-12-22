/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.common.address;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import javax.annotation.Nonnull;

/**
 * A range of {@link InetAddress InetAddresses} with inclusive endpoints.
 *
 * @author shevek
 */
public class InetAddressRange {

    private final InetAddress start;
    private final InetAddress end;

    public InetAddressRange(@Nonnull InetAddress start, @Nonnull InetAddress end) {
        this.start = start;
        this.end = end;
    }

    /** Returns the start of the range, inclusive. */
    @Nonnull
    public InetAddress getStart() {
        return start;
    }

    /** Returns the end of the range, inclusive. */
    @Nonnull
    public InetAddress getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return InetAddresses.toAddrString(getStart()) + "-" + InetAddresses.toAddrString(getEnd());
    }
}
