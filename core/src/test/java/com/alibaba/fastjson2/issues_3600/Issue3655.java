package com.alibaba.fastjson2.issues_3600;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3655 {
    @Test
    public void test() {
//        JSON.config(JSONWriter.Feature.FieldBased);
        A a = new A();
        a.id = 100;
        assertEquals("{}", JSON.toJSONString(a));
//        assertEquals("{}", JSON.toJSONString(a, JSONWriter.Feature.FieldBased));
    }

    public static class A {
        @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased)
        private transient int id;

//        @JSONField(serializeFeatures = JSONWriter.Feature.FieldBased)
//        public int getId() {
//            return id;
//        }
//
//        public void setId(int id) {
//            this.id = id;
//        }
    }
}
