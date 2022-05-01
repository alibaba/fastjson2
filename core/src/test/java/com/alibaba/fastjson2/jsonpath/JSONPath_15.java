package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_15 {
    static final String a = "{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}";
    static final String b = "[{b:{c:1}}, {b:{d:1}}, {b:{c:2}}, {b:{c:23}}]";
    static final String c = "[{c:'aaaa'}, {b:'cccc'}, {c:'cccaa'}]";
//
//    public void test_0() {
//
//        JSONObject object = JSON.parseObject(a);
//
//        List<Object> items = (List<Object>) JSONPath.eval(object, "data.ary2[*].b.c");
//        assertEquals("[\"ddd\"]", JSON.toJSONString(items));
//    }
//
//    public void test_1() {
//        Object object = JSON.parse(b);
//
//        List<Object> items = (List<Object>) JSONPath.eval(object, "$..b[?(@.c == 23)]");
//        assertEquals("[{\"c\":23}]", JSON.toJSONString(items));
//    }

    @Test
    public void test_min() {
        Object object = JSON.parse(b);

        Object min = JSONPath.eval(object, "$..c.min()");
        assertEquals("1", JSON.toJSONString(min));
    }

    @Test
    public void test_max() {
        Object object = JSON.parse(b);

        Object min = JSONPath.eval(object, "$..c.max()");
        assertEquals("23", JSON.toJSONString(min));
    }

    @Test
    public void test_3() {
        Object object = JSON.parse(c);

        Object min = JSONPath.eval(object, "$[?(@.c =~ /a+/)]");
        assertEquals("[{\"c\":\"aaaa\"}]", JSON.toJSONString(min));
    }
//
//    public void test_c() {
//        Object object = JSON.parse(c);
//
//        Object min = JSONPath.eval(object, "data.list[?(@ in $..ary2[0].a)]");
//        assertEquals("[{\"c\":\"aaaa\"}]", JSON.toJSONString(min));
//    }
}
