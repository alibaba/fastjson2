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
        STUDENT(2, "学生");

        @JsonValue
        @JSONField(value = true)
        private final Integer value;
        private final String label;
    }
}
