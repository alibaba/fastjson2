package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
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

    @Test
    public void test1() throws Exception {
        AppResp appResp = new AppResp();
        appResp.setTimestamp(LocalDateTime.of(2017, 03, 15, 12, 13, 14));
        assertEquals("{\"message\":\"成功\",\"status\":\"200\",\"timestamp\":\"2017-03-15 12:13:14\"}", JSON.toJSONString(appResp));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appResp", appResp);
        System.out.println(jsonObject.toJSONString());
    }

    @Data
    public class AppResp<T>
            implements Serializable {
        private String status;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;
        private String message;
        private T body;

        public AppResp() {
            timestamp = LocalDateTime.now();
            this.status = "200";
            this.message = "成功";
        }
    }
}
