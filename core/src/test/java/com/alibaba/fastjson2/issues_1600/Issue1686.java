package com.alibaba.fastjson2.issues_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class Issue1686 {
    @Test
    public void test_nest_map_subclass() {
        A a = new A();
        a.put("a", new B());
        a.get("a").put("b", "1");

        String jsonString = JSON.toJSONString(a);
        assert jsonString.equals("{\"a\":{\"b\":\"1\"}}");

        A a1 = JSON.parseObject(jsonString, A.class);
        // java.lang.ClassCastException: com.alibaba.fastjson2.JSONObject cannot be cast to B
        assert a1.get("a").get("b").equals("1");
    }

    public static class A
            extends HashMap<String, B> {
    }

    public static class B
            extends HashMap<String, String> {
    }
}
