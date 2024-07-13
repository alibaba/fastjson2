package com.alibaba.fastjson2.issues_2700;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson2.JSONObject;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2795 {
    @Builder
    @Data
    public static class Student {
        @JSONField(name = "is_judge")
        private boolean isJudge;
    }

    @Test
    public void testFastJson() {
        Student student = Student.builder().isJudge(true).build();
        System.out.println(com.alibaba.fastjson.JSON.toJSONString(student));
        assertEquals("{\"is_judge\":true}", JSONObject.toJSONString(student));
    }
}
