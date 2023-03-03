package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionTest {
    @Test
    public void test_exception() {
        Object[] objects = new Object[]{new RuntimeException(), new IOException()};
        JSONWriter.Feature[] features = {
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        };
        byte[] bytes = JSONB.toBytes(objects, features);

//        System.out.println(JSONB.toJSONString(bytes));

        Object[] parsed = JSONB.parseObject(bytes, objects.getClass(), JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertEquals(objects.length, parsed.length);
    }

    @Test
    public void test_exception1() {
        Exception exception = null;
        try {
            error();
        } catch (Exception e) {
            exception = e;
        }
        Object[] objects = new Object[]{exception};
        JSONWriter.Feature[] features = {
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol
        };
        byte[] bytes = JSONB.toBytes(objects, features);

//        System.out.println(JSONB.toJSONString(bytes));

        Object[] parsed = JSONB.parseObject(bytes, objects.getClass(), JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
        assertEquals(objects.length, parsed.length);
    }

    public void error() throws Exception {
        try {
            errorNull();
        } catch (Exception e) {
            throw new MyException(e);
        }
    }

    public void errorNull() {
        throw new NullPointerException();
    }

    public static class MyException
            extends Exception {
        public MyException(Throwable cause) {
            super(cause);
        }
    }
}
