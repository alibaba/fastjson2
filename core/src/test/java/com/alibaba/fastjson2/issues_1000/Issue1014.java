package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1014 {
    @Test
    public void test() {
        String str = "{\"callTime\": \"2022-12-02T03:33:52.000+0000\"}";
        BizDTO bizDTO = JSON.parseObject(str, BizDTO.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bizDTO.getCalltime());
        LocalDateTime calltime = bizDTO.calltime;
        assertEquals(2022, calltime.getYear());
        assertEquals(12, calltime.getMonthValue());
        assertEquals(2, calltime.getDayOfMonth());
        assertEquals(11, calltime.getHour());
        assertEquals(33, calltime.getMinute());
    }

    @Data
    public static class BizDTO
            implements Serializable {
        private LocalDateTime calltime;
    }
}
