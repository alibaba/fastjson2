package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.DateUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateFormat10 {
    static final String pattern = "yyyy-MM-dd";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    static Date date = new Date(1673323068000L);
    static final String str = "2023-01-10";

//    @Benchmark
    public void javaTimeDateFormatter(Blackhole bh) throws Throwable {
        LocalDateTime ldt = date.toInstant().atZone(DateUtils.DEFAULT_ZONE_ID).toLocalDateTime();
        String str = formatter.format(ldt);
        bh.consume(str);
    }

//    @Benchmark
    public void fastjson_format(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.format(date, pattern));
    }

//    @Benchmark
    public void fastjson_formatYMD10(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.formatYMD10(date.getTime(), DateUtils.DEFAULT_ZONE_ID));
    }

    @Benchmark
    public void simpleFormat(Blackhole bh) throws Throwable {
        bh.consume(new SimpleDateFormat(pattern).format(date));
    }

    @Benchmark
    public void simpleFormatX(Blackhole bh) throws Throwable {
        bh.consume(new SimpleDateFormatX(pattern).format(date));
    }

    @Benchmark
    public void simpleParse(Blackhole bh) throws Throwable {
        bh.consume(new SimpleDateFormat(pattern).parse(str));
    }

    @Benchmark
    public void simpleParseX(Blackhole bh) throws Throwable {
        bh.consume(new SimpleDateFormatX(pattern).parse(str));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DateFormat10.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
