package com.alibaba.fastjson2.v1issues.issue_1400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1493 {
    @Test
    public void test_for_issue() throws Exception {
        TestBean test = new TestBean();
        String stime2 = "2017-09-22T15:08:56";

        LocalDateTime time1 = LocalDateTime.now();
        time1 = time1.minusNanos(10L);
        System.out.println(time1.getNano());
        LocalDateTime time2 = LocalDateTime.parse(stime2);
        test.setTime1(time1);
        test.setTime2(time2);
        String t1 = JSON.toJSONString(time1);

        String json = JSON.toJSONString(test);
        assertEquals("{\"time1\":" + t1 + ",\"time2\":\"2017-09-22 15:08:56\"}", json);

        //String default_format = JSON.DEFFAULT_LOCAL_DATE_TIME_FORMAT;
        //JSON.DEFFAULT_LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        //String stime1 = DateTimeFormatter.ofPattern(JSON.DEFFAULT_LOCAL_DATE_TIME_FORMAT, Locale.CHINA).format(time1);

        json = JSON.toJSONString(test);
        assertEquals("{\"time1\":" + JSON.toJSONString(time1) + ",\"time2\":\"2017-09-22 15:08:56\"}", json);

        String pattern = "yyyy-MM-dd'T'HH:mm:ss";
        String stime1 = DateTimeFormatter.ofPattern(pattern, Locale.CHINA).format(time1);

        json = JSON.toJSONString(test, "yyyy-MM-dd'T'HH:mm:ss");
        assertEquals("{\"time1\":\"" + stime1 + "\",\"time2\":\"" + stime2 + "\"}", json);

        //JSON.DEFFAULT_LOCAL_DATE_TIME_FORMAT = default_format;
    }

    public static class TestBean {
        LocalDateTime time1;
        LocalDateTime time2;

        public LocalDateTime getTime1() {
            return time1;
        }

        public void setTime1(LocalDateTime time1) {
            this.time1 = time1;
        }

        public LocalDateTime getTime2() {
            return time2;
        }

        public void setTime2(LocalDateTime time2) {
            this.time2 = time2;
        }
    }
}
