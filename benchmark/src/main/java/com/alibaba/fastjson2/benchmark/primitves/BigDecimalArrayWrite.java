package com.alibaba.fastjson2.benchmark.primitves;

import com.alibaba.fastjson2.JSON;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.math.BigDecimal;

public class BigDecimalArrayWrite {
    static final BigDecimal[] decimals;
    static {
        String str = "[\n" +
                "\t13815.355,\n" +
                "\t420910.191,\n" +
                "\t167949.164,\n" +
                "\t942468.967,\n" +
                "\t266528.244,\n" +
                "\t776802.173,\n" +
                "\t110997.327,\n" +
                "\t468119.875,\n" +
                "\t727720.110,\n" +
                "\t62399.251,\n" +
                "\t431677.640,\n" +
                "\t707775.897,\n" +
                "\t887747.151,\n" +
                "\t141447.251,\n" +
                "\t999492.584,\n" +
                "\t342105.052,\n" +
                "\t806888.395,\n" +
                "\t141485.712,\n" +
                "\t549815.092,\n" +
                "\t816489.903\n" +
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
