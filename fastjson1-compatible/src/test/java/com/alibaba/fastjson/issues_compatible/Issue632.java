package com.alibaba.fastjson.issues_compatible;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue632 {
    @Test
    public void test1() throws Exception {
        AppResp appResp = new AppResp();
        appResp.setTimestamp(LocalDateTime.of(2017, 03, 15, 12, 13, 14));
        assertEquals("{\"message\":\"成功\",\"status\":\"200\",\"timestamp\":\"2017-03-15 12:13:14\"}", com.alibaba.fastjson.JSON.toJSONString(appResp));
        assertEquals("{\"message\":\"成功\",\"status\":\"200\",\"timestamp\":\"2017-03-15 12:13:14\"}", com.alibaba.fastjson2.JSON.toJSONString(appResp));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appResp", appResp);
        assertEquals("{\"appResp\":{\"message\":\"成功\",\"status\":\"200\",\"timestamp\":\"2017-03-15 12:13:14\"}}", jsonObject.toJSONString());
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
