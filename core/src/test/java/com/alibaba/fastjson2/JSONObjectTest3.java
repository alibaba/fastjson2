package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class JSONObjectTest3 {
    @Test
    public void test0() {
        assertEquals(
                123,
                JSONObject
                        .of("bean", JSONObject.of("id", 123))
                        .getObject("bean", Bean::new)
                        .id
        );
    }

    @Test
    public void test1() {
        assertEquals(
                123,
                JSONArray
                        .of(JSONObject.of("id", 123))
                        .getObject(0, Bean::new)
                        .id
        );
    }

    @Test
    public void test2() {
        assertThrows(JSONException.class,
                () -> JSONObject.of("id", 1).getObject("id", Bean.class)
        );
    }

    public static class Bean {
        public int id;

        public Bean(JSONObject input) {
            this.id = input.getIntValue("id");
        }
    }

    @Test
    public void test3() {
        JSONObject object = JSON.parseObject("{\"value\":12.34}");
        assertEquals(12.34D, object.to(Bean1.class).value);
        assertEquals(12.34D, object.getObject("value", Double.class));
        assertEquals(12.34D, TypeUtils.cast(new BigDecimal("12.34"), Double.class));
    }

    public static class Bean1 {
        public Double value;
    }

    @Test
    public void test4() {
        JSONObject object = JSON.parseObject("{\"websiteList\":[\"a\",\"b\"]}");
        Bean4 to = object.to(Bean4.class);
        assertEquals("a", to.websiteList[0]);
        assertEquals("b", to.websiteList[1]);
        assertEquals("{\"websiteList\":[\"a\",\"b\"]}", object.to(String.class));
        assertEquals("{\"websiteList\":[\"a\",\"b\"]}", object.to((Type) String.class));

        assertEquals("[]", JSONArray.of().to(String.class));
        assertEquals("[]", JSONArray.of().to((Type) String.class));
    }

    public static class Bean4 {
        public String[] websiteList;
    }

    @Test
    public void test5() {
        assertEquals("{}", JSONObject.toJSONString(JSONObject.of()));
    }

    @Test
    public void of() {
        assertEquals(0, JSONObject.of().size());
        assertEquals(1, JSONObject.of("k0", 0).size());
        assertEquals(2, JSONObject.of("k0", 0, "k1", 1).size());
        assertEquals(3, JSONObject.of("k0", 0, "k1", 1, "k2", 2).size());
        assertEquals(4, JSONObject.of("k0", 0, "k1", 1, "k2", 2, "k3", 3).size());
        assertEquals(5, JSONObject.of("k0", 0, "k1", 1, "k2", 2, "k3", 3, "k4", 4).size());
    }

    @Test
    public void getBooleanValue() {
        JSONObject jsonObject = JSONObject
                .of(
                        "v0", "true",
                        "v1", 1,
                        "v2", true,
                        "v3", false,
                        "v4", "1"
                );
        assertEquals(5, jsonObject.size());
        assertTrue(jsonObject.getBooleanValue("v0", false));

        assertTrue(jsonObject.getBooleanValue("v1", false));
        assertTrue(jsonObject.getBooleanValue("v1", true));

        assertTrue(jsonObject.getBooleanValue("v2", false));
        assertTrue(jsonObject.getBooleanValue("v2", true));

        assertFalse(jsonObject.getBooleanValue("v3", false));
        assertFalse(jsonObject.getBooleanValue("v3", true));

        assertTrue(jsonObject.getBooleanValue("v4", false));
        assertTrue(jsonObject.getBooleanValue("v4", true));

        assertFalse(jsonObject.getBooleanValue("v100", false));
        assertTrue(jsonObject.getBooleanValue("v100", true));
    }

    @Test
    public void getIntValue() {
        JSONObject jsonObject = JSONObject.of("v0", "1", "v1", "null", "v2", "1.0");
        assertEquals(1, jsonObject.getIntValue("v0", 2));
        assertEquals(999, jsonObject.getIntValue("v1", 999));
        assertEquals(1, jsonObject.getIntValue("v2", 999));
        assertEquals(999, jsonObject.getIntValue("v100", 999));
    }
}
