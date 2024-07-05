package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2436 {
    @Test
    public void test() {
        String str = JSONObject.of("date", "2024-06-14 12:13:00").toJSONString();
        String str1 = JSONObject.of("date", "2024-06-14 12:13").toJSONString();
        Date date = JSON.parseObject(str, Bean.class).date;
        Date date1 = JSON.parseObject(str1, Bean.class).date;
        assertEquals(date.getTime(), date1.getTime());
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date date;
    }
}
