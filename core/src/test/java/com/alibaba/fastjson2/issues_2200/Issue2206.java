package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2206 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.birthday = LocalDateTime.of(2012, 2, 3, 12, 13, 14);
        String json = JSON.toJSONString(bean);
        assertEquals("{\"birthday\":\"2012-02-03T12:13:14+08:00\"}", json);
    }

    public static class Bean {
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "GMT+8")
        public LocalDateTime birthday;
    }
}
