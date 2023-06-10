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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.time.ZoneId.DEFAULT_ZONE_ID;

public class DateFormat19 {
    static final String pattern = "yyyy-MM-dd HH:mm:ss";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    static Date date = new Date(1340424794000L);
    static FastDateFormat fastDateFormat = FastDateFormat.getInstance(pattern);
    static String str = new SimpleDateFormat(pattern).format(date);

    static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_LOCAL = ThreadLocal.withInitial(
            () -> new SimpleDateFormat(pattern)
    );

//    @Benchmark
    public void commonsFastFormat(Blackhole bh) throws Throwable {
        String str = fastDateFormat.format(date);
        bh.consume(str);
    }

//    @Benchmark
    public void simpleDateFormat(Blackhole bh) throws Throwable {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        String str = format.format(date);
        bh.consume(str);
    }

//    @Benchmark
    public void simpleDateFormatThreadLocal(Blackhole bh) throws Throwable {
        SimpleDateFormat format = SIMPLE_DATE_FORMAT_LOCAL.get();
        String str = format.format(date);
        bh.consume(str);
    }

//    @Benchmark
    public void fastjsonFormat(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.format(date, pattern));
    }

//    @Benchmark
    public void formatYMDHMS19(Blackhole bh) throws Throwable {
        bh.consume(DateUtils.formatYMDHMS19(date, DEFAULT_ZONE_ID));
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
                .include(DateFormat19.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }
}
