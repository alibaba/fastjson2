package com.alibaba.fastjson.issue_4200;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4291 {
    @Test
    public void test() {
        String jsonString = "{state:{a:{check:['x','y']},b:{}},view:[{id:'a',name:'an'},{id:'b',name:'bn'}]}";
        assertEquals(
                "[{\"id\":\"a\",\"name\":\"an\"},{\"id\":\"b\",\"name\":\"bn\"}]",
                JSONPath.eval(
                        JSON.parseObject(jsonString, Feature.AllowUnQuotedFieldNames, Feature.OrderedField),
                        "$..view"
                ).toString()
        );
        String errorJsonString = "{state:{a:{check:['x',null,'y']},b:{}},view:[{id:'a',name:'an'},{id:'b',name:'bn'}]}";
        assertEquals(
                "[{\"id\":\"a\",\"name\":\"an\"},{\"id\":\"b\",\"name\":\"bn\"}]",
                JSONPath.eval(
                        JSON.parseObject(errorJsonString, Feature.AllowUnQuotedFieldNames, Feature.OrderedField),
                        "$..view"
                ).toString()
        );
    }
}
