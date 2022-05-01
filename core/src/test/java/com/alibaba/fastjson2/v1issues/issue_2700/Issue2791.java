package com.alibaba.fastjson2.v1issues.issue_2700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2791 {
    @Test
    public void test_for_issue() throws Exception {
        JSONObject jsonObject = JSON.parseObject("{\"dependencies\":[{\"values\":[{\"name\":\"Demo\"}]}]}");
        JSONPath.of("$.dependencies.values[?(@.name=='Demo')]")
                .remove(jsonObject);
        assertEquals("{\"dependencies\":[{\"values\":[]}]}", jsonObject.toString());
    }
//
//    public void test_for_issue1() throws Exception {
//        JSONObject jsonObject = JSON.parseObject("{\"dependencies\":[{\"values\":{\"name\":\"Demo\"}}]}");
//        JSONPath.of("$.dependencies.values[?(@.name=='Demo')]")
//                .remove(jsonObject);
//        assertEquals("{\"dependencies\":[]}", jsonObject.toString());
//    }

    @Test
    public void test_for_issue2() throws Exception {
        JSONObject jsonObject = JSON.parseObject("{\"values\":[{\"name\":\"Demo\"}]}");
        JSONPath.of("$.values[?(@.name=='Demo')]")
                .remove(jsonObject);
        assertEquals("{\"values\":[]}", jsonObject.toString());
    }

//
//    public void test_for_issue3() throws Exception {
//        JSONObject jsonObject = JSON.parseObject("{\"values\":{\"name\":\"Demo\"}}");
//        assertTrue(
//                JSONPath.of("$.values[?(@.name=='Demo')]")
//                .remove(jsonObject));
//        assertEquals("{}", jsonObject.toString());
//    }
}
