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

    @Test
    public void testNumber() {
        String[] strings = new String[] {
                "-1",
                "+1",
                "-1.0",
                ".0",
                "+1.0",
                "1.",
                "+1.1e10",
                "+1.1e+10",
                "+1.1e-10"
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string));
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }
    @Test
    public void testNumbers() {
        String[] strings = new String[] {
                "-1 ",
                "+1 ",
                "-1.0 ",
                ".0 ",
                "+1.0 ",
                "1. ",
                "+1.1e10 ",
                "+1.1e+10 ",
                "+1.1e-10 "
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string));
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberArray1() {
        String[] strings = new String[] {
                "[-1]",
                "[+1]",
                "[-1.0]",
                "[.0]",
                "[+1.0]",
                "[1.]",
                "[+1.1e10]",
                "[+1.1e+10]",
                "[+1.1e-10]"
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberArray1False() {
        String[] strings = new String[] {
                "[-1,",
                "[+1,",
                "[-1.0,",
                "[.0,",
                "[+1.0,",
                "[1.,",
                "[+1.1e10,",
                "[+1.1e+10,",
                "[+1.1e-10,"
        };

        for (String string : strings) {
            assertFalse(JSON.isValid(string), string);
            assertFalse(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberArray1sFalse() {
        String[] strings = new String[] {
                "[-1, ",
                "[+1, ",
                "[-1.0, ",
                "[.0, ",
                "[+1.0, ",
                "[1., ",
                "[+1.1e10, ",
                "[+1.1e+10, ",
                "[+1.1e-10, "
        };

        for (String string : strings) {
            assertFalse(JSON.isValid(string), string);
            assertFalse(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberArray2() {
        String[] strings = new String[] {
                "[-1,-1]",
                "[+1,+1]",
                "[-1.0,-1.0]",
                "[.0,.0]",
                "[+1.0,+1.0]",
                "[1.,1.0]",
                "[+1.1e10,+1.1e10]",
                "[+1.1e+10,+1.1e+10]",
                "[+1.1e-10,+1.1e-10]"
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberArray2s() {
        String[] strings = new String[] {
                "[-1 ,-1 ]",
                "[+1 ,+1 ]",
                "[-1.0 ,-1.0 ]",
                "[.0 ,.0 ]",
                "[+1.0 ,+1.0 ]",
                "[1. ,1.0 ]",
                "[+1.1e10 ,+1.1e10 ]",
                "[+1.1e+10 ,+1.1e+10 ]",
                "[+1.1e-10 ,+1.1e-10 ]"
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberMap() {
        String[] strings = new String[] {
                "{\"v\":-1}",
                "{\"v\":+1}",
                "{\"v\":-1.0}",
                "{\"v\":.0}",
                "{\"v\":+1.0}",
                "{\"v\":1.}",
                "{\"v\":+1.1e10}",
                "{\"v\":+1.1e+10}",
                "{\"v\":+1.1e-10}"
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberMap2() {
        String[] strings = new String[] {
                "{\"v\":-1,\"v1\":-1}",
                "{\"v\":+1,\"v1\":+1}",
                "{\"v\":-1.0,\"v1\":-1.0}",
                "{\"v\":.0,\"v1\":.0}",
                "{\"v\":+1.0,\"v1\":+1.0}",
                "{\"v\":1.,\"v1\":1.}",
                "{\"v\":+1.1e10,\"v1\":+1.1e10}",
                "{\"v\":+1.1e+10,\"v1\":+1.1e+10}",
                "{\"v\":+1.1e-10,\"v1\":+1.1e-10}"
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberMap2s() {
        String[] strings = new String[] {
                "{\"v\":-1 ,\"v1\":-1 }",
                "{\"v\":+1 ,\"v1\":+1 }",
                "{\"v\":-1.0 ,\"v1\":-1.0 }",
                "{\"v\":.0 ,\"v1\":.0 }",
                "{\"v\":+1.0 ,\"v1\":+1.0 }",
                "{\"v\":1. ,\"v1\":1. }",
                "{\"v\":+1.1e10 ,\"v1\":+1.1e10 }",
                "{\"v\":+1.1e+10 ,\"v1\":+1.1e+10 }",
                "{\"v\":+1.1e-10 ,\"v1\":+1.1e-10 }"
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberFlase() {
        String[] strings = new String[] {
                "-",
                "+",
                "++1",
                "--1",
                "+1.1.",
                "+1e+",
                "+1e-",
                "+1e1e",
                "+1e+1e ",
                "+1e-1e "
        };

        for (String string : strings) {
            assertFalse(JSON.isValid(string), string);
            assertFalse(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void testNumberFlase1() {
        String[] strings = new String[] {
                "- ",
                "+ ",
                "++1 ",
                "--1 ",
                "+1.1. ",
                "+1e+ ",
                "+1e- ",
                "+1e1e ",
                "+1e+1e ",
                "+1e-1e "
        };

        for (String string : strings) {
            assertFalse(JSON.isValid(string), string);
            assertFalse(JSON.isValid(string.getBytes()));
        }
    }
}
