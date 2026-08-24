package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JSONBDecimalDumpMarkerTest {
    private static final String[] STRING_VALUES = {
            "1", "x", "+", "-", ".", "1e", "--1", "1x"
    };

    private static final byte[] STRING_TYPES = {
            0,
            BC_STR_ASCII,
            BC_STR_UTF8,
            BC_STR_UTF16,
            BC_STR_UTF16LE,
            BC_STR_UTF16BE,
            BC_STR_GB18030
    };

    private static final Charset[] CHARSETS = {
            StandardCharsets.ISO_8859_1,
            StandardCharsets.ISO_8859_1,
            StandardCharsets.UTF_8,
            StandardCharsets.UTF_16,
            StandardCharsets.UTF_16LE,
            StandardCharsets.UTF_16BE,
            Charset.forName("GB18030")
    };

    private static final byte[] UNSUPPORTED_MARKERS = {
            BC_NULL,
            BC_FALSE,
            BC_TRUE,
            BC_FLOAT_INT,
            BC_FLOAT,
            BC_DOUBLE_LONG,
            BC_DOUBLE,
            BC_DECIMAL_LONG,
            BC_DECIMAL,
            BC_INT8,
            BC_INT16,
            BC_INT64_INT,
            BC_BINARY,
            BC_ARRAY_FIX_MIN,
            BC_ARRAY,
            BC_OBJECT,
            BC_LOCAL_DATE,
            BC_SYMBOL,
            BC_TYPED_ANY
    };

    @Test
    public void writerProducedDecimals() {
        BigDecimal[] decimals = {
                new BigDecimal("0.1"),
                new BigDecimal("-0.1"),
                new BigDecimal("204.8"),
                new BigDecimal("-204.9"),
                new BigDecimal("26214.4"),
                new BigDecimal("-26214.5"),
                new BigDecimal("214748364.7"),
                new BigDecimal("-214748364.8"),
                new BigDecimal("214748364.8"),
                new BigDecimal("-214748364.9"),
                new BigDecimal("922337203685477580.7"),
                new BigDecimal("-922337203685477580.8"),
                new BigDecimal("9223372036854775808.0"),
                new BigDecimal("-9223372036854775809.0")
        };
        for (BigDecimal decimal : decimals) {
            assertEquals(decimal.toString(), JSONB.toJSONString(JSONB.toBytes(decimal)));
        }

        assertEquals("123.4", JSONB.toJSONString(concat(
                bytes(BC_DECIMAL, 1, BC_BIGINT_LONG),
                JSONB.toBytes(1234L)
        )));

        BigInteger unscaled = new BigInteger("92233720368547758080");
        byte[] unscaledBytes = unscaled.toByteArray();
        assertEquals(unscaled.toString(), JSONB.toJSONString(concat(
                bytes(BC_DECIMAL, 0, BC_BIGINT),
                JSONB.toBytes(unscaledBytes.length),
                unscaledBytes
        )));
    }

    @Test
    public void stringMarkersAreRejected() {
        int assertions = 0;
        for (int encoding = 0; encoding < STRING_TYPES.length; encoding++) {
            for (String value : STRING_VALUES) {
                byte[] bytes = decimal(string(encoding, value));
                assertThrows(
                        JSONException.class,
                        () -> JSONB.toJSONString(bytes),
                        "encoding=" + encoding + ", value=" + value
                );
                assertions++;
            }
        }
        assertEquals(56, assertions);
    }

    @Test
    public void unsupportedMarkersAreRejected() {
        for (byte marker : UNSUPPORTED_MARKERS) {
            byte[] bytes = concat(bytes(BC_DECIMAL, 0, marker), new byte[16]);
            assertThrows(
                    JSONException.class,
                    () -> JSONB.toJSONString(bytes),
                    "marker=" + marker
            );
        }
    }

    private static byte[] decimal(byte[] unscaledValue) {
        return concat(bytes(BC_DECIMAL, 0), unscaledValue);
    }

    private static byte[] string(int encoding, String value) {
        byte[] content = value.getBytes(CHARSETS[encoding]);
        if (encoding == 0) {
            return concat(bytes(BC_STR_ASCII_FIX_MIN + content.length), content);
        }
        return concat(bytes(STRING_TYPES[encoding]), JSONB.toBytes(content.length), content);
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) values[i];
        }
        return bytes;
    }

    private static byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] bytes = new byte[length];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, bytes, offset, part.length);
            offset += part.length;
        }
        return bytes;
    }
}
