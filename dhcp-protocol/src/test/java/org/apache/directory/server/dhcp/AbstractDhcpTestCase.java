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
package org.apache.directory.server.dhcp;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractDhcpTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDhcpTestCase.class);

    protected void print(@Nonnull DhcpMessage message) {
        LOG.debug(String.valueOf(message.getMessageType()));
        LOG.debug(String.valueOf(message.getHardwareAddress()));
        LOG.debug(String.valueOf(message.getTransactionId()));
        LOG.debug(String.valueOf(message.getSeconds()));
        LOG.debug(String.valueOf(message.getFlags()));
        LOG.debug(String.valueOf(message.getCurrentClientAddress()));
        LOG.debug(String.valueOf(message.getAssignedClientAddress()));
        LOG.debug(String.valueOf(message.getNextServerAddress()));
        LOG.debug(String.valueOf(message.getRelayAgentAddress()));
        LOG.debug(String.valueOf(message.getServerHostname()));
        LOG.debug(String.valueOf(message.getBootFileName()));
        LOG.debug(String.valueOf(message.getOptions()));
    }

    @Nonnull
    protected ByteBuffer getByteBufferFromFile(@Nonnull String file) throws IOException {
        URL resource = Resources.getResource(getClass(), file);
        byte[] data = Resources.toByteArray(resource);
        return ByteBuffer.wrap(data);
    }
}
