package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue261 {
    @Test
    public void test0() {
        assertNull(JSON.parseObject("\"\"", LocalDateTime.class));
        assertNull(JSON.parseObject("null", LocalDateTime.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", LocalDateTime.class)
        );

        assertNull(JSON.parseObject("\"\"", LocalDate.class));
        assertNull(JSON.parseObject("null", LocalDate.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", LocalDate.class)
        );

        assertNull(JSON.parseObject("\"\"", LocalTime.class));
        assertNull(JSON.parseObject("null", LocalTime.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", LocalTime.class)
        );

        assertNull(JSON.parseObject("\"\"", ZonedDateTime.class));
        assertNull(JSON.parseObject("null", ZonedDateTime.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", ZonedDateTime.class)
        );

        assertNull(JSON.parseObject("\"\"", Instant.class));
        assertNull(JSON.parseObject("null", Instant.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", Instant.class)
        );

        assertNull(JSON.parseObject("\"\"", Date.class));
        assertNull(JSON.parseObject("null", Date.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", Date.class)
        );
    }
}
