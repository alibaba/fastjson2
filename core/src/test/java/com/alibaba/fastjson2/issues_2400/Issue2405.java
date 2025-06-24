package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2405 {
    @Test
    public void test() {
        String path = "$.objects[?(@.objdata.Actions[?(@.Type == 'sun')])].objdata.PlantFoodActivationSound";
        Object result = JSONPath.eval(STR, path);
        assertEquals("[\"Play_Plant_TwinSunflower_Nitro\"]", JSON.toJSONString(result));
    }

    static final String STR = "{\n" +
            " \"version\": 1,\n" +
            " \"objects\": [\n" +
            "  {\n" +
            "   \"objclass\": \"SunflowerProps\",\n" +
            "   \"aliases\": [\n" +
            "    \"TwinSunflowerDefault\"\n" +
            "   ],\n" +
            "   \"objdata\": {\n" +
            "    \"PlantFoodActivationSound\": \"Play_Plant_TwinSunflower_Nitro\",\n" +
            "    \"CollectibleTypeName\": \"sun\",\n" +
            "    \"Actions\": [\n" +
            "     {\n" +
            "      \"Type\": \"sun\",\n" +
            "      \"SpawnOffset\": {\n" +
            "       \"x\": -10,\n" +
            "       \"y\": -55\n" +
            "      }\n" +
            "     }\n" +
            "    ]\n" +
            "   }\n" +
            "  }\n" +
            " ]\n" +
            "}";
}
