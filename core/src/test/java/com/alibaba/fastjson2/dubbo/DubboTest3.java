package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubboTest3 {
    @Test
    public void test() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> inner = new HashMap<>();
        map.put("innerObject", inner);
        inner.put("innerA", "v1");
        inner.put("innerB", Integer.valueOf(234));
        inner.put("class", "org.apache.dubbo.service.ComplexObject$InnerObject");

        byte[] jsonbBytes = JSONB.toBytes(map, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        JSONBDump.dump(jsonbBytes);

        Object o = JSONB.parseObject(jsonbBytes, Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.SupportClassForName,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);

        assertEquals(map, o);
    }
}
