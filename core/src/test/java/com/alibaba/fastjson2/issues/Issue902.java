package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue902 {
    final Configuration conf = Configuration.builder()
            .options(Option.ALWAYS_RETURN_LIST, Option.SUPPRESS_EXCEPTIONS).build();

    @Test
    public void test() {
        String str = "{\"firstName\":\"John\",\"lastName\":\"doe\",\"age\":[1,2,3],\"address\":{\"streetAddress\":\"naist street\","
                + "\"city\":\"Nara\",\"postalCode\":\"630-0192\"},\"phoneNumbers\":[{\"type\":\"iPhone\",\"number\":\"0123-4567-8888\"},"
                + "{\"type\":\"home\",\"number\":\"0123-4567-8910\"}]}";

        assertEquals("[1,2,3]", JSONPath.extract(str, "$.age").toString());
        assertEquals("[\"John\"]", JSONPath.extract(str, "$.firstName", JSONPath.Feature.AlwaysReturnList).toString());

        JSONObject object = JSON.parseObject(str);
        assertEquals(
                "[\"John\"]",
                JSONPath
                        .of("$.firstName", JSONPath.Feature.AlwaysReturnList)
                        .eval(object)
                        .toString()
        );
        assertEquals(
                "[\"iPhone\",\"home\"]",
                JSONPath
                        .of("$.phoneNumbers.type", JSONPath.Feature.AlwaysReturnList)
                        .eval(object)
                        .toString()
        );
        assertEquals(
                "[\"iPhone\"]",
                JSONPath
                        .of("$.phoneNumbers[0].type", JSONPath.Feature.AlwaysReturnList)
                        .eval(object)
                        .toString()
        );
        assertEquals(
                "[\"naist street\"]",
                JSONPath
                        .of("$.address.streetAddress", JSONPath.Feature.AlwaysReturnList)
                        .eval(object)
                        .toString()
        );
        assertEquals(
                "[]",
                JSONPath
                        .of("$.eee", JSONPath.Feature.AlwaysReturnList)
                        .eval(object)
                        .toString()
        );
        assertEquals(
                "[]",
                JSONPath
                        .of("$.address.eee", JSONPath.Feature.AlwaysReturnList)
                        .eval(object)
                        .toString()
        );
        assertEquals(
                "[]",
                JSONPath
                        .of("$.phoneNumbers[10].type", JSONPath.Feature.AlwaysReturnList)
                        .eval(object)
                        .toString()
        );

        {
            String path = "$.phoneNumbers[*]";
            assertEquals(
                    JSON.toJSONString(
                            JsonPath.using(conf)
                                    .parse(str)
                                    .read(path)
                    ),
                    JSONPath
                            .of(path, JSONPath.Feature.AlwaysReturnList)
                            .eval(object)
                            .toString()
            );
        }

        {
            String path = "$.phoneNumbers";
            assertEquals(
                    JSON.toJSONString(
                            JsonPath.using(conf)
                                    .parse(str)
                                    .read(path)
                    ),
                    JSONPath
                            .of(path, JSONPath.Feature.AlwaysReturnList)
                            .eval(object)
                            .toString()
            );
        }

        {
            String path = "$.phoneNumbers[0]";
            assertEquals(
                    JSON.toJSONString(
                            JsonPath.using(conf)
                                    .parse(str)
                                    .read(path)
                    ),
                    JSONPath
                            .of(path, JSONPath.Feature.AlwaysReturnList)
                            .eval(object)
                            .toString()
            );
        }

        {
            String path = "$.phoneNumbers[0].*";
            assertEquals(
                    JSON.toJSONString(
                            JsonPath.using(conf)
                                    .parse(str)
                                    .read(path)
                    ),
                    JSONPath
                            .of(path, JSONPath.Feature.AlwaysReturnList)
                            .eval(object)
                            .toString()
            );
        }
    }

    @Test
    public void test1() {
        URL resource = this.getClass().getClassLoader().getResource("data/path_01.json");
        JSONObject object = JSON.parseObject(resource);
        String str = object.toJSONString();

        {
            String path = "$.events";
            assertEquals(
                    JSON.toJSONString(
                            JsonPath.using(conf)
                                    .parse(str)
                                    .read(path)
                    ),
                    JSONPath
                            .of(path, JSONPath.Feature.AlwaysReturnList)
                            .eval(object)
                            .toString()
            );
        }

        {
            String path = "$.events[0].EventProperties";
            assertEquals(
                    JSON.toJSONString(
                            JsonPath.using(conf)
                                    .parse(str)
                                    .read(path)
                    ),
                    JSONPath
                            .of(path, JSONPath.Feature.AlwaysReturnList)
                            .eval(object)
                            .toString()
            );
        }

        {
            String path = "$.events[0].EventProperties.*";
            assertEquals(
                    JSON.toJSONString(
                            JsonPath.using(conf)
                                    .parse(str)
                                    .read(path)
                    ),
                    JSONPath
                            .of(path, JSONPath.Feature.AlwaysReturnList)
                            .eval(object)
                            .toString()
            );
        }
    }
}
