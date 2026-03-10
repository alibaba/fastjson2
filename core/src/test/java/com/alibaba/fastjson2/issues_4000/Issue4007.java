package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4007 {
    @Test
    public void test() {
        String json = "{\n" +
                "    \"activityId\": \"9260304192237310917\",\n" +
                "    \"activityRuleDtos\": [\n" +
                "        {\n" +
                "            \"awardId\": \"8260304192237381811\",\n" +
                "            \"promotionRuleDtos\": [\n" +
                "                {\n" +
                "                    \"field\": \"dataCenterUserTagLimit\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"field\": \"tradeTime\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"field\": \"skuLimit\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"awardId\": \"8260304192237401811\",\n" +
                "            \"promotionRuleDtos\": [\n" +
                "                {\n" +
                "                   \n" +
                "                    \"field\": \"tradeTime\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"field\": \"skuLimit\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        String jsonPathConfig1 = "$.activityRuleDtos[?(@.promotionRuleDtos[?(@.field == 'dataCenterUserTagLimit')])].awardId";
        JSONArray result1 = (JSONArray) JSONPath.extract(json, jsonPathConfig1);
        assertEquals(1, result1.size());
        assertEquals("8260304192237381811", result1.getString(0));

        // no match
        String jsonPathConfig2 = "$.activityRuleDtos[?(@.promotionRuleDtos[?(@.field == 'trade')])].awardId";
        JSONArray result2 = (JSONArray) JSONPath.extract(json, jsonPathConfig2);
        if (result2 != null) {
            assertEquals(0, result2.size());
        }

        // all match
        String jsonPathConfig3 = "$.activityRuleDtos[?(@.promotionRuleDtos[?(@.field == 'tradeTime')])].awardId";
        JSONArray result3 = (JSONArray) JSONPath.extract(json, jsonPathConfig3);
        assertEquals(2, result3.size());
        assertEquals("8260304192237381811", result3.getString(0));
        assertEquals("8260304192237401811", result3.getString(1));
    }
}
