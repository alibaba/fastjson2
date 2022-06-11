package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewDateTest {
    @Test
    public void test0() {
        Date date = (Date) JSON.parseObject("new Date(1654686106602)", Object.class);
        assertEquals(1654686106602L, date.getTime());
    }
}
