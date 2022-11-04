package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue902 {
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
    }
}
