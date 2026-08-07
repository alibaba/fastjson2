package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.DateUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateParseMultiFormat {
    static String[] inputs = {
            "2012-06-23 12:13:14",
            "2012-06-23T12:13:14",
            "23/06/2012 12:13:14",
    };
    static final DateTimeFormatter formatter =
            new DateTimeFormatterBuilder()
                    .appendPattern("[yyyy-MM-dd HH:mm:ss]")
                    .appendPattern("[yyyy-MM-dd'T'HH:mm:ss]")
                    .appendPattern("[yyyy/MM/dd HH:mm:ss]")
                    .appendPattern("[dd/MM/yyyy HH:mm:ss]")
                    .appendPattern("[dd MMM yyyy HH:mm:ss]")
                    .toFormatter();

    @Benchmark
    public void javaTimeFormatter(Blackhole bh) throws Throwable {
        Date[] dates = new Date[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            LocalDateTime ldt = LocalDateTime.parse(input, formatter);
            ZoneId zoneId = DateUtils.DEFAULT_ZONE_ID;
            ZonedDateTime zdt = ldt.atZone(zoneId);
            Instant instant = zdt.toInstant();
            dates[i] = Date.from(instant);
        }
        bh.consume(dates);
    }

    @Benchmark
    public void parseDate(Blackhole bh) throws Throwable {
        Date[] dates = new Date[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            dates[i] = DateUtils.parseDate(input);
        }
        bh.consume(dates);
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DateParseMultiFormat.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
