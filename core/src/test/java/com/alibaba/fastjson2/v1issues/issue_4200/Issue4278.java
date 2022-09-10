package com.alibaba.fastjson2.v1issues.issue_4200;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4278 {
    @Test
    public void test() {
        String str = "{\"num\":Infinity }";
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();

        assertEquals(
                Double.POSITIVE_INFINITY,
                JSON.parseObject(str).get("num")
        );

        assertEquals(
                Double.POSITIVE_INFINITY,
                JSON.parseObject(chars).get("num")
        );

        assertEquals(
                Double.POSITIVE_INFINITY,
                JSON.parseObject(bytes).get("num")
        );

        assertEquals(
                Double.POSITIVE_INFINITY,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.UTF_8).get("num")
        );

        assertEquals(
                Double.POSITIVE_INFINITY,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1).get("num")
        );

        assertEquals(
                Double.POSITIVE_INFINITY,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII).get("num")
        );

        assertEquals(
                Double.POSITIVE_INFINITY,
                new JSONReaderStr(str).readObject().get("num")
        );

        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            JSONObject object = new JSONObject();
            jsonReader.readObject(object);
            assertEquals(
                    Double.POSITIVE_INFINITY,
                    object.get("num")
            );
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
            assertEquals(
                    Double.POSITIVE_INFINITY,
                    jsonReader.readObject().get("num")
            );
        }
    }
}
