package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Differ;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest15_noneStringKey {
    @Test
    public void test_0() throws Exception {
        LinkedHashMap object = new LinkedHashMap();
        object.put(1L, "a");

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        LinkedHashMap object2 = (LinkedHashMap) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(object2);
        assertEquals(object.get(1L), object2.get(1L));

        Differ.diff(object, object2);
    }

    @Test
    public void test_1() throws Exception {
        LinkedHashMap object = new LinkedHashMap();
        object.put(String.class, "S");
        object.put(String.class, "S");

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        LinkedHashMap object2 = (LinkedHashMap) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased, JSONReader.Feature.SupportClassForName);
        assertNotNull(object2);
        assertEquals(object.get(String.class), object2.get(String.class));

        Differ.diff(object, object2);
    }

    @Test
    public void test_3() throws Exception {
        Map object = new HashMap();

        object.put(new HashSet<>(), "A");
        object.put(Collections.emptyMap(), "B");
        object.put(new HashMap<>(), "C");

        HashMap map = new HashMap();
        map.put(new HashMap<>(), "D");
        object.put(map, "E");

        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Map object2 = (Map) JSONB.parseObject(bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );
        assertNotNull(object2);
        assertEquals(object.getClass(), object2.getClass());
        assertEquals(object.get(String.class), object2.get(String.class));

        Differ.diff(object, object2);
    }
}
