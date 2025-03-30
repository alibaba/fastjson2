package com.alibaba.fastjson2.issues_3400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3446 {
    @Test
    public void test() {
        String aaa = "{\n"
                + "  \"valid\": true,\n"
                + "  \"activityId\": 30000148386,\n"
                + "  // 生效日期区间\n"
                + "  \"validPeriod\": {\n"
                + "    \"start\": 1741664231000,\n"
                + "    \"end\": 1771665231000\n"
                + "  },\n"
                + "  // 疲劳度控制的区间\n"
                + "  \"periods\": [{ \n"
                + "    \"start\": \"00:00:00\",\n"
                + "    \"end\": \"09:59:59\"\n"
                + "  },{\n"
                + "    \"start\": \"10:00:00\",\n"
                + "    \"end\": \"15:59:59\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"start\": \"16:00:00\",\n"
                + "    \"end\": \"23:59:59\"\n"
                + "  }]\n"
                + "}";
        assertEquals(30000148386L, JSON.parseObject(aaa, InviteSignReminderBO.class).activityId);
        assertEquals(30000148386L, JSON.parseObject(aaa.toCharArray(), InviteSignReminderBO.class).activityId);
        assertEquals(30000148386L, JSON.parseObject(aaa.getBytes(StandardCharsets.UTF_8), InviteSignReminderBO.class).activityId);
    }

    public static class InviteSignReminderBO {
        public boolean valid;
        public Long activityId;
    }
}
