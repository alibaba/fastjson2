package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
