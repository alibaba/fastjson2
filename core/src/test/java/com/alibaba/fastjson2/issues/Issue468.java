package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue468 {
    @Test
    public void test() {
        Bean bean = new Bean();
        bean.date = new Date(1655235983616L);

        assertEquals("{\"date\":\"2022-06-15\"}", JSON.toJSONString(bean));
        assertEquals("{\"date\":\"2022-06-15\"}", JSON.toJSONString(bean, "yyyy-MM-dd HH:mm:ss"));
        assertEquals("{\"date\":\"2022-06-15\"}", JSON.toJSONString(bean, "millis"));
        assertEquals("{\"date\":\"2022-06-15\"}", JSON.toJSONString(bean, "iso8601"));
    }

    public static class Bean {
        @JSONField(format = "yyyy-MM-dd")
        private Date date;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
}
