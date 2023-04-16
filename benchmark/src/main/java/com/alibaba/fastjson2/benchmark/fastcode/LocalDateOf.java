package com.alibaba.fastjson2.benchmark.fastcode;

import org.openjdk.jmh.infra.Blackhole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LocalDateOf {
    public void ofDate(Blackhole bh) {
        bh.consume(LocalDate.of(2014, 7, 15));
    }

    public void ofTime(Blackhole bh) {
        bh.consume(LocalTime.of(12, 13, 14));
    }

    public void ofDateTime(Blackhole bh) {
        bh.consume(LocalDateTime.of(2014, 7, 15, 12, 13, 14));
    }
}
