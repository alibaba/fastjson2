package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayParseTreeUTF8BytesTest {
    static final EishayParseTreeUTF8Bytes benchmark = new EishayParseTreeUTF8Bytes();

    public static void fastjson2() {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.fastjson2(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseTreeUTF8Bytes-fastjson2 millis : " + millis);
            // zulu8.68.0.21 : 995 715 797
            // zulu11.52.13 : 900 836 642
            // zulu17.40.19 : 967 696 639
        }
    }

    public static void jackson() throws Exception {
        for (int j = 0; j < 10; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000 * 1000; ++i) {
                benchmark.jackson(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("EishayParseTreeUTF8Bytes-jackson millis : " + millis);
            // zulu8.68.0.21 : 1084 1078
            // zulu11.52.13 : 1132 1049
            // zulu17.32.13 : 1011 1123
        }
    }

    public static void main(String[] args) throws Exception {
        fastjson2();
//        jackson();
    }
}
