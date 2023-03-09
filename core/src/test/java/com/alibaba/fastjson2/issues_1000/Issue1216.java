package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1216 {
    @Test
    public void test() throws Exception {
        String value = "2017-07-24 12:13:14";
        String value1 = "2018-07-24 12:13:14";

        long millis = DateUtils.parseMillis(value);
        long millis1 = DateUtils.parseMillis(value1);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        assertEquals(format.parse(value).getTime(), millis);
        assertEquals(format.parse(value1).getTime(), millis1);
    }
}
