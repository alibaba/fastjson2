package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONReaderBooleanTest {
    @Test
    public void numberNotation() {
        assertValue(true, "1");
        assertValue(true, "1.0");
        assertValue(true, "1.00");
        assertValue(true, "1e0");
        assertValue(true, "10e-1");
        assertValue(true, "-1");
        assertValue(true, "-1.0");
        assertValue(false, "2");
        assertValue(false, "2.0");
        assertValue(false, "-2");
        assertValue(false, "-2.0");
        assertValue(false, "0");
        assertValue(false, "0.0");
        assertValue(false, "0.5");
    }

    @Test
    public void nonZeroNumberFeature() {
        for (String value : new String[]{"1", "1.0", "-1", "-1.0", "2", "2.0", "0.5", "-0.5"}) {
            assertFeatureValue(true, value);
        }

        for (String value : new String[]{"0", "0.0", "0e10"}) {
            assertFeatureValue(false, value);
        }
    }

    @Test
    public void stringAndNullBehavior() {
        assertValue(true, "\"1\"");
        assertValue(true, "\"true\"");
        assertValue(false, "null");
        assertThrows(JSONException.class, () -> parse("\"yes\""));
    }

    private static void assertValue(boolean expected, String value) {
        String json = "{\"enabled\":" + value + "}";
        assertEquals(expected, JSON.parseObject(json, Bean.class).enabled, value);
        assertEquals(expected, JSON.parseObject(json.toCharArray(), Bean.class).enabled, value);
        assertEquals(
                expected,
                JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), Bean.class).enabled,
                value
        );
    }

    private static boolean parse(String value) {
        return JSON.parseObject("{\"enabled\":" + value + "}", Bean.class).enabled;
    }

    private static void assertFeatureValue(boolean expected, String value) {
        String json = "{\"enabled\":" + value + "}";
        assertEquals(
                expected,
                JSON.parseObject(
                        json,
                        Bean.class,
                        JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue
                ).enabled,
                value
        );
        assertEquals(
                expected,
                JSON.parseObject(
                        json.toCharArray(),
                        Bean.class,
                        JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue
                ).enabled,
                value
        );
        assertEquals(
                expected,
                JSON.parseObject(
                        json.getBytes(StandardCharsets.UTF_8),
                        Bean.class,
                        JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue
                ).enabled,
                value
        );
    }

    public static class Bean {
        public boolean enabled;
    }
}
