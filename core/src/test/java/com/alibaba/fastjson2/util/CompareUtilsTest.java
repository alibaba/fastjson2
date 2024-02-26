package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class CompareUtilsTest {
    @Test
    public void testDiff() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testDiff1() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testDiff2() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"object.number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"object.string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"array[0].number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"array[0].string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"}}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testDiff3() {
        JSONObject json1 = JSONObject.parseObject("{" +
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.diff(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"string\":{\"equal\":false,\"type\":\"REMOVE\"},\"object.number\":{\"equal\":false,\"type\":\"REMOVE\"},\"object.string\":{\"equal\":false,\"type\":\"REMOVE\"},\"array[0].number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"array[0].string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"number\":{\"equal\":false,\"type\":\"ADD\"}}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testCompare() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"number\":{\"equal\":true},\"string\":{\"equal\":true},\"object.number\":{\"equal\":true},\"object.string\":{\"equal\":true},\"array[0].number\":{\"equal\":true},\"array[0].string\":{\"equal\":true}}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testCompare1() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':11," +
                "'string':'abcd'," +
                "'object': {'number':11,'string':'abcd',}," +
                "'array': [{'number':11,'string':'abcd',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"number\":{\"equal\":false,\"typeEqual\":true,\"type\":\"MODIFY\"},\"string\":{\"equal\":false,\"typeEqual\":true,\"type\":\"MODIFY\"},\"object.number\":{\"equal\":false,\"typeEqual\":true,\"type\":\"MODIFY\"},\"object.string\":{\"equal\":false,\"typeEqual\":true,\"type\":\"MODIFY\"},\"array[0].number\":{\"equal\":false,\"typeEqual\":true,\"type\":\"MODIFY\"},\"array[0].string\":{\"equal\":false,\"typeEqual\":true,\"type\":\"MODIFY\"}}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testCompare2() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"object.number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"object.string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"array[0].number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"array[0].string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"}}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testCompare3() {
        JSONObject json1 = JSONObject.parseObject("{" +
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        System.out.println(json1);
        System.out.println(json2);
        JSONObject result = CompareUtils.compare(json1, json2);
        System.out.println(result.toJSONString(JSONWriter.Feature.PrettyFormat));
        String expected = "{\"string\":{\"equal\":false,\"type\":\"REMOVE\"},\"object.number\":{\"equal\":false,\"type\":\"REMOVE\"},\"object.string\":{\"equal\":false,\"type\":\"REMOVE\"},\"array[0].number\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"array[0].string\":{\"equal\":false,\"typeEqual\":false,\"type\":\"MODIFY\"},\"number\":{\"equal\":false,\"type\":\"ADD\"}}";
        Assert.assertEquals(expected, result.toString());
    }

    @Test
    public void testEquals() {
        JSONObject json1 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
                "'string':'abc'," +
                "'object': {'number':1,'string':'abc',}," +
                "'array': [{'number':1,'string':'abc',}]," +
                "}");
        boolean result = CompareUtils.equals(json1, json2);
        System.out.println(result);
        Object expected = true;
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testEquals3() {
        JSONObject json1 = JSONObject.parseObject("{" +
//                "'number':'abc'," +
                "'string':123," +
                "'object': {'number':'abc','string':123,}," +
                "'array': [{'number':'abc','string':123,}]," +
                "}");
        JSONObject json2 = JSONObject.parseObject("{" +
                "'number':1," +
//                "'string':'abc'," +
//                "'object': {'number':123,'string':'abc',}," +
                "'array': [{'number':123,'string':'abc',}]," +
                "}");
        boolean result = CompareUtils.equals(json1, json2);
        System.out.println(result);
        Object expected = false;
        Assert.assertEquals(expected, result);
    }
}
