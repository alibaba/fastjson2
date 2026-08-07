package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1944 {
    @Test
    public void test_for_issue() throws Exception {
        assertEquals(90.82195113f, JSON.parseObject("{\"value\":90.82195113}", Model.class).value);
    }

    public static class Model {
        public float value;
    }
}
