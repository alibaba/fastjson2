package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Rongzhen Yan
 */
public class Issue828 {
    @Test
    public void test() {
        // test case1 with no feature
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("a", "a value");
        jsonObject1.put("b", null);
        jsonObject1.put("c", "c value");
        String json1A = jsonObject1.toJSONString();
        System.out.println(json1A);

        A object = new A("a value", null, "c value");
        String json1B = JSON.toJSONString(object);
        Assert.assertEquals(json1A, json1B);

        // test case2 with no feature
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("a", null);
        jsonObject2.put("b", null);
        jsonObject2.put("c", "c value");
        String json2 = jsonObject2.toJSONString();
        System.out.println(json2);
        Assert.assertEquals("{\"c\":\"c value\"}", json2);

        // test case3 with (WriteMapNullValue) feature
        JSONObject jsonObject3 = new JSONObject();
        jsonObject3.put("a", null);
        jsonObject3.put("b", null);
        jsonObject3.put("c", "c value");
        String json3 = jsonObject3.toJSONString(JSONWriter.Feature.WriteMapNullValue);

        System.out.println(json3);
        Assert.assertEquals("{\"a\":null,\"b\":null,\"c\":\"c value\"}", json3);

        // test case4 with (WriteMapNullValue) feature
        JSONObject jsonObject4 = new JSONObject();
        jsonObject4.put("a", null);
        jsonObject4.put("b", "b value");
        jsonObject4.put("c", null);
        String json4 = jsonObject4.toJSONString(JSONWriter.Feature.WriteMapNullValue);

        System.out.println(json4);
        Assert.assertEquals("{\"a\":null,\"b\":\"b value\",\"c\":null}", json4);
    }

    @Data
    @AllArgsConstructor
    static class A {
        private String a;

        private String b;

        private String c;
    }
}
