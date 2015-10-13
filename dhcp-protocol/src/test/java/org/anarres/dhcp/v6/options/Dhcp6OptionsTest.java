package org.anarres.dhcp.v6.options;

import static org.junit.Assert.*;

import org.junit.Test;

public class Dhcp6OptionsTest {

    @Test
    public void testContains() {
        Dhcp6Options options = new Dhcp6Options();
        options.add(StatusCodeOption.create((short) 0));
        assertTrue(options.contains(StatusCodeOption.class));
    }

    @Test
    public void testNotContains() {
        Dhcp6Options options = new Dhcp6Options();
        options.add(StatusCodeOption.create((short) 0));
        assertFalse(options.contains(IaNaOption.class));
    }

}
