package com.alibaba.fastjson2.read.type;

import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberTest {
    @Test
    public void test_0_38() throws Exception {
        char[] chars = new char[256];
        chars[0] = '-';
        for (int i = 0; i < 38; ++i) {
            for (int j = 0; j < 10; ++j) {
                char ch = (char) ('0' + ((1 + i + j) % 10));
                if (ch == '0' && i == 0) {
                    continue;
                }

                chars[i] = ch;
                String str = new String(chars, 0, i + 1);
                String json = "{\"v\":" + str + "}";

                //System.out.println(json);

                Number num = (Number) JSONReader.of(json)
                        .readObject()
                        .get("v");
                assertEquals(str,
                        num.toString());
            }

            chars[i + 1] = (char) ('0' + ((1 + i) % 10));
        }
    }

    @Test
    public void test_0_38_negative() throws Exception {
        char[] chars = new char[256];
        chars[0] = '-';
        for (int i = 0; i < 38; ++i) {
            for (int j = 0; j < 10; ++j) {
                char ch = (char) ('0' + ((1 + i + j) % 10));
                if (ch == '0' && i == 0) {
                    continue;
                }

                chars[i + 1] = ch;
                String str = new String(chars, 0, i + 2);
                String json = "{\"v\":" + str + "}";

                //System.out.println(json);

                Number num = (Number) JSONReader.of(json)
                        .readObject()
                        .get("v");
                assertEquals(str,
                        num.toString());
            }

            chars[i + 1] = (char) ('0' + ((1 + i) % 10));
        }
    }

    @Test
    public void test_bigint_parse() throws Exception {
        String str = "12345678901234567890";
        String json = "{\"v\":" + str + "}";

        Number num = (Number) JSONReader.of(json)
                .readObject()
                .get("v");
        assertEquals(str,
                num.toString());
    }

    @Test
    public void test_bigint_parse_1() throws Exception {
//        String str = "-1234567890123456789012345678";
        String str = "-12345678901234567890";
        BigInteger bigInt = new BigInteger(str);
        byte[] bytes = bigInt.toByteArray();
        String json = "{\"v\":" + str + "}";

        Number num = (Number) JSONReader.of(json)
                .readObject()
                .get("v");
        assertEquals(str,
                num.toString());
    }

    @Test
    public void test_dec_0() throws Exception {
        String str = "12.34";
        String json = "{\"v\":" + str + "}";

        Number num = (Number) JSONReader.of(json)
                .readObject()
                .get("v");
        assertEquals(str,
                num.toString());
    }

    @Test
    public void test_dec_1() throws Exception {
        String str = "12.345678901234567890";
        String json = "{\"v\":" + str + "}";

        BigDecimal decimal = new BigDecimal(str);
        BigInteger bigInt = decimal.unscaledValue();
        byte[] bytes = bigInt.toByteArray();

        Number num = (Number) JSONReader.of(json)
                .readObject()
                .get("v");
        assertEquals(str,
                num.toString());
    }

    @Test
    public void test_dec_2() throws Exception {
        String str = "1234567890123456789.0123456789";
        String json = "{\"v\":" + str + "}";

        BigDecimal decimal = new BigDecimal(str);
        BigInteger bigInt = decimal.unscaledValue();
        byte[] bytes = bigInt.toByteArray();

        Number num = (Number) JSONReader.of(json)
                .readObject()
                .get("v");
        assertEquals(str,
                num.toString());
    }
}
