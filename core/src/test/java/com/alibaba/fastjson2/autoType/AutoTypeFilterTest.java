package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoTypeFilterTest {
    @Test
    public void testSet() {
        HashSet set = new HashSet();
        set.add(123);
        byte[] bytes = JSONB.toBytes(set, JSONWriter.Feature.WriteClassName);

        assertEquals(
                set,
                JSONB.parseObject(
                        bytes, Object.class,
                        JSONReader.autoTypeFilter(HashSet.class)
                )
        );

        assertEquals(
                set,
                JSONB.parseObject(
                        bytes, Object.class,
                        JSONReader.autoTypeFilter("Set")
                )
        );

        assertEquals(
                set,
                JSONB.parseObject(
                        bytes, Object.class,
                        JSONReader.autoTypeFilter("HashSet")
                )
        );
    }

    @Test
    public void test1() {
        Object[] array = {"0", "1"};
        byte[] jsonbBytes = JSONB.toBytes(array, JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);

        JSONReader.Feature[] features = {
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.SupportClassForName,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased
        };

        assertArrayEquals(
                array,
                (Object[]) JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        JSONReader.autoTypeFilter(String.class, Object[].class),
                        features
                )
        );

        assertArrayEquals(
                array,
                (Object[]) JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        JSONReader.autoTypeFilter(String.class, Object.class),
                        features
                )
        );

        assertArrayEquals(
                array,
                (Object[]) JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        JSONReader.autoTypeFilter(String.class.getName(), Object[].class.getName()),
                        features
                )
        );

        assertArrayEquals(
                array,
                (Object[]) JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        JSONReader.autoTypeFilter(String.class.getName(), Object.class.getName()),
                        features
                )
        );

        assertArrayEquals(
                array,
                (Object[]) JSONB.parseObject(
                        jsonbBytes,
                        Object.class,
                        new ContextAutoTypeBeforeHandler(true),
                        features
                )
        );
    }
}
