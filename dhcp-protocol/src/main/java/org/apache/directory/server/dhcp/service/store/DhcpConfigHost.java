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

import java.net.InetAddress;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.messages.HardwareAddress;

/**
 * The definition of a host.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DhcpConfigHost extends DhcpConfigElement {

    private final String name;

    private final HardwareAddress hardwareAddress;

    /**
     * The host's fixed address. May be <code>null</code>.
     */
    private final InetAddress clientAddress;

    public DhcpConfigHost(String name, HardwareAddress hardwareAddress, InetAddress clientAddress) {
        this.name = name;
        this.hardwareAddress = hardwareAddress;
        this.clientAddress = clientAddress;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public HardwareAddress getHardwareAddress() {
        return hardwareAddress;
    }

    @CheckForNull
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    @Override
    public String toString() {
        return getName() + "(" + getHardwareAddress() + ": " + getClientAddress() + ")";
    }

}
