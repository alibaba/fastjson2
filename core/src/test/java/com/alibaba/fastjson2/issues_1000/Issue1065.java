package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1065 {
    @Test
    public void test() {
        String data = "{\"date\":\"2022-08-12\",\"createTime\":\"20220812214301538\"}";
        Bean bean = JSON.parseObject(data, Bean.class);

        String expected = "2022-08-12T21:43:01.538";
        assertEquals(expected, bean.createTime.toString());

        Bean bean1 = JSON.parseObject(data.getBytes(), Bean.class);
        assertEquals(expected, bean1.createTime.toString());

        Bean bean2 = JSON.parseObject(data.toCharArray(), Bean.class);
        assertEquals(expected, bean2.createTime.toString());

        byte[] bytes = JSONObject.parseObject(data).toJSONBBytes();
        Bean bean3 = JSONB.parseObject(bytes, Bean.class);
        assertEquals(expected, bean3.createTime.toString());
    }

    public static class Bean {
        private LocalDate date;
        private LocalDateTime createTime;

        @Override
        public String toString() {
            return "DateTime{" +
                    "date=" + date +
                    ", createTime=" + createTime +
                    '}';
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }
    }
}
