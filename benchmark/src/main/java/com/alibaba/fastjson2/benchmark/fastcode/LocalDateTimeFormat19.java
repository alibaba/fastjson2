package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson2.util.IOUtils.PACKED_DIGITS;
import static com.alibaba.fastjson2.util.JDKUtils.*;

public class LocalDateTimeFormat19 {
    static final String pattern = "yyyy-MM-dd HH:mm:ss";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    static LocalDateTime ldt = LocalDateTime.of(2023, 8, 16, 20, 21, 15);

    @Benchmark
    public void javaTimeFormatter(Blackhole bh) throws Throwable {
        String str = formatter.format(ldt);
        bh.consume(str);
    }

    @Benchmark
    public void formatYMDHMS19(Blackhole bh) throws Throwable {
        bh.consume(formatYMDHMS19(ldt));
    }

    @Benchmark
    public void formatYMDHMS19_o(Blackhole bh) throws Throwable {
        bh.consume(formatYMDHMS19_o(ldt));
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(LocalDateTimeFormat19.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(1)
                .forks(1)
                .build();
        new Runner(options).run();
    }

    public static String formatYMDHMS19(LocalDateTime ldt) {
        int year = ldt.getYear();
        int month = ldt.getMonthValue();
        int dayOfMonth = ldt.getDayOfMonth();
        int hour = ldt.getHour();
        int minute = ldt.getMinute();
        int second = ldt.getSecond();

        byte[] bytes = new byte[19];
        int y01 = year / 100;
        int y23 = year - y01 * 100;
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET, PACKED_DIGITS[y01]);
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + 2, PACKED_DIGITS[y23]);
        bytes[4] = '-';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + 5, PACKED_DIGITS[month]);
        bytes[7] = '-';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + 8, PACKED_DIGITS[dayOfMonth]);
        bytes[10] = ' ';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + 11, PACKED_DIGITS[hour]);
        bytes[13] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + 14, PACKED_DIGITS[minute]);
        bytes[16] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + 17, PACKED_DIGITS[second]);
        return STRING_CREATOR_JDK11.apply(bytes, LATIN1);
    }

    static String formatYMDHMS19_o(LocalDateTime ldt) {
        int year = ldt.getYear();
        int month = ldt.getMonthValue();
        int dayOfMonth = ldt.getDayOfMonth();
        int hour = ldt.getHour();
        int minute = ldt.getMinute();
        int second = ldt.getSecond();

        int y0 = year / 1000 + '0';
        int y1 = (year / 100) % 10 + '0';
        int y2 = (year / 10) % 10 + '0';
        int y3 = year % 10 + '0';
        int m0 = month / 10 + '0';
        int m1 = month % 10 + '0';
        int d0 = dayOfMonth / 10 + '0';
        int d1 = dayOfMonth % 10 + '0';
        int h0 = hour / 10 + '0';
        int h1 = hour % 10 + '0';
        int i0 = minute / 10 + '0';
        int i1 = minute % 10 + '0';
        int s0 = second / 10 + '0';
        int s1 = second % 10 + '0';

        byte[] bytes = new byte[19];
        bytes[0] = (byte) y0;
        bytes[1] = (byte) y1;
        bytes[2] = (byte) y2;
        bytes[3] = (byte) y3;
        bytes[4] = '-';
        bytes[5] = (byte) m0;
        bytes[6] = (byte) m1;
        bytes[7] = '-';
        bytes[8] = (byte) d0;
        bytes[9] = (byte) d1;
        bytes[10] = ' ';
        bytes[11] = (byte) h0;
        bytes[12] = (byte) h1;
        bytes[13] = ':';
        bytes[14] = (byte) i0;
        bytes[15] = (byte) i1;
        bytes[16] = ':';
        bytes[17] = (byte) s0;
        bytes[18] = (byte) s1;

        return JDKUtils.STRING_CREATOR_JDK11.apply(bytes, JDKUtils.LATIN1);
    }
}
