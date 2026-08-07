package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.JDKUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;

import static java.lang.invoke.MethodType.methodType;

public class DecimalHugToString {
    static final BigInteger unscaledVal;
    static int scale = 2;
    static BigDecimal decimal;
    static BiFunction<BigDecimal, Boolean, String> LAYOUT_CHARS;

    static {
        decimal = new BigDecimal("37335022433733502243.55");
        unscaledVal = decimal.unscaledValue();

        try {
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(BigDecimal.class);

            MethodHandle handle = lookup.findVirtual(
                    BigDecimal.class, "layoutChars", methodType(String.class, boolean.class)
            );

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    methodType(BiFunction.class),
                    methodType(Object.class, Object.class, Object.class),
                    handle,
                    methodType(String.class, BigDecimal.class, Boolean.class)
            );
            LAYOUT_CHARS = (BiFunction<BigDecimal, Boolean, String>) callSite.getTarget().invokeExact();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void toPlainString(Blackhole bh) {
        bh.consume(DecimalUtils.toString(unscaledVal, scale));
    }

    @Benchmark
    public void layoutChars(Blackhole bh) {
        bh.consume(LAYOUT_CHARS.apply(decimal, Boolean.TRUE));
    }

    @Benchmark
    public void toPlainStringDec(Blackhole bh) {
        bh.consume(
                decimal.toPlainString()
        );
    }

    public void toStringCharWithInt8(Blackhole bh) {
        StringBuilder result = new StringBuilder();
        result.append(2048);
        result.append(31337);
        result.append(0xbeefcace);
        result.append(9000);
        result.append(4711);
        result.append(1337);
        result.append(2100);
        result.append(2600);
        bh.consume(result.toString());
    }

    public void toStringCharWithInt8UTF16(Blackhole bh) {
        StringBuilder result = new StringBuilder();
        result.append('\u4e2d');
        result.append(2048);
        result.append(31337);
        result.append(0xbeefcace);
        result.append(9000);
        result.append(4711);
        result.append(1337);
        result.append(2100);
        result.append(2600);
        bh.consume(result.toString());
    }
}
