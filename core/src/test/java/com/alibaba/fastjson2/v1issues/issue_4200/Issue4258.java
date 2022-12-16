package com.alibaba.fastjson2.v1issues.issue_4200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue4258 {
    @Test
    public void test() {
        String str = "[{\"name\":\"<font color=\\\"#F93A49\\\">+4.28%\",\"desc\":\"近一年涨跌幅\"},{\"name\":\"<font color=\\\"#F93A49\\\">+0.01%\",\"desc\":\"日涨跌幅\"},{\"name\":\"1.0526\",\"desc\":\"净值08-08\"}]";
        JSONArray growthList = JSON.parseArray(str);
        assertNotNull(growthList);
    }
}
