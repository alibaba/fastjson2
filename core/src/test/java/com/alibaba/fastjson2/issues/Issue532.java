package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

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
        String expected = "[1,\"Hello world\",{\"list\":[1,2,3,4,5],\"ary2\":[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}]},[1,2,3,4,5],[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}],1,2,3,4,5,{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}},2,3,{\"c\":\"ddd\"},\"ddd\"]";
        assertEquals(expected, JSONPath.eval(object, "$..*").toString());
    }

    @Test
    public void test2() {
        String str = "{\"1\":{\"a1\":[{\"id\":\"a1\"},{\"id\":\"a2\"}]},\"2\":2,\"3\":[3,3]}";
        String expected = "[{\"a1\":[{\"id\":\"a1\"},{\"id\":\"a2\"}]},2,[3,3],[{\"id\":\"a1\"},{\"id\":\"a2\"}],{\"id\":\"a1\"},{\"id\":\"a2\"},\"a1\",\"a2\",3,3]";
        assertEquals(expected, JSONPath.eval(JSON.parseObject(str), "$..*").toString());
        str = "{\"firstName\":\"John\",\"lastName\":\"doe\",\"age\":26,\"address\":{\"streetAddress\":\"naist street\",\"city\":\"Nara\",\"postalCode\":\"630-0192\"},\"phoneNumbers\":[{\"type\":\"iPhone\",\"number\":\"0123-4567-8888\"},{\"type\":\"home\",\"number\":\"0123-4567-8910\"}]}";
        expected = "[\"John\",\"doe\",26,{\"streetAddress\":\"naist street\",\"city\":\"Nara\",\"postalCode\":\"630-0192\"},[{\"type\":\"iPhone\",\"number\":\"0123-4567-8888\"},{\"type\":\"home\",\"number\":\"0123-4567-8910\"}],\"naist street\",\"Nara\",\"630-0192\",{\"type\":\"iPhone\",\"number\":\"0123-4567-8888\"},{\"type\":\"home\",\"number\":\"0123-4567-8910\"},\"iPhone\",\"0123-4567-8888\",\"home\",\"0123-4567-8910\"]";
        assertEquals(expected, JSONPath.eval(JSON.parseObject(str), "$..*").toString());
        str = "{\"code\":1,\"msg\":\"Hello world\",\"data\":{\"list\":[1,2,3,4,5],\"ary2\":[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}]}}";
        expected = "[1,\"Hello world\",{\"list\":[1,2,3,4,5],\"ary2\":[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}]},[1,2,3,4,5],[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}],1,2,3,4,5,{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}},2,3,{\"c\":\"ddd\"},\"ddd\"]";
        assertEquals(expected, JSONPath.eval(JSON.parseObject(str), "$..*").toString());
        str = "{\"testa\":[{\"name\":\"test\"}, {\"name\":\"test2\"}]}";
        expected = "[[{\"name\":\"test\"},{\"name\":\"test2\"}],{\"name\":\"test\"},{\"name\":\"test2\"},\"test\",\"test2\"]";
        assertEquals(expected, JSONPath.eval(JSON.parseObject(str, TestB.class), "$..*").toString());
        str = "{\"code\":1,\"msg\":\"Hello world\",\"data\":{\"list\":[1,2,null],\"ary2\":[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}]}}";
        expected = "[1,\"Hello world\",{\"list\":[1,2,null],\"ary2\":[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}]},[1,2,null],[{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}}],1,2,null,{\"a\":2},{\"a\":3,\"b\":{\"c\":\"ddd\"}},2,3,{\"c\":\"ddd\"},\"ddd\"]";
        assertEquals(expected, JSONPath.eval(JSON.parseObject(str), "$..*").toString());
    }

    @Data
    public class TestB {
        private LinkedList<TestA> testa;
    }

    @Data
    public class TestA {
        private String name;
    }
}
