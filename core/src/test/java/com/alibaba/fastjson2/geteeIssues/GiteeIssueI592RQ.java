package com.alibaba.fastjson2.geteeIssues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class GiteeIssueI592RQ {
    @Test
    public void test() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean.class).date);
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date date;
    }
}
