package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.time.ZoneId;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1325 {
    @Test
    public void test() {
        Date date = new Date(
                DateUtils.parseMillis("2022-02-16T16:00:00.000Z", ZoneId.DEFAULT_ZONE_ID)
        );

        String[] strings = new String[] {
                "\"2022-02-16T16:00:00Z\"",
                "\"2022-02-16T16:00:00.0Z\"",
                "\"2022-02-16T16:00:00.00Z\"",
                "\"2022-02-16T16:00:00.000Z\"",
                "\"2022-02-16T16:00:00.0000Z\"",
                "\"2022-02-16T16:00:00.00000Z\"",
                "\"2022-02-16T16:00:00.000000Z\"",
                "\"2022-02-16T16:00:00.0000000Z\"",
                "\"2022-02-16T16:00:00.00000000Z\"",
                "\"2022-02-16T16:00:00.000000000Z\"",
        };

        for (String string : strings) {
            assertEquals(
                    date.getTime(),
                    JSON.parseObject(string, Date.class).getTime()
            );
            assertEquals(
                    date.getTime(),
                    JSON.parseObject(string.getBytes(), Date.class).getTime()
            );
            assertEquals(
                    date.getTime(),
                    JSON.parseObject(string.toCharArray(), Date.class).getTime()
            );
        }

        String str = " {\"activeDate\": \"2022-02-16T16:00:00.000Z\"}";
        assertEquals(
                date.getTime(),
                JSON.parseObject(str, Bean.class)
                        .activeDate
                        .getTime()
        );
        assertEquals(
                date.getTime(),
                JSON.parseObject(str.toCharArray(), Bean.class)
                        .activeDate
                        .getTime()
        );
        assertEquals(
                date.getTime(),
                JSON.parseObject(str.getBytes(), Bean.class)
                        .activeDate
                        .getTime()
        );
    }

    public static class Bean {
        public Date activeDate;
    }
}
