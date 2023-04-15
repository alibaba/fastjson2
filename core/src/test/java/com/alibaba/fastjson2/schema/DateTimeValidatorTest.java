package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateTimeValidatorTest {
    @Test
    public void test() {
        assertFalse(DateUtils.isDate(null));
        assertFalse(DateUtils.isDate(""));
        assertFalse(DateUtils.isDate("A000-00-00 00:00:00"));
        assertTrue(DateUtils.isDate("2000-04-30 00:00:00"));
        assertTrue(DateUtils.isDate("2000-05-31 00:00:00"));
        assertFalse(DateUtils.isDate("2000-04-31 00:00:00"));
        assertFalse(DateUtils.isDate("2000-05-41 00:00:00"));

        assertFalse(DateUtils.isDate("2000-05-01 25:00:00"));
        assertFalse(DateUtils.isDate("2000-05-01 00:61:00"));
        assertFalse(DateUtils.isDate("2000-05-01 00:00:62"));

        assertFalse(DateUtils.isDate("2000-05-01 00:00:620"));
        assertFalse(DateUtils.isDate("2000-05-01 00:00:62.0"));
    }
}
