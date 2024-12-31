package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3134 {
    @Test
    public void test() {
        String json = "{\"name\":\"希望小学\",\"students\":{\"age\":\"12\",\"id\":\"1\",\"name\":\"张宇\"}}";
        School schoolOne = JSON.parseObject(json, School.class);
        JSONObject schoolTwo = JSON.parseObject(json);
        School schoolThree = schoolTwo.toJavaObject(School.class);
        JSONObject studentOne = schoolTwo.getJSONObject("students");
        JSONArray studentTwo = schoolTwo.getJSONArray("students");
        assertEquals("[{\"age\":\"12\",\"id\":\"1\",\"name\":\"张宇\"}]", studentTwo.toString());
    }

    @Data
    private static class School{
        private String name;
        private List<Student> students;
    }

    @Data
    private static class Student{
        private String name;
        private String id;
        private String age;
    }
}
