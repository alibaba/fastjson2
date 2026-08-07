package com.alibaba.fastjson2.benchmark.wast;

import com.alibaba.fastjson2.annotation.JSONField;
import io.github.wycst.wast.json.annotations.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateBean2 {
    @JSONField(format = "yyyyMMddHHmmss")
    @JsonProperty(pattern = "yyyyMMddHHmmss")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyyMMddHHmmss", timezone = "GMT+8")
    public Date date = new Date();

    @JSONField(format = "yyyyMMddHHmmss")
    @JsonProperty(pattern = "yyyyMMddHHmmss")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyyMMddHHmmss", timezone = "GMT+8")
    public LocalDateTime localDateTime = LocalDateTime.now();

    @JSONField(format = "yyyyMMddHHmmss")
    @JsonProperty(pattern = "yyyyMMddHHmmss")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyyMMddHHmmss", timezone = "GMT+8")
    public ZonedDateTime zonedDateTime = ZonedDateTime.now();

    @JSONField(format = "yyyyMMdd")
    @JsonProperty(pattern = "yyyyMMdd")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyyMMdd", timezone = "GMT+8")
    public LocalDate localDate = LocalDate.now();

    @JSONField(format = "yyyyMMdd")
    @JsonProperty(pattern = "yyyyMMdd")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyyMMdd", timezone = "GMT+8")
    public Instant instant = Instant.now();
}
