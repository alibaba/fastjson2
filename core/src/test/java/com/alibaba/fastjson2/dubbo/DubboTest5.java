package com.alibaba.fastjson2.dubbo;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DubboTest5 {
    @Test
    public void test() {
        RuntimeException ex = new RuntimeException();
        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        RuntimeException ex1 = (RuntimeException) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);
        assertNotNull(ex1);
        assertEquals(ex.getStackTrace().length, ex1.getStackTrace().length);
    }

    @Test
    public void test1() {
        RuntimeException ex = null;
        try {
            error();
        } catch (RuntimeException e) {
            ex = e;
        }
        byte[] jsonbBytes = JSONB.toBytes(ex, writerFeatures);
        RuntimeException ex1 = (RuntimeException) JSONB.parseObject(jsonbBytes, Object.class, readerFeatures);
        assertNotNull(ex1);
        assertEquals(ex.getStackTrace().length, ex1.getStackTrace().length);
    }

    void error() {
        throw new RuntimeException();
    }

    static final JSONReader.Feature[] readerFeatures = {
            JSONReader.Feature.SupportAutoType,
            JSONReader.Feature.UseDefaultConstructorAsPossible,
            JSONReader.Feature.ErrorOnNoneSerializable,
            JSONReader.Feature.UseNativeObject,
            JSONReader.Feature.FieldBased
    };

    static final JSONWriter.Feature[] writerFeatures = {
            JSONWriter.Feature.WriteClassName,
            JSONWriter.Feature.FieldBased,
            JSONWriter.Feature.ErrorOnNoneSerializable,
            JSONWriter.Feature.ReferenceDetection,
            JSONWriter.Feature.WriteNulls,
            JSONWriter.Feature.NotWriteDefaultValue,
            JSONWriter.Feature.NotWriteHashMapArrayListClassName,
            JSONWriter.Feature.WriteNameAsSymbol
    };
}
