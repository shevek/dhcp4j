/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.v6.options;

import com.google.common.io.BaseEncoding;
import java.util.Arrays;
import javax.annotation.Nonnull;

/**
 *
 * @author shevek
 */
public abstract class DuidOption extends Dhcp6Option {

    @Nonnull
    public Duid getDuid() {
        return new Duid(getData());
    }

    public void setDuid(@Nonnull byte[] duid) {
        setData(duid);
    }

    public void setDuid(@Nonnull Duid duid) {
        setData(duid.getData());
    }

    public static final class Duid {

        private byte[] data;

        public Duid(final byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return data;
        }

        @Override public String toString() {
            return "Duid{" +
                "data=" + BaseEncoding.base16().encode(data) +
                '}';
        }

        @Override public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            final Duid duid = (Duid) o;
            return Arrays.equals(data, duid.data);
        }

        @Override public int hashCode() {
            return Arrays.hashCode(data);
        }
    }
}
