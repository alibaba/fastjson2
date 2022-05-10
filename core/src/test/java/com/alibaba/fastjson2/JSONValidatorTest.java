package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class JSONValidatorTest {
    @Test
    public void validate_test_quotation() {
        assertFalse(JSON.isValid("{noQuotationMarksError}"));
        byte[] utf8 = "{noQuotationMarksError}".getBytes(StandardCharsets.UTF_8);
        assertFalse(JSON.isValid(utf8));
        assertFalse(JSON.isValid(utf8, 0, utf8.length, StandardCharsets.UTF_8));
        assertFalse(JSON.isValid(utf8, 0, utf8.length, StandardCharsets.US_ASCII));
    }

    @Test
    public void test0() {
        assertTrue(JSONValidator.from("{}").validate());
        assertTrue(JSONValidator.fromUtf8("{}".getBytes(StandardCharsets.UTF_8)).validate());

        assertTrue(JSONValidator.from("[]").validate());
        assertTrue(JSONValidator.fromUtf8("[]".getBytes(StandardCharsets.UTF_8)).validate());

        assertTrue(JSONValidator.from("1").validate());
        assertTrue(JSONValidator.from("\"123\"").validate());
        assertEquals(JSONValidator.Type.Value, JSONValidator.from("\"123\"").getType());

        JSONValidator validator = JSONValidator.from("{}");
        assertTrue(validator.validate());
        assertTrue(validator.validate());
    }

    @Test
    public void test0_str() {
        assertTrue(JSONValidator.from(new JSONReaderStr(JSONFactory.createReadContext(), "{}")).validate());
        assertTrue(JSONValidator.from(new JSONReaderStr(JSONFactory.createReadContext(), "[]")).validate());
        assertTrue(JSONValidator.from(new JSONReaderStr(JSONFactory.createReadContext(), "1")).validate());
        assertTrue(JSONValidator.from(new JSONReaderStr(JSONFactory.createReadContext(), "\"123\"")).validate());

        assertEquals(JSONValidator.Type.Value, JSONValidator.from(new JSONReaderStr(JSONFactory.createReadContext(), "\"123\"")).getType());

        JSONValidator validator = JSONValidator.from(new JSONReaderStr(JSONFactory.createReadContext(), "{}"));
        assertTrue(validator.validate());
        assertTrue(validator.validate());
    }
}
