package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1516 {
    @Test
    public void test() {
        String jsonArray = "[{\"name\":\"小花\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小花\",\"age\":20,\"city\":\"扬州\"},{\"name\":\"小明\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小花\",\"age\":18,\"city\":\"苏州\"}]";
        String expected = "[{\"name\":\"小花\",\"age\":18,\"city\":\"扬州\"}]";
        String jsonPath = "$[?( @.name=='小花' && @.age==18 && @.city=='扬州' )]";
        Object result = JSONPath.of(jsonPath).extract(JSONReader.of(jsonArray));
        String jsonPath2 = "$[?( @.name=='小花' && @.age==18)][?( @.city=='扬州' )]";
        Object result2 = JSONPath.of(jsonPath2).extract(JSONReader.of(jsonArray));
        assertEquals(expected, JSON.toJSONString(result));
        assertEquals(expected, JSON.toJSONString(result2));
    }
}
