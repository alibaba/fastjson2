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
    static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    static String INPUT = "2012-06-23 12:13:14";
    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
    static final FastDateFormat FAST_DATE_FORMAT = FastDateFormat.getInstance(PATTERN);

    static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_LOCAL = ThreadLocal.withInitial(
            () -> new SimpleDateFormat(PATTERN)
    );

    @Benchmark
    public void javaTimeDateTimeFormatter(Blackhole bh) throws Throwable {
        LocalDateTime ldt = LocalDateTime.parse(INPUT, FORMATTER);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        ZonedDateTime zdt = ldt.atZone(zoneId);
        Instant instant = zdt.toInstant();
        Date date = Date.from(instant);
        bh.consume(date);
    }

//    @Benchmark
    public void javaTimeDateTimeFormatter1(Blackhole bh) throws Throwable {
        LocalDateTime ldt = LocalDateTime.parse(INPUT, FORMATTER);
        ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
        long millis = DateUtils.millis(ldt, zoneId);
        Date date = new Date(millis);
        bh.consume(date);
    }

    @Benchmark
    public void parseDateSmart(Blackhole bh) throws Throwable {
        Date date = DateUtils.parseDate(INPUT);
        bh.consume(date);
    }

    @Benchmark
    public void parseDateYMDHMS19(Blackhole bh) throws Throwable {
        Date date = DateUtils.parseDateYMDHMS19(INPUT);
        bh.consume(date);
    }

    @Benchmark
    public void parseDate(Blackhole bh) throws Throwable {
        Date date = DateUtils.parseDate(INPUT, PATTERN);
        bh.consume(date);
    }

    @Benchmark
    public void simpleDateFormat(Blackhole bh) throws Throwable {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN);
        Date date = format.parse(INPUT);
        bh.consume(date);
    }

    @Benchmark
    public void simpleDateFormatThreadLocal(Blackhole bh) throws Throwable {
        SimpleDateFormat format = SIMPLE_DATE_FORMAT_LOCAL.get();
        Date date = format.parse(INPUT);
        bh.consume(date);
    }

    @Benchmark
    public void commonLangFastDateFormat(Blackhole bh) throws Throwable {
        Date date = FAST_DATE_FORMAT.parse(INPUT);
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
