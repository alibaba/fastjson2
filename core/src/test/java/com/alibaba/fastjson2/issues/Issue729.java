package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue729 {
    @Test
    public void test() {
        A a = new A();
        B b = new B();
        a.b = b;
        b.a = a;

        String str = JSON.toJSONString(a, JSONWriter.Feature.ReferenceDetection);
        assertEquals("{\"b\":{\"a\":{\"$ref\":\"$\"}}}", str);
        A a1 = JSON.parseObject(str, A.class);
        assertSame(a1, a1.b.a);
    }

    private static class A {
        @JsonManagedReference
        private B b;

        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }
    }

    private static class B {
        @JsonManagedReference
        private A a;

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }
    }
}
