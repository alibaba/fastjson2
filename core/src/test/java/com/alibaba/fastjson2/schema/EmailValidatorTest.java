package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.schema.StringSchema.isEmail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailValidatorTest {
    @Test
    public void test() {
        assertFalse(isEmail(null));
        assertFalse(isEmail("a."));
        assertFalse(isEmail("@b.c"));
        assertFalse(isEmail("1@b.c"));
        assertFalse(isEmail("a01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789@b.c"));
        assertFalse(isEmail("a@192.168.0.1"));
        assertTrue(isEmail("a@[192.168.0.1]"));
        assertFalse(isEmail("a@[192.a.0.1]"));
        assertFalse(isEmail("a@[192..0.1]"));
        assertFalse(isEmail("a@[256.0.0.1]"));
        assertFalse(isEmail("a@[192.A1.0.1]"));
        assertTrue(isEmail("a@[fe80::1]"));
        assertFalse(isEmail("a@[fe80::1234567]"));
        assertTrue(isEmail("a@[fe80::f490:7eff:fe22:7f8b]"));
        assertTrue(isEmail("a@[fe80::f490:7eff:fe22:7f8b:7f8b]"));
        assertTrue(isEmail("a@[fe80::f490:7eff:fe22:7f8b:7f8b:7f8b]"));
        assertFalse(isEmail("a@[fe80::f490:7eff:fe22:7f8b:7f8b:7f8b:7f8b]"));
        assertFalse(isEmail("a@[fe80::f490:7eff:fe22:7f8b:7f8b:7f8k]"));
    }
}
