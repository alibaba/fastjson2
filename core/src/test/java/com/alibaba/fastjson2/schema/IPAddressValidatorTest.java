package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IPAddressValidatorTest {
    @Test
    public void isValid() {
        assertFalse(IPAddressValidator.IPV4.isValid(null));
        assertFalse(IPAddressValidator.IPV4.isValid(""));

        assertTrue(IPAddressValidator.IPV4.isValid("192.168.1.1"));
        assertFalse(IPAddressValidator.IPV4.isValid("::1"));

        assertFalse(IPAddressValidator.IPV6.isValid("192.168.1.1"));
        assertTrue(IPAddressValidator.IPV6.isValid("::1"));
        assertFalse(IPAddressValidator.IPV6.isValid("1002:003B:456C:678D:890E:0012:234F:56G7"));
        assertTrue(IPAddressValidator.IPV6.isValid("2408:84e4:404:f993:4ff1:48f5:2c71:a41"));
    }
}
