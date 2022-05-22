package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPath_2 {
    @Test
    public void test_path() {
        String json = "{\"user\":[{\"amount\":1.11,\"isadmin\":true,\"age\":18},{\"amount\":0.22,\"isadmin\":false,\"age\":28}]}";

        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user");
            assertEquals(2, array.size());

            assertEquals(1.11D, array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertTrue(array.getJSONObject(0).getBoolean("isadmin"));
            assertEquals(18, array.getJSONObject(0).getIntValue("age"));

            assertEquals(0.22D, array.getJSONObject(1).getDoubleValue("amount"));
            Assertions.assertFalse(array.getJSONObject(1).getBoolean("isadmin"));
            assertEquals(28, array.getJSONObject(1).getIntValue("age"));
        }

        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.age = 18)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(1.11D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertTrue(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(18 == array.getJSONObject(0).getIntValue("age"));
        }

        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.isadmin = true)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(1.11D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertTrue(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(18 == array.getJSONObject(0).getIntValue("age"));
        }

        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.isadmin = false)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(0.22D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertFalse(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(28 == array.getJSONObject(0).getIntValue("age"));
        }

        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.amount = 0.22)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(0.22D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertFalse(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(28 == array.getJSONObject(0).getIntValue("age"));
        }

        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.amount < 0.3)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(0.22D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertFalse(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(28 == array.getJSONObject(0).getIntValue("age"));
        }

        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.amount <= 0.22)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(0.22D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertFalse(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(28 == array.getJSONObject(0).getIntValue("age"));
        }
        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.amount <= 1)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(0.22D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertFalse(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(28 == array.getJSONObject(0).getIntValue("age"));
        }
        {
            JSONArray array = (JSONArray) JSONPath.extract(json, "$.user[?(@.amount > 1)]");
            assertEquals(1, array.size());

            Assertions.assertTrue(1.11D == array.getJSONObject(0).getDoubleValue("amount"));
            Assertions.assertTrue(array.getJSONObject(0).getBoolean("isadmin"));
            Assertions.assertTrue(18 == array.getJSONObject(0).getIntValue("age"));
        }
    }

    @Test
    public void test_path_1() {
        JSONObject root = new JSONObject()
                .fluentPut("user",
                        new JSONArray()
                                .fluentAdd(
                                        new JSONObject()
                                                .fluentPut("id", 101)
                                                .fluentPut("amount", BigInteger.valueOf(1))
                                )
                                .fluentAdd(
                                        new JSONObject()
                                                .fluentPut("id", 102)
                                                .fluentPut("amount", 2F)
                                )
                                .fluentAdd(
                                        new JSONObject()
                                                .fluentPut("id", 103)
                                                .fluentPut("amount", 3D)
                                )
                );

        assertEquals("[{\"id\":101,\"amount\":1}]",
                JSONPath.of("$.user[?(@.amount <= 1)]")
                        .eval(root)
                        .toString()
        );
        assertEquals("[{\"id\":101,\"amount\":1}]",
                JSONPath.of("$.user[?(@.amount < 2)]")
                        .eval(root)
                        .toString()
        );
        assertEquals("[{\"id\":102,\"amount\":2.0},{\"id\":103,\"amount\":3.0}]",
                JSONPath.of("$.user[?(@.amount >= 2)]")
                        .eval(root)
                        .toString()
        );
        assertEquals("[{\"id\":103,\"amount\":3.0}]",
                JSONPath.of("$.user[?(@.amount > 2)]")
                        .eval(root)
                        .toString()
        );
    }

    @Test
    public void test_path_len() {
        assertEquals("0",
                JSONPath.of("$.length()")
                        .eval(Collections.emptySet())
                        .toString()
        );
        assertEquals("0",
                JSONPath.of("$.length()")
                        .eval(new Object[0])
                        .toString()
        );
    }

    @Test
    public void test_path_offset() {
        String content = "{\n"
                + "    \"code\":\"success\",\n"
                + "    \"data\":{\n"
                + "        \"data2\":{\n"
                + "            \"id\":null,\n"
                + "            \"type\":null,\n"
                + "            \"serveCategoryInfoList\":[\n"
                + "                {\n"
                + "                    \"id\":0,\n"
                + "                    \"infoList\":[\n"
                + "\n"
                + "                    ],\n"
                + "                    \"idList\":[\n"
                + "                        200226\n"
                + "                    ]\n"
                + "                },\n"
                + "                {\n"
                + "                    \"id\":1,\n"
                + "                    \"idList\":[\n"
                + "                        1,\n"
                + "                        2,\n"
                + "                        3,\n"
                + "                        4,\n"
                + "                        5,\n"
                + "                        6,\n"
                + "                        7\n"
                + "                    ]\n"
                + "                },\n"
                + "            ]\n"
                + "        },\n"
                + "    },\n"
                + "    \"msg\":\"\",\n"
                + "    \"status\":\"1\"\n"
                + "}\n";
        JSONPath path = JSONPath.of("$.status");
        assertEquals(path.extract(JSONReader.of(content)), "1");
    }
}
