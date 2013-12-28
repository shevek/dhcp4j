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

import com.google.common.primitives.UnsignedBytes;
import javax.annotation.Nonnegative;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * The Dynamic Host Configuration Protocol (DHCP) provides a framework for
 * passing configuration information to hosts on a TCP/IP network. Configuration
 * parameters and other control information are carried in tagged data items
 * that are stored in the 'options' field of the DHCP message. The data items
 * themselves are also called "options."
 * 
 * This abstract base class is for options that carry an unsigned byte value
 * (8 bit).
 *  
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * 
 */
public abstract class ByteOption extends DhcpOption {

    @Nonnegative
    public int getByteValue() {
        return getData()[0] & 0xFF;
    }

    public void setByteValue(@Nonnegative int value) {
        setData(new byte[]{UnsignedBytes.checkedCast(value)});
    }

    @Override
    public void validate() throws DhcpException {
        super.validate();
        if (getData().length != 1)
            throw new DhcpException("Expected exactly 1 data byte in " + this);
    }
}
