package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.TypeUtils;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1146 {
    @Test
    public void test() {
        String str2 = "[{\"ts\":1677459527000,\"name\":\"商品\"}]";

        JSONArray jsonArray = JSON.parseArray(str2);
        List<Product> products = jsonArray.toJavaList(Product.class);
        assertEquals(1677459527000L, products.get(0).ts.getTime());
    }

    @Test
    public void test1() {
        assertEquals(1L, TypeUtils.cast(1, java.sql.Date.class).getTime());
        assertEquals(1L, TypeUtils.cast(1L, java.sql.Date.class).getTime());

        assertEquals(1L, TypeUtils.cast(1, java.sql.Timestamp.class).getTime());
        assertEquals(1L, TypeUtils.cast(1L, java.sql.Timestamp.class).getTime());
    }

    @Test
    public void test2() {
        long millis = 1677459527000L;
        JSONObject object = JSONObject.of("date", millis, "time", millis, "timestamp", millis);
        String str = object.toString();

        Bean2 bean = JSON.parseObject(str, Bean2.class);
        assertEquals(millis, bean.date.getTime());
        assertEquals(millis, bean.timestamp.getTime());
        assertEquals(millis, bean.time.getTime());

        Bean2 bean1 = object.toJavaObject(Bean2.class);
        assertEquals(millis, bean1.date.getTime());
        assertEquals(millis, bean1.timestamp.getTime());
        assertEquals(millis, bean1.time.getTime());
    }

    static class Bean2 {
        public java.sql.Date date;
        public java.sql.Timestamp timestamp;
        public java.sql.Time time;
    }

    static class Product {
        public String name;
        public Timestamp ts;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Timestamp getTs() {
            return ts;
        }

        public void setTs(Timestamp ts) {
            this.ts = ts;
        }
    }
}
