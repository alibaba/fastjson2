package com.alibaba.fastjson2.v1issues.issue_4300;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue4316 {
    @Test
    public void test() {
        Animal animal = JSON.parseObject("{\"val\":1,\"type\":\"A\"}", Animal.class);
        assertTrue(animal instanceof Cat);
        Cat cat = (Cat) animal;
        assertEquals(1, cat.val);
    }

    @JSONType(typeKey = "type", seeAlso = Cat.class)
    public interface Animal {
    }

    @JSONType(typeKey = "type", typeName = "A", serialzeFeatures = SerializerFeature.WriteClassName)
    public static class Cat
            implements Animal {
        int val;

        public Cat(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }
    }
}
