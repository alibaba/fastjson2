package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateParse19 {
    static final String pattern = "yyyy-MM-dd HH:mm:ss";
    static String input = "2012-06-23 12:13:14";
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    static final FastDateFormat FAST_DATE_FORMAT = FastDateFormat.getInstance(pattern);

    static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_LOCAL = ThreadLocal.withInitial(
            () -> new SimpleDateFormat(pattern)
    );

    @Benchmark
    public void javaTimeFormatter(Blackhole bh) throws Throwable {
        LocalDateTime ldt = LocalDateTime.parse(input, formatter);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        ZonedDateTime zdt = ldt.atZone(zoneId);
        Instant instant = zdt.toInstant();
        Date date = Date.from(instant);
        bh.consume(date);
    }

//    @Benchmark
    public void javaTimeDateTimeFormatter1(Blackhole bh) throws Throwable {
        LocalDateTime ldt = LocalDateTime.parse(input, formatter);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        long millis = DateUtils.millis(ldt, zoneId);
        Date date = new Date(millis);
        bh.consume(date);
    }

//    @Benchmark
    public void parseDateSmart(Blackhole bh) throws Throwable {
        Date date = DateUtils.parseDate(input);
        bh.consume(date);
    }

    @Benchmark
    public void parseDateYMDHMS19(Blackhole bh) throws Throwable {
        Date date = DateUtils.parseDateYMDHMS19(input);
        bh.consume(date);
    }

//    @Benchmark
    public void parseDate(Blackhole bh) throws Throwable {
        Date date = DateUtils.parseDate(input, pattern);
        bh.consume(date);
    }

    @Benchmark
    public void simpleDateFormat(Blackhole bh) throws Throwable {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date date = format.parse(input);
        bh.consume(date);
    }

    @Benchmark
    public void simpleDateFormatThreadLocal(Blackhole bh) throws Throwable {
        SimpleDateFormat format = SIMPLE_DATE_FORMAT_LOCAL.get();
        Date date = format.parse(input);
        bh.consume(date);
    }

    @Benchmark
    public void commonLangFastDateFormat(Blackhole bh) throws Throwable {
        Date date = FAST_DATE_FORMAT.parse(input);
        bh.consume(date);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DateParse19.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
