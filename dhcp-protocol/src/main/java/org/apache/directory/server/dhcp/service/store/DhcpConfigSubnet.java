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

import com.google.common.primitives.UnsignedBytes;
import java.net.InetAddress;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.anarres.dhcp.common.address.NetworkAddress;

/**
 * The definition of a Subnet.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DhcpConfigSubnet extends DhcpConfigElement {

    /** The subnet's address. */
    @Nonnull
    private final NetworkAddress network;
    /** The subnet's range: minimum address in range. */
    private InetAddress rangeMin;
    /** The subnet's range: maximum address in range. */
    private InetAddress rangeMax;

    // This will suppress PMD.EmptyCatchBlock warnings in this method
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public DhcpConfigSubnet(@Nonnull NetworkAddress network, @CheckForNull InetAddress rangeMin, @CheckForNull InetAddress rangeMax) {
        this.network = network;
        setRangeMin(rangeMin);
        setRangeMax(rangeMax);
    }

    @Nonnull
    public NetworkAddress getNetwork() {
        return network;
    }

    @CheckForNull
    public InetAddress getRangeMax() {
        return rangeMax;
    }

    public void setRangeMax(InetAddress rangeMax) {
        this.rangeMax = rangeMax;
    }

    @CheckForNull
    public InetAddress getRangeMin() {
        return rangeMin;
    }

    public void setRangeMin(InetAddress rangeMin) {
        this.rangeMin = rangeMin;
    }

    /**
     * Check whether the given address resides within this subnet.
     *
     * @param address
     * @return boolean
     */
    public boolean contains(@Nonnull InetAddress address) {
        return network.contains(address);
    }

    /**
     * Check whether the specified address is within the range for this subnet.
     *
     * @param address
     * @return boolean
     */
    public boolean isInRange(@Nonnull InetAddress address) {
        if (!contains(address))
            return false;

        byte[] addressBytes = address.getAddress();

        if (null != rangeMin && arrayComp(addressBytes, rangeMin.getAddress()) < 0) {
            return false;
        }

        if (null != rangeMin && arrayComp(addressBytes, rangeMax.getAddress()) > 0) {
            return false;
        }

        return true;
    }

    private static int arrayComp(@Nonnull byte[] a1, @Nonnull byte[] a2) {
        return UnsignedBytes.lexicographicalComparator().compare(a1, a2);
    }
}
