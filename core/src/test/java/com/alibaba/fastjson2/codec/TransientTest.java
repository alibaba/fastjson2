package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.beans.Transient;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransientTest {
    @Test
    public void test_0() {
        A a = new A();
        a.id = 100;
        assertEquals("{}", JSON.toJSONString(a));
    }

    @Test
    public void test_1() {
        B b = new B();
        b.id = 100;
        assertEquals("{}", JSON.toJSONString(b));
    }

    @Test
    public void test_2() {
        C c = new C();
        c.id = 100;
        assertEquals("{}", JSON.toJSONString(c));
    }

    public static class A {
        public transient int id;
    }

    public static class B {
        private transient int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class C {
        private int id;

        @Transient
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
