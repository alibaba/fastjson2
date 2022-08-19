package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue570 {
    @Test
    public void test() {
        U u = new U(180, 1);
        assertEquals("{\"a\":180,\"b\":1}", JSON.toJSONString(u, JSONWriter.Feature.FieldBased));
    }

    public static class U {
        private Integer a;
        private Integer b;

        public U(Integer a, Integer b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "U{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }
    }
}
