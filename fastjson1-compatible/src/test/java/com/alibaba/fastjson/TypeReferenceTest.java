package com.alibaba.fastjson;

import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeReferenceTest {
    @Test
    public void test() {
        Type type0 = new TypeReference<List<Integer>>() {}.getType();
        Type type1 = new TypeReference<List<Integer>>() {}.getType();
        assertEquals(type0, type1);
        assertEquals(type0.hashCode(), type1.hashCode());
    }
}
