package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OffsetDateTimeTest {
    @Test
    public void test() {
        OffsetDateTime odt = OffsetDateTime.now();
        String str = JSON.toJSONString(odt);
        OffsetDateTime odt1 = JSON.parseObject(str, OffsetDateTime.class);
        assertEquals(odt, odt1);
    }
}
