package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonPropertyOrderTest {
    @Test
    public void test() {
        Student student = new Student("Mark", 1);
        assertEquals("{\"rollNo\":1,\"name\":\"Mark\"}", JSON.toJSONString(student));
    }

    @JsonPropertyOrder({"rollNo", "name"})
    class Student {
        private String name;
        private int rollNo;

        public Student(String name, int rollNo) {
            this.name = name;
            this.rollNo = rollNo;
        }

        public String getName() {
            return name;
        }

        public int getRollNo() {
            return rollNo;
        }
    }
}
