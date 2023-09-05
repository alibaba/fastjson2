package com.alibaba.fastjson2.fastcode;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class DateTest {
    @Test
    public void test() {
        Date date = new Date();
        String str = date.toString();
        char[] chars = str.toCharArray();
        System.out.println(str);
    }
}
