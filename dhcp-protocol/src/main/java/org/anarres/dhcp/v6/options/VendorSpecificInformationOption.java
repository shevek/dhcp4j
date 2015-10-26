package org.anarres.dhcp.v6.options;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.anarres.dhcp.v6.io.Dhcp6MessageEncoder;
import org.apache.directory.server.dhcp.DhcpException;

/**
 * https://tools.ietf.org/html/rfc3315#section-22.17
 */
public class VendorSpecificInformationOption extends SuboptionOption {

    private static final short TAG = 17;
    private static final int HEADER_SIZE = 4;

    /**
     * Default decoder treats all suboptions as unknown
     */
    public static final Dhcp6MessageDecoder DEFAULT_DECODER = new Dhcp6MessageDecoder(new Dhcp6OptionsRegistry());
    public static final Map<Integer, Dhcp6MessageDecoder> DECODERS = new HashMap<>();

    /**
     *
     * Add vendor specific suboption decoder
     *
     * @param enterpriseNumber
     * @param dhcp6MessageDecoder
     */
    public static void addDecoder(final int enterpriseNumber, final Dhcp6MessageDecoder dhcp6MessageDecoder) {
        // TODO enable multiple option type registries per vendor
        DECODERS.put(enterpriseNumber, dhcp6MessageDecoder);
    }

    public void setEnterpriseNumber(int enterpriseNumber) {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        buf.putInt(0, enterpriseNumber);
    }

    public int getEnterpriseNumber() {
        ByteBuffer buf = ByteBuffer.wrap(getData());
        return buf.getInt(0);
    }

    @Override
    public int getHeaderSize() {
        return HEADER_SIZE;
    }

    @Override
    public short getTag() {
        return TAG;
    }

    @Override
    protected Dhcp6MessageDecoder getDecoder() {
        final Dhcp6MessageDecoder specificDecoder = DECODERS.get(getEnterpriseNumber());
        return specificDecoder == null ? DEFAULT_DECODER : specificDecoder;
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

    public static VendorSpecificInformationOption create(@Nonnull final int enterpriseNumber, @Nonnull final Dhcp6Options options) {
        final VendorSpecificInformationOption iaNaOption = new VendorSpecificInformationOption();
        int length = HEADER_SIZE;

        ByteBuffer encodedOptions = null;
        if (!options.isEmpty()) {
            encodedOptions = Dhcp6MessageEncoder.getInstance().encode(options);
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
