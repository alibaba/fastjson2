package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailValidatorTest {
    EmailValidator validator = EmailValidator.INSTANCE;

    @Test
    public void test() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid("a."));
        assertFalse(validator.isValid("@b.c"));
        assertFalse(validator.isValid("1@b.c"));
        assertFalse(validator.isValid("a01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789@b.c"));
        assertFalse(validator.isValid("a@192.168.0.1"));
        assertTrue(validator.isValid("a@[192.168.0.1]"));
        assertFalse(validator.isValid("a@[192.a.0.1]"));
        assertFalse(validator.isValid("a@[192..0.1]"));
        assertFalse(validator.isValid("a@[256.0.0.1]"));
        assertFalse(validator.isValid("a@[192.01.0.1]"));
        assertTrue(validator.isValid("a@[fe80::1]"));
        assertFalse(validator.isValid("a@[fe80::1234567]"));
        assertTrue(validator.isValid("a@[fe80::f490:7eff:fe22:7f8b]"));
        assertTrue(validator.isValid("a@[fe80::f490:7eff:fe22:7f8b:7f8b]"));
        assertTrue(validator.isValid("a@[fe80::f490:7eff:fe22:7f8b:7f8b:7f8b]"));
        assertFalse(validator.isValid("a@[fe80::f490:7eff:fe22:7f8b:7f8b:7f8b:7f8b]"));
        assertFalse(validator.isValid("a@[fe80::f490:7eff:fe22:7f8b:7f8b:7f8k]"));
    }
}
