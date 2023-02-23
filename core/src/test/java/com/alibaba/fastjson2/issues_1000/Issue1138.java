package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1138 {
    @Test
    public void test() {
        Bean bean = JSON.parseObject(
                "{\"date\":\"2023-02-20 16:03:25\"}",
                Bean.class,
                JSONReader.Feature.SupportSmartMatch
        );
        assertNotNull(bean.date);
    }

    @Data
    public static class Bean {
        @JSONField(format = "HH:mm:ss")
        private Date date;
    }
}
