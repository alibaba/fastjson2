package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson2.JSONB;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2332 {
    LocalDateTime now = LocalDateTime.of(2018, 7, 14, 12, 13, 14, 56789);
    LocalDateTime nowT = now.truncatedTo(ChronoUnit.SECONDS);

    @Test
    public void test1() {
        Bean1 entity = new Bean1();
        entity.setNow(Timestamp.valueOf(now));

        byte[] jsonbBytes = JSONB.toBytes(entity);
        Bean1 bean1 = JSONB.parseObject(jsonbBytes, Bean1.class);
        assertEquals(Timestamp.valueOf(nowT), bean1.now);
    }

    @Test
    public void test2() {
        Bean2 entity = new Bean2();
        entity.setNow(now);

        byte[] jsonbBytes = JSONB.toBytes(entity);
        Bean2 bean1 = JSONB.parseObject(jsonbBytes, Bean2.class);
        assertEquals(nowT, bean1.now);
    }

    @Data
    public static class Bean1 {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Timestamp now;
    }

    @Data
    public static class Bean2 {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime now;
    }

    @Test
    public void test3() {
        Bean3 entity = new Bean3();
        entity.setNow(LocalDate.now());

        byte[] jsonbBytes = JSONB.toBytes(entity);
        Bean3 bean1 = JSONB.parseObject(jsonbBytes, Bean3.class);
        assertEquals(entity.now, bean1.now);
    }

    @Data
    public static class Bean3 {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate now;
    }

    @Test
    public void test4() {
        Bean4 entity = new Bean4();
        entity.setNow(LocalTime.now());

        byte[] jsonbBytes = JSONB.toBytes(entity);
        Bean4 bean1 = JSONB.parseObject(jsonbBytes, Bean4.class);
        assertEquals(entity.now, bean1.now);
    }

    @Data
    public static class Bean4 {
        @JsonFormat(pattern = "HH:mm:ss")
        private LocalTime now;
    }

    @Test
    public void test5() {
        Bean5 entity = new Bean5();
        entity.setNow(LocalTime.now());

        byte[] jsonbBytes = JSONB.toBytes(entity);
        Bean5 bean1 = JSONB.parseObject(jsonbBytes, Bean5.class);
        assertEquals(entity.now, bean1.now);
    }

    @Data
    public static class Bean5 {
        @JsonFormat(pattern = "HHmmss")
        private LocalTime now;
    }

    @Test
    public void test6() {
        Bean6 entity = new Bean6();
        entity.setNow(now);

        byte[] jsonbBytes = JSONB.toBytes(entity);
        Bean6 bean1 = JSONB.parseObject(jsonbBytes, Bean6.class);
        assertEquals(nowT, bean1.now);
    }

    @Data
    public static class Bean6 {
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime now;
    }
}
