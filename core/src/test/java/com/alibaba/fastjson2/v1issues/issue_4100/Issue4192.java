package com.alibaba.fastjson2.v1issues.issue_4100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4192 {
    @Test
    public void test() {
        String json = "{\n" +
                "                    \"head\": {\n" +
                "                        \"requestTime\": \"1654070371884\",\n" +
                "                        \"merchantcode\": \"111\",\n" +
                "                        \"ticket\": \"111\",\n" +
                "                        \"version\": \"1.0.0\"\n" +
                "                    },\n" +
                "                    \"body\": {\n" +
                "                        \"phone\": \"111\",\n" +
                "                        \"type\": \"1\",\n" +
                "                        \"ticket\": \"\"\n" +
                "                    },\n" +
                "                    \"sign\": \"111\"\n" +
                "                }";

        assertEquals("{\n" +
                        "\t\"head\":{\n" +
                        "\t\t\"requestTime\":\"1654070371884\",\n" +
                        "\t\t\"merchantcode\":\"111\",\n" +
                        "\t\t\"ticket\":\"111\",\n" +
                        "\t\t\"version\":\"1.0.0\"\n" +
                        "\t},\n" +
                        "\t\"body\":{\n" +
                        "\t\t\"phone\":\"111\",\n" +
                        "\t\t\"type\":\"1\",\n" +
                        "\t\t\"ticket\":\"\"\n" +
                        "\t},\n" +
                        "\t\"sign\":\"111\"\n" +
                        "}",
                JSON.parseObject(json).toString(JSONWriter.Feature.PrettyFormat)
        );
    }
}
