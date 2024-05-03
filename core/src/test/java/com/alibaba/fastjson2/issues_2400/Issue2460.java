package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2460 {
    @Test
    public void test() {
        Timestamp ts1 = Timestamp.from(Instant.ofEpochMilli(1713494836000L));
        String str1 = JSON.toJSONString(ts1);
        Timestamp ts1Parsed = JSON.parseObject(str1, Timestamp.class);
        assertEquals(ts1.getNanos(), ts1Parsed.getNanos());

        Timestamp ts2 = Timestamp.from(Instant.ofEpochMilli(1713494836123L));
        String str2 = JSON.toJSONString(ts2);
        Timestamp ts2Parsed = JSON.parseObject(str2, Timestamp.class);
        assertEquals(ts2.getNanos(), ts2Parsed.getNanos());

        assertEquals(str1.substring(0, 20), str2.substring(0, 20));
    }
}
