package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * https://github.com/alibaba/fastjson2/issues/7746
 */
public class Issue7746 {
    public static class SelfBean {
        public String name;
        public SelfBean self;
    }

    @Test
    public void test() {
        String[] names = {"a", "b"};

        byte[] bytes = JSONB.toBytes(selfReferencingList(names), JSONWriter.Feature.ReferenceDetection);
        List<SelfBean> parsed = JSONB.parseObject(
                bytes,
                new TypeReference<List<SelfBean>>() {
                }.getType()
        );
        assertSelfReferencesPreserved(parsed, names);
    }

    @Test
    public void testFieldBased() {
        String[] names = {"a", "b"};

        byte[] bytes = JSONB.toBytes(
                selfReferencingList(names),
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.FieldBased
        );
        List<SelfBean> parsed = JSONB.parseObject(
                bytes,
                new TypeReference<List<SelfBean>>() {
                }.getType(),
                JSONReader.Feature.FieldBased
        );
        assertSelfReferencesPreserved(parsed, names);
    }

    /**
     * Consecutive ".." self-references must remain intact for every element, not only the first.
     */
    @Test
    public void testMultipleElements() {
        String[] names = {"a", "b", "c"};

        byte[] bytes = JSONB.toBytes(selfReferencingList(names), JSONWriter.Feature.ReferenceDetection);
        List<SelfBean> parsed = JSONB.parseObject(
                bytes,
                new TypeReference<List<SelfBean>>() {
                }.getType()
        );
        assertSelfReferencesPreserved(parsed, names);
    }

    /**
     * Covers ObjectWriterImplMap's writeReference("..") path, which Bean field writers do not hit.
     */
    @Test
    public void testMapSelfReference() {
        String[] names = {"a", "b", "c"};
        List<Map<String, Object>> list = new ArrayList<>();
        for (String name : names) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("self", map);
            list.add(map);
        }

        byte[] bytes = JSONB.toBytes(list, JSONWriter.Feature.ReferenceDetection);
        List<Map<String, Object>> parsed = JSONB.parseObject(
                bytes,
                new TypeReference<List<Map<String, Object>>>() {
                }.getType()
        );
        assertEquals(names.length, parsed.size());
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> map = parsed.get(i);
            assertEquals(names[i], map.get("name"));
            assertSame(map, map.get("self"));
        }
    }

    /**
     * Covers ASM {@code JSONB.IO.writeReference}: Collection-typed bean fields that share one
     * instance go through that path under ReferenceDetection, not JSONWriterJSONB.writeReference.
     */
    @Test
    public void testAsmCollectionFieldRefs() {
        List<String> shared = new ArrayList<>();
        shared.add("x");

        CollectionRefBean bean = new CollectionRefBean();
        bean.first = shared;
        bean.second = shared;
        bean.third = shared;

        byte[] bytes = JSONB.toBytes(bean, JSONWriter.Feature.ReferenceDetection);
        String dump = JSONB.toJSONString(bytes);
        assertTrue(dump.contains("#-1"), dump);

        CollectionRefBean parsed = JSONB.parseObject(bytes, CollectionRefBean.class);
        assertSame(parsed.first, parsed.second);
        assertSame(parsed.first, parsed.third);
        assertEquals(1, parsed.first.size());
        assertEquals("x", parsed.first.get(0));
    }

    public static class CollectionRefBean {
        public List<String> first;
        public List<String> second;
        public List<String> third;
    }

    static List<SelfBean> selfReferencingList(String... names) {
        List<SelfBean> list = new ArrayList<>();
        for (String name : names) {
            SelfBean bean = new SelfBean();
            bean.name = name;
            bean.self = bean;
            list.add(bean);
        }
        return list;
    }

    static void assertSelfReferencesPreserved(List<SelfBean> parsed, String... names) {
        assertEquals(names.length, parsed.size());
        for (int i = 0; i < names.length; i++) {
            SelfBean bean = parsed.get(i);
            assertEquals(names[i], bean.name);
            assertSame(bean, bean.self);
        }
    }
}
