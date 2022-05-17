package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LargeNumberTest {
    @Test
    public void test_0() {
        String str = "{\"val\":0.784018486000000000000000000000000000000}";
        BigDecimal expected = new BigDecimal("0.784018486000000000000000000000000000000");
        assertEquals(
                expected,
                JSON.parseObject(str).get("val")
        );

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        assertEquals(
                expected,
                JSON.parseObject(bytes).get("val")
        );

        assertEquals(
                expected,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII).get("val")
        );

        byte[] utf16 = str.getBytes(StandardCharsets.UTF_16);
        assertEquals(
                expected,
                JSON.parseObject(utf16, 0, utf16.length, StandardCharsets.UTF_16).get("val")
        );

        assertEquals(
                expected,
                TestUtils.createJSONReaderStr(str).read(JSONObject.class).get("val"));

        assertEquals(expected, JSON.parseObject(bytes, Bean.class).val);
        assertEquals(expected, JSON.parseObject(str, Bean.class).val);
        assertEquals(
                expected,
                TestUtils.createJSONReaderStr(str).read(Bean.class).val);
    }

    @Test
    public void test_1() {
        String str = "{\"val\":784018486000000000000000000000000000000}";
        BigInteger expected = new BigInteger("784018486000000000000000000000000000000");

        assertEquals(
                expected,
                JSON.parseObject(str).get("val")
        );

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        assertEquals(
                expected,
                JSON.parseObject(bytes).get("val")
        );

        assertEquals(
                expected,
                JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.US_ASCII).get("val")
        );

        byte[] utf16 = str.getBytes(StandardCharsets.UTF_16);
        assertEquals(
                expected,
                JSON.parseObject(utf16, 0, utf16.length, StandardCharsets.UTF_16).get("val")
        );

        assertEquals(
                expected,
                TestUtils.createJSONReaderStr(str).read(JSONObject.class).get("val"));

        assertEquals(expected, JSON.parseObject(bytes, Bean1.class).val);
        assertEquals(expected, JSON.parseObject(str, Bean1.class).val);
        assertEquals(
                expected,
                TestUtils.createJSONReaderStr(str).read(Bean1.class).val);

        BigDecimal expectedDec = new BigDecimal(new BigInteger("784018486000000000000000000000000000000"));
        assertEquals(expectedDec, JSON.parseObject(bytes, Bean.class).val);
        assertEquals(expectedDec, JSON.parseObject(str, Bean.class).val);
        assertEquals(
                expectedDec,
                TestUtils.createJSONReaderStr(str).read(Bean.class).val);

        double expectedDoubleValue = new BigDecimal(new BigInteger("784018486000000000000000000000000000000")).doubleValue();
        assertEquals(expectedDoubleValue, JSON.parseObject(bytes, Bean2.class).val);
        assertEquals(expectedDoubleValue, JSON.parseObject(str, Bean2.class).val);
        assertEquals(
                expectedDoubleValue,
                TestUtils.createJSONReaderStr(str).read(Bean2.class).val);
    }

    public static class Bean {
        public BigDecimal val;
    }

    public static class Bean1 {
        public BigInteger val;
    }

    public static class Bean2 {
        public double val;
    }
}
