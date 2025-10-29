package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue3845 {
    @Test
    void test_issue3845() {
        LocalDateTime originalTime = LocalDateTime.of(0, 1, 1, 0, 0, 0).minusHours(8);
        String jsonString = "\"-0001-12-31 16:00:00\"";
        LocalDateTime parsedTime = JSON.parseObject(jsonString, LocalDateTime.class);
        assertEquals(originalTime, parsedTime);
    }

    //测试基础公元前日期（年份为0，即公元前1年）
    @Test
    void test_1() {
        LocalDateTime dateTime = LocalDateTime.of(0, 10, 5, 14, 30, 15);
        assertEquals(dateTime, JSON.parseObject(JSON.toJSONString(dateTime), LocalDateTime.class));
        assertEquals(dateTime, JSON.parseObject(JSON.toJSONBytes(dateTime), LocalDateTime.class));
        assertEquals(dateTime, JSONB.parseObject(JSONB.toBytes(dateTime), LocalDateTime.class));
    }

    //测试负数年份
    @Test
    void test_2() {
        LocalDateTime dateTime0 = LocalDateTime.of(-1, 3, 20, 8, 0);
        assertEquals(dateTime0, JSON.parseObject(JSON.toJSONString(dateTime0), LocalDateTime.class));
        assertEquals(dateTime0, JSON.parseObject(JSON.toJSONBytes(dateTime0), LocalDateTime.class));
        assertEquals(dateTime0, JSONB.parseObject(JSONB.toBytes(dateTime0), LocalDateTime.class));

        LocalDateTime dateTime = LocalDateTime.of(-1, 3, 20, 8, 0, 5);
        assertEquals(dateTime, JSON.parseObject(JSON.toJSONString(dateTime), LocalDateTime.class));
        assertEquals(dateTime, JSON.parseObject(JSON.toJSONBytes(dateTime), LocalDateTime.class));
        assertEquals(dateTime, JSONB.parseObject(JSONB.toBytes(dateTime), LocalDateTime.class));

        LocalDateTime dateTime2 = LocalDateTime.of(-9999, 11, 30, 23, 59, 59);
        assertEquals(dateTime2, JSON.parseObject(JSON.toJSONString(dateTime2), LocalDateTime.class));
        assertEquals(dateTime2, JSON.parseObject(JSON.toJSONBytes(dateTime2), LocalDateTime.class));
        assertEquals(dateTime2, JSONB.parseObject(JSONB.toBytes(dateTime2), LocalDateTime.class));
    }

    //测试公元前日期带微秒（6位小数）、纳秒（9位小数）
    @Test
    void test_3() {
        LocalDateTime dateTime = LocalDateTime.of(-50, 8, 15, 10, 10, 10, 123456000);
        assertEquals(dateTime, JSON.parseObject(JSON.toJSONString(dateTime), LocalDateTime.class));
        assertEquals(dateTime, JSON.parseObject(JSON.toJSONBytes(dateTime), LocalDateTime.class));
        assertEquals(dateTime, JSONB.parseObject(JSONB.toBytes(dateTime), LocalDateTime.class));

        LocalDateTime dateTime2 = LocalDateTime.of(-1234, 9, 9, 9, 9, 9, 123456789);
        assertEquals(dateTime2, JSON.parseObject(JSON.toJSONString(dateTime2), LocalDateTime.class));
        assertEquals(dateTime2, JSON.parseObject(JSON.toJSONBytes(dateTime2), LocalDateTime.class));
        assertEquals(dateTime2, JSONB.parseObject(JSONB.toBytes(dateTime2), LocalDateTime.class));
    }

    //测试无效的公元前日期-日期错误（非闰年）
    @Test
    void test_5() {
        String invalidJson = "\"-0002-02-29 10:00:00\"";
        byte[] invalidUtf8 = invalidJson.getBytes(StandardCharsets.UTF_8);

        assertThrows(JSONException.class, () -> JSON.parseObject(invalidJson, LocalDateTime.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(invalidUtf8, LocalDateTime.class));
    }

    //测试无效的公元前日期-时间错误
    @Test
    void test_6() {
        String invalidJson = "\"-0001-01-01 25:00:00\"";
        byte[] invalidUtf8 = invalidJson.getBytes(StandardCharsets.UTF_8);

        assertThrows(JSONException.class, () -> JSON.parseObject(invalidJson, LocalDateTime.class));
        assertThrows(JSONException.class, () -> JSON.parseObject(invalidUtf8, LocalDateTime.class));
    }
}
