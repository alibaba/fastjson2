package com.alibaba.fastjson2.v1issues.issue_4000;

import com.alibaba.fastjson2.JSON;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Issue4008 {
    @Test
    public void test_for_issue4008() throws JSONException {
        Map<String, String> studentMap = new HashMap<>();
        studentMap.put("name", "jerry");
        studentMap.put("sex", "male");
        Map<String, Map<String, String>> classMap = new HashMap<>();
        classMap.put("student1", studentMap);
        classMap.put("student2", studentMap);
        Map<String, List<Map<String, Map<String, String>>>> schoolMap = new HashMap<>();
        schoolMap.put("class", Arrays.asList(classMap));
        String fastJSON = JSON.toJSONString(schoolMap);
        JSONAssert.assertEquals("{\"class\":[{\"student2\":{\"sex\":\"male\",\"name\":\"jerry\"},\"student1\":{\"sex\":\"male\",\"name\":\"jerry\"}}]}", fastJSON, true);
    }
}
