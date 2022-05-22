package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest1 {
    @Test
    public void test_ref_0() {
        A a = new A();
        a.c1 = new C();
        a.c2 = a.c1;

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b.c1, b.c2);

        A a1 = JSONB.parseObject(bytes, A.class);
        assertSame(a1.c1, a1.c2);
    }

    public static class A {
        public C c1;
        public C c2;
    }

    public static class C {
    }

    private static class B {
        public C c1;
        public C c2;
    }
}
