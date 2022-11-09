package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class Issue896 {
    @Test
    public void test() {
        String s1Req = "{\"LimitPayTime\": \"\\/Date(-2209017600000+0800)\\/\",\"travelDate\": \"\\/Date(2209017600000+0800)\\/\"}";
        DateTimeConvertDto s1 = JSON.parseObject(s1Req, DateTimeConvertDto.class);
    }

    @Data
    public class DateTimeConvertDto {
        private LocalDateTime limitPayTime;
        private LocalDateTime travelDate;
    }
}
