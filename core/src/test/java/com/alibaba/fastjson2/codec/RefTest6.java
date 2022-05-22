package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest6 {
    @Test
    public void test_ref_0() {
        A a = new A();
        a.c = new C();
        a.values = new C[]{a.c};

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b.c, b.values[0]);

        A a1 = JSONB.parseObject(bytes, (Type) A.class, JSONReader.Feature.SupportAutoType);
        assertSame(a1.c, a1.values[0]);
    }

    public static class A {
        public C c;
        public C[] values;
    }

    private static class B {
        public C c;
        public C[] values;
    }

    public class C {
    }
}
