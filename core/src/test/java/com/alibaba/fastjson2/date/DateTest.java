package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest {
    @Test
    public void test() {
        String str = "2023-03-11T11:33:22Z";
        long millis = DateUtils.parseMillis(str);

        assertEquals(millis, DateUtils.parseMillis("2023-03-11 19:33:22 CST"));
        assertEquals(
                millis,
                JSON.parseObject("\"2023-03-11 19:33:22 CST\"", Date.class)
                        .getTime()
        );

        assertEquals(
                millis,
                JSON.parseObject("\"2023-03-11T11:33:22Z\"", Date.class)
                        .getTime()
        );

        // ISO8601
        assertEquals(
                millis,
                JSON.parseObject("\"2023-03-11T11:33:22+0000\"", Date.class)
                        .getTime()
        );
    }
}
