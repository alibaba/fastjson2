package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3125 {
    @Getter
    @Setter
    static class User {
        private String name;
        private List<NVString> additionalInfo;
    }

    @Getter
    @Setter
    @ToString
    static class NVString {
        String name;
        String value;
    }

    public static final String str = "{\"name\":\"fastjson2\",\"additionalInfo\":[{\"name\":\"srv6-color\",\"value\":\"test\"},{\"name\":\"test2\",\"value\":\"3\"}]}";

    public static String path = "$.additionalInfo[?(@.name=='srv6-color')].value";

    @Test
    public void test_fastjson2_case1() {
        User user = JSON.parseObject(str, User.class);
        JSONPath jsonPath = JSONPath.of("$.additionalInfo[?(@.name=='srv6-color')].value");
        jsonPath.set(user, "modify");
        assertEquals(user.getAdditionalInfo().get(0).getValue(), "modify");
    }

    @Test
    public void test_fastjson_case1() {
        User user = JSON.parseObject(str, User.class);
        com.alibaba.fastjson.JSONPath.set(user, "$.additionalInfo[?(@.name=='srv6-color')].value", "modify");
        JSONPath jsonPath = JSONPath.of("$.additionalInfo[?(@.name=='srv6-color')].value");
        jsonPath.set(user, "modify");
        assertEquals(user.getAdditionalInfo().get(0).getValue(), "modify");
    }
}
