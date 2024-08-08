package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2836 {
    @Getter
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum Type
            implements Serializable {
        A("1", "1"),
        B("2", "2"),
        C("3", "3");
        private final String code;
        private final String msg;

        Type(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    @AllArgsConstructor
    @Data
    public static class User
            implements Serializable {
        private String name;
        private Type type;
    }

    @Test
    public void test() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        User user = new User("test", Type.B);
        String test = JSON.toJSONString(user);
        String jacksonResult = objectMapper.writeValueAsString(user);
        assertEquals(jacksonResult, test);
        String jsonString = JSON.toJSONString(Type.A);
        assertEquals(
                objectMapper.writeValueAsString(Type.A),
                jsonString);
    }
}
