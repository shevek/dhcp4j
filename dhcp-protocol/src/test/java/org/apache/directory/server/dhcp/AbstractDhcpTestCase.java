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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.server.dhcp.messages.DhcpMessage;

/**
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractDhcpTestCase {

    protected static final int MINIMUM_DHCP_DATAGRAM_SIZE = 576;
    private static final Log LOG = LogFactory.getLog(AbstractDhcpTestCase.class);

    protected void print(DhcpMessage message) {
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
    }

    protected ByteBuffer getByteBufferFromFile(String file) throws IOException {
        InputStream is = getClass().getResourceAsStream(file);

        byte[] bytes = new byte[MINIMUM_DHCP_DATAGRAM_SIZE];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        is.close();
        return ByteBuffer.wrap(bytes);
    }
}
