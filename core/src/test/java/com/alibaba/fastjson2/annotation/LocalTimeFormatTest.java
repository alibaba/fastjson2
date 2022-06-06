package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LocalTimeFormatTest {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{ \"localTime\": \"2022-05-30 11:36:11\" }", Bean.class);
        assertNotNull(bean.localTime);
        assertEquals(11, bean.localTime.getHour());
        assertEquals(36, bean.localTime.getMinute());
        assertEquals(11, bean.localTime.getSecond());
    }

    public static class Bean {
        public LocalTime localTime;
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{ \"localTime\": \"2022-05-30T11:36:11\" }", Bean1.class);
        assertNotNull(bean.localTime);
        assertEquals(11, bean.localTime.getHour());
        assertEquals(36, bean.localTime.getMinute());
        assertEquals(11, bean.localTime.getSecond());
    }

    public static class Bean1 {
        private LocalTime localTime;

        public LocalTime getLocalTime() {
            return localTime;
        }

        public void setLocalTime(LocalTime localTime) {
            this.localTime = localTime;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{ \"localTime\": \"2022-05-30 11:36:11\" }", Bean2.class);
        assertNotNull(bean.localTime);
        assertEquals(11, bean.localTime.getHour());
        assertEquals(36, bean.localTime.getMinute());
        assertEquals(11, bean.localTime.getSecond());
    }

    public static class Bean2 {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public LocalTime localTime;
    }
}
