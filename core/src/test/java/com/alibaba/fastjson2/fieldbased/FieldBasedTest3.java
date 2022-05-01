package com.alibaba.fastjson2.fieldbased;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldBasedTest3 {
    @Test
    public void test_0() {
        A a = new A(101);

        String str = JSON.toJSONString(a, JSONWriter.Feature.FieldBased);
        assertEquals("{\"id\":101}", str);
        A a1 = JSON.parseObject(str, A.class, JSONReader.Feature.FieldBased);
        assertEquals(a.id, a1.id);
    }
    public static class A {
        private int id;
        private A() {

        }

        private A(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
