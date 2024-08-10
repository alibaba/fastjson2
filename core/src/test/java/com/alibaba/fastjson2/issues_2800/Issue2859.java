package com.alibaba.fastjson2.issues_2800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2859 {
    @Test
    public void test() {
        String str = "{\"time\":\"-999999999-11-12 13:14:15\"}";
        {
            Bean bean = JSON.parseObject(str, Bean.class);
            assertEquals(str, JSON.toJSONString(bean));
        }
        {
            Bean bean = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean.class);
            assertEquals(str, JSON.toJSONString(bean));
        }
        {
            Bean bean = JSON.parseObject(str.toCharArray(), Bean.class);
            assertEquals(str, JSON.toJSONString(bean));
        }
    }

    static class Bean {
        public LocalDateTime time;
    }

    @Test
    public void test1() {
        String str = "{\"date\":\"-999999999-11-12\"}";
        {
            Bean1 bean = JSON.parseObject(str, Bean1.class);
            assertEquals(str, JSON.toJSONString(bean));
        }
        {
            Bean1 bean = JSON.parseObject(str.getBytes(StandardCharsets.UTF_8), Bean1.class);
            assertEquals(str, JSON.toJSONString(bean));
        }
        {
            Bean1 bean = JSON.parseObject(str.toCharArray(), Bean1.class);
            assertEquals(str, JSON.toJSONString(bean));
        }
    }

    static class Bean1 {
        public LocalDate date;
    }
}
