package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONPath_10_contains {
    @Test
    public void test() {
        String json = "{\n" +
                "    \"queryScene\":{\n" +
                "        \"scene\":[\n" +
                "            {\n" +
                "                \"innerSceneId\":3,\n" +
                "                \"name\":\"场景绑8测试-笑幽\",\n" +
                "                \"sceneSetId\":8,\n" +
                "                \"formInfo\":\"{}\",\n" +
                "                \"queryDataSet\":{\n" +
                "                    \"dataSet\":[\n" +
                "                        {\n" +
                "                            \"id\":6,\n" +
                "                            \"sceneId\":3,\n" +
                "                            \"name\":\"测试商品集\",\n" +
                "                            \"dataSetRuleCode\":null,\n" +
                "                            \"resourceId\":null,\n" +
                "                            \"udsOffer\":{\n" +
                "                                \"offer\":[\n" +
                "\n" +
                "                                ]\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"id\":5,\n" +
                "                            \"sceneId\":3,\n" +
                "                            \"name\":\"测试卖家集\",\n" +
                "                            \"dataSetRuleCode\":null,\n" +
                "                            \"resourceId\":null,\n" +
                "                            \"udsOffer\":{\n" +
                "                                \"offer\":[\n" +
                "\n" +
                "                                ]\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        JSONObject root = JSON.parseObject(json);
        assertTrue(
                JSONPath.of("$.queryScene.scene.queryDataSet.dataSet")
                        .contains(root));
        assertFalse(
                JSONPath
                        .of("$.queryScene.scene.queryDataSet.dataSet.abcd")
                        .contains(root));
        assertTrue(
                JSONPath.of("$.queryScene.scene.queryDataSet.dataSet.name")
                        .contains(root));
    }
}
