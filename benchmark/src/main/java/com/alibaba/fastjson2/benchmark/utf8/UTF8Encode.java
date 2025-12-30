package com.alibaba.fastjson2.benchmark.utf8;

import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class UTF8Encode {
    static final CacheItem[] CACHE_ITEMS;

    static {
        final CacheItem[] items = new CacheItem[16];
        for (int i = 0; i < items.length; i++) {
            items[i] = new CacheItem();
        }
        CACHE_ITEMS = items;
    }

    static final int CACHE_THRESHOLD = 1024 * 1024 * 4;
    static final AtomicReferenceFieldUpdater<CacheItem, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, char[].class, "chars");
    static final AtomicReferenceFieldUpdater<CacheItem, byte[]> BYTES_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(CacheItem.class, byte[].class, "bytes");

    static final class CacheItem {
        volatile char[] chars;
        volatile byte[] bytes;
    }

    static final String STR_CN_SMALL = "規定了四種表";
    static final String STR0 = "EEE 754規定了四種表示浮點數值的方式：單精確度（32位元）、雙精確度（64位元）、延伸單精確度（43位元以上，很少使用）與延伸雙精確度（79位元以上，通常以80位元實做）。只有32位元模式有強制要求，其他都是選擇性的。大部分程式語言都提供了IEEE浮点数格式與算術，但有些將其列為非必需的。例如，IEEE 754問世之前就有的C語言，現在包括了IEEE算術，但不算作強制要求（C語言的float通常是指IEEE單精確度，而double是指雙精確度）。";
    static final String STR1 = "ISO/IEC 8859-1:1998, Information technology — 8-bit single-byte coded graphic character sets — Part 1: Latin alphabet No. 1, is part of the ISO/IEC 8859 series of ASCII-based standard character encodings, first edition published in 1987. ISO/IEC 8859-1 encodes what it refers to as \"Latin alphabet no. 1\", consisting of 191 characters from the Latin script. This character-encoding scheme is used throughout the Americas, Western Europe, Oceania, and much of Africa. It is the basis for some popular 8-bit character sets and the first two blocks of characters in Unicode.";

    public void jdk(Blackhole bh) {
        bh.consume(
                STR0.getBytes(StandardCharsets.UTF_8)
        );
    }

    public void jdk_small(Blackhole bh) {
        bh.consume(
                STR_CN_SMALL.getBytes(StandardCharsets.UTF_8)
        );
    }

    public void fj(Blackhole bh) {
        bh.consume(
                encode(STR0)
        );
    }

    public void fj_small(Blackhole bh) {
        bh.consume(
                encode(STR_CN_SMALL)
        );
    }

    public void jdk_ascii(Blackhole bh) {
        bh.consume(
                STR1.getBytes(StandardCharsets.UTF_8)
        );
    }

    public void fj_ascii(Blackhole bh) {
        byte[] utf8 = encode(STR1);
        bh.consume(utf8);
    }

    private static byte[] jdk8(String str) {
        char[] chars = JDKUtils.getCharArray(str);

        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);

        int bufferSize = chars.length * 3;
        if (bytes == null || bytes.length < bufferSize) {
            bytes = new byte[bufferSize];
        }

        int cnt = IOUtils.encodeUTF8(chars, 0, chars.length, bytes, 0);
        byte[] utf8 = Arrays.copyOf(bytes, cnt);
        BYTES_UPDATER.lazySet(cacheItem, bytes);
        return utf8;
    }

    private static byte[] encode(String str) {
        if (JDKUtils.JVM_VERSION <= 8 || JDKUtils.STRING_CODER == null) {
            return jdk8(str);
        }
        int coder = JDKUtils.STRING_CODER.applyAsInt(str);
        byte[] value = JDKUtils.STRING_VALUE.apply(str);

        if (coder == 0) {
            boolean hasNegative;
            try {
                hasNegative = (Boolean) JDKUtils.METHOD_HANDLE_HAS_NEGATIVE.invoke(value, 0, value.length);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            if (!hasNegative) {
                return value;
            }
            return str.getBytes(StandardCharsets.UTF_8);
        }

        int cacheIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1);
        CacheItem cacheItem = CACHE_ITEMS[cacheIndex];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);

        int bufferSize = str.length() * 3;
        if (bytes == null || bytes.length < bufferSize) {
            bytes = new byte[bufferSize];
        }

        int cnt = IOUtils.encodeUTF8(value, 0, value.length >> 1, bytes, 0);
        byte[] utf8 = Arrays.copyOf(bytes, cnt);
        BYTES_UPDATER.lazySet(cacheItem, bytes);
        return utf8;
    }
}
