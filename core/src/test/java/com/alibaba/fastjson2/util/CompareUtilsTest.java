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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":1,\"type\":\"MODIFY\"},\"string\":{\"path\":\"string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123,\"type\":\"MODIFY\"},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123,\"type\":\"MODIFY\"},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"}}";
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
        String expected = "{\"string\":{\"path\":\"string\",\"equal\":false,\"type\":\"REMOVE\",\"value1\":123},\"object.number\":{\"path\":\"object.number\",\"equal\":false,\"type\":\"REMOVE\",\"value1\":\"abc\"},\"object.string\":{\"path\":\"object.string\",\"equal\":false,\"type\":\"REMOVE\",\"value1\":123},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123,\"type\":\"MODIFY\"},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"},\"number\":{\"path\":\"number\",\"equal\":false,\"type\":\"ADD\",\"value2\":1}}";
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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":true},\"string\":{\"path\":\"string\",\"valueEqual\":true},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":true},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":true},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":true},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":true}}";
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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":false,\"typeEqual\":true,\"value1\":11,\"value2\":1,\"type\":\"MODIFY\"},\"string\":{\"path\":\"string\",\"valueEqual\":false,\"typeEqual\":true,\"value1\":\"abcd\",\"value2\":\"abc\",\"type\":\"MODIFY\"},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"typeEqual\":true,\"value1\":11,\"value2\":1,\"type\":\"MODIFY\"},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"typeEqual\":true,\"value1\":\"abcd\",\"value2\":\"abc\",\"type\":\"MODIFY\"},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"typeEqual\":true,\"value1\":11,\"value2\":1,\"type\":\"MODIFY\"},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"typeEqual\":true,\"value1\":\"abcd\",\"value2\":\"abc\",\"type\":\"MODIFY\"}}";
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
        String expected = "{\"number\":{\"path\":\"number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":1,\"type\":\"MODIFY\"},\"string\":{\"path\":\"string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"},\"object.number\":{\"path\":\"object.number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123,\"type\":\"MODIFY\"},\"object.string\":{\"path\":\"object.string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123,\"type\":\"MODIFY\"},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"}}";
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
        String expected = "{\"string\":{\"path\":\"string\",\"equal\":false,\"type\":\"REMOVE\",\"value1\":123},\"object.number\":{\"path\":\"object.number\",\"equal\":false,\"type\":\"REMOVE\",\"value1\":\"abc\"},\"object.string\":{\"path\":\"object.string\",\"equal\":false,\"type\":\"REMOVE\",\"value1\":123},\"array[0].number\":{\"path\":\"array[0].number\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":\"abc\",\"value2\":123,\"type\":\"MODIFY\"},\"array[0].string\":{\"path\":\"array[0].string\",\"valueEqual\":false,\"typeEqual\":false,\"value1\":123,\"value2\":\"abc\",\"type\":\"MODIFY\"},\"number\":{\"path\":\"number\",\"equal\":false,\"type\":\"ADD\",\"value2\":1}}";
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
