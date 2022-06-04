package com.alibaba.fastjson2.v1issues.issue_4100;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Time;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4193 {
    @Test
    public void test() {
        DemoBean demoBean = new DemoBean();
        demoBean.date = Date.valueOf("2022-06-01");
        demoBean.time = Time.valueOf("13:13:13");
        String str = JSON.toJSONString(demoBean);
        assertEquals("{\"date\":\"2022-06-01\",\"time\":\"13:13:13\"}", str);
    }

    @Data
    private static class DemoBean {
        private Date date;
        private Time time;
    }
}
