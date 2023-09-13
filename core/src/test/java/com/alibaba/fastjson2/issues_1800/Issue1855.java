package com.alibaba.fastjson2.issues_1800;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue1855 {
    @Test
    public void test() {
        byte[] bytes = JSONB.toBytes(JSONObject.of("date", null), JSONWriter.Feature.WriteNulls);
        assertNull(JSONB.parseObject(bytes, Bean.class).date);
        assertNull(JSONB.parseObject(bytes, Bean1.class).date);
        assertNull(JSONB.parseObject(bytes, Bean2.class).date);
        assertNull(JSONB.parseObject(bytes, Bean3.class).date);
        assertNull(JSONB.parseObject(bytes, Bean4.class).date);
        assertNull(JSONB.parseObject(bytes, Bean5.class).date);
        assertNull(JSONB.parseObject(bytes, Bean6.class).date);
    }

    public static class Bean {
        public LocalDate date;
    }

    public static class Bean1 {
        public LocalDateTime date;
    }

    public static class Bean2 {
        public LocalTime date;
    }

    public static class Bean3 {
        public OffsetTime date;
    }

    public static class Bean4 {
        public OffsetDateTime date;
    }

    public static class Bean5 {
        public ZonedDateTime date;
    }

    public static class Bean6 {
        public Instant date;
    }
}
