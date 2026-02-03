package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3875 {
    @Test
    public void test() {
        assertThrows(JSONException.class, () -> JSON.parseObject("{\"schools\":[{nu}]}", User.class));
    }

    @Data
    public static class User {
        private List<School> schools;
    }

    @Data
    public static class School {
        private String name;
    }
}
