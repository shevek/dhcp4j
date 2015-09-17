package org.anarres.dhcp.v6.options;

import com.google.common.base.Charsets;

/**
 * Base option for string based options
 */
public abstract class StringOption extends Dhcp6Option {

    public String getString() {
        return new String(getData(), Charsets.UTF_8);
    }

    public void setString(final String value) {
        setData(value.getBytes(Charsets.UTF_8));
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + getString();
    }
}
