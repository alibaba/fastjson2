package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DateFormatBugTest {
    @Test
    public void testDateFormatBug() {
        // 使用特殊的日期格式 "yyyyMMddHHmmssSSSZ"
        JSONReader.Context context = new JSONReader.Context();
        context.setDateFormat("yyyyMMddHHmmssSSSZ");

        // 检查是否正确设置了日期格式标志
        assertFalse(context.isFormatyyyyMMddhhmmss19(),
                "formatyyyyMMddhhmmss19 should be false when dateFormat is 'yyyyMMddHHmmssSSSZ'");

        // 由于bug的存在，formatyyyyMMddhhmmss19会被错误地设置为true
        // 这会导致日期解析行为不正确
    }

    @Test
    public void testDateFormatFlags() {
        JSONReader.Context context = new JSONReader.Context();

        // 测试 "yyyyMMddHHmmssSSSZ" 格式
        context.setDateFormat("yyyyMMddHHmmssSSSZ");
        assertTrue(context.useSimpleFormatter, "useSimpleFormatter should be true");
        assertFalse(context.isFormatyyyyMMddhhmmss19(), "formatyyyyMMddhhmmss19 should be false");
        assertFalse(context.isFormatyyyyMMddhhmmssT19(), "formatyyyyMMddhhmmssT19 should be false");
        assertFalse(context.isFormatyyyyMMdd8(), "formatyyyyMMdd8 should be false");
        assertFalse(context.isFormatISO8601(), "formatISO8601 should be false");
        assertFalse(context.isFormatUnixTime(), "formatUnixTime should be false");
        assertFalse(context.isFormatMillis(), "formatMillis should be false");
    }
}
