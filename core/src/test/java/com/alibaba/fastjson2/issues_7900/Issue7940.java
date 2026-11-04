package com.alibaba.fastjson2.issues_7900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
public class Issue7940 {
    @Test
    public void truncatedObjectFromBytes() {
        String[] inputs = {
                "{\"a\":1",
                "{\"a\":1,\"b\":2",
                "{\"a\":1,\"b\":2,"
        };
        for (String input : inputs) {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            assertThrows(JSONException.class, () -> JSON.parse(bytes), input);
            assertThrows(JSONException.class, () -> JSONReader.of(bytes).readAny(), input);
        }
    }

    @Test
    public void truncatedObjectWithNonAsciiBytes() {
        // non-ASCII content makes JSON.parse(byte[]) pick the UTF8 reader instead of the ASCII one
        String[] inputs = {
                "{\"中\":1",
                "{\"中\":1,\"文\":2",
                "{\"中\":1,\"文\":2,"
        };
        for (String input : inputs) {
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            assertThrows(JSONException.class, () -> JSON.parse(bytes), input);
            assertThrows(JSONException.class, () -> JSONReader.of(bytes).readAny(), input);
            assertThrows(JSONException.class, () -> JSONReader.of(ByteBuffer.wrap(bytes), StandardCharsets.UTF_8).readAny(), input);
            assertThrows(JSONException.class, () -> JSONReader.of(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8).readAny(), input);
        }
    }

    @Test
    public void truncatedObjectFromChars() {
        String[] inputs = {
                "{\"a\":1",
                "{\"a\":1,\"b\":2",
                "{\"中\":1",
                "{\"中\":1,\"文\":2"
        };
        for (String input : inputs) {
            char[] chars = input.toCharArray();
            assertThrows(JSONException.class, () -> JSONReader.of(input).readAny(), input);
            assertThrows(JSONException.class, () -> JSONReader.of(chars).readAny(), input);
        }
    }

    @Test
    public void completeObjectsStillParse() {
        assertEquals(1, JSON.parseObject("{\"a\":1}").getIntValue("a"));
        assertEquals(2, JSON.parseObject("{\"a\":1,\"b\":2}".getBytes(StandardCharsets.UTF_8)).getIntValue("b"));

        JSONObject o1 = JSONReader.of("{\"a\":1}".toCharArray()).read(JSONObject.class);
        assertEquals(1, o1.getIntValue("a"));

        JSONObject o2 = JSON.parseObject("{\"a\":1} \n");
        assertEquals(1, o2.getIntValue("a"));

        JSONObject o3 = JSON.parseObject("{\"a\":1,\"b\":2,\"c\":3}");
        assertEquals(3, o3.getIntValue("c"));

        assertEquals(0, JSON.parseObject("{}".getBytes(StandardCharsets.UTF_8)).size());
    }
}
