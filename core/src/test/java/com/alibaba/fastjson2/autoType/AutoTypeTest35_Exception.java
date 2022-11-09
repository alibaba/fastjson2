package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class AutoTypeTest35_Exception {
    @Test
    public void test_1() throws Exception {
        Exception error = new UncheckedIOException("xxx", new IOException());
        error.getStackTrace();

        byte[] bytes = JSONB.toBytes(error,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName
        );

        Exception error2 = (Exception) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(error2.getStackTrace());
        assertEquals(error.getStackTrace().length, error2.getStackTrace().length);
        assertEquals(error.getClass(), error2.getClass());
    }

    @Test
    public void test_2() throws Exception {
        String str = "kmVqYXZhLmlvLlVuY2hlY2tlZElPRXhjZXB0aW9uAKZ/TmNhdXNlAZJcamF2YS5pby5JT0V4Y2VwdGlvbgKmfwGTSy4uf1NzdGFja1RyYWNlA5R/XXN1cHByZXNzZWRFeGNlcHRpb25zBJJoamF2YS51dGlsLkNvbGxlY3Rpb25zJEVtcHR5TGlzdAWUpX9WZGV0YWlsTWVzc2FnZQZMeHh4fwOTWyQuY2F1c2Uuc3RhY2tUcmFjZX8Ek2UkLmNhdXNlLnN1cHByZXNzZWRFeGNlcHRpb25zpQ==";
        byte[] bytes = Base64.getDecoder().decode(str);

        Exception error2 = (Exception) JSONB.parseObject(
                bytes,
                Object.class,
                JSONReader.Feature.SupportAutoType,
                JSONReader.Feature.FieldBased,
                JSONReader.Feature.UseNativeObject
        );

        assertNotNull(error2.getStackTrace());
    }
}
