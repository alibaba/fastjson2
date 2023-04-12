package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTypedMultiNamesPrefixIndex1Test {
    @Test
    public void test() {
        JSONPath path = JSONPath.of(
                new String[]{"$[0].f0", "$[0].f1"},
                new Type[]{Long.class, BigDecimal.class}
        );
        String jsonStr = JSONArray.of(
                JSONObject.of("f0", 1, "f1", 2)
        ).toString();
        Object[] result = (Object[]) path.extract(jsonStr);
        assertEquals(1L, result[0]);

        assertNull(((Object[]) path.extract("null"))[0]);
        assertNull(((Object[]) path.extract("[null]"))[0]);
        assertNull(((Object[]) path.extract("[]"))[0]);
        assertThrows(JSONException.class, () -> path.extract("{\"items\":1}"));
        assertThrows(JSONException.class, () -> path.extract("["));
    }

    @Test
    public void test1() {
        JSONPath path = JSONPath.of(
                new String[]{"$[1].f0", "$[1].f1"},
                new Type[]{Long.class, BigDecimal.class}
        );
        String jsonStr = JSONArray.of(
                JSONObject.of("x0", 101, "x1", 102),
                JSONObject.of("f0", 1, "f1", 2, "f2", 3)
        ).toString();
        Object[] result = (Object[]) path.extract(jsonStr);
        assertEquals(1L, result[0]);

        assertNull(((Object[]) path.extract("null"))[0]);
        assertNull(((Object[]) path.extract("[null]"))[0]);
        assertNull(((Object[]) path.extract("[null, null]"))[0]);
        assertNull(((Object[]) path.extract("[]"))[0]);
        assertThrows(JSONException.class, () -> path.extract("[1,"));
        assertThrows(JSONException.class, () -> path.extract("["));
        assertThrows(JSONException.class, () -> path.extract("[null,{\"f0\":\"xx\"}]"));
    }

    @Test
    public void test2_error() {
        JSONPath path = JSONPath.of(
                new String[]{"$[1].f0", "$[1].f1"},
                new Type[]{Long.class, BigDecimal.class},
                null,
                new long[]{JSONPath.Feature.NullOnError.mask},
                ZoneId.systemDefault()
        );
        assertNull(((Object[]) path.extract("[null,{\"f0\":\"xx\"}]"))[0]);
    }
}
