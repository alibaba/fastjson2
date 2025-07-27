package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.beans.Transient;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONType_skipTransient { // 测试 transient && public field
    @Test
    public void test_A() {
        A a = new A();
        a.id = 100;
        a.name = "fast";
        assertEquals("{}", JSON.toJSONString(a));

        A2 a2 = new A2();
        a2.id = 100;
        a2.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(a2));

        // 以下四个测试优先级
        A3 a3 = new A3();
        a3.id = 100;
        a3.name = "fast";
        assertEquals("{}", JSON.toJSONString(a3));

        A4 a4 = new A4();
        a4.id = 100;
        a4.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(a4));

        A5 a5 = new A5();
        a5.id = 100;
        a5.name = "fast";
        assertEquals("{\"id\":100}", JSON.toJSONString(a5));

        A6 a6 = new A6();
        a6.id = 100;
        a6.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(a6));
    }

    @JSONType(skipTransient = true)
    public static class A {
        public transient int id;
        public transient String name;
    }

    @JSONType(skipTransient = false)
    public static class A2 {
        public transient int id;
        public transient String name;
    }

    @JSONType(skipTransient = true)
    public static class A3 {
        @JSONField(skipTransient = true)
        public transient int id;
        public transient String name;
    }

    @JSONType(skipTransient = false) // 优先级：类 > 字段
    public static class A4 {
        @JSONField(skipTransient = true)
        public transient int id;
        public transient String name;
    }

    @JSONType(skipTransient = true) // 优先级：字段 > 类
    public static class A5 {
        @JSONField(skipTransient = false)
        public transient int id;
        public transient String name;
    }

    @JSONType(skipTransient = false)
    public static class A6 {
        @JSONField(skipTransient = false)
        public transient int id;
        public transient String name;  // 优先级：类 > 字段
    }

    @Test
    public void test_B() { // 测试 transient && private field
        B b = new B();
        b.id = 100;
        b.name = "fast";
        assertEquals("{}", JSON.toJSONString(b));

        B2 b2 = new B2();
        b2.id = 100;
        b2.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(b2));

        // 以下4个测试优先级
        B3 b3 = new B3();
        b3.id = 100;
        b3.name = "fast";
        assertEquals("{}", JSON.toJSONString(b3));

        B4 b4 = new B4();
        b4.id = 100;
        b4.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(b4));

        B5 b5 = new B5();
        b5.id = 100;
        b5.name = "fast";
        assertEquals("{\"id\":100}", JSON.toJSONString(b5));

        B6 b6 = new B6();
        b6.id = 100;
        b6.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(b6));

        // 以下2个测试 方法上的@JSONField
        B7 b7 = new B7();
        b7.id = 100;
        b7.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(b7));

        B8 b8 = new B8();
        b8.id = 100;
        b8.name = "fast";
        assertEquals("{\"id\":100}", JSON.toJSONString(b8));
    }

    @JSONType(skipTransient = true)
    public static class B {
        private transient int id;
        private transient String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class B2 {
        private transient int id;
        private transient String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = true)
    public static class B3 {
        @JSONField(skipTransient = true)
        private transient int id;
        private transient String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class B4 {
        @JSONField(skipTransient = true)
        private transient int id;
        private transient String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = true)
    public static class B5 {
        @JSONField(skipTransient = false)
        private transient int id;
        private transient String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class B6 {
        @JSONField(skipTransient = false)
        private transient int id;
        private transient String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class B7 {
        private transient int id;
        private transient String name;

        @JSONField(skipTransient = true)
        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = true)
    public static class B8 {
        private transient int id;
        private transient String name;

        @JSONField(skipTransient = false)
        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void test_C() { // 测试 @java.beans.Transient
        C c = new C();
        c.id = 100;
        c.name = "fast";
        assertEquals("{}", JSON.toJSONString(c));

        C2 c2 = new C2();
        c2.id = 100;
        c2.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(c2));

        // 以下4个测试优先级
        C3 c3 = new C3();
        c3.id = 100;
        c3.name = "fast";
        assertEquals("{}", JSON.toJSONString(c3));

        C4 c4 = new C4();
        c4.id = 100;
        c4.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(c4));

        C5 c5 = new C5();
        c5.id = 100;
        c5.name = "fast";
        assertEquals("{\"id\":100}", JSON.toJSONString(c5));

        C6 c6 = new C6();
        c6.id = 100;
        c6.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(c6));

        // 以下2个测试 方法上的@JSONField
        C7 c7 = new C7();
        c7.id = 100;
        c7.name = "fast";
        assertEquals("{\"id\":100}", JSON.toJSONString(c7));

        C8 c8 = new C8();
        c8.id = 100;
        c8.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(c8));
    }

    @JSONType(skipTransient = true)
    public static class C {
        private int id;
        private String name;

        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class C2 {
        private int id;
        private String name;

        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = true)
    public static class C3 {
        @JSONField(skipTransient = true)
        private int id;
        private String name;

        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class C4 {
        @JSONField(skipTransient = true)
        private int id;
        private String name;

        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = true)
    public static class C5 {
        @JSONField(skipTransient = false)
        private int id;
        private String name;

        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class C6 {
        @JSONField(skipTransient = false)
        private int id;
        private String name;

        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = true)
    public static class C7 {
        private int id;
        private String name;

        @JSONField(skipTransient = false)
        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = false)
    public static class C8 {
        private int id;
        private String name;

        @JSONField(skipTransient = true)
        @Transient
        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }
}
