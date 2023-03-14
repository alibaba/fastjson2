package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1231 {
    @Test
    public void test() {
        String str = "{\"date\":\"1649636031000\"}";
        Bean bean = JSON.parseObject(str, Bean.class, "yyyy-MM-dd HH:mm:ss");
        assertEquals(1649636031000L, bean.date.getTime());
    }

    public static class Bean {
        public Date date;
    }
}
