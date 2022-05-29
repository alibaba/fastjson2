package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2_vo.UUID1;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UUIDTest2 {
    static UUID[] values = new UUID[]{
            UUID.randomUUID(),
    };

    @Test
    public void test_jsonb() {
        for (UUID dateTime : values) {
            UUID1 vo = new UUID1();
            vo.setId(dateTime);
            byte[] jsonbBytes = JSONB.toBytes(
                    Collections.singletonMap("id", vo.getId().toString()));

            UUID1 v1 = JSONB.parseObject(jsonbBytes, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_utf8() {
        for (UUID dateTime : values) {
            UUID1 vo = new UUID1();
            vo.setId(dateTime);
            byte[] utf8Bytes = JSON.toJSONBytes(
                    Collections.singletonMap("id", vo.getId().toString()));

            UUID1 v1 = JSON.parseObject(utf8Bytes, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_str() {
        for (UUID dateTime : values) {
            UUID1 vo = new UUID1();
            vo.setId(dateTime);
            String str = JSON.toJSONString(
                    Collections.singletonMap("id", vo.getId().toString())
            );

            UUID1 v1 = JSON.parseObject(str, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }

    @Test
    public void test_ascii() {
        for (UUID dateTime : values) {
            UUID1 vo = new UUID1();
            vo.setId(dateTime);
            byte[] utf8Bytes = JSON.toJSONBytes(
                    Collections.singletonMap("id", vo.getId().toString())
            );

            UUID1 v1 = JSON.parseObject(utf8Bytes, 0, utf8Bytes.length, StandardCharsets.US_ASCII, UUID1.class);
            assertEquals(vo.getId(), v1.getId());
        }
    }
}
