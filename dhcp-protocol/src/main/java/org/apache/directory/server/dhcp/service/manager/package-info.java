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
/**
 * A {@link org.apache.directory.server.dhcp.service.manager.LeaseManager} is easier to write than a {@link org.apache.directory.server.dhcp.service.DhcpService}.
 * 
 * Using {@link org.apache.directory.server.dhcp.service.manager.LeaseManagerDhcpService} is probably a good idea.
 * However, the default implementations in this package are a bit of a shambles,
 * and you will be better off writing something simple and single-purpose
 * for your application. Most of the critical stuff is in LeaseManagerDhcpService.
 */
package org.apache.directory.server.dhcp.service.manager;