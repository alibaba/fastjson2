package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue117 {
    @Test
    public void test() {
        String text = ("{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:3,b:{c:'ddd'}}]}}");
        JSONObject obj = JSON.parseObject(text, JSONReader.Feature.AllowUnQuotedFieldNames);

        JSONPath.eval(obj, "$..ary2[0].a");

        long start = System.currentTimeMillis();
        for (int i = 0, len = 1000000; i < len; i++) {
            JSONPath.eval(obj, "$..ary2[0].a");
        }

        long times = System.currentTimeMillis() - start;

        System.out.println(times);
    }

    @Test
    public void test2() {
        String text = ("{code:1,msg:'Hello world',data:{list:[1,2,3,4,5], ary2:[{a:2},{a:[2]},{a:3,b:{c:'ddd'}}]}}");
        JSONObject object = JSON.parseObject(text, JSONReader.Feature.AllowUnQuotedFieldNames);
        assertEquals("[2]", JSONPath.eval(object, "$.data.list[?(@ in (2))]").toString());
        assertEquals("[2]", JSONPath.eval(object, "$.data.list[?(@ in [2])]").toString());
        assertEquals("[2]", JSONPath.eval(object, "$.data.list[?(@ in $..ary2[0].a)]").toString());
        assertEquals("[2]", JSONPath.eval(object, "$.data.list[?(@ in $..ary2[1].a)]").toString());
    }
}
