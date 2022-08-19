package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue416 {
    @Test
    public void test() {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalTest localTest1 = new LocalTest();
        localTest1.setLocalDate(localDateTime.toLocalDate());
        localTest1.setLocalTime(localDateTime.toLocalTime());
        localTest1.setLocalDateTime(localDateTime);

        LocalTest localTest2 = JSON.parseObject(JSON.toJSONString(localTest1), LocalTest.class);

        assertEquals(localTest1.getLocalDate(), localTest2.getLocalDate());
    }

    @Data
    static class LocalTest {
        @JSONField(format = "yyyy-MM-dd")
        private LocalDate localDate;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime localDateTime;
        @JSONField(format = "HH:mm:ss")
        private LocalTime localTime;
    }
}
