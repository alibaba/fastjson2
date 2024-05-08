package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2537 {
    @Test
    public void testMutated() {
        String s = "{\"dateTime\":\"2023-06-27T16:53:24.123Z\"}";
        BeanMutated bean = JSON.parseObject(s, BeanMutated.class);
        String s1 = JSON.toJSONString(bean);
        assertEquals("{\"dateTime\":\"2023-06-27T16:53:24.123Z\"}", s1);
    }

    @Test
    public void testMutatedfj() {
        String s = "{\"dateTime\":\"2023-06-27T16:53:24.123Z\"}";
        BeanMutated bean = com.alibaba.fastjson.JSON.parseObject(s, BeanMutated.class);
        String s1 = com.alibaba.fastjson.JSON.toJSONString(bean);
        assertEquals("{\"dateTime\":\"2023-06-27T16:53:24.123Z\"}", s1);
    }

    public static class BeanMutated {
        public DateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(DateTime dateTime) {
            this.dateTime = dateTime;
        }

        public DateTime dateTime;
    }
}
