package org.anarres.dhcp.v6.options;

import com.google.common.primitives.Ints;

public abstract class IntOption extends Dhcp6Option {

    public int getValue() {
        return Ints.fromByteArray(getData());
    }

    public void setValue(int value) {
        setData(Ints.toByteArray(value));
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + getValue();
    }
}
