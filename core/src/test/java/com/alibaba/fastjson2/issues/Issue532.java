package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue532 {
    JSONObject object;

    @BeforeEach
    public void init() {
        String str = "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}";
        object = JSON.parseObject(str, JSONReader.Feature.AllowUnQuotedFieldNames);
    }

    @Test
    public void test() {
        assertNull(JSONPath.eval(object, "$[?(@.c =~ /a+/)]"));
        assertEquals("ddd", JSONPath.eval(object, "$..c.min()"));
        assertEquals("[]", JSONPath.eval(object, "$..b[?(@.c == 12)]").toString());
        assertEquals("[1,\"Hello world\",1,2,3,4,5,2,3,\"ddd\"]", JSONPath.eval(object, "$..*").toString());
    }
}
