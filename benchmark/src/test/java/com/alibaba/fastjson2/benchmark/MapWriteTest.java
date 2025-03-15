package com.alibaba.fastjson2.benchmark;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class MapWriteTest {
    static final int LOOP = 1_000_000;
    static final MapWrite benchmark = new MapWrite();

    public static void writeMap() {
        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.writeMap(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("writeMap millis : " + millis);
        }
    }

    public static void main(String[] args) throws Exception {
        writeMap();
    }
}
