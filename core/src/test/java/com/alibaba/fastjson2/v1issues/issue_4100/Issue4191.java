package com.alibaba.fastjson2.v1issues.issue_4100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue4191 {
    @Test
    public void test() {
        Object jsonObject = JSON.parse("{\"path\":\"\"}");
        Object readResult = JSONPath.of("$.path.docExt.a").eval(jsonObject);
        assertNull(readResult);
    }

    @Test
    public void test1() {
        Object jsonObject = JSON.parse("{\"path\":1}");
        Object readResult = JSONPath.of("$.path.docExt.a").eval(jsonObject);
        assertNull(readResult);
    }

    @Test
    public void test2() {
        Object jsonObject = JSON.parse("{\"path\":true}");
        Object readResult = JSONPath.of("$.path.docExt.a").eval(jsonObject);
        assertNull(readResult);
    }

    @Test
    public void test3() {
        Object jsonObject = JSON.parse("{\"path\":null}");
        Object readResult = JSONPath.of("$.path.docExt.a").eval(jsonObject);
        assertNull(readResult);
    }
}
