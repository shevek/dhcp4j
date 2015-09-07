package org.anarres.dhcp.v6.io;

import com.google.common.io.BaseEncoding;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import org.anarres.dhcp.v6.messages.Dhcp6Message;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Created by marosmars
 */
@RunWith(Parameterized.class)
public class Dhcp6MessageCodecTest {

    /**
     * DHCP_SOLICIT: tx=2677248,
     *
     * options=Dhcp6Options(
     * [ClientIdOption[1]: 636C6965 6E746964 2D646562 31303030 30303030 31,
     * IaNaOption[3]: 00000001 00000000 00000000,
     * ElapsedTimeOption[8]: 0001])
     */
    private static final byte[] DHCP_6_SOLICIT = decode(
        "0128DA0000010015636C69656E7469642D6465623130303030303030310003000C000000010000000000000000000800020001");

    /**
     * DHCP_SOLICIT: tx=1050740,
     *
     * options=Dhcp6Options(
     * [ClientIdOption[1]: 00010001 1C39CF88 080027FE 8F95,
     * OptionRequestOption[6]: requestedOptions:[[23, 24]],
     * ElapsedTimeOption[8]: 0000,
     * UnrecognizedOption[25]: 27FE8F95 00000E10 00001518])
     */
    private static final byte[] DHCP_6_SOLICIT2 = decode(
        "011008740001000e000100011c39cf88080027fe8f9500060004001700180008000200000019000c27fe8f9500000e1000001518");

    /**
     * DHCP_ADVERTISE: tx=1050740,
     *
     * options=Dhcp6Options(
     * [UnrecognizedOption[25]: 27FE8F95 00000000 00000000 001A0019 00001194 00001C20 40200100 000000FE 00000000 00000000 00,
     * ClientIdOption[1]: 00010001 1C39CF88 080027FE 8F95,
     * ServerIdOption[2]: 00010001 1C3825E8 080027D4 10BB])
     */
    private static final byte[] DHCP_6_ADVERTISE = decode(
        "021008740019002927fe8f950000000000000000001a00190000119400001c2040200100000000fe0000000000000000000001000e000100011c39cf88080027fe8f950002000e000100011c3825e8080027d410bb");

    /**
     * DHCP_REQUEST: tx=4790094,
     *
     * options=Dhcp6Options(
     * [ClientIdOption[1]: 00010001 1C39CF88 080027FE 8F95,
     * ServerIdOption[2]: 00010001 1C3825E8 080027D4 10BB,
     * OptionRequestOption[6]: requestedOptions:[[23, 24]],
     * ElapsedTimeOption[8]: 0000,
     * UnrecognizedOption[25]: 27FE8F95 00000E10 00001518 001A0019 00001C20 00001D4C 40200100 000000FE 00000000 00000000 00])
     */
    private static final byte[] DHCP_6_REQUEST = decode(
        "0349174e0001000e000100011c39cf88080027fe8f950002000e000100011c3825e8080027d410bb00060004001700180008000200000019002927fe8f9500000e1000001518001a001900001c2000001d4c40200100000000fe000000000000000000");

    /**
     * DHCP_REPLY: tx=4790094,
     *
     * options=Dhcp6Options(
     * [UnrecognizedOption[25]: 27FE8F95 00000000 00000000 001A0019 00001194 00001C20 40200100 000000FE 00000000 00000000 00,
     * ClientIdOption[1]: 00010001 1C39CF88 080027FE 8F95,
     * ServerIdOption[2]: 00010001 1C3825E8 080027D4 10BB])
     */
    private static final byte[] DHCP_6_REPLY = decode(
        "0749174e0019002927fe8f950000000000000000001a00190000119400001c2040200100000000fe0000000000000000000001000e000100011c39cf88080027fe8f950002000e000100011c3825e8080027d410bb");

    /**
     * DHCP_RELEASE: tx=13076912,
     *
     * options=Dhcp6Options(
     * [ClientIdOption[1]: 00010001 1C39CF88 080027FE 8F95,
     * ServerIdOption[2]: 00010001 1C3825E8 080027D4 10BB,
     * OptionRequestOption[6]: requestedOptions:[[23, 24]],
     * ElapsedTimeOption[8]: 0000,
     * UnrecognizedOption[25]: 27FE8F95 00000000 00000000 001A0019 00000000 00000000 40200100 000000FE 00000000 00000000 00])
     */
    private static final byte[] DHCP_6_RELEASE = decode(
        "08c789b00001000e000100011c39cf88080027fe8f950002000e000100011c3825e8080027d410bb00060004001700180008000200000019002927fe8f950000000000000000001a0019000000000000000040200100000000fe000000000000000000");

    /**
     * DHCP_REPLY: tx=13076912,
     *
     * options=Dhcp6Options(
     *
     * [ClientIdOption[1]: 00010001 1C39CF88 080027FE 8F95,
     * ServerIdOption[2]: 00010001 1C3825E8 080027D4 10BB,
     * StatusCodeOption[13]: Code:0, Message:Release received.])
     */
    private static final byte[] DHCP_6_REPLY2 = decode(
        "07c789b00001000e000100011c39cf88080027fe8f950002000e000100011c3825e8080027d410bb000d0013000052656c656173652072656365697665642e");

    private static byte[] decode(final String msg) {
        return BaseEncoding.base16().decode(
            msg
                .toUpperCase());
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {DHCP_6_SOLICIT, "solicit"},
            {DHCP_6_SOLICIT2, "solicit2"},
            {DHCP_6_ADVERTISE, "advertise"},
            {DHCP_6_REQUEST, "request"},
            {DHCP_6_REPLY, "reply"},
            {DHCP_6_RELEASE, "release"},
            {DHCP_6_REPLY2, "reply2"}
        });
    }

    final Dhcp6MessageDecoder decoder = Dhcp6MessageDecoder.getInstance();
    final Dhcp6MessageEncoder encoder = Dhcp6MessageEncoder.getInstance();

    @Parameterized.Parameter(0)
    public byte[] message;
    @Parameterized.Parameter(1)
    public String name;

    @Test
    public void testSolicit() throws Exception {
        final Dhcp6Message decoded = decoder.decode(ByteBuffer.wrap(message));

        final ByteBuffer encoded = ByteBuffer.allocate(message.length);
        encoder.encode(encoded, decoded);

        Assert.assertArrayEquals(message, encoded.array());
    }
}