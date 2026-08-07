package com.alibaba.fastjson2.benchmark.eishay;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class EishayWriteBinaryTest {
    static final EishayWriteBinary benchmark = new EishayWriteBinary();
    static final int LOOP = 1_000_000;

    public static void jsonb() throws Exception {
        System.out.println("jsonb size " + benchmark.jsonbSize()); // 362

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.jsonb(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("jsonb millis : " + millis);
            // zulu8.60.0.21 : 226
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void msgpack() throws Exception {
        System.out.println("msgpack size " + benchmark.msgpackSize()); // 390

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.msgpack(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("msgpack millis : " + millis);
            // zulu8.60.0.21 : 1978
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void protobuf() throws Exception {
        System.out.println("protobuf size " + benchmark.protobufSize()); // 235

        for (int j = 0; j < 5; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < LOOP; ++i) {
                benchmark.protobuf(BH);
            }
            long millis = System.currentTimeMillis() - start;
            System.out.println("msgpack millis : " + millis);
            // zulu8.60.0.21 : 481
            // zulu11.52.13 :
            // zulu17.32.13 :
        }
    }

    public static void main(String[] args) throws Exception {
//        jsonb();
//        msgpack();
        protobuf();
    }
}
