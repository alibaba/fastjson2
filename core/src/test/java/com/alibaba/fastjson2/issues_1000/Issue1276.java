package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1276 {
    @Test
    public void test() {
        String s = "{\"id\":1,\"sendTime\":\"2023-03-24 11:10:11\"}";
        Bean notice = JSON.parseObject(s, Bean.class);
        assertEquals("\"2023-03-24 11:10:00\"", JSON.toJSONString(notice.sendTime));
        assertEquals("{\"id\":1,\"sendTime\":\"2023-03-24 11:10\"}", JSON.toJSONString(notice));
    }

    @Data
    public static class Bean {
        private Long id;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        private Date sendTime;
    }
}
