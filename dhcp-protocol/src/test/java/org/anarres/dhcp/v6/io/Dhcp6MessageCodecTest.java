package org.anarres.dhcp.v6.io;

import com.google.common.io.BaseEncoding;
import java.nio.ByteBuffer;
import junit.framework.TestCase;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.junit.Assert;

/**
 * Created by marosmars
 */
public class Dhcp6MessageCodecTest extends TestCase {

    /**
     * DHCP_SOLICIT: tx=2677248,
     *
     * options=Dhcp6Options(
     * [ClientIdOption[1]: 636C6965 6E746964 2D646562 31303030 30303030 31,
     * IaNaOption[3]: 00000001 00000000 00000000,
     * ElapsedTimeOption[8]: 0001])
     */
    private static final byte[] DHCP_6_SOLICIT =
        BaseEncoding.base16().decode("0128DA0000010015636C69656E7469642D6465623130303030303030310003000C000000010000000000000000000800020001");

    final Dhcp6MessageDecoder decoder = Dhcp6MessageDecoder.getInstance();
    final Dhcp6MessageEncoder encoder = Dhcp6MessageEncoder.getInstance();

    public void testSolicit() throws Exception {
        final Dhcp6Message decodedOffer = decoder.decode(ByteBuffer.wrap(DHCP_6_SOLICIT));
        final ByteBuffer encoded = ByteBuffer.allocate(DHCP_6_SOLICIT.length);
        encoder.encode(encoded, decodedOffer);

        Assert.assertArrayEquals(DHCP_6_SOLICIT, encoded.array());
    }
}