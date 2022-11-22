package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue952 {
    @Test
    public void test() {
        assertEquals(
                1672502399000L,
                DateUtils.toMillis19("2022-12-31 23:59:59", 0, ZoneId.of("Asia/Shanghai"))
        );
    }
}
