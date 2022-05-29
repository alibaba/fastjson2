package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSpecial_3 {
    @Test
    public void test_special() {
        String json = "[{\"@type\":\"NAME_CORRECTION\",\"value\":23}]";
        JSONArray array = (JSONArray) JSON.parse(json);
        Object obj = JSONPath.eval(array, "[?(@.\\@type='NAME_CORRECTION')]");
        assertNotNull(obj);
    }

    @Test
    public void test_special_1() {
        String json = "[{\":lang\":\"NAME_CORRECTION\",\"value\":23}]";
        JSONArray array = (JSONArray) JSON.parse(json);
        Object obj = JSONPath.eval(array, "[?(@.\\:lang='NAME_CORRECTION')]");
        assertNotNull(obj);
    }

    @Test
    public void test_special_2() {
        String json = "{\"cpe-item\":{\"@name\":\"cpe:/a:google:chrome:4.0.249.19\",\"cpe-23:cpe23-item\":{\"@name\":\"cpe:2.3:a:google:chrome:4.0.249.19:*:*:*:*:*:*:*\"},\"title\":[{\"#text\":\"グーグル クローム 4.0.249.19\",\"@xml:lang\":\"ja-JP\"},{\"#text\":\"Google Chrome 4.0.249.19\",\"@xml:lang\":\"en-US\"}]}}";
        String path = "['cpe-item']['title'][?(@.\\@xml\\:lang='en-US')]['#text'][0]";
        JSONObject object = (JSONObject) JSON.parse(json);
        Object obj = JSONPath.eval(object, path);
        assertNotNull(obj);
    }
}
