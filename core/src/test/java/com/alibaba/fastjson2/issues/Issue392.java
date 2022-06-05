package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue392 {
    @Test
    public void test() {
        String date1 = JSONObject.of("date1", 1654053162).toString();
        Date date = JSON.parseObject(date1, Bean.class).getDate1();
        assertEquals(1654053162000L, date.getTime());
    }

    @Data
    static class Bean {
        @JSONField(format = "unixtime")
        private Date date1;
    }

    @Test
    public void test1() {
        String date1 = JSONObject.of("date1", 1654053162).toString();
        Date date = JSON.parseObject(date1, Bean1.class).getDate1();
        assertEquals(1654053162000L, date.getTime());
    }

    public static class Bean1 {
        @JSONField(format = "unixtime")
        private Date date1;

        public Date getDate1() {
            return date1;
        }

        public void setDate1(Date date1) {
            this.date1 = date1;
        }
    }

    @Test
    public void test2() {
        String date1 = JSONObject.of("date1", 1654053162).toString();
        Date date = JSON.parseObject(date1, Bean2.class).date1;
        assertEquals(1654053162000L, date.getTime());
    }

    @Test
    public void test2_str() {
        String date1 = JSONObject.of("date1", "1654053162").toString();
        Date date = JSON.parseObject(date1, Bean2.class).date1;
        assertEquals(1654053162000L, date.getTime());
    }

    @Test
    public void test2_str1() {
        String date1 = JSONObject.of("date1", "165405316").toString();
        Date date = JSON.parseObject(date1, Bean2.class).date1;
        assertEquals(165405316000L, date.getTime());
    }

    @Test
    public void test2_str2() {
        String date1 = JSONObject.of("date1", "16540531").toString();
        Date date = JSON.parseObject(date1, Bean2.class).date1;
        assertEquals(16540531000L, date.getTime());
    }

    @Test
    public void test2_str3() {
        String date1 = JSONObject.of("date1", "1654053").toString();
        Date date = JSON.parseObject(date1, Bean2.class).date1;
        assertEquals(1654053000L, date.getTime());
    }

    public static class Bean2 {
        @JSONField(format = "unixtime")
        public Date date1;
    }
}
