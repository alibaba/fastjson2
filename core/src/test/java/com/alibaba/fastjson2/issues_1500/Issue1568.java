package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;

import java.util.Date;

/**
 * @author Gabriel
 * @date 2023/6/16 10:53:48
 */
public class Issue1568 {
    static class Time {
        public Date d = new Date();
    }

    public static void main(String[] args) {
        JSON.configWriterDateFormat("millis");
        Time t = new Time();
        byte[] bytes = JSONB.toBytes(t);
        System.out.println(JSON.toJSONString(t));
        System.out.println(JSONB.toJSONString(bytes));
    }
}
