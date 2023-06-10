package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AutoTypeTest26 {
    @Test
    public void test_1() throws Exception {
        TreeMap<String, Object> treeMap = new TreeMap<>();
        Bean1 bean = new Bean1();
        bean.items = Collections.unmodifiableMap(treeMap);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue
        );

        JSONB.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.items);

        assertSame(bean.items.getClass(), bean2.items.getClass());
    }

    @Test
    public void test_emptyMap() throws Exception {
        Bean1 bean = new Bean1();
        bean.items = Collections.emptyMap();

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue
        );

        JSONB.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.items);

        assertSame(bean.items.getClass(), bean2.items.getClass());
    }

    @Test
    public void test_singletonMap() throws Exception {
        Bean1 bean = new Bean1();
        bean.items = Collections.singletonMap("a", 1);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue
        );

        JSONB.dump(bytes);

        Bean1 bean2 = (Bean1) JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.FieldBased);
        assertNotNull(bean2);
        assertNotNull(bean2.items);

        assertSame(bean.items.getClass(), bean2.items.getClass());
    }

    public static class Bean1 {
        Map<String, Object> items;
    }
}
