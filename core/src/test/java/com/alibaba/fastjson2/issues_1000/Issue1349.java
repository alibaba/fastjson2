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
}
