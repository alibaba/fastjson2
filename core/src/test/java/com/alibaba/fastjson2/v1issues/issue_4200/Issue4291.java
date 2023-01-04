package com.alibaba.fastjson2.v1issues.issue_4200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4291 {
    @Test
    public void test() {
        String jsonString = "{state:{a:{check:['x','y']},b:{}},view:[{id:'a',name:'an'},{id:'b',name:'bn'}]}";
        assertEquals(
                "[{\"id\":\"a\",\"name\":\"an\"},{\"id\":\"b\",\"name\":\"bn\"}]",
                JSONPath.eval(
                        JSON.parseObject(jsonString, JSONReader.Feature.AllowUnQuotedFieldNames),
                        "$..view"
                ).toString()
        );
        String errorJsonString = "{state:{a:{check:['x',null,'y']},b:{}},view:[{id:'a',name:'an'},{id:'b',name:'bn'}]}";
        assertEquals(
                "[{\"id\":\"a\",\"name\":\"an\"},{\"id\":\"b\",\"name\":\"bn\"}]",
                JSONPath.eval(
                        JSON.parseObject(errorJsonString, JSONReader.Feature.AllowUnQuotedFieldNames),
                        "$..view"
                ).toString()
        );
    }
}
