package com.alibaba.fastjson2.reader;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectReaderImplMapTypedTest {
    @Test
    public void test() {
        ObjectReaderImplMapTyped reader = new ObjectReaderImplMapTyped(
                Map.class,
                TreeMap.class,
                String.class,
                Long.class,
                0,
                null
        );
        HashMap map = new HashMap();
        map.put("value", null);
        TreeMap instance = (TreeMap) reader.createInstance(map, 0);
        assertEquals(1, instance.size());
        assertNull(instance.get("value"));
    }
}
