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
package org.anarres.dhcp.v6.options;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Dhcp6Options implements Iterable<Dhcp6Option> {

    private final ListMultimap<Short, Dhcp6Option> options = LinkedListMultimap.create();

    public boolean isEmpty() {
        return options.isEmpty();
    }

    @Override
    public Iterator<Dhcp6Option> iterator() {
        return options.values().iterator();
    }

    @Nonnull
    public <T extends Dhcp6Option> Iterable<T> getAll(@Nonnull final Class<T> type) {
        Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();
        final Iterable<Dhcp6Option> option = get(registry.getOptionTag(type));

        return Iterables.transform(option, new Function<Dhcp6Option, T>() {
            @Nullable @Override public T apply(final Dhcp6Option input) {
                if (type.isInstance(input)) {
                    return type.cast(input);
                }
                T impl = Dhcp6OptionsRegistry.newInstance(type);
                impl.setData(input.getData());
                return impl;
            }
        });
    }

    @CheckForNull
    public <T extends Dhcp6Option> T get(@Nonnull final Class<T> type) {
        Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();
        final List<Dhcp6Option> dhcp6Options = options.get(registry.getOptionTag(type));
        Preconditions.checkArgument(dhcp6Options.size() < 2, "Not a singleton option: %s, %s", type, getAll(type));

        if(dhcp6Options.isEmpty()) {
            return null;
        }

        return getAll(type).iterator().next();
    }

    @CheckForNull
    public Iterable<Dhcp6Option> get(short tag) {
        return options.get(tag);
    }

    public void add(@Nonnull Dhcp6Option option) {
        options.put(option.getTag(), option);
    }

    public void addAll(@CheckForNull Iterable<? extends Dhcp6Option> options) {
        if (options == null)
            return;
        for (Dhcp6Option option : options)
            add(option);
    }

    /**
     * Remove instances of the given option class.
     *
     * @param type
     */
    public void remove(@Nonnull Class<? extends Dhcp6Option> type) {
        Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();
        removeAll(registry.getOptionTag(type));
    }

    public Collection<Dhcp6Option> removeAll(final short optionTag) {
        return options.removeAll(optionTag);
    }

    public void remove(@Nonnull Class<? extends Dhcp6Option> type, Dhcp6Option value) {
        Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();
        remove(value, registry.getOptionTag(type));
    }

    public boolean remove(final Dhcp6Option value, final short optionTag) {
        return options.remove(optionTag, value);
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

    public boolean contains(@Nonnull final Class<? extends Dhcp6Option> type) {
        return options.get(Dhcp6OptionsRegistry.getInstance().getOptionTag(type)).isEmpty();
    }

    /**
     *
     * @return total length of all options
     */
    public int getLength() {
        int length = 0;
        for (Dhcp6Option dhcp6Option : options.values()) {
            length = dhcp6Option.getData().length + 4;
        }

        return length;
    }
}
