package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue740 {
    @Test
    public void test() {
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022-09-07T12:38:31\"}",
                        Bean.class
                ).date
        );
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022-09-07T12:38:31\"}".getBytes(StandardCharsets.UTF_8),
                        Bean.class
                ).date
        );
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022/09/07T12:38:31\"}",
                        Bean.class
                ).date
        );
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022/09/07T12:38:31\"}".getBytes(StandardCharsets.UTF_8),
                        Bean.class
                ).date
        );
    }

    @Test
    public void test1() {
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022-09-07T12:38:31.000+08:00\"}",
                        Bean.class,
                        JSONReader.Feature.SupportSmartMatch
                ).date
        );
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022-09-07T12:38:31.000+08:00\"}".getBytes(StandardCharsets.UTF_8),
                        Bean.class,
                        JSONReader.Feature.SupportSmartMatch
                ).date
        );
    }

    @Data
    public class Bean{
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date date;
    }

    @Test
    public void test2() {
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022-09-07T12:38:31.000+08:00\"}",
                        Bean2.class,
                        JSONReader.Feature.SupportSmartMatch
                ).date
        );
        assertNotNull(
                JSON.parseObject(
                        "{\"date\":\"2022-09-07T12:38:31.000+08:00\"}".getBytes(StandardCharsets.UTF_8),
                        Bean2.class,
                        JSONReader.Feature.SupportSmartMatch
                ).date
        );
    }

    @Data
    public static class Bean2{
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date date;
    }
}
