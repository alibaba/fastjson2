package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest5 {
    @Test
    public void test_ref_0() {
        A a = new A();
        a.values = new A[]{a};

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b, b.values[0]);

        A a1 = JSONB.parseObject(bytes, A.class);
        assertSame(a1, a1.values[0]);
    }

    public static class A {
        public A[] values;
    }

    private static class B {
        public B[] values;
    }
}
