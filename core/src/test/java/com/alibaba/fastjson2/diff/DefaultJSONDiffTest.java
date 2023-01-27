package com.alibaba.fastjson2.diff;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.diff.path.JsonCompareResult;
import com.alibaba.fastjson2.diff.path.JsonComparedOption;
import org.junit.jupiter.api.Test;

public class DefaultJSONDiffTest {
    @Test
    void contextLoads() throws IllegalAccessException {
        String array1 = "[1, 2, 3, 4, 5]";
        String array2 = "[1, 6, 3, 4, 5]";

        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(true);
        JsonCompareResult jsonCompareResult = new DefaultJSONDiff()
            .option(jsonComparedOption)
            .detectDiff(JSON.parseArray(array1), JSON.parseArray(array2));
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }


    @Test
    void test2() throws IllegalAccessException {
        String array1 = "[1, 2, 3, 4, 5]";
        String array2 = "[1, 6, 3, 4, 5]";

        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(true);

        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
            .option(jsonComparedOption)
            .detectDiff(JSON.parseArray(array1), JSON.parseArray(array2));
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }


    @Test
    void test3() throws IllegalAccessException {
        String object1 = "{\n" +
            "  \"count\": 0,\n" +
            "  \"pageCount\": 0,\n" +
            "  \"floorCount\": 0,\n" +
            "  \"foldCount\": 0,\n" +
            "  \"list\": []\n" +
            "}";
        String object2 = "{\n" +
            "  \"count\": 0,\n" +
            "  \"pageCount\": 0,\n" +
            "  \"floorCount\": 3,\n" +
            "  \"foldCount\": 0,\n" +
            "  \"list\": []\n" +
            "}";

        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(true);

        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
            .option(jsonComparedOption)
            .detectDiff(JSON.parseObject(object1), JSON.parseObject(object2));
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }

    @Test
    void test4() throws IllegalAccessException {
        String object1 = "{\n" +
            "    \"cityInfo\": {\n" +
            "        \"city\": \"天津市\",\n" +
            "        \"citykey\": \"101030100\",\n" +
            "        \"parent\": \"天津\",\n" +
            "        \"updateTime\": \"05:46\",\n" +
            "        \"cityInfoTest\": {\n" +

            "            \"cityInfoTest2\": {\n" +
            "                \"count\": 2,\n" +
            "                \"count2\": \"12\",\n" +
            "                \"count3\": true,\n" +
            "                \"cityInfoTest3\": {\n" +
            "                    \"count\": 2,\n" +
            "                    \"count2\": \"12\",\n" +
            "                    \"count3\": true\n" +
            "                }\n" +
            "            \"count\": 2,\n" +
            "            \"count2\": \"12\",\n" +
            "            \"count3\": true,\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"count\": 2,\n" +
            "    \"count2\": \"12\",\n" +
            "    \"count3\": true\n" +
            "}";
        String object2 = "{\n" +
            "    \"cityInfo\": {\n" +
            "        \"city\": \"天津市\",\n" +
            "        \"citykey\": \"101030100\",\n" +
            "        \"parent\": \"天津\",\n" +
            "        \"updateTime\": \"05:46\",\n" +
            "        \"cityInfoTest\": {\n" +
            "            \"cityInfoTest2\": {\n" +
            "                \"count\": 2,\n" +
            "                \"count2\": \"15\",\n" +
            "                \"count3\": true,\n" +
            "                \"cityInfoTest3\": {\n" +
            "                    \"count\": 312,\n" +
            "                    \"count2\": \"12\",\n" +
            "                    \"count3\": true\n" +
            "                }\n" +
            "            \"count\": 2,\n" +
            "            \"count2\": \"12\",\n" +
            "            \"count3\": false,\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"count\": 2,\n" +
            "    \"count2\": \"1ssss2\",\n" +
            "    \"count3\": true\n" +
            "}";

        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(true);

        System.out.println(JSON.parseObject(object1));
        System.out.println(JSON.parseObject(object2));
        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
            .option(jsonComparedOption)
            .detectDiff(JSON.parseObject(object1), JSON.parseObject(object2));
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }

    @Test
    void test5() throws IllegalAccessException {
        String object1 = "{\n" +
            "    \"cityInfo\": {\n" +
            "        \"city\": \"天津市\",\n" +
            "        \"citykey\": [12,23,45,67],\n" +
            "        \"parent\": \"天津\",\n" +
            "        \"updateTime\": \"05:46\",\n" +
            "        \"cityInfoTest\": {\n" +

            "            \"cityInfoTest2\": {\n" +
            "                \"count\": 2,\n" +
            "                \"count2\": \"12\",\n" +
            "                \"count3\": true,\n" +
            "                \"cityInfoTest3\": {\n" +
            "                    \"count\": 2,\n" +
            "                    \"count2\": \"12\",\n" +
            "                    \"count3\": true\n" +
            "                }\n" +
            "            \"count\": 2,\n" +
            "            \"count2\": \"12\",\n" +
            "            \"count3\": true,\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"count\": 2,\n" +
            "    \"count2\": \"12\",\n" +
            "    \"count3\": true\n" +
            "}";
        String object2 = "{\n" +
            "    \"cityInfo\": {\n" +
            "        \"city\": \"天津市\",\n" +
            "        \"citykey\": [12,232,45,67],\n" +
            "        \"parent\": \"天津\",\n" +
            "        \"updateTime\": \"05:46\",\n" +
            "        \"cityInfoTest\": {\n" +
            "            \"cityInfoTest2\": {\n" +
            "                \"count\": 2,\n" +
            "                \"count2\": \"15\",\n" +
            "                \"count3\": true,\n" +
            "                \"cityInfoTest3\": {\n" +
            "                    \"count\": 312,\n" +
            "                    \"count2\": \"12\",\n" +
            "                    \"count3\": true\n" +
            "                }\n" +
            "            \"count\": 2,\n" +
            "            \"count2\": \"12\",\n" +
            "            \"count3\": false,\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"count\": 2,\n" +
            "    \"count2\": \"1ssss2\",\n" +
            "    \"count3\": true\n" +
            "}";

        JsonComparedOption jsonComparedOption = new JsonComparedOption().setIgnoreOrder(false);

        System.out.println(JSON.parseObject(object1));
        System.out.println(JSON.parseObject(object2));
        DefaultJSONDiff defaultJsonDifference = new DefaultJSONDiff();
        JsonCompareResult jsonCompareResult = defaultJsonDifference
            .option(jsonComparedOption)
            .detectDiff(JSON.parseObject(object1), JSON.parseObject(object2));
        System.out.println(JSON.toJSONString(jsonCompareResult));
    }

}
