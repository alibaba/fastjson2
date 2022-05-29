package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONPathSetCallbackTest {
    @Test
    public void test() {
        JSONObject object = JSONObject.of("id", 101);

        JSONPath.setCallback(object, "$.id", o -> ((Integer) o).intValue() + 1);
        assertEquals(102, object.getIntValue("id"));

        JSONPath.setCallback(object, "$.id1", o -> ((Integer) o).intValue() + 1);
        assertEquals(102, object.getIntValue("id"));
    }

    @Test
    public void test1() {
        JSONObject object = JSONObject.of("id", 101);

        JSONPath.setCallback(object, "$..id", o -> ((Integer) o).intValue() + 1);
        assertEquals(102, object.getIntValue("id"));

        JSONPath.setCallback(object, "$..id2", o -> ((Integer) o).intValue() + 1);
        assertEquals(102, object.getIntValue("id"));
        assertEquals(1, object.size());
    }

    @Test
    public void test2() {
        JSONObject object = JSONObject.of("item", JSONObject.of("id", 101));

        JSONPath.setCallback(object, "$.item.id",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONObject("item").getIntValue("id"));

        JSONPath.setCallback(object, "$.item.id2",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONObject("item").getIntValue("id"));
        assertEquals(1, object.getJSONObject("item").size());
    }

    @Test
    public void test3() {
        JSONObject object = JSONObject.of("item", JSONObject.of("id", 101));

        JSONPath.setCallback(object, "$.item.id",
                (obj, val) -> ((Integer) val).intValue() + 1
        );
        assertEquals(102, object.getJSONObject("item").getIntValue("id"));

        JSONPath.setCallback(object, "$.item.id2",
                (obj, val) -> ((Integer) val).intValue() + 1
        );
        assertEquals(102, object.getJSONObject("item").getIntValue("id"));
        assertEquals(1, object.getJSONObject("item").size());
    }

    @Test
    public void testJSONArray() {
        JSONObject object = JSONObject.of("items", JSONArray.of(101));

        JSONPath.setCallback(object, "$.items[0]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONArray("items").get(0));

        JSONPath.setCallback(object, "$.items[2]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONArray("items").get(0));
        assertEquals(1, object.getJSONArray("items").size());
    }

    @Test
    public void testObjectArray() {
        JSONObject object = JSONObject.of("items", new Integer[]{101});

        JSONPath.setCallback(object, "$.items[0]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONArray("items").get(0));

        JSONPath.setCallback(object, "$.items[2]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONArray("items").get(0));
        assertEquals(1, object.getJSONArray("items").size());
    }

    @Test
    public void testObjectArray1() {
        JSONArray array = JSONArray.of().fluentAdd(new Integer[]{101}).fluentAdd(null);

        JSONPath.setCallback(array, "$[0][0]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, array.getJSONArray(0).get(0));
        assertNull(array.getJSONArray(1));
    }

    @Test
    public void testIntArray() {
        JSONObject object = JSONObject.of("items", new int[]{101});

        JSONPath.setCallback(object, "$.items[0]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONArray("items").get(0));
        assertNull(object.getJSONArray("items1"));

        JSONPath.setCallback(object, "$.items[2]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, object.getJSONArray("items").get(0));
        assertEquals(1, object.getJSONArray("items").size());
    }

    @Test
    public void testIntArray2() {
        JSONArray array = JSONArray.of().fluentAdd(new int[]{101}).fluentAdd(null);

        JSONPath.setCallback(array, "$[0][0]",
                o -> ((Integer) o).intValue() + 1
        );
        assertEquals(102, array.getJSONArray(0).get(0));
        assertNull(array.getJSONArray(1));
    }

    @Test
    public void testBean0() {
        Bean bean = new Bean();
        bean.id = 101;

        JSONPath.setCallback(bean, "$.id", o -> ((Integer) o).intValue() + 1);
        assertEquals(102, bean.id);

        JSONPath.setCallback(bean, "$.id2", o -> ((Integer) o).intValue() + 1);
        assertEquals(102, bean.id);
    }

    public static class Bean {
        public int id;
    }
}
