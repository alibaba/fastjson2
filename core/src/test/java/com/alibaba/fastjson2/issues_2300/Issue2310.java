package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

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
                "    \"publishTime4\": \"9:0:00\",\n" +
                "    \"publishTime5\": \"9:0:00.000\"\n" +
                "}";
        TaskJobVO taskJobVO = JSONObject.parseObject(str, TaskJobVO.class);
        assertEquals(nine, taskJobVO.getPublishTime());
        assertEquals(nine, taskJobVO.getPublishTime2());
        assertEquals(nine, taskJobVO.getPublishTime3());
        assertEquals(nine, taskJobVO.getPublishTime4());

        assertEquals(nine, JSONReader.ofJSONB(JSONB.toBytes("9:0:0")).readLocalTime());
        assertEquals(nine, JSONReader.ofJSONB(JSONB.toBytes("09:0:0")).readLocalTime());
        assertEquals(nine, JSONReader.ofJSONB(JSONB.toBytes("9:00:0")).readLocalTime());
        assertEquals(nine, JSONReader.ofJSONB(JSONB.toBytes("9:0:00")).readLocalTime());
        assertEquals(nine, JSONReader.ofJSONB(JSONB.toBytes("9:0:0.000")).readLocalTime());
    }

    @Data
    private static class TaskJobVO {
        public LocalTime publishTime;
        public LocalTime publishTime2;
        public LocalTime publishTime3;
        public LocalTime publishTime4;
        public LocalTime publishTime5;
    }
}
