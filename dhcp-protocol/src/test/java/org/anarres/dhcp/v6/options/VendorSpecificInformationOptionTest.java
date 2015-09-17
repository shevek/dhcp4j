package org.anarres.dhcp.v6.options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import java.net.InetAddress;
import org.anarres.dhcp.v6.io.Dhcp6MessageDecoder;
import org.junit.Before;
import org.junit.Test;

public class VendorSpecificInformationOptionTest {

    @Before public void setUp() throws Exception {

        final Dhcp6OptionsRegistry registry = new Dhcp6OptionsRegistry();
        registry.addOptionType(Test1Option.class);
        registry.addOptionType(Test2Option.class);
        VendorSpecificInformationOption.addDecoder(1, new Dhcp6MessageDecoder(registry));
    }

    @Test public void testDecode() throws Exception {
        final String ip = "10.0.1.1";
        final String testValue = "test1";

        final Dhcp6Options options = new Dhcp6Options();
        final Test1Option option = new Test1Option();
        option.setString(testValue);
        options.add(option);
        final Test2Option option2 = new Test2Option();
        option2.setAddress(ip);
        options.add(option2);
        final VendorSpecificInformationOption vendorSpecificInformationOption = VendorSpecificInformationOption.create(
            1, options);

        final Test1Option test1Option = vendorSpecificInformationOption.getOptions().get(Test1Option.class);
        assertNotNull(test1Option);
        assertEquals(testValue, test1Option.getString());

        assertEquals(1, Iterables.size(vendorSpecificInformationOption.getOptions().getAll(Test1Option.class)));
        assertEquals(1, Iterables.size(vendorSpecificInformationOption.getOptions().get(((short) 10))));

        final Test2Option test2Option = vendorSpecificInformationOption.getOptions().get(Test2Option.class);
        assertNotNull(test2Option);
        assertEquals(InetAddress.getByName(ip), test2Option.getAddress());

        assertTrue(Iterables.isEmpty(vendorSpecificInformationOption.getOptions().get(((short) 99))));
    }

    @Test public void testDecodeWithUnknownSuboptions() throws Exception {
        final Dhcp6Options options = new Dhcp6Options();
        final Test3Option option = new Test3Option();
        option.setValue(22);
        options.add(option);
        final VendorSpecificInformationOption vendorSpecificInformationOption = VendorSpecificInformationOption.create(
            1, options);

        final Test3Option test3Option = vendorSpecificInformationOption.getOptions().get(Test3Option.class);
        final Dhcp6Option next = vendorSpecificInformationOption.getOptions().get((short) 4).iterator().next();

        assertTrue(next instanceof UnrecognizedOption);
        assertEquals(test3Option.getValue(), Ints.fromByteArray(next.getData()));
    }

    public static final class Test1Option extends StringOption {

        @Override public short getTag() {
            return 10;
        }
    }

    public static final class Test2Option extends InetAddressOption {

        @Override public short getTag() {
            return 3;
        }
    }

    public static final class Test3Option extends IntOption {

        @Override public short getTag() {
            return 4;
        }
    }
}