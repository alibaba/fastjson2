package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue557 {
    static {
        ObjectReader objectReader1 = ObjectReaderImplMap.of(new TypeReference<Map<String, Object>>() {
        }.getType(), TestPublicMap.class, 0);
        JSONFactory.getDefaultObjectReaderProvider().register(TestPublicMap.class, objectReader1);

        ObjectReader objectReader2 = ObjectReaderImplMap.of(new TypeReference<Map<String, Object>>() {
        }.getType(), TestPrivateMap.class, 0);
        JSONFactory.getDefaultObjectReaderProvider().register(TestPrivateMap.class, objectReader2);
    }

    @Test
    public void test() {
        Map<String, Object> a = JSONObject.parseObject("{'a':'b'}").to(TestPublicMap.class);
        assertEquals("b", a.get("a"));

        Map<String, Object> b = JSONObject.parseObject("{'a':'b'}").to(TestPrivateMap.class);
        assertEquals("b", b.get("a"));
    }

    public static class TestPublicMap
            extends HashMap<String, Object> {
    }

    private static class TestPrivateMap
            extends HashMap<String, Object> {
    }
}
