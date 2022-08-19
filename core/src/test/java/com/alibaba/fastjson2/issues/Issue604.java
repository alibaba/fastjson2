package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue604 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.setPickUpTime(LocalDateTime.of(2014, 3, 18, 12, 13, 14));
        assertEquals(
                "{\"pickUpTime\":\"2014-03-18\"}",
                JSON.toJSONString(bean, "millis")
        );
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd")
        private LocalDateTime pickUpTime;

        public LocalDateTime getPickUpTime() {
            return pickUpTime;
        }

        public void setPickUpTime(LocalDateTime pickUpTime) {
            this.pickUpTime = pickUpTime;
        }
    }
}
