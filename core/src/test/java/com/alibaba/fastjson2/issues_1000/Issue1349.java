package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1349 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"date\":\"1863792000000\"}", Bean.class);
        assertEquals(1863792000000L, bean.date.getTime());
    }

    public static class Bean {
        @JSONField(locale = "zh", format = "yyyy-mm-dd")
        public Date date;
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"date\":\"1863792000000\"}", Bean1.class);
        assertEquals(1863792000000L, bean.date.getTime());
    }

    public static class Bean1 {
        private final Date date;

        public Bean1(@JSONField(locale = "zh", format = "yyyy-mm-dd")Date date) {
            this.date = date;
        }
    }

    @Test
    public void test2() {
        Bean2 bean = JSON.parseObject("{\"date\":\"1863792000000\"}", Bean2.class);
        assertEquals(1863792000000L, bean.date.getTime());
    }

    private static class Bean2 {
        @JSONField(locale = "zh", format = "yyyy-mm-dd")
        public Date date;
    }

    @Test
    public void test3() {
        Bean2 bean = JSON.parseObject("{\"date\":\"1863792000000\"}", Bean2.class);
        assertEquals(1863792000000L, bean.date.getTime());
    }

    public static class Bean3 {
        @JSONField(locale = "zh", format = "yyyy-mm-dd")
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    @Test
    public void test4() {
        Bean4 bean = JSON.parseObject("{\"date\":\"1863792000000\"}", Bean4.class);
        assertEquals(1863792000000L, bean.date.getTime());
    }

    private class Bean4 {
        @JSONField(locale = "zh", format = "yyyy-MM-dd")
        private Date date;

        public Bean4() {
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
