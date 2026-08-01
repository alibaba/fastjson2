package com.alibaba.fastjson2;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3849 {
    @Test
    public void test() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(1, "1");
        map.put(2, "2");
        assertEquals("{\"1\":\"1\",\"2\":\"2\"}", JSONObject.toJSON(map).toString());
    }
}
