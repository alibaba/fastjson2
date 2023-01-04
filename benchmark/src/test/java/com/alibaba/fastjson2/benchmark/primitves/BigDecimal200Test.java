package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Random;

import static com.alibaba.fastjson2.benchmark.JMH.BH;

public class BigDecimal200Test {
    public static void fastjson2_perf_test() {
        for (int i = 0; i < 10; i++) {
            fastjson2_perf();
        }
    }

    public static void fastjson2_perf() {
        Date20 benchmark = new Date20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("Date20-fastjson2 : " + millis);

        // zulu8.62.0.19 : 1182 1178 1160
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu18.28.13 :
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void fastjson1_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastjson1_perf();
        }
    }

    public static void fastjson2_millis_perf() {
        Date20 benchmark = new Date20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson2_millis(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("Date20-fastjson2_millis : " + millis);

        // zulu8.62.0.19 : 610
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu18.28.13 :
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void fastjson2_millis_perf_test() throws Exception {
        for (int i = 0; i < 10; i++) {
            fastjson2_millis_perf();
        }
    }

    public static void fastjson1_perf() throws Exception {
        Date20 benchmark = new Date20();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000 * 1000; ++i) {
            benchmark.fastjson1(BH);
        }
        long millis = System.currentTimeMillis() - start;
        System.out.println("Date20-fastjson1 : " + millis);
        // zulu8.62.0.19 : 5874
        // zulu11.52.13 :
        // zulu17.32.13 :
        // zulu18.28.13 :
        // zulu19.0.47 :
        // corretto-8 :
        // corretto-11 :
        // corretto-17 :
        // corretto-18 :
        // oracle-jdk-17.0.4 :
        // oracle-jdk-18.0.2 :
    }

    public static void main(String[] args) throws Exception {
//        fastjson2_perf_test();
//        fastjson2_millis_perf_test();
//        fastjson1_perf_test();
        JSONObject array = new JSONObject();

        NumberFormat fmt = NumberFormat.getNumberInstance();
        fmt.setMinimumIntegerDigits(4);
        fmt.setGroupingUsed(false);

        Random random = new Random();
        for (int i = 0; i < 20; ++i) {
            int unscaleValue = random.nextInt(1_000_000_000);
            BigDecimal decimal = BigDecimal.valueOf(unscaleValue, 3);
            // v0000
            array.put("v" + fmt.format(i), decimal);
        }
        System.out.println(JSON.toJSONString(array, JSONWriter.Feature.PrettyFormat));
    }
}
