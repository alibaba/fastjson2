package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1513 {
    @Test
    public void test() {
        B b = new B();
        ((A) b).id = 101;
        b.id = 201;

        String str = JSON.toJSONString(b);
        assertEquals("{\"id\":201}", str);
    }

    public static class A {
        @JSONField(serialize = false)
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class B
            extends A {
        private int id;

        @Override
        public int getId() {
            return id;
        }

        @Override
        public void setId(int id) {
            this.id = id;
        }
    }
}
