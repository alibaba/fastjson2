package com.alibaba.fastjson.issue_3600;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

public class Issue3637 {
    @Test
    public void test_for_issue() throws Exception {
//        java.sql.Time.valueOf("01:00:00");
        JSON.parseObject("\"01:00:00\"", java.sql.Time.class);
        TypeUtils.cast("01:00:00", java.sql.Time.class);
    }
}
