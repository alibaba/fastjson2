package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigIntTest {
    /**
     * This mask is used to obtain the value of an int as if it were unsigned.
     */
    static final long LONG_MASK = 0xffffffffL;

    private int signum;

    private int mag0;
    private int mag1;
    private int mag2;
    private int mag3;

    @Test
    public void test_json_bigint_0() throws Exception {
        String str = "{\"id\":123}";
        Object id = JSONReader.of(str
                        .toCharArray())
                .readObject()
                .get("id");

        assertEquals(123, id);
    }

    @Test
    public void test_json_bigint_1() throws Exception {
        String str = "123456789012345678";
        BigInteger bigInt = new BigInteger(str);
        bigInt.longValue();
        String json = "{\"id\":" + str + "}";
        Object id = JSONReader.of(json
                        .toCharArray())
                .readObject()
                .get("id");

        assertEquals(123456789012345678L, id);
    }

    @Test
    public void test_json_bigint_2() throws Exception {
        String str = "12345678901234567890";
        BigInteger bigInt = new BigInteger(str);
        bigInt.longValue();
        String json = "{\"id\":" + str + "}";
        Object id = JSONReader.of(json
                        .toCharArray())
                .readObject()
                .get("id");

        assertEquals("12345678901234567890", id.toString());
    }

    @Test
    public void test_json_bigint_1_negative() throws Exception {
        String str = "-123456789012345678";
        BigInteger bigInt = new BigInteger(str);
        bigInt.longValue();
        String json = "{\"id\":" + str + "}";
        Object id = JSONReader.of(json
                        .toCharArray())
                .readObject()
                .get("id");

        assertEquals(-123456789012345678L, id);
    }

    @Test
    public void test_json_bigint_2_negative() throws Exception {
        String str = "-12345678901234567890";
        BigInteger bigInt = new BigInteger(str);
        bigInt.longValue();
        byte[] bytes = bigInt.toByteArray();
        String json = "{\"id\":" + str + "}";
        Object id = JSONReader.of(json
                        .toCharArray())
                .readObject()
                .get("id");

        assertEquals("-12345678901234567890", id.toString());
    }

    @Test
    public void test_bigInt_0() throws Exception {
        String str = "12345678901234567890";
        char[] chars = str.toCharArray();
        BigInteger bigInt0 = new BigInteger(str);

        // isneg ? -1 : 1
        bigInt(chars, 1, 0, 0, chars.length);

        assertEquals(1, signum);
        assertEquals(mag2, -1420514932);
        assertEquals(mag3, -350287150);
    }

    @Test
    public void test_bigInt_1() throws Exception {
        String str = "-12345678901234567890123456789012345678";
        char[] chars = str.toCharArray();
        BigInteger bigInt1 = new BigInteger(str);

        // isneg ? -1 : 1
        bigInt(chars, -1, 0, 1, chars.length);

        assertEquals(-1, signum);
        assertEquals(mag0, 155824374);
        assertEquals(mag1, -268291309);
        assertEquals(mag2, -1001811888);
        assertEquals(mag3, -566693042);
    }

    @Test
    public void test_bigInt_2() throws Exception {
        String str = "-1234567890.1234567890";
        char[] chars = str.toCharArray();
        BigDecimal decimal = new BigDecimal(str);

        // isneg ? -1 : 1
        bigInt(chars, -1, 10, 1, chars.length);

        assertEquals(-1, signum);
        assertEquals(mag2, -1420514932);
        assertEquals(mag3, -350287150);
    }

    @Test
    public void test_bigInt_3() throws Exception {
        String str = "-.12345678901234567890";
        char[] chars = str.toCharArray();
        BigDecimal decimal = new BigDecimal(str);

        // isneg ? -1 : 1
        bigInt(chars, -1, 10, 1, chars.length);

        assertEquals(-1, signum);
        assertEquals(mag2, -1420514932);
        assertEquals(mag3, -350287150);
    }

    @Test
    public void test_bigInt_4() throws Exception {
        String str = "-1.2345678901234567890";
        char[] chars = str.toCharArray();
        BigDecimal decimal = new BigDecimal(str);

        // isneg ? -1 : 1
        bigInt(chars, -1, 10, 1, chars.length);

        assertEquals(-1, signum);
        assertEquals(mag2, -1420514932);
        assertEquals(mag3, -350287150);
    }

    @Test
    public void test_bigInt_5() throws Exception {
        String str = "-12.345678901234567890";
        char[] chars = str.toCharArray();
        BigDecimal decimal = new BigDecimal(str);

        // isneg ? -1 : 1
        bigInt(chars, -1, 10, 1, chars.length);

        assertEquals(-1, signum);
        assertEquals(mag2, -1420514932);
        assertEquals(mag3, -350287150);
    }

    void bigInt(char[] chars, int sign, int scale, int off, int len) {
        int cursor = off, numDigits;

        numDigits = len - cursor;
        if (scale > 0) {
            numDigits--;
        }
        signum = sign;

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % 9;
        if (firstGroupLen == 0) {
            firstGroupLen = 9;
        }

        {
            int start = cursor;
            int end = cursor += firstGroupLen;

            char c = chars[start++];
            if (c == '.') {
                c = chars[start++];
                cursor++;
                end++;
            }

            int result = c - '0';

            for (int index = start; index < end; index++) {
                c = chars[index];
                if (c == '.') {
                    c = chars[++index];
                    cursor++;
                    end++;
                }

                int nextVal = c - '0';
                result = 10 * result + nextVal;
            }
            mag3 = result;
        }

        // Process remaining digit groups
        while (cursor < len) {
            int groupVal;
            {
                int start = cursor;
                int end = cursor += 9;

                char c = chars[start++];
                if (c == '.') {
                    c = chars[start++];
                    cursor++;
                    end++;
                }

                int result = c - '0';

                for (int index = start; index < end; index++) {
                    c = chars[index];
                    if (c == '.') {
                        c = chars[++index];
                        cursor++;
                        end++;
                    }

                    int nextVal = c - '0';
                    result = 10 * result + nextVal;
                }
                groupVal = result;
            }

            // destructiveMulAdd
            long ylong = 1000000000 & LONG_MASK;
            long zlong = groupVal & LONG_MASK;

            long product = 0;
            long carry = 0;
            for (int i = 3; i >= 0; i--) {
                switch (i) {
                    case 0:
                        product = ylong * (mag0 & LONG_MASK) + carry;
                        mag0 = (int) product;
                        break;
                    case 1:
                        product = ylong * (mag1 & LONG_MASK) + carry;
                        mag1 = (int) product;
                        break;
                    case 2:
                        product = ylong * (mag2 & LONG_MASK) + carry;
                        mag2 = (int) product;
                        break;
                    case 3:
                        product = ylong * (mag3 & LONG_MASK) + carry;
                        mag3 = (int) product;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = product >>> 32;
            }

            long sum = (mag3 & LONG_MASK) + zlong;
            mag3 = (int) sum;

            // Perform the addition
            carry = sum >>> 32;
            for (int i = 2; i >= 0; i--) {
                switch (i) {
                    case 0:
                        sum = (mag0 & LONG_MASK) + carry;
                        mag0 = (int) sum;
                        break;
                    case 1:
                        sum = (mag1 & LONG_MASK) + carry;
                        mag1 = (int) sum;
                        break;
                    case 2:
                        sum = (mag2 & LONG_MASK) + carry;
                        mag2 = (int) sum;
                        break;
                    case 3:
                        sum = (mag3 & LONG_MASK) + carry;
                        mag3 = (int) sum;
                        break;
                    default:
                        throw new ArithmeticException("BigInteger would overflow supported range");
                }
                carry = sum >>> 32;
            }
        }
    }

    //int[] mag = new int[] {mag0, mag1, mag2, mag3};
    private int firstNonzeroIntNum(int[] mag) {
        for (int i = mag.length - 1, j = 0; i >= 0; i--, j++) {
            if (mag[i] != 0) {
                return j;
            }
        }
        return 0;
    }
}
