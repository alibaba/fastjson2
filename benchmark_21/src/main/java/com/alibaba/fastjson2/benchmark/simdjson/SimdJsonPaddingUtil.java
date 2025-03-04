package com.alibaba.fastjson2.benchmark.simdjson;

import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

class SimdJsonPaddingUtil {

    static byte[] padded(byte[] src) {
        byte[] bufferPadded = new byte[src.length + 64];
        System.arraycopy(src, 0, bufferPadded, 0, src.length);
        return bufferPadded;
    }

    static byte[] padWithSpaces(String str) {
        byte[] strBytes = str.getBytes(UTF_8);
        byte[] padded = new byte[strBytes.length + 64];
        Arrays.fill(padded, (byte) ' ');
        System.arraycopy(strBytes, 0, padded, 0, strBytes.length);
        return padded;
    }
}
