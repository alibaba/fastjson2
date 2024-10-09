package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2310 {
    @Test
    void test() {
        LocalTime nine = LocalTime.of(9, 0, 0);
        String str = "{\n" +
                "    \"publishTime\": \"9:0:0\",\n" +
                "    \"publishTime2\": \"09:0:0\",\n" +
                "    \"publishTime3\": \"9:00:0\",\n" +
                "    \"publishTime4\": \"9:0:00\"" +
                "}";
        TaskJobVO taskJobVO = JSONObject.parseObject(str, TaskJobVO.class);
        assertEquals(nine, taskJobVO.getPublishTime());
        assertEquals(nine, taskJobVO.getPublishTime2());
        assertEquals(nine, taskJobVO.getPublishTime3());
        assertEquals(nine, taskJobVO.getPublishTime4());

        TaskJobVO taskJobVO1 = JSON.parseObject(str.toCharArray(), TaskJobVO.class);
        assertEquals(nine, taskJobVO1.getPublishTime());
        assertEquals(nine, taskJobVO1.getPublishTime2());
        assertEquals(nine, taskJobVO1.getPublishTime3());
        assertEquals(nine, taskJobVO1.getPublishTime4());

        TaskJobVO taskJobVO2 = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), TaskJobVO.class);
        assertEquals(nine, taskJobVO2.getPublishTime());
        assertEquals(nine, taskJobVO2.getPublishTime2());
        assertEquals(nine, taskJobVO2.getPublishTime3());
        assertEquals(nine, taskJobVO2.getPublishTime4());

        String[] strings = {
                "9:0:0", "09:0:0", "9:00:0", "9:0:00", "9:00:00", "09:0:00", "09:00:0", "09:00:00"
        };
        for (String string : strings) {
            assertEquals(nine, JSONReader.ofJSONB(JSONB.toBytes(string))
                    .readLocalTime());

            String json = JSON.toJSONString(string);
            assertEquals(nine, JSONReader.of(json.toCharArray())
                    .readLocalTime());
            byte[] utf8 = json.getBytes(StandardCharsets.UTF_8);
            assertEquals(nine, JSONReader.of(utf8).readLocalTime());
            assertEquals(nine, JSONReader.of(utf8, 0, utf8.length, StandardCharsets.US_ASCII).readLocalTime());
            assertEquals(nine, JSONReader.of(json).readLocalTime());
        }
    }

    @Data
    private static class TaskJobVO {
        public LocalTime publishTime;
        public LocalTime publishTime2;
        public LocalTime publishTime3;
        public LocalTime publishTime4;
    }
}
