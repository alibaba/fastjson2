package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue554 {
    @Test
    public void test() {
        Tuple2<A, B> data = Tuples.of(new A("a"), new B("b"));
        assertEquals(
                "{\"t1\":{\"a\":\"a\"},\"t2\":{\"b\":\"b\"}}",
                JSON.toJSONString(data)
        );
    }

    static class A {
        private String a;

        public A(String a) {
            this.a = a;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }
    }

    static class B {
        private String b;

        public B(String b) {
            this.b = b;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }
    }
}
