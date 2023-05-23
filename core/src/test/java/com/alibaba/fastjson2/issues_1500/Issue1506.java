package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1506 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"a\":{}}", Bean.class);
        assertEquals(A2.class, bean.type);
    }

    public static class Bean {
        private Class type;

        public void setA(A a) {
            type = a.getClass();
        }

        public void setA(A1 a) {
            type = a.getClass();
        }

        @JSONField
        public void setA(A2 a) {
            type = a.getClass();
        }

        public void setA(A3 a) {
            type = a.getClass();
        }

        public void setA(A4 a) {
            type = a.getClass();
        }

        public void setA(A5 a) {
            type = a.getClass();
        }
    }

    public static class A {
    }

    public static class A1 {
    }

    public static class A2 {
    }

    public static class A3 {
    }

    public static class A4 {
    }

    public static class A5 {
    }
}
