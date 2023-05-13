package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1479 {
    @Test
    public void test() {
        JSON.register(Gender.class, new ObjectWriter<Object>() {
            @Override
            public void write(JSONWriter writer, Object value, Object fieldName, Type type, long feature) {
                Gender gender = (Gender) value;
                Map<String, Object> result = new HashMap<>();
                result.put("value", gender.getGender());
                result.put("remark", gender.getRemark());
                writer.write(result);
            }
        });

        Student student = new Student();
        student.setName("张三");
        student.setGender(Gender.MALE);
        assertEquals("{\"gender\":{\"remark\":\"male\",\"value\":\"M\"},\"name\":\"张三\"}", JSON.toJSONString(student));
    }

    public enum Gender {
        MALE("M", "男", "male") {
            @Override
            public void test() {
            }
        },
        FEMALE("F", "女", "female") {
            @Override
            public void test() {
            }
        };

        private final String gender;
        private final String name;
        private final String remark;

        Gender(String gender, String name, String remark) {
            this.gender = gender;
            this.name = name;
            this.remark = remark;
        }

        public String getRemark() {
            return remark;
        }

        public String getGender() {
            return gender;
        }

        public String getName() {
            return name;
        }

        public abstract void test();
    }

    @Data
    public static class Student {
        private String name;
        private Integer age;
        private Gender gender;
    }
}
