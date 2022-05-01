package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class RefTest0 {
    @Test
    public void test_ref_0() {
        A a = new A();
        a.ref = a;

        byte[] bytes = JSONB.toBytes(a, JSONWriter.Feature.ReferenceDetection);

        JSONBDump.dump(bytes);

        B b = JSONB.parseObject(bytes, B.class);
        assertSame(b, b.ref);

        A a1 = JSONB.parseObject(bytes, A.class);
        assertSame(a1, a1.ref);
    }

    public static class A {
        private A ref;

        public A getRef() {
            return ref;
        }

        public void setRef(A ref) {
            this.ref = ref;
        }
    }

    private static class B {
        public B ref;
    }
}
