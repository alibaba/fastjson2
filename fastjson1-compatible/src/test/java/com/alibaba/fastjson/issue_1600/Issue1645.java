package com.alibaba.fastjson.issue_1600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class Issue1645 {
    @Test
    public void test_for_issue() throws Exception {
        String test = "{\"name\":\"test\",\"testDateTime\":\"2017-12-08 14:55:16\"}";
        JSON.toJSONString(JSON.parseObject(test).toJavaObject(TestDateClass.class), SerializerFeature.PrettyFormat);
    }

    public static class TestDateClass {
        public String name;
        public LocalDateTime testDateTime;
    }
}
