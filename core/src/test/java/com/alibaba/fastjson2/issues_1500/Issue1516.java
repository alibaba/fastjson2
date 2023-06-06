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
        String jsonPath3 = "$[?( @.name=='小花' && @.age==18 || @.city=='扬州' )]";
        Object result3 = JSONPath.of(jsonPath3).extract(JSONReader.of(jsonArray));
        assertEquals(expected, JSON.toJSONString(result));
        assertEquals(expected, JSON.toJSONString(result2));
        assertEquals(jsonArray, JSON.toJSONString(result3));
    }

    @Test
    public void testFastjson2JSONPathCompile() {
        String jsonArray = "[{\"name\":\"小花\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小花\",\"age\":20,\"city\":\"扬州\"},{\"name\":\"小明\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小花\",\"age\":18,\"city\":\"苏州\"}]";
        String path = "$[?( @.name=='aa' && @.age==18 && @.city=='beijing' && @.province=='beijing' )]";
        String empty = "[]";
        String ageOf18 = "[{\"name\":\"小花\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小明\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小花\",\"age\":18,\"city\":\"苏州\"}]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( @.name=='aa' || @.age==16 || @.city=='beijing' || @.province=='beijing')]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( @.name =~ /aa/ )]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( @.age==18 && (@.name =~ /aa/)  )]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( @.age==18 || (@.name =~ /aa/)  )]";
        assertEquals(ageOf18, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( @.name =~ /aa/ && @.age==18 )]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( (@.name =~ /aa/ && (@.city=='aa')) && @.age==18 )]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( (@.name =~ /aa/ && (@.city=='aa')) || @.age==18 )]";
        assertEquals(ageOf18, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( @.age==18 && (@.name in ('aa', 'aa2') )  )]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?( @.age==18 || (@.name in ('aa', 'aa2') )  )]";
        assertEquals(ageOf18, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?(@.name in ('aa', 'aa2') && @.age==18 )]";
        assertEquals(empty, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[?(@.name in ('aa', 'aa2') || @.age==18 )]";
        assertEquals(ageOf18, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
    }

    @Test
    public void testWithLengthOrSize() {
        String jsonArray = "[{\"name\":\"小花\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小花\",\"age\":20,\"city\":\"扬州\"},{\"name\":\"小明\",\"age\":18,\"city\":\"扬州\"},{\"name\":\"小花\",\"age\":18,\"city\":\"苏州\"}]";
        String last = "{\"name\":\"小花\",\"age\":18,\"city\":\"苏州\"}";
        String path = "$[-1]";
        assertEquals(last, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[(@.length-1)]";
        assertEquals(last, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
        path = "$[(@.size-1)]";
        assertEquals(last, JSON.toJSONString(JSONPath.of(path).extract(JSONReader.of(jsonArray))));
    }
}
