package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2905 {
    @Test
    public void test() {
        String s = "{\"saleEndTime\": \"23:00:00\"}";
        Bean order = JSON.parseObject(s, Bean.class);
        assertNotNull(order.saleEndTime);
    }

    public static class Bean {
        @JSONField(format = "HH:mm:ss")
        public Date saleEndTime;
    }
}
