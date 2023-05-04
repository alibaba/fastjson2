package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
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
        String expected = "{\"createTime\":\"2023-04-06 12:48:11\"}";
        {
            DataDemo data = JSON.parseObject(json2, DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
        {
            DataDemo data = JSON.parseObject(json2.getBytes(), DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
        {
            DataDemo data = JSON.parseObject(json2.toCharArray(), DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
        {
            byte[] bytes = JSONObject.parseObject(json2).toJSONBBytes();
            DataDemo data = JSONB.parseObject(bytes, DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
    }

    @Test
    public void test1() {
        String json2 = "{\"createTime\":\"202304061248\"}";
        String expected = "{\"createTime\":\"2023-04-06 12:48:00\"}";
        {
            DataDemo data = JSON.parseObject(json2, DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
        {
            DataDemo data = JSON.parseObject(json2.getBytes(), DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
        {
            DataDemo data = JSON.parseObject(json2.toCharArray(), DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
        {
            byte[] bytes = JSONObject.parseObject(json2).toJSONBBytes();
            DataDemo data = JSONB.parseObject(bytes, DataDemo.class);
            assertEquals(expected, JSON.toJSONString(data));
        }
    }
}
