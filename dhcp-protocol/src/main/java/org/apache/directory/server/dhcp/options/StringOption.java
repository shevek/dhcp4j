/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *
 */
package org.apache.directory.server.dhcp.options;

import com.google.common.base.Charsets;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * The Dynamic Host Configuration Protocol (DHCP) provides a framework for
 * passing configuration information to hosts on a TCP/IP network. Configuration
 * parameters and other control information are carried in tagged data items
 * that are stored in the 'options' field of the DHCP message. The data items
 * themselves are also called "options." 
 * 
 * This abstract base class is for options
 * that carry a string.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class StringOption extends DhcpOption {

    @Nonnull
    protected byte[] getStringData() {
        return getData();
    }

    protected void setStringData(@Nonnull byte[] data) {
        setData(data);
    }

    @Nonnull
    public String getString() {
        return new String(getStringData(), Charsets.ISO_8859_1);
    }

    public void setString(@Nonnull String string) {
        setStringData(string.getBytes(Charsets.ISO_8859_1));
    }

    @Override
    protected String toStringData() throws DhcpException {
        return getString();
    }

}
