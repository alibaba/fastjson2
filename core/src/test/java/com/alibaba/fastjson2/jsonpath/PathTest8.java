package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PathTest8 {
    @Test
    public void test0() {
        assertEquals("{\"color\":\"red\",\"price\":19.95,\"gears\":[23,50],\"extra\":{\"x\":0},\"escape\":\"Esc\\b\\f\\n\\r\\t*\",\"nullValue\":null}",
                JSON.toJSONString(
                        JSONPath.extract(STR, "$.store.bicycle[?(@.color == 'red' )]"),
                        JSONWriter.Feature.WriteNulls
                )
        );
    }

    @Test
    public void test1() {
        assertEquals("{\"color\":\"red\",\"price\":19.95,\"gears\":[23,50],\"extra\":{\"x\":0},\"escape\":\"Esc\\b\\f\\n\\r\\t*\",\"nullValue\":null}",
                JSON.toJSONString(
                        JSONPath.extract(STR, "$.store.bicycle[?(@.gears == [23, 50])]"),
                        JSONWriter.Feature.WriteNulls
                )
        );
        assertNull(JSONPath.extract(STR, "$.store.bicycle[?(@.gears == [23, 77])]"));

        assertEquals("{\"color\":\"red\",\"price\":19.95,\"gears\":[23,50],\"extra\":{\"x\":0},\"escape\":\"Esc\\b\\f\\n\\r\\t*\",\"nullValue\":null}",
                JSON.toJSONString(
                        JSONPath.extract(STR, "$.store.bicycle[?(@.extra == {\"x\":0})]"),
                        JSONWriter.Feature.WriteNulls
                )
        );

        assertEquals("{\"color\":\"red\",\"price\":19.95,\"gears\":[23,50],\"extra\":{\"x\":0},\"escape\":\"Esc\\b\\f\\n\\r\\t*\",\"nullValue\":null}",
                JSON.toJSONString(
                        JSONPath.extract(STR, "$.store.bicycle[?(@.escape == 'Esc\\b\\f\\n\\r\\t\\u002A')]"),
                        JSONWriter.Feature.WriteNulls
                )
        );
    }

    public static final String STR =
            "{ \"store\": {\n" +
                    "    \"book\": [ \n" +
                    "      { \"category\": \"reference\",\n" +
                    "        \"author\": \"Nigel Rees\",\n" +
                    "        \"title\": \"Sayings of the Century\",\n" +
                    "        \"price\": 8.95\n" +
                    "      },\n" +
                    "      { \"category\": \"fiction\",\n" +
                    "        \"author\": \"Evelyn Waugh\",\n" +
                    "        \"title\": \"Sword of Honour\",\n" +
                    "        \"price\": 12.99\n" +
                    "      },\n" +
                    "      { \"category\": \"fiction\",\n" +
                    "        \"author\": \"Herman Melville\",\n" +
                    "        \"title\": \"Moby Dick\",\n" +
                    "        \"isbn\": \"0-553-21311-3\",\n" +
                    "        \"price\": 8.99\n" +
                    "      },\n" +
                    "      { \"category\": \"fiction\",\n" +
                    "        \"author\": \"J. R. R. Tolkien\",\n" +
                    "        \"title\": \"The Lord of the Rings\",\n" +
                    "        \"isbn\": \"0-395-19395-8\",\n" +
                    "        \"price\": 22.99\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"bicycle\": {\n" +
                    "      \"color\": \"red\",\n" +
                    "      \"price\": 19.95\n," +
                    "      \"gears\": [23, 50]\n," +
                    "      \"extra\": {\"x\": 0}\n," +
                    "      \"escape\" : \"Esc\\b\\f\\n\\r\\t\\u002A\",\n" +
                    "      \"nullValue\": null\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
}
