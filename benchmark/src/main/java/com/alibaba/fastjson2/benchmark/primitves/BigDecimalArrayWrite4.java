package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;

public class BigDecimalArrayWrite4 {
    static final BigDecimal[] decimals;
    static {
        String str = "[\n" +
                "\t13815.1355,\n" +
                "\t420910.1191,\n" +
                "\t167949.1164,\n" +
                "\t942468.1967,\n" +
                "\t266528.1244,\n" +
                "\t776802.1173,\n" +
                "\t110997.1327,\n" +
                "\t468119.1875,\n" +
                "\t727720.1110,\n" +
                "\t62399.1251,\n" +
                "\t431677.1640,\n" +
                "\t707775.1897,\n" +
                "\t887747.1151,\n" +
                "\t141447.1251,\n" +
                "\t999492.1584,\n" +
                "\t342105.1052,\n" +
                "\t806888.1395,\n" +
                "\t141485.1712,\n" +
                "\t549815.1092,\n" +
                "\t816489.1903\n" +
                "]";
        decimals = JSON.parseObject(str, BigDecimal[].class);
    }

    @Benchmark
    public void fastjson2(Blackhole bh) {
        bh.consume(
                JSON.toJSONBytes(decimals)
        );
    }

    public void fastjson2_str(Blackhole bh) {
        bh.consume(
                JSON.toJSONString(decimals)
        );
    }
}
