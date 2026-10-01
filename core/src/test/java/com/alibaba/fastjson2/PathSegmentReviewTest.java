package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PathSegmentReviewTest {
    @Test
    void randomIndexUsesBoundedRandomAndHandlesEmptyArrays() {
        JSONPathSegment.RandomIndexSegment segment = new JSONPathSegment.RandomIndexSegment();
        segment.random = new Random() {
            @Override
            public int nextInt() {
                return Integer.MIN_VALUE;
            }

            @Override
            public int nextInt(int bound) {
                return bound - 1;
            }
        };
        JSONPath.Context context = new JSONPath.Context(JSONPath.of("$"), null, segment, null, 0);
        context.root = new Object[] {1, 2, 3};
        segment.eval(context);
        assertEquals(3, context.value);
        context.root = JSONArray.of(1, 2, 3);
        segment.eval(context);
        assertEquals(3, context.value);
        segment.setCallback(context, (o, v) -> 4);
        assertEquals(JSONArray.of(1, 2, 4), context.root);
        JSONPath path = JSONPath.of("$[randomIndex()]");
        assertNull(path.extract("[]"));
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(new Object[0]))) {
            assertNull(path.extract(reader));
        }
        assertDoesNotThrow(() -> path.setCallback(new JSONArray(), v -> v));
    }

    @Test
    void arraySlicesMatchListSlices() {
        Object[] values = {0, 1, 2, 3, 4};
        for (String expression : new String[] {"$[1:3]", "$[-3:-1]"}) {
            JSONPath path = JSONPath.of(expression);
            assertEquals(path.eval(JSONArray.of(values)), path.eval(values));
        }
        JSONArray list = JSONArray.of(values);
        JSONPath.of("$[-3:-1]").setCallback(list, v -> ((Integer) v) + 10);
        assertEquals(JSONArray.of(0, 1, 12, 13, 4), list);
    }

    @Test
    void recursiveMutationsSupportSetsAndNullBeanFields() {
        JSONObject item = JSONObject.of("value", 1);
        LinkedHashSet<JSONObject> set = new LinkedHashSet<>(Collections.singleton(item));
        JSONPath path = JSONPath.of("$..value");
        path.set(set, 2);
        assertEquals(2, item.get("value"));
        path.setCallback(set, v -> ((Integer) v) + 1);
        assertEquals(3, item.get("value"));
        assertTrue(path.remove(set));
        assertFalse(item.containsKey("value"));
        EmptyBean bean = new EmptyBean();
        assertDoesNotThrow(() -> path.set(bean, 2));
        assertDoesNotThrow(() -> path.setCallback(bean, v -> v));
        assertDoesNotThrow(() -> path.remove(bean));
    }

    @Test
    void recursiveContainerCyclesHitDepthLimit() {
        JSONArray array = new JSONArray();
        array.add(array);
        assertThrows(JSONException.class, () -> JSONPath.of("$..*").eval(array));
    }

    public static class EmptyBean {
        public Object child;
    }
}
