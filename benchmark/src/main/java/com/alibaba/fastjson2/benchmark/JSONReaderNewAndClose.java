package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSONWriter;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class JSONReaderNewAndClose {
    public static void fastjson2() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100_000_000; ++i) {
                fastjson2_0();
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("fastjson2 millis : " + millis);
            // zulu8.58.0.13 : 1234
            // zulu11.52.13 : 1123
            // zulu17.32.13 : 1073
        }
    }

    public static void fastjson2_0() {
        JSONWriter writer = JSONWriter.of();
        BH.consume(writer);
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        fastjson2_0();
        fastjson2();
    }
}
