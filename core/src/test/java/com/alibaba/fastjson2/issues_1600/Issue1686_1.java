package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1686_1 {
    @Test
    public void test_nest_map_subclass() {
        A a = new A();
        a.put("a", new B());
        a.get("a").put("1", "b");

        String jsonString = JSON.toJSONString(a);
        assertEquals(jsonString, "{\"a\":{\"1\":\"b\"}}");

        A a1 = JSON.parseObject(jsonString, A.class);
        assertEquals(a1.get("a").get("1"), "b");
    }

    public static class A
            extends HashMap<String, B> {
    }

    public static class B
            extends HashMap<String, String> {
        public String get(Integer number) {
            return get(String.valueOf(number));
        }
    }
}
