package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class Issue3102 {
    @Test
    public void test() {
        long timestamp = 1728957746640L;
        String jsonString = "{\"createTime\":\"" + timestamp + "\"}";
        TimeDemo demo = JSON.parseObject(jsonString, TimeDemo.class);
        System.out.println(demo.getCreateTime());
    }

    @Data
    public static class TimeDemo {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
        private Date createTime;
    }
}
