package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1520 {
    @Test
    public void test() {
        String json = "{\"testa\":[{\"name\":\"test\"}, {\"name\":\"test2\"}]}";
        TestB testB1 = com.alibaba.fastjson2.JSON.parseObject(json, new TypeReference<TestB>(){});
        assertEquals(TestA.class.getName(), testB1.getTesta().get(0).getClass().getName());
    }

    @Data
    public class TestB {
        private LinkedList<TestA> testa;
    }

    @Data
    public class TestA {
        private String name;
    }
}
