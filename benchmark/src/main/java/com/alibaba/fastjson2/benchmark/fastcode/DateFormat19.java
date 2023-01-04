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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateFormat19 {
    static final String pattern = "yyyy-MM-dd HH:mm:ss";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    static Date date = new Date(1340424794000L);
    static FastDateFormat fastDateFormat = FastDateFormat.getInstance(pattern);

    static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_LOCAL = ThreadLocal.withInitial(
            () -> new SimpleDateFormat(pattern)
    );

    @Benchmark
    public void javaTimeFormatter(Blackhole bh) throws Throwable {
        ZoneId zonedId = DateUtils.DEFAULT_ZONE_ID;
        Instant instant = date.toInstant();
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, zonedId);
        String str = formatter.format(ldt);
        bh.consume(str);
    }

    @Benchmark
    public void commonsFastFormat(Blackhole bh) throws Throwable {
        String str = fastDateFormat.format(date);
        bh.consume(str);
    }

    @Benchmark
    public void simpleDateFormat(Blackhole bh) throws Throwable {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        String str = format.format(date);
        bh.consume(str);
    }

    @Benchmark
    public void simpleDateFormatThreadLocal(Blackhole bh) throws Throwable {
        SimpleDateFormat format = SIMPLE_DATE_FORMAT_LOCAL.get();
        String str = format.format(date);
        bh.consume(str);
    }

//    @Benchmark
    public void fastjsonFormat(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.format(date, pattern));
    }

    @Benchmark
    public void formatYMDHMS19(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.formatYMDHMS19(date));
    }

//    @Benchmark
    public void fastjsonFormat2(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.format(date.getTime()));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DateFormat19.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
