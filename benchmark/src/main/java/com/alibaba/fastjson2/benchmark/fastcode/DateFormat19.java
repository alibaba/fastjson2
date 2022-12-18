package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateFormat19 {
    static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(PATTERN);
    static Date DATE = new Date(1340424794000L);

    @Benchmark
    public void javaTimeDateFormatter(Blackhole bh) throws Throwable {
        LocalDateTime ldt = DATE.toInstant().atZone(IOUtils.DEFAULT_ZONE_ID).toLocalDateTime();
        String str = FORMATTER.format(ldt);
        bh.consume(str);
    }

    @Benchmark
    public void fastjsonFormat(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.format(DATE, PATTERN));
    }

    @Benchmark
    public void fastjsonFormat2(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.format(DATE.getTime()));
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
