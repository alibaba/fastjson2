package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue632 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.timestamp = LocalDateTime.of(2017, 03, 15, 12, 13, 14);
        assertEquals("{\"timestamp\":\"2017-03-15 12:13:14\"}", JSON.toJSONString(bean, "millis"));
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
