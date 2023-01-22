package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1065 {
    @Test
    public void test() {
        String data = "{\"date\":\"2022-08-12\",\"createTime\":\"20220812214301538\"}";
        Bean bean = JSON.parseObject(data, Bean.class);
        assertEquals("2022-08-12T21:43:01.538", bean.createTime.toString());

        Bean bean1 = JSON.parseObject(data.getBytes(), Bean.class);
        assertEquals("2022-08-12T21:43:01.538", bean1.createTime.toString());

        Bean bean2 = JSON.parseObject(data.toCharArray(), Bean.class);
        assertEquals("2022-08-12T21:43:01.538", bean2.createTime.toString());
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
