package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3357 {
    @Test
    public void cat() {
        String s1 = "{\"age\": 20, \"type\": \"cat\"}";  // type comes second
        Cat c1 = (Cat) JSON.parseObject(s1, Animal.class);
        assertEquals("cat", c1.type);  // succeeds

        String s2 = "{\"type\": \"cat\", \"age\": 20}";  // type comes first
        Cat c2 = (Cat) JSON.parseObject(s2, Animal.class);
        assertEquals("cat", c2.type);  // fails
    }

    @JSONType(typeKey = "type", seeAlso = Cat.class)
    interface Animal {
    }

    @JSONType(typeName = "cat")
    static class Cat
            implements Animal {
        private final String type;
        private final int age;

        public Cat(String type, int age) {
            this.type = type;
            this.age = age;
        }

        public String getType() {
            return type;
        }

        public int getAge() {
            return age;
        }
    }
}
