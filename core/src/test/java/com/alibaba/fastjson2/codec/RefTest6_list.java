package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest6_list {
    @Test
    public void test_ref_0() {
        A a = new A();
        a.c = new C();
        a.values = Arrays.asList(a.c);

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b.c, b.values.get(0));

        A a1 = JSONB.parseObject(bytes, (java.lang.reflect.Type) A.class);
        assertSame(a1.c, a1.values.get(0));
    }

    @Test
    public void test_ref_0_symbol() {
        A a = new A();
        a.c = new C();
        a.values = Arrays.asList(a.c);

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        B b = JSONB.parseObject(bytes, (java.lang.reflect.Type) B.class, JSONB.symbolTable(""), JSONReader.Feature.SupportAutoType);
        assertSame(b.c, b.values.get(0));

        A a1 = JSONB.parseObject(bytes, (java.lang.reflect.Type) A.class, JSONB.symbolTable(""));
        assertSame(a1.c, a1.values.get(0));
    }

    public static class A {
        public C c;
        public List<C> values;
    }

    private static class B {
        public C c;
        public List<C> values;
    }

    public class C {
    }
}
