package com.alibaba.fastjson2.benchmark.fastcode;

import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.*;
import java.math.BigDecimal;
import java.util.function.Function;

import static java.lang.invoke.MethodType.methodType;

public class BigDecimalNew {
    @Benchmark
    public void string(Blackhole bh) throws Throwable {
        for (int i = 0; i < decimalBytes.length; i++) {
            byte[] bytes = decimalBytes[i];
            BigDecimal decimal = new BigDecimal(new String(bytes));
            bh.consume(decimal);
        }
    }

    @Benchmark
    public void string1(Blackhole bh) throws Throwable {
        for (int i = 0; i < strings.length; i++) {
            String str = strings[i];
            BigDecimal decimal = new BigDecimal(str);
            bh.consume(decimal);
        }
    }

    @Benchmark
    public void string2(Blackhole bh) throws Throwable {
        for (int i = 0; i < strings.length; i++) {
            String str = strings[i];
            BigDecimal decimal = TypeUtils.toBigDecimal(str);
            bh.consume(decimal);
        }
    }

    @Benchmark
    public void chars(Blackhole bh) throws Throwable {
        for (int i = 0; i < decimalBytes.length; i++) {
            byte[] bytes = decimalBytes[i];
            char[] chars = toAsciiCharArray(bytes);
            BigDecimal decimal = new BigDecimal(chars, 0, chars.length);
            bh.consume(decimal);
        }
    }

    @Benchmark
    public void chars2(Blackhole bh) throws Throwable {
        for (int i = 0; i < decimalBytes.length; i++) {
            byte[] bytes = decimalBytes[i];
            char[] chars = TO_CHARS.apply(bytes);
            BigDecimal decimal = new BigDecimal(chars, 0, chars.length);
            bh.consume(decimal);
        }
    }

    @Benchmark
    public void bytes(Blackhole bh) throws Throwable {
        for (int i = 0; i < decimalBytes.length; i++) {
            byte[] bytes = decimalBytes[i];
            BigDecimal decimal = TypeUtils.toBigDecimal(bytes);
            bh.consume(decimal);
        }
    }

    public static char[] toAsciiCharArray(byte[] bytes) {
        char[] charArray = new char[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            charArray[i] = (char) bytes[i];
        }
        return charArray;
    }

    static final String[] strings = {
            "567988.735",
            "-811227.824",
            "17415.508",
            "668069.440",
            "77259.887",
            "733032.058",
            "44402.415",
            "99328.975",
            "759431.827",
            "651998.851",
            "595127.733",
            "872747.476",
            "976748.491",
            "63991.314",
            "436269.240",
            "509959.652",
            "648017.400",
            "86751.384",
            "800272.803",
            "639564.823",
            "88635.267",
            "409446.022",
            "228804.504",
            "640130.935",
            "941728.712",
            "668647.192",
            "746452.938",
            "88000.517",
            "175690.681",
            "442989.476",
            "714895.680",
            "271997.015",
            "784747.089",
            "357574.796",
            "497020.456",
            "361937.673",
            "731252.665",
            "328984.250",
            "402177.572",
            "511251.084",
            "290164.359",
            "844655.633",
            "238646.400",
            "209082.573",
            "800429.012",
            "612647.616",
            "434125.300",
            "308113.583",
            "481771.315",
            "394124.322",
            "818335.777",
            "339450.066",
            "334937.770",
            "304400.447",
            "533111.800",
            "743212.248",
            "328471.243",
            "193255.426",
            "892754.606",
            "951287.847",
            "272599.471",
            "262161.834",
            "290162.866",
            "320829.094",
            "412294.692",
            "521239.528",
            "841545.834",
            "252217.529",
            "271679.523",
            "291849.519",
            "563712.454",
            "374797.778",
            "467001.597",
            "760154.498",
            "426363.937",
            "706653.732",
            "578078.926",
            "460563.960",
            "158475.411",
            "655223.901",
            "263773.087",
            "169458.408",
            "324783.323",
            "331908.388",
            "64351.359",
            "262647.243",
            "573084.414",
            "55618.851",
            "742849.227",
            "726686.140",
            "468504.798",
            "983562.626",
            "754044.022",
            "239351.762",
            "72823.402",
            "517170.424",
            "759187.394",
            "624425.622",
            "742522.595",
            "713384.831"
    };

    static final byte[][] decimalBytes;
    static final Function<byte[], char[]> TO_CHARS;

    static {
        byte[][] array2 = new byte[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            array2[i] = strings[i].getBytes();
        }
        decimalBytes = array2;

        Function<byte[], char[]> toChars = null;
        try {
            Class<?> latin1Class = Class.forName("java.lang.StringLatin1");
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(latin1Class);
            MethodHandle handle = lookup.findStatic(
                    latin1Class, "toChars", MethodType.methodType(char[].class, byte[].class)
            );

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    methodType(Function.class),
                    methodType(Object.class, Object.class),
                    handle,
                    methodType(char[].class, byte[].class)
            );
            toChars = (Function<byte[], char[]>) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            ignored.printStackTrace();
            // ignored
        }

        if (toChars == null) {
            toChars = BigDecimalNew::toAsciiCharArray;
        }
        TO_CHARS = toChars;
    }
}
