package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeTest36_MapLong {
    @Test
    public void test_1() throws Exception {
        Bean bean = new Bean();
        bean.values = new HashMap<>();
        bean.values.put("abc", 100L);

        byte[] bytes = JSONB.toBytes(bean,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        JSONBDump.dump(bytes);

        Bean bean2 = (Bean) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertEquals(bean.getClass(), bean2.getClass());
        assertEquals(bean.values.getClass(), bean2.values.getClass());
        assertEquals(bean.values.size(), bean2.values.size());
        assertEquals(bean.values.get("abc"), bean2.values.get("abc"));
    }

    public static class Bean {
        public Map<String, Long> values;
    }
}
