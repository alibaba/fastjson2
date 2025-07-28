package com.alibaba.fastjson2.write;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import java.beans.Transient;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SkipTransientTest2 { // 模拟命令行测试
    @Test
    public void test() {
        System.setProperty("fastjson2.writer.skipTransient", "false");
        JSONFactory.getDefaultObjectWriterProvider().clear();

        A a = new A();
        a.id = 100;
        a.name = "fast";
        assertEquals("{\"id\":100,\"name\":\"fast\"}", JSON.toJSONString(a));

        A2 a2 = new A2();
        a2.id = 100;
        assertEquals("{\"id\":100}", JSON.toJSONString(a2));

        A3 a3 = new A3();
        a3.id = 100;
        assertEquals("{\"id\":100}", JSON.toJSONString(a3));

        A4 a4 = new A4();
        a4.id = 100;
        assertEquals("{\"id\":100}", JSON.toJSONString(a4));

        A5 a5 = new A5();
        a5.id = 100;
        assertEquals("{\"id\":100}", JSON.toJSONString(a5));
    }

//    @Test
//    public void test_2() {
//        System.setProperty("fastjson2.writer.skipTransient", "true");
//        JSONFactory.getDefaultObjectWriterProvider().clear();
//
//        A a = new A();
//        a.id = 100;
//        a.name = "fast";
//        assertEquals("{}", JSON.toJSONString(a));
//
//        A2 a2 = new A2();
//        a2.id = 100;
//        assertEquals("{}", JSON.toJSONString(a2));
//
//        A3 a3 = new A3();
//        a3.id = 100;
//        assertEquals("{}", JSON.toJSONString(a3));
//
//        A4 a4 = new A4();
//        a4.id = 100;
//        assertEquals("{}", JSON.toJSONString(a4));
//
//        A5 a5 = new A5();
//        a5.id = 100;
//        assertEquals("{}", JSON.toJSONString(a5));
//    }

    public static class A {
        private transient int id;
        private String name;

        public int getId() {
            return id;
        }

        @Transient
        public String getName() {
            return name;
        }
    }

    @JSONType(skipTransient = true)
    public static class A2 {
        private transient int id;

        public int getId() {
            return id;
        }
    }

    @JSONType(skipTransient = true)
    public static class A3 {
        @JSONField(skipTransient = true)
        private transient int id;

        public int getId() {
            return id;
        }
    }

    @JSONType(skipTransient = true)
    public static class A4 {
        @JSONField(skipTransient = true)
        private int id;

        @Transient
        public int getId() {
            return id;
        }
    }

    @JSONType(skipTransient = true)
    public static class A5 {
        private int id;

        @JSONField(skipTransient = true)
        @Transient
        public int getId() {
            return id;
        }
    }
}
