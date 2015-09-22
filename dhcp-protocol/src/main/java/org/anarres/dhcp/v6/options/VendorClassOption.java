package org.anarres.dhcp.v6.options;

import com.google.common.io.BaseEncoding;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.16
 */
public class VendorClassOption extends Dhcp6Option {
    private static final short TAG = 16;

    @Override public short getTag() {
        return TAG;
    }

    public void setEnterpriseNumber(int enterpriseNumber) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(0, enterpriseNumber);
    }

    public int getEnterpriseNumber() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(0);
    }

    @Nonnull
    public byte[] getVendorClassData() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        final byte[] vendorClassData = new byte[getData().length - 4];
        buf.get(vendorClassData, 4, getData().length);
        return vendorClassData;
    }

    public void setVendorClassData(@Nonnull final byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.put(data, 4, data.length);
    }

    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        values.append("enterpriseNumber:");
        values.append(getEnterpriseNumber());
        values.append(", vendorClassData:");
        values.append(BaseEncoding.base16().encode(getVendorClassData()));
        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }

    public static VendorClassOption create(final int enterpriseNumber, final byte[] vendorClassData) {
        final VendorClassOption iaNaOption = new VendorClassOption();
        iaNaOption.setData(new byte[vendorClassData.length + 4]);
        iaNaOption.setEnterpriseNumber(enterpriseNumber);
        iaNaOption.setVendorClassData(vendorClassData);
        return iaNaOption;
    }
}
