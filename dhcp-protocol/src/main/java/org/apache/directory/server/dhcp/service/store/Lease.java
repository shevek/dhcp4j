/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.dhcp.service.store;

import com.google.common.base.Preconditions;
import java.net.InetAddress;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.options.OptionsField;

/**
 * Leases represent a temporary assignment of an IP address to a DHCP client.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Lease {

    public static enum LeaseState {

        /** Lease state: newly created */
        NEW,
        /** Lease state: offered to client */
        OFFERED,
        /** Lease state: active - assigned to client */
        ACTIVE,
        /** Lease state: released by client */
        RELEASED,
        /** Lease state: expired */
        EXPIRED;
    }

    /**
     * The client's hardware address.
     */
    private final HardwareAddress hardwareAddress;
    /**
     * The assigned client address.
     */
    private final InetAddress clientAddress;
    private LeaseState state = LeaseState.NEW;
    /**
     * The next-server (boot-server) address.
     */
    private InetAddress nextServerAddress;
    /**
     * The DhcpOptions to provide to the client along with the lease.
     */
    private final OptionsField options = new OptionsField();
    private long acquired = -1;
    private long expires = -1;

    public Lease(@Nonnull HardwareAddress hardwareAddress, @Nonnull InetAddress clientAddress) {
        this.hardwareAddress = Preconditions.checkNotNull(hardwareAddress, "Hardware address was null.");
        this.clientAddress = Preconditions.checkNotNull(clientAddress, "Client address was null.");
    }

    @Nonnull
    public HardwareAddress getHardwareAddress() {
        return hardwareAddress;
    }

    @Nonnull
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    /**
     * @return InetAddress
     */
    public InetAddress getNextServerAddress() {
        return nextServerAddress;
    }

    public void setNextServerAddress(InetAddress nextServerAddress) {
        this.nextServerAddress = nextServerAddress;
    }

    /**
     * @return OptionsField
     */
    public OptionsField getOptions() {
        return options;
    }

    /**
     * @return int
     */
    public LeaseState getState() {
        return state;
    }

    /**
     * @param state
     */
    public void setState(LeaseState state) {
        this.state = state;
    }

    /** Returns the time in seconds since the epoch at which this lease was acquired. */
    public long getAcquired() {
        return acquired;
    }

    /** Sets the time in seconds since the epoch at which this lease was acquired. */
    public void setAcquired(long acquired) {
        this.acquired = acquired;
    }

    /** Returns the time in seconds since the epoch at which this lease expires. */
    public long getExpires() {
        return expires;
    }

    /** Sets the time in seconds since the epoch at which this lease expires. */
    public void setExpires(long expires) {
        this.expires = expires;
    }

}
