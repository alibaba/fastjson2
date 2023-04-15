package com.alibaba.fastjson2.schema;

import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateValidatorTest {
    @Test
    public void test() {
        assertFalse(DateUtils.isLocalDate(null));
        assertFalse(DateUtils.isLocalDate(""));

        assertFalse(DateUtils.isLocalDate("2022-02-30"));
        assertTrue(DateUtils.isLocalDate("2022-02-18"));

        assertFalse(DateUtils.isLocalDate("2022-04-31"));
        assertTrue(DateUtils.isLocalDate("2022-04-30"));

        assertFalse(DateUtils.isLocalDate("2022"));
        assertFalse(DateUtils.isLocalDate("https://github.com/alibaba/fastjson2/issues"));
    }
}
