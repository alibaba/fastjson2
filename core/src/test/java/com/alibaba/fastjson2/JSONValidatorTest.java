package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class JSONValidatorTest {
    @Test
    public void validate_test_quotation() {
        assertFalse(JSON.isValid("{noQuotationMarksError}"));
        byte[] utf8 = "{noQuotationMarksError}".getBytes(StandardCharsets.UTF_8);
        assertFalse(JSON.isValid(utf8));
        assertFalse(JSON.isValid(utf8, 0, utf8.length, StandardCharsets.UTF_8));
        assertFalse(JSON.isValid(utf8, 0, utf8.length, StandardCharsets.US_ASCII));
    }
}
