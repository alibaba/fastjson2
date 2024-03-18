package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2332 {
    @Test
    public void test() {
        DemoEntity entity = new DemoEntity();
        DemoEntity2 entity2 = new DemoEntity2();

        entity2.setNow(LocalDateTime.now());
        byte[] jsonString2 = JSONB.toBytes(entity2);
        // 不会报错
        DemoEntity2 demoEntity3 = JSONB.parseObject(jsonString2, DemoEntity2.class);
        assertEquals(entity2.now, demoEntity3.now);

        entity.setNow(Timestamp.valueOf(LocalDateTime.now()));
        String jsonString3 = JSON.toJSONString(entity2);
        // 不会报错
        DemoEntity2 demoEntity2 = JSON.parseObject(jsonString3, DemoEntity2.class);
        assertEquals(entity2.now.truncatedTo(ChronoUnit.SECONDS), demoEntity2.now);

        byte[] jsonb = JSONB.toBytes(entity);
        DemoEntity entity1 = JSONB.parseObject(jsonb, DemoEntity.class);
        assertEquals(entity.now, entity1.now);
    }

    @Data
    public static class DemoEntity {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Timestamp now;
    }

    @Data
    public static class DemoEntity2 {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime now;
    }
}
