package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest4 {
    @Test
    public void test_ref_0() {
        A a = new A();
        a.values = Arrays.asList(a);

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b, b.values.get(0));

        A a1 = JSONB.parseObject(bytes, 0, bytes.length, A.class);
        assertSame(a1, a1.values.get(0));

        A a2 = JSONB.parseObject(bytes, 0, bytes.length, A.class, JSONReader.Feature.SupportAutoType);
        assertSame(a2, a2.values.get(0));

        A a3 = JSONB.parseObject(bytes, 0, bytes.length, (Type) A.class, JSONReader.Feature.SupportAutoType);
        assertSame(a3, a3.values.get(0));

        A a4 = JSONB.parseObject(bytes, 0, bytes.length, (Type) A.class, JSONB.symbolTable(""));
        assertSame(a4, a4.values.get(0));

        A a5 = JSONB.parseObject(bytes, 0, bytes.length, A.class, JSONB.symbolTable(""), JSONReader.Feature.SupportAutoType);
        assertSame(a5, a5.values.get(0));

        A a6 = JSONB.parseObject(bytes, 0, bytes.length, (Type) A.class, JSONB.symbolTable(""), JSONReader.Feature.SupportAutoType);
        assertSame(a6, a6.values.get(0));
    }

    public static class A {
        public List<A> values;
    }

    private static class B {
        public List<B> values;
    }
}
