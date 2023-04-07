package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1325 {
    @Test
    public void test() {
        Date date = DateUtils.parseDate("2022-02-16T16:00:00.000Z");
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
