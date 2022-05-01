package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApacheTripleTest {
    @Test
    public void test_0() throws Exception {
        Bean bean = new Bean();
        bean.triple = Triple.of(101, 102, 103);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);

        Triple triple = bean2.triple;
        assertEquals(101, triple.getLeft());
        assertEquals(102, triple.getMiddle());
        assertEquals(103, triple.getRight());
    }

    @Test
    public void test_0_mutable() throws Exception {
        Bean bean = new Bean();
        bean.triple = MutableTriple.of(101, 102, 103);

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.WriteClassName, JSONWriter.Feature.FieldBased, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);

        Triple triple = bean2.triple;
        assertEquals(101, triple.getLeft());
        assertEquals(102, triple.getMiddle());
        assertEquals(103, triple.getRight());
    }

    @Test
    public void test_1() throws Exception {
        String str = "{\"left\":101,\"middle\":102,\"right\":103}";
        Triple triple = JSON.parseObject(str, Triple.class);
        assertEquals(101, triple.getLeft());
        assertEquals(102, triple.getMiddle());
        assertEquals(103, triple.getRight());
    }

    @Test
    public void test_1_jsonb() throws Exception {
        Map map = new HashMap();
        map.put("left", 101);
        map.put("middle", 102);
        map.put("right", 103);
        byte[] jsonbBytes = JSONB.toBytes(map);

        Triple triple = JSONB.parseObject(jsonbBytes, Triple.class);
        assertEquals(101, triple.getLeft());
        assertEquals(102, triple.getMiddle());
        assertEquals(103, triple.getRight());
    }

    public static class Bean {
        public Triple triple;
    }
}
