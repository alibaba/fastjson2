package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest4 {
    @Test
    public void getString() {
        assertEquals(
                "2022-09-24 17:14:03.321",
                JSONObject
                        .of("date", new Date(1664010843321L))
                        .getString("date")
        );

        assertEquals(
                "2022-09-24 17:14:03.32",
                JSONObject
                        .of("date", new Date(1664010843320L))
                        .getString("date")
        );

        assertEquals(
                "2022-09-24 17:14:03.3",
                JSONObject
                        .of("date", new Date(1664010843300L))
                        .getString("date")
        );

        assertEquals(
                "2022-09-24 17:14:03",
                JSONObject
                        .of("date", new Date(1664010843000L))
                        .getString("date")
        );

        Object[] values = new Object[] {
                Boolean.TRUE,
                'A',
                UUID.randomUUID(),
                1,
                2L,
                TimeUnit.DAYS
        };
        for (Object value : values) {
            assertEquals(
                    value.toString(),
                    JSONObject
                            .of("value", value)
                            .getString("value")
            );
        }
    }
}
