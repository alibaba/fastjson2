package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateTimeValidatorTest {
    DateTimeValidator validator = DateTimeValidator.INSTANCE;

    @Test
    public void test() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("A000-00-00 00:00:00"));
        assertTrue(validator.isValid("2000-04-30 00:00:00"));
        assertTrue(validator.isValid("2000-05-31 00:00:00"));
        assertFalse(validator.isValid("2000-04-31 00:00:00"));
        assertFalse(validator.isValid("2000-05-41 00:00:00"));

        assertFalse(validator.isValid("2000-05-01 25:00:00"));
        assertFalse(validator.isValid("2000-05-01 00:61:00"));
        assertFalse(validator.isValid("2000-05-01 00:00:62"));

        assertFalse(validator.isValid("2000-05-01 00:00:620"));
        assertFalse(validator.isValid("2000-05-01 00:00:62.0"));
    }
}
