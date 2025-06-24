package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2563 {
    DateTime nowD = new DateTime(2018, 7, 14, 12, 13, 14, 0);

    @Test
    public void mutatedTest0() {
        Bean0 entity = new Bean0();
        entity.setNow(nowD);

        String json = JSON.toJSONString(entity);
        Bean0 bean0 = JSON.parseObject(json, Bean0.class);
        assertEquals(entity.now, bean0.now);
    }

    @Test
    public void mutatedTest1() {
        Bean0 entity = new Bean0();
        entity.setNow(nowD);

        byte[] jsonbBytes = JSONB.toBytes(entity);
        Bean0 bean0 = JSONB.parseObject(jsonbBytes, Bean0.class);
        assertEquals(entity.now, bean0.now);
    }

    @Data
    public static class Bean0 {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private DateTime now;
    }
}
