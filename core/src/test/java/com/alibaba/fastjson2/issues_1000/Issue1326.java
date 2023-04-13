package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1326 {
    public static class DataDemo {
        private Date createTime;

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }
    }

    @Test
    public void test() {
        String json2 = "{\"createTime\":\"20230406124811\"}";
        DataDemo data = JSON.parseObject(json2, DataDemo.class);
        assertEquals("{\"createTime\":\"2023-04-06 12:48:11\"}", JSON.toJSONString(data));
    }

    @Test
    public void test1() {
        String json2 = "{\"createTime\":\"202304061248\"}";
        DataDemo data = JSON.parseObject(json2, DataDemo.class);
        assertEquals("{\"createTime\":\"2023-04-06 12:48:00\"}", JSON.toJSONString(data));
    }
}
