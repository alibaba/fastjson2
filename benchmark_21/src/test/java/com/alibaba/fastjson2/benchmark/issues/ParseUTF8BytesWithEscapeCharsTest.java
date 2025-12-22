package com.alibaba.fastjson2.benchmark.issues;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class ParseUTF8BytesWithEscapeCharsTest {
    private static ParseUTF8BytesWithEscapeChars benchmark = new ParseUTF8BytesWithEscapeChars();

    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ParseUTF8BytesWithEscapeChars-fastjson2 : " + millis);
            // 4291 3479
        }
    }

    public static void fastjson2_str() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100000; ++i) {
                benchmark.fastjson2_str(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ParseUTF8BytesWithEscapeChars-fastjson2_str : " + millis);
            // 4350 3537 1552
        }
    }

    public static void jackson() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100000; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("ParseUTF8BytesWithEscapeChars-jackson : " + millis);
            // 2355
        }
    }

    public static void main(String[] args) throws Exception {
//        fastjson2();
        fastjson2_str();
//        jackson();
    }
}
