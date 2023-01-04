package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue550 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"date\":\"\"}", Bean.class);
        assertNull(bean.date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean.class).date);
    }

    public static class Bean {
        public Date date;
    }

    @Test
    public void test1() {
        Bean1 bean = JSON.parseObject("{\"date\":\"\"}", Bean1.class);
        assertNull(bean.date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").to(Bean1.class).date);
    }

    private static class Bean1 {
        public Date date;
    }
}
