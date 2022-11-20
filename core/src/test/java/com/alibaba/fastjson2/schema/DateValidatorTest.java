package com.alibaba.fastjson2.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateValidatorTest {
    @Test
    public void test() {
        assertFalse(DateValidator.INSTANCE.isValid(null));
        assertFalse(DateValidator.INSTANCE.isValid(""));

        assertFalse(DateValidator.INSTANCE.isValid("2022-02-30"));
        assertTrue(DateValidator.INSTANCE.isValid("2022-02-18"));

        assertFalse(DateValidator.INSTANCE.isValid("2022-04-31"));
        assertTrue(DateValidator.INSTANCE.isValid("2022-04-30"));

        assertFalse(DateValidator.INSTANCE.isValid("2022"));
        assertFalse(DateValidator.INSTANCE.isValid("https://github.com/alibaba/fastjson2/issues"));
    }
}
