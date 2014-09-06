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

import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.CheckForSigned;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.DhcpException;

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

    @CheckForNull
    public byte[] getOption(@Nonnull Class<? extends DhcpOption> type) {
        DhcpOption option = get(type);
        if (option == null)
            return null;
        return option.getData();
    }

    public void setOption(@Nonnull Class<? extends DhcpOption> type, @Nonnull byte[] value) {
        DhcpOption option = DhcpOptionsRegistry.newInstance(type);
        option.setData(value);
        add(option);
    }

    public int getByteOption(@Nonnull Class<? extends ByteOption> type, int dflt) {
        ByteOption option = get(type);
        if (option == null)
            return dflt;
        return option.getByteValue();
    }

    @CheckForSigned
    public int getByteOption(@Nonnull Class<? extends ByteOption> type) {
        return getByteOption(type, -1);
    }

    public void setByteOption(@Nonnull Class<? extends ByteOption> type, @Nonnegative int value) {
        ByteOption option = DhcpOptionsRegistry.newInstance(type);
        option.setByteValue(value);
        add(option);
    }

    public int getShortOption(@Nonnull Class<? extends ShortOption> type, int dflt) {
        ShortOption option = get(type);
        if (option == null)
            return dflt;
        return option.getShortValue();
    }

    @CheckForSigned
    public int getShortOption(@Nonnull Class<? extends ShortOption> type) {
        return getShortOption(type, -1);
    }

    public void setShortOption(@Nonnull Class<? extends ShortOption> type, @Nonnegative int value) {
        ShortOption option = DhcpOptionsRegistry.newInstance(type);
        option.setShortValue(value);
        add(option);
    }

    public long getIntOption(@Nonnull Class<? extends IntOption> type, int dflt) {
        IntOption option = get(type);
        if (option == null)
            return dflt;
        return option.getIntValue();
    }

    @CheckForSigned
    public long getIntOption(@Nonnull Class<? extends IntOption> type) {
        return getIntOption(type, -1);
    }

    public void setIntOption(@Nonnull Class<? extends IntOption> type, @Nonnegative long value) {
        IntOption option = DhcpOptionsRegistry.newInstance(type);
        option.setIntValue(value);
        add(option);
    }

    @CheckForNull
    public String getStringOption(@Nonnull Class<? extends StringOption> type) {
        StringOption option = get(type);
        if (option == null)
            return null;
        return option.getStringValue();
    }

    public void setStringOption(@Nonnull Class<? extends StringOption> type, @Nonnull String value) {
        StringOption option = DhcpOptionsRegistry.newInstance(type);
        option.setStringValue(value);
        add(option);
    }

    @CheckForNull
    public InetAddress getAddressOption(@Nonnull Class<? extends AddressOption> type) throws DhcpException {
        AddressOption option = get(type);
        if (option == null)
            return null;
        return option.getAddress();
    }

    public void setAddressOption(@Nonnull Class<? extends AddressOption> type, @Nonnull InetAddress value) {
        AddressOption option = DhcpOptionsRegistry.newInstance(type);
        option.setAddress(value);
        add(option);
    }

    public void setAddressOption(@Nonnull Class<? extends AddressOption> type, @Nonnull String value) {
        setAddressOption(type, InetAddresses.forString(value));
    }

    @CheckForNull
    public Inet4Address[] getAddressListOption(@Nonnull Class<? extends AddressListOption> type) throws DhcpException {
        AddressListOption option = get(type);
        if (option == null)
            return null;
        return option.getAddresses();
    }

    @CheckForNull
    public void setAddressListOption(@Nonnull Class<? extends AddressListOption> type, Inet4Address... value) {
        AddressListOption option = DhcpOptionsRegistry.newInstance(type);
        option.setAddresses(value);
        add(option);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + options.values() + ")";
    }
}
