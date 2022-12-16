package com.alibaba.fastjson2.v1issues.issue_4200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4299 {
    @Test
    public void test() {
        String json = "{\"testDate\":\"2022-10-1\"}";
        DemoDto demoDto = JSON.parseObject(json, DemoDto.class);
        assertEquals("{\"testDate\":\"2022-10-01 00:00:00\"}", JSON.toJSONString(demoDto));
    }

    public class DemoDto
            implements java.io.Serializable {
        public Date testDate;
    }
}
