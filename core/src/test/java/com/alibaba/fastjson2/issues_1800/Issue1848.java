package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONPath;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1848 {
    @Test
    public void test() {
        Person person = new Person();
        person.setId("1");
        person.setAge(10);
        Object eval = JSONPath.eval(person, "@[?age=10]");
        assertNotNull(eval);
    }

    @Data
    public static class Person {
        private String id;
        private int age;
    }
}
