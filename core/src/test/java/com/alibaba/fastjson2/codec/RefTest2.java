package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest2 {
    @Test
    public void test_ref_0() {
        A a = new A();
        a.c1 = new C();
        a.c1.root = a;

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b, b.c1.root);

        A a1 = JSONB.parseObject(bytes, A.class);
        assertSame(a1, a1.c1.root);
    }

    public static class A {
        public C c1;
    }

    public static class C {
        public A root;
    }

    public static class D {
        public B root;
    }

    private static class B {
        public D c1;
    }
}
