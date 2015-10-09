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
package org.apache.directory.server.dhcp.options.vendor;

import org.apache.directory.server.dhcp.options.StringOption;

/**
 * <a href="http://tools.ietf.org/html/rfc4833">RFC 4833</a> defines new timezone options for DHCPv4. Use TCode class
 * together with {@link PCode} instead of deprecated {@link TimeOffset}. <br>
 * <br>
 * POSIX provides a standard for how to express timezone information in a character string. However, for accuracy over
 * longer periods that involve daylight- saving rule changes or other irregular changes, a more detailed mechanism is
 * necessary. <br>
 * <br>
 * The <a href="http://www.twinsun.com/tz/tz-link.htm">TZ Database</a> that is used in many operating systems provides
 * backwards consistency and accuracy for almost all real-world locations since 1970. The TZ database also attempts to
 * provide a stable set of human readable timezone identifiers. <br>
 * <br>
 * TZ Name is the name of a Zone entry in the database commonly referred to as the TZ database. Specifically, in the
 * database's textual form, the string refers to the name field of a zone line. In order for this option to be useful,
 * the client must already have a copy of the database. This string is NOT terminated with an ASCII NULL.
 *
 * An example string is Europe/Zurich.
 *
 * @author marekgr
 * @see <a href="http://tools.ietf.org/html/rfc4833#section-5">RFC 4833 (section 5)</a>
 */
public class TCode extends StringOption {

    private static final short TAG = 101;

    public TCode() {
    }

    /**
     * @param tZDatabaseString
     *            name of Zone entry in TZ Database
     */
    public TCode(String tZDatabaseString) {
        setStringValue(tZDatabaseString);
    }

    /*
     * @see org.apache.directory.server.dhcp.options.DhcpOption#getTag()
     */
    @Override
    public byte getTag() {
        return TAG;
    }
}
