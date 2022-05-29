package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest16_pairKey {
    @Test
    public void test_0() throws Exception {
        Bean bean = new Bean();
        bean.map = new HashMap<>();
        bean.map.put(Pair.of(1L, 2L), "12");

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertEquals(bean2.map.size(), bean2.map.size());
        assertEquals(bean2.map.keySet().iterator().next(), bean2.map.keySet().iterator().next());
    }

    @Test
    public void test_0_mutable() throws Exception {
        Bean bean = new Bean();
        bean.map = new HashMap<>();
        bean.map.put(MutablePair.of(1L, 2L), "12");

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertEquals(bean2.map.size(), bean2.map.size());
        assertEquals(bean2.map.keySet().iterator().next(), bean2.map.keySet().iterator().next());
    }

    @Test
    public void test_1() throws Exception {
        String str = "{\"left\":\"key\",\"right\":101}";
        Pair pair = JSON.parseObject(str, Pair.class);
        assertEquals("key", pair.getLeft());
        assertEquals(101, pair.getRight());
    }

    @Test
    public void test_1_jsonb() throws Exception {
        Map map = new HashMap();
        map.put("left", "key");
        map.put("right", 101);
        byte[] jsonbBytes = JSONB.toBytes(map);

        Pair pair = JSONB.parseObject(jsonbBytes, Pair.class);
        assertEquals("key", pair.getLeft());
        assertEquals(101, pair.getRight());
    }

    public static class Bean {
        public Map<Pair<Long, Long>, Object> map;
    }
}
