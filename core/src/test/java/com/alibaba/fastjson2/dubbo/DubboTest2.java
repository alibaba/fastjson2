package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubboTest2 {
    @Test
    public void test() {
        Map<String, Object> map = new HashMap<>();
        List<Integer> var4 = Arrays.asList(2, 4, 8);
        map.put("intList", var4);

        byte[] jsonbBytes = JSONB.toBytes(
                map,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        );

        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter(true, HashMap.class.getName(), var4.getClass().getName());

        assertEquals(
                map,
                JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        filter,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.SupportClassForName,
                        JSONReader.Feature.ErrorOnNoneSerializable,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased
                )
        );

        assertEquals(
                map,
                JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        filter,
                        JSONReader.Feature.UseDefaultConstructorAsPossible,
                        JSONReader.Feature.SupportClassForName,
                        JSONReader.Feature.ErrorOnNoneSerializable,
                        JSONReader.Feature.UseNativeObject,
                        JSONReader.Feature.FieldBased
                )
        );
    }

    @Test
    public void test1() {
        Bean bean = new Bean();
        bean.type = String.class;

        byte[] bytes = JSONB.toBytes(bean);
        Bean bean1 = JSONB.parseObject(bytes, Bean.class, JSONReader.autoTypeFilter(String.class));
        assertEquals(bean.type, bean1.type);
    }

    public static class Bean {
        public Class type;
    }
}
