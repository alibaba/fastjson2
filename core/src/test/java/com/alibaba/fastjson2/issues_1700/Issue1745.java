package com.alibaba.fastjson2.issues_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1745 {
    @Test
    public void test() {
        String str = "{\n" +
                "    \"features\": [\n" +
                "        {\n" +
                "            \"maxFillRatio\": {\n" +
                "                \"original\": 100,\n" +
                "                \"ref\": \"不引用\"\n" +
                "            },\n" +
                "            \"minFillRatio\": {\n" +
                "                \"original\": 0,\n" +
                "                \"ref\": \"不引用\"\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        JSONReader.Feature[] features = {
                JSONReader.Feature.SupportSmartMatch,
                JSONReader.Feature.SupportArrayToBean,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.SupportClassForName,
                JSONReader.Feature.IgnoreSetNullValue,
                JSONReader.Feature.AllowUnQuotedFieldNames,
                JSONReader.Feature.IgnoreCheckClose,
                JSONReader.Feature.IgnoreAutoTypeNotMatch
        };

        JSONObject object = JSON.parseObject(str, features);
        JSONArray jsonArray = object.getJSONArray("features");
        assertEquals("[{\"maxFillRatio\":{\"ref\":\"不引用\",\"original\":100},\"minFillRatio\":{\"ref\":\"不引用\",\"original\":0}}]", jsonArray.toJSONString());
        assertEquals(1, jsonArray.size());
        assertEquals(100, jsonArray.getJSONObject(0).getJSONObject("maxFillRatio").getIntValue("original"));
        assertEquals(0, jsonArray.getJSONObject(0).getJSONObject("minFillRatio").getIntValue("original"));
    }
}
