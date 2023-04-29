package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1412 {
    @Test
    public void test() {
        String s = "{\"date\":\"2023-03-24 11:10:11\"}";
        Bean bean = JSON.parseObject(s, Bean.class, JSONReader.Feature.SupportSmartMatch);
        assertNotNull(bean);
    }

    @Data
    public static class Bean {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date date;
    }
}
