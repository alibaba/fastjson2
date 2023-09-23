package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Issue1857 {

    @Test
    public void test() {
        Map<String, String> map = new HashMap<>();

        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        byte[] bytes = JSONB.toBytes(map.keySet(),
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        Collection<String> result = JSONB.parseObject(bytes, Collection.class, new Handler(),
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.FieldBased);
        assertEquals(result, map.keySet());
    }

    private static class Handler extends ContextAutoTypeBeforeHandler {

        public Handler() {
            super(true, new String[0]);
        }

    }

}
