package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2134 {
    @Test
    public void test() {
        String json = "{\n" +
                "\t\"BidNode\":[\n" +
                "\t\t{\n" +
                "\t\t\t\"ID\":1,\n" +
                "\t\t\t\"Code\":\"1\",\n" +
                "\t\t\t\"Description\":\"土建工程\",\n" +
                "\t\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\t\"TradeName\":\"土建工程\",\n" +
                "\t\t\t\"type\":\"zy\",\n" +
                "\t\t\t\"Amount\":80,\n" +
                "\t\t\t\"AmountIncludeTax\":87.2,\n" +
                "\t\t\t\"BuildingArea\":6,\n" +
                "\t\t\t\"ExistLY\":false,\n" +
                "\t\t\t\"IsMainMajor\":true,\n" +
                "\t\t\t\"Children\":[\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"ID\":2,\n" +
                "\t\t\t\t\t\"PID\":1,\n" +
                "\t\t\t\t\t\"Code\":\"1.1\",\n" +
                "\t\t\t\t\t\"Description\":\"多层住宅（≤7F）3\",\n" +
                "\t\t\t\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\t\t\t\"type\":\"yt\",\n" +
                "\t\t\t\t\t\"Amount\":8,\n" +
                "\t\t\t\t\t\"AmountIncludeTax\":8.72,\n" +
                "\t\t\t\t\t\"BuildingArea\":4,\n" +
                "\t\t\t\t\t\"ExistLY\":true,\n" +
                "\t\t\t\t\t\"ProjectCategoryCode\":\"001001001\",\n" +
                "\t\t\t\t\t\"IsMainMajor\":true\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"ID\":3,\n" +
                "\t\t\t\t\t\"PID\":1,\n" +
                "\t\t\t\t\t\"Code\":\"1.2\",\n" +
                "\t\t\t\t\t\"Description\":\"中高层住宅（≤18F）\",\n" +
                "\t\t\t\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\t\t\t\"type\":\"yt\",\n" +
                "\t\t\t\t\t\"Amount\":36,\n" +
                "\t\t\t\t\t\"AmountIncludeTax\":39.24,\n" +
                "\t\t\t\t\t\"BuildingArea\":6,\n" +
                "\t\t\t\t\t\"ExistLY\":true,\n" +
                "\t\t\t\t\t\"ProjectCategoryCode\":\"001001002\",\n" +
                "\t\t\t\t\t\"IsMainMajor\":true\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t{\n" +
                "\t\t\t\t\t\"ID\":4,\n" +
                "\t\t\t\t\t\"PID\":1,\n" +
                "\t\t\t\t\t\"Code\":\"1.3\",\n" +
                "\t\t\t\t\t\"Description\":\"高层住宅（≤43F）\",\n" +
                "\t\t\t\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\t\t\t\"type\":\"yt\",\n" +
                "\t\t\t\t\t\"Amount\":36,\n" +
                "\t\t\t\t\t\"AmountIncludeTax\":39.24,\n" +
                "\t\t\t\t\t\"BuildingArea\":0,\n" +
                "\t\t\t\t\t\"ExistLY\":false,\n" +
                "\t\t\t\t\t\"ProjectCategoryCode\":\"001001003\",\n" +
                "\t\t\t\t\t\"IsMainMajor\":true\n" +
                "\t\t\t\t}\n" +
                "\t\t\t]\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        String expect = "[\n" +
                "\t{\n" +
                "\t\t\"ID\":1,\n" +
                "\t\t\"Code\":\"1\",\n" +
                "\t\t\"Description\":\"土建工程\",\n" +
                "\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\"TradeName\":\"土建工程\",\n" +
                "\t\t\"type\":\"zy\",\n" +
                "\t\t\"Amount\":80,\n" +
                "\t\t\"AmountIncludeTax\":87.2,\n" +
                "\t\t\"BuildingArea\":6,\n" +
                "\t\t\"ExistLY\":false,\n" +
                "\t\t\"IsMainMajor\":true,\n" +
                "\t\t\"Children\":[\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"ID\":2,\n" +
                "\t\t\t\t\"PID\":1,\n" +
                "\t\t\t\t\"Code\":\"1.1\",\n" +
                "\t\t\t\t\"Description\":\"多层住宅（≤7F）3\",\n" +
                "\t\t\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\t\t\"type\":\"yt\",\n" +
                "\t\t\t\t\"Amount\":8,\n" +
                "\t\t\t\t\"AmountIncludeTax\":8.72,\n" +
                "\t\t\t\t\"BuildingArea\":4,\n" +
                "\t\t\t\t\"ExistLY\":true,\n" +
                "\t\t\t\t\"ProjectCategoryCode\":\"001001001\",\n" +
                "\t\t\t\t\"IsMainMajor\":true\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"ID\":3,\n" +
                "\t\t\t\t\"PID\":1,\n" +
                "\t\t\t\t\"Code\":\"1.2\",\n" +
                "\t\t\t\t\"Description\":\"中高层住宅（≤18F）\",\n" +
                "\t\t\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\t\t\"type\":\"yt\",\n" +
                "\t\t\t\t\"Amount\":36,\n" +
                "\t\t\t\t\"AmountIncludeTax\":39.24,\n" +
                "\t\t\t\t\"BuildingArea\":6,\n" +
                "\t\t\t\t\"ExistLY\":true,\n" +
                "\t\t\t\t\"ProjectCategoryCode\":\"001001002\",\n" +
                "\t\t\t\t\"IsMainMajor\":true\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"ID\":4,\n" +
                "\t\t\t\t\"PID\":1,\n" +
                "\t\t\t\t\"Code\":\"1.3\",\n" +
                "\t\t\t\t\"Description\":\"高层住宅（≤43F）\",\n" +
                "\t\t\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\t\t\"type\":\"yt\",\n" +
                "\t\t\t\t\"Amount\":36,\n" +
                "\t\t\t\t\"AmountIncludeTax\":39.24,\n" +
                "\t\t\t\t\"BuildingArea\":0,\n" +
                "\t\t\t\t\"ExistLY\":false,\n" +
                "\t\t\t\t\"ProjectCategoryCode\":\"001001003\",\n" +
                "\t\t\t\t\"IsMainMajor\":true\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"ID\":2,\n" +
                "\t\t\"PID\":1,\n" +
                "\t\t\"Code\":\"1.1\",\n" +
                "\t\t\"Description\":\"多层住宅（≤7F）3\",\n" +
                "\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\"type\":\"yt\",\n" +
                "\t\t\"Amount\":8,\n" +
                "\t\t\"AmountIncludeTax\":8.72,\n" +
                "\t\t\"BuildingArea\":4,\n" +
                "\t\t\"ExistLY\":true,\n" +
                "\t\t\"ProjectCategoryCode\":\"001001001\",\n" +
                "\t\t\"IsMainMajor\":true\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"ID\":3,\n" +
                "\t\t\"PID\":1,\n" +
                "\t\t\"Code\":\"1.2\",\n" +
                "\t\t\"Description\":\"中高层住宅（≤18F）\",\n" +
                "\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\"type\":\"yt\",\n" +
                "\t\t\"Amount\":36,\n" +
                "\t\t\"AmountIncludeTax\":39.24,\n" +
                "\t\t\"BuildingArea\":6,\n" +
                "\t\t\"ExistLY\":true,\n" +
                "\t\t\"ProjectCategoryCode\":\"001001002\",\n" +
                "\t\t\"IsMainMajor\":true\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"ID\":4,\n" +
                "\t\t\"PID\":1,\n" +
                "\t\t\"Code\":\"1.3\",\n" +
                "\t\t\"Description\":\"高层住宅（≤43F）\",\n" +
                "\t\t\"TradeCode\":\"1001\",\n" +
                "\t\t\"type\":\"yt\",\n" +
                "\t\t\"Amount\":36,\n" +
                "\t\t\"AmountIncludeTax\":39.24,\n" +
                "\t\t\"BuildingArea\":0,\n" +
                "\t\t\"ExistLY\":false,\n" +
                "\t\t\"ProjectCategoryCode\":\"001001003\",\n" +
                "\t\t\"IsMainMajor\":true\n" +
                "\t}\n" +
                "]";
        Object results = JSONPath.eval(JSONObject.parseObject(json), "$.BidNode..[?(@.type in ('zy', 'yt','category'))]");
        assertEquals(expect, JSON.toJSONString(results, JSONWriter.Feature.PrettyFormat));
    }
}
