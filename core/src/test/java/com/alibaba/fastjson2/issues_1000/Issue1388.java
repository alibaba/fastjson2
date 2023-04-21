package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1388 {
    @Test
    public void test() {
        String json = "{\"fooList\":[{\"name\":\"Kyrie Irving\"},{\"name\":\"LeBron James\"}],\"name\":\"NBA2016\"}";
        Object result = JSONPath.extract(json, "$.fooList[0].name");
        assertEquals("Kyrie Irving", result.toString());
    }
}
