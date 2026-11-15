package com.alibaba.fastjson2.issues_7900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class Issue7944 {
    public static class Bean {
        public String name;
    }

    /**
     * Every input below ends inside a string value that is skipped over, either because the
     * JSONPath name does not match or because the bean has no such field.
     */
    static String[] truncatedValues() {
        return new String[]{
                "{\"中\":\"",
                "{\"中\":\"abc",
                "{\"中\":\"中文",
                "{\"中\":\"a\\",
                "{\"中\":\"a\\\\",
                "{\"中\":\"\\u4e",
                "{\"中\":\"\\x4"
        };
    }

    @Test
    public void skippedTruncatedValue() {
        for (String input : truncatedValues()) {
            assertThrows(JSONException.class, () -> JSONPath.extract(input, "$.a"), input);
            assertThrows(JSONException.class, () -> JSONPath.of("$.a").extract(JSONReader.of(input)), input);
            assertThrows(JSONException.class, () -> JSONPath.of("$.a").extract(JSONReader.of(input.toCharArray())), input);
        }
    }

    @Test
    public void unknownFieldWithTruncatedValue() {
        for (String input : truncatedValues()) {
            assertThrows(JSONException.class, () -> JSON.parseObject(input, Bean.class), input);
        }
    }

    @Test
    public void completeValuesStillSkip() {
        // the value is not needed, so it is skipped: both forms must keep working
        assertEquals(1, intValue(JSONPath.extract("{\"中\":\"abc\",\"a\":1}", "$.a")));
        assertEquals(2, intValue(JSONPath.extract("{\"中\":\"a\\\"b\\\\c\\u4e2d\",\"a\":2}", "$.a")));
        assertEquals(3, intValue(JSONPath.of("$.a").extract(JSONReader.of("{\"中\":\"x\",\"a\":3}"))));
        assertEquals(4, intValue(JSONPath.of("$.a").extract(JSONReader.of("{\"中\":\"x\",\"a\":4}".toCharArray()))));

        // ASCII input already took the UTF-8 path and was rejected before this change
        assertThrows(JSONException.class, () -> JSONPath.extract("{\"b\":\"", "$.a"));

        Bean bean = JSON.parseObject("{\"name\":\"张三\",\"extra\":\"x\"}", Bean.class);
        assertEquals("张三", bean.name);
    }

    static int intValue(Object value) {
        return ((Number) value).intValue();
    }
}
