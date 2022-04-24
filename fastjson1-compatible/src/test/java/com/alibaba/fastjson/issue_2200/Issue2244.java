package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class Issue2244 {
    @Test
    public void test_for_issue() throws Exception {
        String str = "\"2019-01-14T06:32:09.029Z\"";
        JSON.parseObject(str, Date.class);
    }
}
