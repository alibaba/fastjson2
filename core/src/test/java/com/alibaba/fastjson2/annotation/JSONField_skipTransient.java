package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.beans.Transient;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONField_skipTransient {
    @Test
    public void test_A() { // 测试 transient && public field
        A a = new A();
        a.id = 100;
        assertEquals("{}", JSON.toJSONString(a)); // base

        A2 a2 = new A2();
        a2.id = 200;
        assertEquals("{\"id\":200}", JSON.toJSONString(a2)); // 注解位于field上

        A3 a3 = new A3();
        a3.id = 300;
        assertEquals("{\"id\":300}", JSON.toJSONString(a3)); // 注解位于getter上
    }

    public static class A {
        public transient int id;
    }

    public static class A2 {
        @JSONField(skipTransient = false)
        public transient int id;
    }

    public static class A3 {
        public transient int id;

        @JSONField(skipTransient = false)
        public int getId() {
            return id;
        }
    }

    @Test
    public void test_B() { // 测试 transient && private field
        B b = new B();
        b.id = 100;
        assertEquals("{}", JSON.toJSONString(b)); // base

        B2 b2 = new B2();
        b2.id = 200;
        assertEquals("{\"id\":200}", JSON.toJSONString(b2)); // 注解位于field上

        B3 b3 = new B3();
        b3.id = 300;
        assertEquals("{\"id\":300}", JSON.toJSONString(b3)); // 注解注解位于getter上
    }

    public static class B {
        private transient int id;

        public int getId() {
            return id;
        }
    }

    public static class B2 {
        @JSONField(skipTransient = false)
        private transient int id;

        public int getId() {
            return id;
        }
    }

    public static class B3 {
        private transient int id;

        @JSONField(skipTransient = false)
        public int getId() {
            return id;
        }
    }

    @Test
    public void test_C() { // 测试 @java.beans.Transient
        C c = new C();
        c.id = 100;
        assertEquals("{}", JSON.toJSONString(c)); // base

        C2 c2 = new C2();
        c2.id = 200;
        assertEquals("{\"id\":200}", JSON.toJSONString(c2)); // 注解位于field上

        C3 c3 = new C3();
        c3.id = 300;
        assertEquals("{\"id\":300}", JSON.toJSONString(c3)); // 注解位于getter上
    }

    public static class C {
        private int id;

        @Transient
        public int getId() {
            return id;
        }
    }

    public static class C2 {
        @JSONField(skipTransient = false)
        private int id;

        @Transient
        public int getId() {
            return id;
        }
    }

    public static class C3 {
        private int id;

        @JSONField(skipTransient = false)
        @Transient
        public int getId() {
            return id;
        }
    }

    @Test
    public void test_D() { // 测试同时有 transient 和 @Transient
        D d = new D();
        d.id = 100;
        assertEquals("{}", JSON.toJSONString(d));
    }

    public static class D {
        private transient int id;

        @Transient
        public int getId() {
            return id;
        }
    }

    @Test
    public void test_E() { // 测试显式设置skipTransient=true
        E e = new E();
        e.id = 200;
        assertEquals("{}", JSON.toJSONString(e));
    }

    public static class E {
        @JSONField(skipTransient = true)
        public transient int id;
    }

    @Test
    public void test_F() { // 测试非transient字段设置skipTransient=false
        F f = new F();
        f.id = 100;
        assertEquals("{\"id\":100}", JSON.toJSONString(f));
    }

    public static class F {
        @JSONField(skipTransient = false)
        public int id;
    }

    @Test
    public void test_Inheritance() { // 测试继承场景
        Child child = new Child();
        child.setId(100);
        assertEquals("{\"id\":100}", JSON.toJSONString(child));
    }

    public static class Parent {
        private transient int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class Child
            extends Parent {
        @Override
        @JSONField(skipTransient = false)
        public int getId() {
            return super.getId();
        }
    }
}
