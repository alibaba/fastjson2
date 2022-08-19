package com.alibaba.fastjson2.date;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;

public class ShanghaiOffsetTest {
    @Test
    public void test() {
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");

        for (int year = 1900; year < 2000; year++) {
            for (int month = 1; month <= 12; month++) {
                LocalDateTime ldt = LocalDateTime.of(year, month, 1, 0, 0, 0);
                ZoneRules rules = zoneId.getRules();
                ZoneOffset offset = rules.getOffset(ldt);
                System.out.println(ldt.toLocalDate() + "\t" + offset);
            }
        }
    }
}
