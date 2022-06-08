package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue436 {
    @Test
    public void test() {
        for (int i = 0; i < 1000; i++) {
            Date now = new Date();
            Date startTime = new Date(now.getTime() + 10 * 1000);
            Date endTime = new Date(startTime.getTime() + 1000 * 60 * 1000);
            Bean bean = new Bean();
            bean.setAvailableBeginTime(startTime);
            bean.setAvailableEndTime(endTime);
            String jsonStr1 = JSON.toJSONString(bean);
            assertTrue(jsonStr1.contains("+"));
            String jsonStr2 = JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss");
            assertFalse(jsonStr2.contains("+"));
        }
    }

    @Data
    public static class Bean {
        public Date availableBeginTime;
        public Date availableEndTime;
    }
}
