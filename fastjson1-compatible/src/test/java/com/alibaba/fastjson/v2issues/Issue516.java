package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue516 {
    @Test
    public void test() {
        Date date = new Date(1656479550452L);
        String str = JSON.toJSONStringWithDateFormat(date, "yyyy-MM-dd HH:mm:ss.SSS");
        assertEquals("\"2022-06-29 13:12:30.452\"", str);
    }
}
