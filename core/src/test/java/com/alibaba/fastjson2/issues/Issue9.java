package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue9 {
    @Test
    public void test_ordinal() {
        Product p = new Product();
        p.id = 101;
        p.name = "DataWorks";

        String str = JSON.toJSONString(p);
        assertEquals("{\"name\":\"DataWorks\",\"id\":101}", str);
    }

    public static class Product {
        @JSONField(ordinal = 2)
        public int id;

        @JSONField(ordinal = 1)
        public String name;
    }

    @Test
    public void test_ordinal_1x() {
        B b = new B();
        b.id = 101;
        b.name = "DataWorks";

        String str = JSON.toJSONString(b);
        assertEquals("{\"BName\":\"DataWorks\",\"id\":101}", str);
        B b1 = JSON.parseObject(str, B.class);
        assertEquals(b.id, b1.id);
        assertEquals(b.name, b1.name);
    }

    public static class B {
        @com.alibaba.fastjson.annotation.JSONField(ordinal = 2)
        public int id;

        @com.alibaba.fastjson.annotation.JSONField(ordinal = 1, name = "BName", parseFeatures = com.alibaba.fastjson.parser.Feature.SupportAutoType)
        public String name;
    }
}
