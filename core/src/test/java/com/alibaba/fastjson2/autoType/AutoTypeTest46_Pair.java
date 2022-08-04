package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AutoTypeTest46_Pair {
    @Test
    public void test_1() throws Exception {
        Bean bean = new Bean();
        bean.pair1 = Pair.of("101", Boolean.TRUE);
        bean.pair2 = Pair.of("102", Boolean.TRUE);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        );

        JSONBDump.dump(bytes);

        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.autoType.AutoTypeTest46_Pair$Bean#0\",\n" +
                "\t\"@value\":{\n" +
                "\t\t\"pair1#1\":{\n" +
                "\t\t\t\"@type\":\"org.apache.commons.lang3.tuple.ImmutablePair#2\",\n" +
                "\t\t\t\"@value\":{\n" +
                "\t\t\t\t\"left#3\":\"101\",\n" +
                "\t\t\t\t\"right#4\":true\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"pair2#5\":{\n" +
                "\t\t\t\"@type\":\"#2\",\n" +
                "\t\t\t\"@value\":{\n" +
                "\t\t\t\t\"#3\":\"102\",\n" +
                "\t\t\t\t\"#4\":true\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}", new JSONBDump(bytes, true).toString());

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(bean2.pair1);
        assertNotNull(bean2.pair1.getLeft());
        assertNotNull(bean2.pair1.getRight());
        assertNotNull(bean2.pair2);
        assertNotNull(bean2.pair2.getLeft());
        assertNotNull(bean2.pair2.getRight());
    }

    public static class Bean {
        public Pair<String, Boolean> pair1;
        public Pair<String, Boolean> pair2;
    }
}
