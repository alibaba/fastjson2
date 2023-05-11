package com.alibaba.fastjson2.aliyun;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatTest {
    @Test
    public void test() {
        String str = "\"2017-11-12 13:14:15\"";
        Date date = JSON.parseObject(str, Date.class);
        String str1 = JSON.toJSONString(date, "yyyy-MM-dd HH:mm:ss");
        assertEquals(str, str1);
    }
}
