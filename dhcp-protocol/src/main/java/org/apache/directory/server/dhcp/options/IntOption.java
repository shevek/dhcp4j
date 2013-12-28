/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package org.apache.directory.server.dhcp.options;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedInts;
import javax.annotation.Nonnegative;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * The Dynamic Host Configuration Protocol (DHCP) provides a framework for
 * passing configuration information to hosts on a TCP/IP network. Configuration
 * parameters and other control information are carried in tagged data items
 * that are stored in the 'options' field of the DHCP message. The data items
 * themselves are also called "options."
 * 
 * This abstract base class is for options that carry an int value (32 bit).
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class IntOption extends DhcpOption {

    @Nonnegative
    public long getIntValue() {
        return UnsignedInts.toLong(Ints.fromByteArray(getData()));
    }

    public void setIntValue(@Nonnegative long value) {
        Preconditions.checkArgument(value >> Integer.SIZE == 0, "out of range: %s", value);
        setData(Ints.toByteArray((int) value));
    }

    @Override
    public void validate() throws DhcpException {
        super.validate();
        if (getData().length != 4)
            throw new DhcpException("Expected exactly 4 data bytes in " + this);
    }

    @Override
    protected String toStringData() {
        return String.valueOf(getIntValue());
    }

}
