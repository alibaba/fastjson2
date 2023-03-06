package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONPathTypedMultiNamesPrefixName2Test {
    @Test
    public void test2() {
        JSONPath path = JSONPath.of(
                new String[]{"$.k0.k1.f0", "$.k0.k1.f1"},
                new Type[]{Long.class, BigDecimal.class}
        );
        assertThrows(JSONException.class, () -> path.extract("{\"k0\":{\"k1\":{\"f0\":\"xx\"}}}"));
        assertThrows(JSONException.class, () -> path.extract("{\"k0\":{\"k1\":1}}"));
        assertThrows(JSONException.class, () -> path.extract("{\"k0\":1}"));
        assertThrows(JSONException.class, () -> path.extract("1"));
    }

    @Test
    public void test2_error() {
        JSONPath path = JSONPath.of(
                new String[]{"$.k0.k1.f0", "$.k0.k1.f1"},
                new Type[]{Long.class, BigDecimal.class},
                null,
                new long[]{JSONPath.Feature.NullOnError.mask},
                ZoneId.systemDefault()
        );
        assertNull(((Object[]) path.extract("{\"k0\":{\"k1\":{\"f0\":\"xx\"}}}"))[0]);
        assertNull(((Object[]) path.extract("{\"k0\":{\"k1\":null}}"))[0]);
        assertNull(((Object[]) path.extract("{\"k0\":null}"))[0]);
    }
}
