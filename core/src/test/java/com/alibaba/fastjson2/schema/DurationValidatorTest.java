package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DurationValidatorTest {
    final DurationValidator validator = DurationValidator.INSTANCE;

    @Test
    public void test() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertTrue(validator.isValid("P2D"));
        assertFalse(validator.isValid("1 days"));
    }
}
