package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1000 {
    @Test
    public void test() {
        String json = "{\"whiteListIds\":[]}";
        JSONObject jsonObject = JSONObject.parseObject(json);
        assertNotNull(jsonObject.toJavaObject(TestObj.class).whiteListIds);
        assertNotNull(jsonObject.to(TestObj.class));
        assertNotNull(JSON.parseObject(json, TestObj.class).whiteListIds);
    }

    @Data
    public static class TestObj {
        private Set<Integer> whiteListIds;
    }
}
