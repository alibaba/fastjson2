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

    @Test
    public void validateTrue() {
        String[] strings = new String[] {
                "true",
                "true ",
                "[true]",
                "[true,true]",
                "[true ,true ]",
                "{\"v0\":true,\"v1\":true}",
                "{\"v0\":true ,\"v1\":true }",
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void validateTrueError() {
        String[] strings = new String[] {
                "t",
                "t ",
                "tr",
                "tr ",
                "tru",
                "tru ",
                "true1",
                "true1 ",
                "true,",
                "true ,"
        };

        for (String string : strings) {
            assertFalse(JSON.isValid(string), string);
            assertFalse(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void validateFalse() {
        String[] strings = new String[] {
                "false",
                "false ",
                "[false]",
                "[false,false]",
                "[false ,false ]",
                "{\"v0\":false,\"v1\":false}",
                "{\"v0\":false ,\"v1\":false }",
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()), string);
        }
    }

    @Test
    public void validateFalseError() {
        String[] strings = new String[] {
                "f",
                "f ",
                "fa",
                "fa ",
                "fal",
                "fal ",
                "fals",
                "fals ",
                "false1",
                "false,",
                "false ,",
                "false1 "
        };

        for (String string : strings) {
            assertFalse(JSON.isValid(string), string);
            assertFalse(JSON.isValid(string.getBytes()), string);
        }
    }

    @Test
    public void validateNull() {
        String[] strings = new String[] {
                "null",
                "null ",
                "[null]",
                "[null,null]",
                "[null ,null ]",
                "{\"v0\":null,\"v1\":null}",
                "{\"v0\":null ,\"v1\":null }",
        };

        for (String string : strings) {
            assertTrue(JSON.isValid(string), string);
            assertTrue(JSON.isValid(string.getBytes()));
        }
    }

    @Test
    public void validateNullError() {
        String[] strings = new String[] {
                "n",
                "n ",
                "nu",
                "nu ",
                "nul",
                "nul ",
                "null,",
                "null ,",
                "null1",
                "null1 "
        };

        for (String string : strings) {
            assertFalse(JSON.isValid(string), string);
            assertFalse(JSON.isValid(string.getBytes()), string);
        }
    }
}
