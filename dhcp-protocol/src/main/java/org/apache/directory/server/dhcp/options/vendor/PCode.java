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
 * <a href="http://tools.ietf.org/html/rfc4833">RFC 4833</a> defines new timezone options for DHCPv4. Use PCode class
 * together with {@link TCode} instead of deprecated {@link org.apache.directory.server.dhcp.options.vendor.TimeOffset
 * TimeOffset} . <br>
 * <br>
 * POSIX provides a standard for how to express timezone information in a character string. However, for accuracy over
 * longer periods that involve daylight- saving rule changes or other irregular changes, a more detailed mechanism is
 * necessary. <br>
 * <br>
 * The <a href="http://www.twinsun.com/tz/tz-link.htm">TZ Database</a> that is used in many operating systems provides
 * backwards consistency and accuracy for almost all real-world locations since 1970. The TZ database also attempts to
 * provide a stable set of human readable timezone identifiers. <br>
 * <br>
 * TZ POSIX string is a string suitable for the TZ variable as specified by section 8.3 of POSIX standard, with the
 * exception that a string may not begin with a colon (":"). This string is NOT terminated by an ASCII NULL. Here is an
 * example: <br>
 * <br>
 * EST5EDT4,M3.2.0/02:00,M11.1.0/02:00
 *
 * @author marekgr
 * @see <a href="http://tools.ietf.org/html/rfc4833#section-4">RFC 4833 (section 4)</a>
 */
public class PCode extends StringOption {

    private static final short TAG = 100;

    public PCode() {
    }

    /**
     * @param tZPOSIXString
     *            TZ POSIX String
     */
    public PCode(String tZPOSIXString) {
        setStringValue(tZPOSIXString);
    }

    /*
     * @see org.apache.directory.server.dhcp.options.DhcpOption#getTag()
     */
    @Override
    public byte getTag() {
        return TAG;
    }
}
