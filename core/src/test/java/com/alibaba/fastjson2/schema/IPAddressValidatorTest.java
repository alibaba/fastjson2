package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IPAddressValidatorTest {
    @Test
    public void isValid() {
        JSONSchema ipv4 = JSONSchema.of(JSONObject.of("format", "ipv4"));
        JSONSchema ipv6 = JSONSchema.of(JSONObject.of("format", "ipv6"));
        assertFalse(ipv4.isValid(""));

        assertTrue(ipv4.isValid("192.168.1.1"));
        assertFalse(ipv4.isValid("::1"));

        assertFalse(ipv6.isValid("192.168.1.1"));
        assertTrue(ipv6.isValid("::1"));
        assertFalse(ipv6.isValid("1002:003B:456C:678D:890E:0012:234F:56G7"));
        assertTrue(ipv6.isValid("2408:84e4:404:f993:4ff1:48f5:2c71:a41"));
    }

    @Test
    public void isValidIPv6() {
        assertFalse(TypeUtils.validateIPv6("1002:003B:456C:678D:890E:0012:234F:56G7"));

        assertFalse(TypeUtils.validateIPv6(":"));
        assertFalse(TypeUtils.validateIPv6("1002:003B:456C:678D:890E:0012:234F:56G71002:003B:456C:678D:890E:0012:234F:56G7"));

        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:1:*01"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:*01:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:*01:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:*01:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:*01:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:*01:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:*01:1:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("*01:1:1:1:1:1:1:1"));

        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:1:G01"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:G01:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:G01:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:G01:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:G01:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:G01:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:G01:1:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("G01:1:1:1:1:1:1:1"));

        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:1:G1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:G1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:G1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:G1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:G1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:G1:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:G1:1:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("G1:1:1:1:1:1:1:1"));

        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:*:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:*:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:*:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:*:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:*:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:*:1:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("*:1:1:1:1:1:1:1"));

        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:1:G"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:1:G:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:1:G:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:1:G:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:1:G:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:1:G:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("1:G:1:1:1:1:1:1"));
        assertFalse(TypeUtils.validateIPv6("G:1:1:1:1:1:1:1"));

        assertTrue(TypeUtils.validateIPv6("::1"));
        assertTrue(TypeUtils.validateIPv6("FE80:CD00:0:CDE:1257:0:211E:729C"));
        assertTrue(TypeUtils.validateIPv6("FE80:CD00:0:CDE:1257:0:211E:729C"));
        assertTrue(TypeUtils.validateIPv6("2001:db8:3333:4444:CCCC:DDDD:EEEE:FFFF"));
        assertTrue(TypeUtils.validateIPv6("2001:db8:3333:4444:5555:6666:7777:8888"));
        assertTrue(TypeUtils.validateIPv6("::"));
        assertTrue(TypeUtils.validateIPv6("2001:db8::"));
        assertTrue(TypeUtils.validateIPv6("::1234:5678"));
        assertTrue(TypeUtils.validateIPv6("2001:db8::1234:5678"));
        assertTrue(TypeUtils.validateIPv6("2001:0db8:0001:0000:0000:0ab9:C0A8:0102"));
        assertTrue(TypeUtils.validateIPv6("2001:db8:1::ab9:C0A8:102"));
        assertTrue(TypeUtils.validateIPv6("2001:db8:3333:4444:5555:6666:1.2.3.4"));
        assertTrue(TypeUtils.validateIPv6("::11.22.33.44"));
        assertTrue(TypeUtils.validateIPv6("2001:db8::123.123.123.123"));
        assertTrue(TypeUtils.validateIPv6("::1234:5678:91.123.4.56"));
        assertTrue(TypeUtils.validateIPv6("::1234:5678:1.2.3.4"));
        assertTrue(TypeUtils.validateIPv6("2001:db8::1234:5678:5.6.7.8"));
        assertTrue(TypeUtils.validateIPv6("0000:0000:0000:0000:0000:0000:0000:0000"));
        assertTrue(TypeUtils.validateIPv6("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
    }

    @Test
    public void isValidIPv4() {
        assertFalse(TypeUtils.validateIPv4("::1"));
        assertFalse(TypeUtils.validateIPv4("1002:003B:456C:678D:890E:0012:234F:56G7"));

        assertTrue(TypeUtils.validateIPv4("192.168.0.16"));
        assertTrue(TypeUtils.validateIPv4("255.255.255.255"));

        assertFalse(TypeUtils.validateIPv4("*01.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.*01.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.*01.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.1.*01"));

        assertFalse(TypeUtils.validateIPv4("301.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.301.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.301.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.1.301"));

        assertFalse(TypeUtils.validateIPv4("256.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.256.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.256.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.256.256"));

        assertFalse(TypeUtils.validateIPv4("A.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.A.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.A.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.1.A"));

        assertFalse(TypeUtils.validateIPv4("*.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.*.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.*.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.1.*"));

        assertFalse(TypeUtils.validateIPv4("1."));
        assertFalse(TypeUtils.validateIPv4("1.1."));
        assertFalse(TypeUtils.validateIPv4("1.1.1."));
        assertFalse(TypeUtils.validateIPv4("1.1.1.1."));
        assertFalse(TypeUtils.validateIPv4("1.1.1.1.1."));
        assertFalse(TypeUtils.validateIPv4("1.1.1.1.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.1.1.1.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.1.1.1.1.1.1.1"));
        assertFalse(TypeUtils.validateIPv4("1.1.1.1.1.1.1.1.1.1"));
    }
}
