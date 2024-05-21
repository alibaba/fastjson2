package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue2598 {
    @Test
    public void test1() {
        assertThrows(JSONException.class, () -> JSON.parseObject("2023-03-24 11:10:00", Date.class));
    }

    @Test
    public void test2() {
        assertThrows(com.alibaba.fastjson.JSONException.class, () -> com.alibaba.fastjson.JSON.parseObject("2023-03-24 11:10:00", Date.class));
    }

    @Test
    public void test3() {
        Date sendTime1 = JSON.parseObject("\"2023-03-24 11:10:00\"", Date.class);
        Date sendTime2 = com.alibaba.fastjson.JSON.parseObject("\"2023-03-24 11:10:00\"", Date.class);
        assertEquals(sendTime1, sendTime2);
    }
}
