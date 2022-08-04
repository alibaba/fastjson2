package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue608 {
    @Test
    public void pair_serialize() {
        assertEquals(
                "{\"aaa\":\"bbb\"}",
                JSON.toJSONString(Pair.of("aaa", "bbb"))
        );

        assertEquals(
                "{\"left\":\"aaa\",\"right\":\"bbb\"}",
                JSON.toJSONString(Pair.of("aaa", "bbb"), JSONWriter.Feature.WritePairAsJavaBean)
        );
    }

    @Test
    public void pair_deserialize() {
        Pair pair = JSON.parseObject("{\"aaa\":\"bbb\"}", Pair.class);
        assertEquals("aaa", pair.getLeft());
        assertEquals("bbb", pair.getRight());
    }

    @Test
    public void pair_deserialize_jsonb() {
        byte[] jsonbBytes = JSONObject.of("aaa", "bbb").toJSONBBytes();
        Pair pair = JSONB.parseObject(jsonbBytes, Pair.class);
        assertEquals("aaa", pair.getLeft());
        assertEquals("bbb", pair.getRight());
    }

    @Test
    public void pair_deserialize1() {
        Pair<String, Integer> pair = JSON.parseObject("{\"aaa\":\"123\"}", new TypeReference<Pair<String, Integer>>() {
        });
        assertEquals(123, pair.getRight());
    }

    @Test
    public void pair_deserialize1_jsonb() {
        byte[] jsonbBytes = JSONObject.of("aaa", "123").toJSONBBytes();
        Pair<String, Integer> pair = JSONB.parseObject(jsonbBytes, new TypeReference<Pair<String, Integer>>() {
        });
        assertEquals(123, pair.getRight());
    }

    @Test
    public void pair_deserialize2() {
        Pair<String, Integer> pair = JSON.parseObject("{\"left\":\"aaa\",\"right\":\"123\"}", new TypeReference<Pair<String, Integer>>() {
        });
        assertEquals(123, pair.getRight());
    }

    @Test
    public void pair_deserialize2_jsonb() {
        byte[] jsonbBytes = JSONObject.of("left", "aaa", "right", "123").toJSONBBytes();
        Pair<String, Integer> pair = JSONB.parseObject(jsonbBytes, new TypeReference<Pair<String, Integer>>() {
        });
        assertEquals(123, pair.getRight());
    }

    @Test
    public void pair_deserialize3() {
        Pair<String, Integer> pair = JSON.parseObject("{\"left\":\"aaa\",\"right\":\"123\",\"x\":123}", new TypeReference<Pair<String, Integer>>() {
        });
        assertEquals(123, pair.getRight());
    }

    @Test
    public void pair_deserialize4() {
        Pair<String, Integer> pair = JSON.parseObject("[\"aaa\",\"123\"]", new TypeReference<Pair<String, Integer>>() {
        });
        assertEquals(123, pair.getRight());
    }
}
