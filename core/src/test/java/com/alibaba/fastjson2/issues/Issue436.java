package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue436 {
    @Test
    public void test() {
        for (int i = 0; i < 1000; i++) {
            Date now = new Date();
            Date startTime = new Date(now.getTime() + 10 * 1000);
            Date endTime = new Date(startTime.getTime() + 1000 * 60 * 1000);
            Bean bean = new Bean();
            bean.setAvailable_begin_time(startTime);
            bean.setAvailable_end_time(endTime);
            String jsonStr = JSON.toJSONString(bean);
            assertTrue(jsonStr.contains("+") && jsonStr.contains("T"));
        }
    }

    @Data
    public static class Bean {
        public Date available_begin_time;
        public Date available_end_time;
    }
}
