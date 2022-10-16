package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CopyToTest {
    @Test
    public void testNull() {
        assertNull(JSON.copyTo(null, HashMap.class));
        assertNull(JSON.copyTo(null, HashMap.class, JSONWriter.Feature.FieldBased));
        assertNull(JSON.copyTo(null, HashMap.class, JSONWriter.Feature.BeanToArray));
        assertNull(JSON.copyTo(null, HashMap.class, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection));
    }

    @Test
    public void test() {
        JSONObject object = JSONObject.of("id", 101);

        LinkedHashMap map = JSON.copyTo(object, LinkedHashMap.class);
        assertEquals(map.get("id"), object.get("id"));

        LinkedHashMap map1 = JSON.copyTo(object, LinkedHashMap.class, JSONWriter.Feature.BeanToArray);
        assertEquals(map1.get("id"), object.get("id"));
    }
}
