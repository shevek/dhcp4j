/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * See http://www.iana.org/assignments/bootp-dhcp-parameters/bootp-dhcp-parameters.xhtml#options
 *
 * @author shevek
 */
public class Dhcp6OptionsRegistry {

    private static class Inner {

        private static final Dhcp6OptionsRegistry INSTANCE = new Dhcp6OptionsRegistry();

        private static final Class OPTION_CLASSES[] = {

        };

        static {
            for (Class<? extends Dhcp6Option> optionType : OPTION_CLASSES) {
                INSTANCE.addOptionType(optionType);
            }
        }

    }

    @Nonnull
    public static Dhcp6OptionsRegistry getInstance() {
        return Inner.INSTANCE;
    }

    private final BiMap<Short, Class<? extends Dhcp6Option>> optionTypes = HashBiMap.create();

    @Nonnull
    public static <T extends Dhcp6Option> T newInstance(@Nonnull Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate " + type, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot instantiate " + type, e);
        }
    }

    private short getTagFrom(@Nonnull Class<? extends Dhcp6Option> type) {
        Dhcp6Option o = newInstance(type);
        return o.getTag();
    }

    public void addOptionType(@Nonnull Class<? extends Dhcp6Option> type) {
        short tag = getTagFrom(type);
        if (optionTypes.put(tag, type) != null)
            throw new IllegalArgumentException("Duplicate tag: " + type);
    }

    @CheckForNull
    public Class<? extends Dhcp6Option> getOptionType(short tag) {
        return optionTypes.get(tag);
    }

    @Nonnull
    public short getOptionTag(@Nonnull Class<? extends Dhcp6Option> type) {
        Short tag = optionTypes.inverse().get(type);
        if (tag != null)
            return tag;
        // TODO: Warn about unregistered option.
        return getTagFrom(type);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + optionTypes + ")";
    }

}
