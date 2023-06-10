package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.time.Instant;
import com.alibaba.fastjson2.time.LocalDate;
import com.alibaba.fastjson2.time.LocalDateTime;
import com.alibaba.fastjson2.time.ZoneId;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue445 {
    Instant current = LocalDateTime.of(2022, 6, 9, 9, 19, 13, 17).toInstant(ZoneId.DEFAULT_ZONE_ID);
    Instant yesterday = LocalDate.of(1990, 12, 11).atStartOfDay().toInstant(ZoneId.DEFAULT_ZONE_ID);

    @Test
    public void test0() {
        Bean bean = new Bean();
        bean.setCurrent(current.toTimestamp());
        bean.history = new Date(1654737514867L);
        bean.yesterday = new Timestamp(yesterday.toEpochMilli());
        bean.tomorrow = new java.sql.Date(1654737514867L);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);
        String str1 = JSON.toJSONString(bean);
        assertEquals("{\"current\":\"2022-06-09 09:19:13.000000017\",\"history\":\"2022-06-09 09\",\"tomorrow\":\"2022-06-09 09:18\",\"yesterday\":\"1990-12-11\"}", str);
        assertEquals("{\"current\":\"2022-06-09 09:19:13.000000017\",\"history\":\"2022-06-09 09\",\"tomorrow\":\"2022-06-09 09:18\",\"yesterday\":\"1990-12-11\"}", str1);
    }

    @Test
    public void test1() {
        Bean1 bean = new Bean1();
        bean.setCurrent(current.toTimestamp());
        bean.history = new Date(1654737514867L);
        bean.yesterday = new Timestamp(yesterday.toEpochMilli());
        bean.tomorrow = new java.sql.Date(1654737514867L);
        String str = JSON.toJSONString(bean, JSONWriter.Feature.FieldBased);
        String str1 = JSON.toJSONString(bean);
        assertEquals("{\"current\":\"2022-06-09 09:19:13.000000017\",\"history\":\"2022-06-09 09\",\"tomorrow\":\"2022-06-09 09:18\",\"yesterday\":\"1990-12-11\"}", str);
        assertEquals("{\"current\":\"2022-06-09 09:19:13.000000017\",\"history\":\"2022-06-09 09\",\"tomorrow\":\"2022-06-09 09:18\",\"yesterday\":\"1990-12-11\"}", str1);
    }

    @Data
    public static class Bean {
        private Timestamp current;
        @JSONField(format = "yyyy-MM-dd HH")
        private Date history;
        @JSONField(format = "yyyy-MM-dd")
        private Timestamp yesterday;
        @JSONField(format = "yyyy-MM-dd HH:mm")
        private java.sql.Date tomorrow;
    }

    public static class Bean1 {
        private Timestamp current;
        @JSONField(format = "yyyy-MM-dd HH")
        private Date history;
        @JSONField(format = "yyyy-MM-dd")
        private Timestamp yesterday;
        @JSONField(format = "yyyy-MM-dd HH:mm")
        private java.sql.Date tomorrow;

        public Timestamp getCurrent() {
            return current;
        }

        public void setCurrent(Timestamp current) {
            this.current = current;
        }

        public Date getHistory() {
            return history;
        }

        public void setHistory(Date history) {
            this.history = history;
        }

        public Timestamp getYesterday() {
            return yesterday;
        }

        public void setYesterday(Timestamp yesterday) {
            this.yesterday = yesterday;
        }

        public java.sql.Date getTomorrow() {
            return tomorrow;
        }

        public void setTomorrow(java.sql.Date tomorrow) {
            this.tomorrow = tomorrow;
        }
    }
}
