package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3210 {
    @Data
    public static class Bean {
        private Date time;
    }

    @Test
    public void test() {
        String s = "{\"time\":\"2024-12-04T20:43:15.000000999\"}";
        {
            Bean bean = JSONObject.parseObject(s, Bean.class);
            assertNotNull(bean.time);
        }
        {
            Bean bean = JSON.parseObject(s.getBytes(StandardCharsets.UTF_8), Bean.class);
            assertNotNull(bean.time);
        }
        {
            Bean bean = JSON.parseObject(s.toCharArray(), Bean.class);
            assertNotNull(bean.time);
        }
    }

    @Data
    private static class Bean1 {
        private Date time;
    }

    @Test
    public void test1() {
        String s = "{\"time\":\"2024-12-04T20:43:15.000000999\"}";
        {
            Bean1 bean = JSONObject.parseObject(s, Bean1.class);
            assertNotNull(bean.time);
        }
        {
            byte[] bytes = s.getBytes();
            Bean1 bean = JSON.parseObject(bytes, 0, bytes.length, Bean1.class);
            assertNotNull(bean.time);
        }
        {
            Bean1 bean = JSON.parseObject(s.toCharArray(), Bean1.class);
            assertNotNull(bean.time);
        }
    }

    public static class Bean2 {
        public final Date time;

        public Bean2(Date time) {
            this.time = time;
        }
    }

    @Test
    public void test2() {
        String s = "{\"time\":\"2024-12-04T20:43:15.000000999\"}";
        {
            Bean2 bean = JSONObject.parseObject(s, Bean2.class);
            assertNotNull(bean.time);
        }
        {
            byte[] bytes = s.getBytes();
            Bean2 bean = JSON.parseObject(bytes, 0, bytes.length, Bean2.class);
            assertNotNull(bean.time);
        }
        {
            Bean2 bean = JSON.parseObject(s.toCharArray(), Bean2.class);
            assertNotNull(bean.time);
        }
    }
}
