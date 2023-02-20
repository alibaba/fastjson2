package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1125 {
    @Test
    public void test() {
        String str = "{\"date\":\"Sun Feb 19 18:46:07 GMT+08:00 2023\"}";
        Bean bean = JSON.parseObject(str, Bean.class);
        assertNotNull(bean);
        assertEquals(1676803567000L, bean.date.getTime());

        Bean bean1 = JSON.parseObject(str).toJavaObject(Bean.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
    }

    public static class Bean {
        public Date date;
    }
}
