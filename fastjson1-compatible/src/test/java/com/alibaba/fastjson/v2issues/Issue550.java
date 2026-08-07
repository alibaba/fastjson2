package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue550 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject("{\"date\":\"\"}", Bean.class);
        assertNull(bean.date);
        assertNull(JSON.parseObject("{\"date\":\"\"}").toJavaObject(Bean.class).date);
    }

    public static class Bean {
        public Date date;
    }
}
