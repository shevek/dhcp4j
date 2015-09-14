package org.anarres.dhcp.v6.options;

import com.google.common.base.Optional;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.17
 */
public class VendorSpecificInformationOption extends SuboptionOption {

    private static final short TAG = 17;
    private static final int HEADER_SIZE = 4;

    public void setEnterpriseNumber(int enterpriseNumber) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(0, enterpriseNumber);
    }

    public int getEnterpriseNumber() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(0);
    }

    @Override protected int getHeaderSize() {
        return HEADER_SIZE;
    }

    @Override public short getTag() {
        return TAG;
    }


    @Override
    public String toString() {
        final StringBuilder values = new StringBuilder();
        values.append("Enterprise number:");
        values.append(getEnterpriseNumber());
        values.append(", ");
        try {
            values.append(getOptions().toString());
        } catch (DhcpException e) {
            values.append("options:[");
            values.append(toStringDataFallback(getOptionsRaw().array()));
            values.append("]");
        }

        return getClass().getSimpleName() + "[" + getTagAsInt() + "]: " + values;
    }

    public static VendorSpecificInformationOption create(@Nonnull final int enterpriseNumber, final Optional<Dhcp6Options> options) {
        final VendorSpecificInformationOption iaNaOption = new VendorSpecificInformationOption();
        int length = HEADER_SIZE;

        ByteBuffer encodedOptions = null;
        if(options.isPresent() && !options.get().isEmpty()) {
            encodedOptions = Dhcp6MessageEncoder.getInstance().encode(options.get());
            length += encodedOptions.limit();
        }

        iaNaOption.setData(new byte[length]);
        iaNaOption.setEnterpriseNumber(enterpriseNumber);
        if (encodedOptions != null) {
            iaNaOption.setOptions(encodedOptions);
        }
        return iaNaOption;
    }
}
