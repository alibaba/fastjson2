package com.alibaba.fastjson2.read.type;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapTest {
    @Test
    public void hashMap() {
        String str = "{\"value\":123}";
        HashMap<String, String> hashMap = JSON.parseObject(str, new HashMap<String, String>() {}.getClass());
        assertEquals("123", hashMap.get("value"));
    }

    @Test
    public void treeMap() {
        String str = "{\"value\":123}";
        TreeMap<String, String> hashMap = JSON.parseObject(str, new TreeMap<String, String>() {}.getClass());
        assertEquals("123", hashMap.get("value"));
    }
}
