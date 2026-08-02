package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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
