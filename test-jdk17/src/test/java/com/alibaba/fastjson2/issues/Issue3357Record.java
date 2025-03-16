package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3357Record {
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
    sealed interface Animal permits Cat {
    }

    @JSONType(typeName = "cat")
    record Cat(String type, int age)
            implements Animal {}

    @Test
    public void cat2() {
        String s1 = "{\"age\": 20, \"type\": \"cat\"}";  // type comes second
        Cat2 c1 = (Cat2) JSON.parseObject(s1, Animal2.class);
        assertEquals("cat", c1.type);  // succeeds

        String s2 = "{\"type\": \"cat\", \"age\": 20}";  // type comes first
        Cat2 c2 = (Cat2) JSON.parseObject(s2, Animal2.class);
        assertEquals("cat", c2.type);  // fails
    }

    @JSONType(typeKey = "type")
    sealed interface Animal2 permits Cat2 {
    }

    @JSONType(typeName = "cat")
    record Cat2(String type, int age) implements Animal2 {}
}
