package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
