package com.alibaba.fastjson.issue_3200;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3282 {
    @Test
    public void test_for_issue() {
        Demo demo = JSON.parseObject("{'date':'2020-01-01 00:00:00 000'}", Demo.class);
        assertNotNull(demo.date);
    }

    public static class Demo {
        public java.util.Date date;
    }
}
