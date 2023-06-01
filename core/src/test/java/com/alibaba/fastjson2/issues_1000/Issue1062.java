package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1062 {
    @Test
    public void test() {
        Person person = new Person("张三", RoleType.TEACHER);
        String str = JSON.toJSONString(person);
        assertEquals("{\"name\":\"张三\",\"roleType\":1}", str);
    }

    @Test
    public void test1() {
        String str = JSON.toJSONString(RoleType.ADMIN);
        assertEquals("100", str);
    }

    @Data
    @ToString
    @AllArgsConstructor
    static class Person {
        private String name;
        private RoleType roleType;
    }

    @Getter
    @RequiredArgsConstructor
    enum RoleType {
        TEACHER(1, "老师"),
        STUDENT(2, "学生"),
        ADMIN(100, "行政");

        @JsonValue
        @JSONField(value = true)
        private final Integer value;
        private final String label;
    }

    @Test
    public void test2() {
        assertEquals("\"Type1\"", JSON.toJSONString(TypeA.A1));
    }

    public interface Type {
        @JSONField
        String getValue();
    }

    public enum TypeA implements Type {
        A1("Type1"),
        A2("Type2"),
        A3("Type3");

        private final String value;

        TypeA(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
