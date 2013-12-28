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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The Dynamic Host Configuration Protocol (DHCP) provides a framework
 * for passing configuration information to hosts on a TCP/IP network.  
 * Configuration parameters and other control information are carried in
 * tagged data items that are stored in the 'options' field of the DHCP
 * message.  The data items themselves are also called "options."
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class OptionsField implements Iterable<DhcpOption> {

    /**
     * A map of option code (Integer)->DhcpOption. FIXME: use IntHashtable from
     * commons collections
     */
    private final Map<Byte, DhcpOption> options = new HashMap<Byte, DhcpOption>();

    public boolean isEmpty() {
        return options.isEmpty();
    }

    @Override
    public Iterator<DhcpOption> iterator() {
        return options.values().iterator();
    }

    /**
     * Return the (first) DHCP option matching a given option class or
     * <code>null</code> of the option isn't set.
     * 
     * @param optionClass
     */
    @CheckForNull
    public <T extends DhcpOption> T get(@Nonnull Class<T> type) {
        DhcpOptionsRegistry registry = DhcpOptionsRegistry.getInstance();
        DhcpOption option = get(registry.getOptionTag(type));
        if (option == null)
            return null;
        if (type.isInstance(option))
            return type.cast(option);
        T impl = DhcpOptionsRegistry.newInstance(type);
        impl.setData(option.getData());
        return impl;
    }

    /**
     * Return the (first) DHCP option matching a given tag or <code>null</code>
     * of the option isn't set.
     * 
     * @param tag
     */
    @CheckForNull
    public DhcpOption get(byte tag) {
        return options.get(tag);
    }

    public void add(@Nonnull DhcpOption option) {
        options.put(option.getTag(), option);
    }

    /**
     * Merge the options from the given options field into my options. Existing
     * options are replaced by the ones from the supplied options field.
     * 
     * @param options
     */
    public void addAll(@CheckForNull OptionsField options) {
        if (options == null)
            return;
        for (DhcpOption option : options)
            add(option);
    }

    /**
     * Remove instances of the given option class.
     * 
     * @param type
     */
    public void remove(@Nonnull Class<? extends DhcpOption> type) {
        DhcpOptionsRegistry registry = DhcpOptionsRegistry.getInstance();
        remove(registry.getOptionTag(type));
    }

    /**
     * Remove options matching the given tag
     * 
     * @param tag
     */
    public void remove(byte tag) {
        options.remove(tag);
    }

    /**
     * @see Map#clear()
     */
    public void clear() {
        options.clear();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + options.values() + ")";
    }
}
