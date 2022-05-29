package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class Issue27 {
    @Test
    public void test_parse() {
        char a = 0x5c;
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("1", a);

        String string = JSON.toJSONString(hashMap);
        Assertions.assertEquals("{\"1\":\"\\\\\"}", string);
        JSON.parse(string);
    }

    @Test
    public void test_parse1() {
        char a = '"';
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("1", a);

        String string = JSON.toJSONString(hashMap);
        Assertions.assertEquals("{\"1\":\"\\\"\"}", string);
        JSON.parse(string);
    }
}
