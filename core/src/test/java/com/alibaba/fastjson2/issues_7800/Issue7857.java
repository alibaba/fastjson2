package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class Issue7857 {
    private static final String[] OPERATION_NAMES = {
            "readAny", "readString", "readInt64Value", "readInt32Value", "readFloatValue",
            "readDoubleValue", "readNumber", "readBigDecimal", "readBigInteger"
    };

    private static final List<Consumer<JSONReader>> OPERATIONS = Arrays.asList(
            JSONReader::readAny,
            JSONReader::readString,
            JSONReader::readInt64Value,
            JSONReader::readInt32Value,
            JSONReader::readFloatValue,
            JSONReader::readDoubleValue,
            JSONReader::readNumber,
            JSONReader::readBigDecimal,
            JSONReader::readBigInteger
    );

    @Test
    public void scaleOverflow() {
        for (int scale : new int[]{-2049, 2049, -67000000, 1 << 24, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            String message = "scale overflow : " + scale;
            assertRejected(decimal(scale), message, false, "flat scale " + scale);
            assertRejected(nestedDecimal(scale), message, true, "nested unscaled, scale " + scale);
            assertRejected(nestedScaleDecimal(scale), message, true, "nested scale field, scale " + scale);
        }
    }

    @Test
    public void computedScaleOverflow() {
        // Five nested BC_DECIMAL records; two inner scales are computed by readInt32Value0's
        // BC_DECIMAL arm: BC_TRUE(1) over INT8(-67) is -6.7, intValue() truncates to -6, so
        // level C is BigDecimal(-67, -6) and its intValue() -67000000 becomes level B's scale,
        // which the guard rejects. The last two bytes (INT8, -67) are level B's own unscaled
        // value: structurally required but never reached, since the guard fires while decoding
        // level B's scale.
        byte[] e = decimal(JSONB.toBytes(0), int8(-67));
        byte[] d = decimal(new byte[]{BC_TRUE}, e);
        byte[] c = decimal(d, int8(-67));
        byte[] b = decimal(c, int8(-67));
        byte[] payload = decimal(JSONB.toBytes(45), b);
        assertRejected(payload, "scale overflow : -67000000", true, "computed nested scale");
    }

    @Test
    public void scaleFieldEncodingOverflow() {
        // scale field encoded as the FIX-ASCII string "1.0e-3000": declares scale 3001 but
        // intValue() narrows it to 0, so only the type check can reject it
        byte[] strScale = JSONB.toBytes("1.0e-3000");
        byte[] p1 = new byte[1 + strScale.length + 2];
        p1[0] = BC_DECIMAL;
        System.arraycopy(strScale, 0, p1, 1, strScale.length);
        p1[p1.length - 2] = BC_BIGINT_LONG;
        p1[p1.length - 1] = 1;
        assertRejected(p1, "decimal scale not support STR_ASCII 82, offset 1", true, "string-encoded scale field");

        // scale field encoded as BC_INT64((1L << 32) + 2048): declares 4294969344 but the
        // (int) cast narrows it to 2048
        long declared = (1L << 32) + 2048;
        byte[] p2 = new byte[12];
        p2[0] = BC_DECIMAL;
        p2[1] = BC_INT64;
        for (int i = 0; i < 8; i++) {
            p2[2 + i] = (byte) (declared >>> (56 - i * 8));
        }
        p2[10] = BC_BIGINT_LONG;
        p2[11] = 1;
        assertRejected(p2, "decimal scale not support INT64 -66, offset 1", true, "int64-encoded scale field");
    }

    @Test
    public void nestedComposedScaleOverflow() {
        // 100 nested BC_DECIMAL levels (below the depth cap of 128), each with an in-bound scale
        // of -2048: the per-level bound never fires, but the magnitude multiplies by 10^2048 per
        // level, so the cumulative budget rejects it. 99 x 2048 + 2048 = 204800.
        int depth = 100;
        byte[] bytes = new byte[depth * 6 + 2];
        int off = 0;
        for (int i = 0; i < depth; i++) {
            bytes[off++] = BC_DECIMAL;
            bytes[off++] = BC_INT32;
            bytes[off++] = (byte) 0xFF;
            bytes[off++] = (byte) 0xFF;
            bytes[off++] = (byte) 0xF8;
            bytes[off++] = 0x00; // scale -2048
        }
        bytes[off++] = BC_BIGINT_LONG;
        bytes[off] = 1;
        // skipValue never adds the outermost scale to decimalScaleSum, so its cumulative
        // total is one level lower than the value-returning operations
        assertRejected(bytes, "composed scale overflow : -2048, total 204800",
                "composed scale overflow : -2048, total 202752", true,
                "100 nested levels of scale -2048");
    }

    @Test
    public void composedScaleOverflowAttribution() {
        // the in-bound inner scale -11 is not the offender; the message must carry the total,
        // which includes the pending outer 3000 (3000 + 11 = 3011)
        byte[] inner = decimal(JSONB.toBytes(-11), int8(-15));
        byte[] payload = decimal(JSONB.toBytes(3000), inner);
        JSONException error = assertThrows(JSONException.class, () -> JSONB.parse(payload));
        assertEquals("composed scale overflow : -11, total 3011", error.getMessage());
    }

    @Test
    public void nestingDepthOverflow() {
        // 3000 nested BC_DECIMAL records whose scale fields nest down to a single int32 scale;
        // every scale is 0, so only the depth bound rejects this before StackOverflowError
        assertRejected(nestingPayload(3000), "level too large : 128", true, "3000 nested levels");
    }

    @Test
    public void nestingDepthOverflowSmallStack() throws Exception {
        // the depth bound must fire before StackOverflowError even on a 256 KB thread stack
        final byte[] bytes = nestingPayload(3000);
        final Throwable[] error = new Throwable[1];
        Thread probe = new Thread(null, () -> {
            try {
                JSONB.parse(bytes);
            } catch (Throwable e) {
                error[0] = e;
            }
        }, "issue7857-probe", 256 * 1024);
        probe.start();
        probe.join();
        assertInstanceOf(JSONException.class, error[0]);
        assertEquals("level too large : 128", error[0].getMessage());
    }

    @Test
    public void textDecimalScaleOverflow() {
        // the text-side guard shares JSONFactory.checkDecimalScale after dedup; pin it here.
        // the BIG_DEC branch requires magnitude overflow, hence the 40-digit integer part
        char[] zeros = new char[2048];
        Arrays.fill(zeros, '0');
        char[] digits = new char[40];
        Arrays.fill(digits, '1');
        String text = new String(digits) + "." + new String(zeros) + "1";
        JSONException error = assertThrows(JSONException.class, () -> JSONReader.of(text).readNumber());
        assertEquals("scale overflow : 2049", error.getMessage());
        error = assertThrows(JSONException.class,
                () -> JSONReader.of(text.getBytes(StandardCharsets.UTF_8)).readNumber());
        assertEquals("scale overflow : 2049", error.getMessage());
    }

    private static byte[] nestingPayload(int depth) {
        byte[] bytes = new byte[depth * 3 + 1];
        int off = 0;
        for (int i = 0; i < depth; i++) {
            bytes[off++] = BC_DECIMAL;
        }
        bytes[off++] = 0; // innermost scale
        for (int i = 0; i < depth; i++) {
            bytes[off++] = BC_BIGINT_LONG;
            bytes[off++] = 1;
        }
        return bytes;
    }

    @Test
    public void stringDecimalScaleOverflow() {
        byte[] bytes = JSONB.toBytes("1e2000000");
        assertEquals("1e2000000", JSONB.parse(bytes)); // plain string reads stay unaffected
        assertThrows(JSONException.class, () -> JSONB.parseObject(bytes, BigDecimal.class));
        try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
            assertThrows(JSONException.class, reader::readBigDecimal);
        }
        try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
            assertThrows(JSONException.class, reader::readNumber);
        }

        byte[] str = "1.0e2000000".getBytes(StandardCharsets.ISO_8859_1);
        byte[] ascii = new byte[str.length + 2];
        ascii[0] = BC_STR_ASCII;
        ascii[1] = (byte) str.length;
        System.arraycopy(str, 0, ascii, 2, str.length);
        try (JSONReader reader = JSONReader.ofJSONB(ascii)) {
            assertThrows(JSONException.class, reader::readBigInteger);
        }
    }

    @Test
    public void writerScaleOverflow() {
        BigDecimal positive = new BigDecimal(BigInteger.ONE, 3000);
        JSONException error = assertThrows(JSONException.class, () -> JSONB.toBytes(positive));
        assertEquals("scale overflow : 3000", error.getMessage());
        assertThrows(JSONException.class, () -> JSONB.toBytes(new BigDecimal(BigInteger.ONE, -3000)));

        BigDecimal boundary = new BigDecimal(BigInteger.ONE, 2048);
        assertEquals(boundary, JSONB.parse(JSONB.toBytes(boundary)));
    }

    @Test
    public void nullOnErrorNeighbourField() {
        // {amount: <decimal scale 3000>, name: "n"}: the rejection must fire only after the
        // decimal record is fully consumed, so a NullOnError field reader resumes aligned and
        // the neighbouring field is not corrupted
        byte[] amount = decimal(JSONB.toBytes(3000), new byte[]{1});
        byte[] bytes = new byte[1 + 7 + amount.length + 5 + 2 + 1];
        int off = 0;
        bytes[off++] = BC_OBJECT;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 6);
        off = putAscii(bytes, off, "amount");
        System.arraycopy(amount, 0, bytes, off, amount.length);
        off += amount.length;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 4);
        off = putAscii(bytes, off, "name");
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 1);
        bytes[off++] = 'n';
        bytes[off] = BC_OBJECT_END;
        NullOnErrorBean bean = JSONB.parseObject(bytes, NullOnErrorBean.class);
        assertNull(bean.amount);
        assertEquals("n", bean.name);
    }

    @Test
    public void nullOnErrorNeighbourFieldExoticScaleEncoding() {
        // same neighbour-field contract for the type-whitelist rejection: the exotic scale
        // field is skipped without decoding, the unscaled value is consumed, and the throw
        // fires only with the record fully consumed
        byte[] amount = decimal(JSONB.toBytes("1.0e-3000"), new byte[]{BC_BIGINT_LONG, 1});
        byte[] bytes = new byte[1 + 7 + amount.length + 5 + 2 + 1];
        int off = 0;
        bytes[off++] = BC_OBJECT;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 6);
        off = putAscii(bytes, off, "amount");
        System.arraycopy(amount, 0, bytes, off, amount.length);
        off += amount.length;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 4);
        off = putAscii(bytes, off, "name");
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 1);
        bytes[off++] = 'n';
        bytes[off] = BC_OBJECT_END;
        NullOnErrorBean bean = JSONB.parseObject(bytes, NullOnErrorBean.class);
        assertNull(bean.amount);
        assertEquals("n", bean.name);
    }

    @Test
    public void nullOnErrorNestedScaleField() {
        // the outer record's scale field is itself a BC_DECIMAL whose inner magnitude guard
        // fires; the rejection must still consume the outer unscaled value so the NullOnError
        // field reader resumes at the record boundary and trailing bytes cannot be re-read as
        // a forged entry
        byte[] inner = decimal(JSONB.toBytes(3000), new byte[]{BC_BIGINT_LONG, 1});
        byte[] amount = decimal(inner, new byte[]{BC_BIGINT_LONG, 7});
        byte[] bytes = new byte[1 + 7 + amount.length + 5 + 2 + 1];
        int off = 0;
        bytes[off++] = BC_OBJECT;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 6);
        off = putAscii(bytes, off, "amount");
        System.arraycopy(amount, 0, bytes, off, amount.length);
        off += amount.length;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 4);
        off = putAscii(bytes, off, "name");
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 1);
        bytes[off++] = 'n';
        bytes[off] = BC_OBJECT_END;
        NullOnErrorBean bean = JSONB.parseObject(bytes, NullOnErrorBean.class);
        assertNull(bean.amount);
        assertEquals("n", bean.name);
    }

    @Test
    public void nullStringDecimal() {
        // TypeUtils.toBigDecimal returns null for empty and "null" strings; the scale guard
        // must preserve that contract, not NPE or convert it to a JSONException
        for (String str : new String[]{"", "null"}) {
            byte[] bytes = JSONB.toBytes(str);
            assertNull(JSONB.parseObject(bytes, BigDecimal.class));
            try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                assertNull(reader.readBigDecimal());
            }
            try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                assertNull(reader.readNumber());
            }
        }
        assertEquals("", JSONB.parse(JSONB.toBytes(""))); // plain string read unaffected
    }

    @Test
    public void siblingDecimalsRestore() {
        // 600 sibling decimals, each individually in-bound: only state leaking across siblings
        // (a dropped level or decimalScaleSum restore) would trip the depth or cumulative guards
        int count = 600;
        byte[] scaleBytes = JSONB.toBytes(100);
        byte[] lengthBytes = JSONB.toBytes(count);
        byte[] bytes = new byte[1 + lengthBytes.length + count * (1 + scaleBytes.length + 1)];
        int off = 0;
        bytes[off++] = BC_ARRAY;
        System.arraycopy(lengthBytes, 0, bytes, off, lengthBytes.length);
        off += lengthBytes.length;
        for (int i = 0; i < count; i++) {
            bytes[off++] = BC_DECIMAL;
            System.arraycopy(scaleBytes, 0, bytes, off, scaleBytes.length);
            off += scaleBytes.length;
            bytes[off++] = 1;
        }
        List<Object> list = (List<Object>) JSONB.parse(bytes);
        assertEquals(count, list.size());
        BigDecimal expected = new BigDecimal(BigInteger.ONE, 100);
        assertEquals(expected, list.get(0));
        assertEquals(expected, list.get(count - 1));
    }

    @Test
    public void stringDecimalScaleCharsets() {
        for (byte strType : new byte[]{BC_STR_ASCII, BC_STR_UTF8, BC_STR_UTF16LE}) {
            Charset charset = strType == BC_STR_ASCII
                    ? StandardCharsets.ISO_8859_1
                    : strType == BC_STR_UTF8
                            ? StandardCharsets.UTF_8
                            : StandardCharsets.UTF_16LE;
            byte[] bytes = strRecord(strType, "1.0e2000000".getBytes(charset));
            try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                assertThrows(JSONException.class, reader::readBigDecimal);
            }
            try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                assertThrows(JSONException.class, reader::readBigInteger);
            }
            if (strType != BC_STR_UTF16LE) { // readNumber has no UTF16LE arm
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertThrows(JSONException.class, reader::readNumber);
                }
            }

            byte[] valid = strRecord(strType, "1.5".getBytes(charset));
            try (JSONReader reader = JSONReader.ofJSONB(valid)) {
                assertEquals(new BigDecimal("1.5"), reader.readBigDecimal());
            }
        }
    }

    private static byte[] strRecord(byte strType, byte[] str) {
        byte[] bytes = new byte[str.length + 2];
        bytes[0] = strType;
        bytes[1] = (byte) str.length;
        System.arraycopy(str, 0, bytes, 2, str.length);
        return bytes;
    }

    @Test
    public void skipUnknownField() {
        // the unknown "extra" field is skipped without materializing a BigDecimal, so its
        // scale is bounded by encoding, not by magnitude
        byte[] extra = decimal(JSONB.toBytes(3000), new byte[]{1});
        byte[] bytes = new byte[1 + 3 + 1 + 6 + extra.length + 1];
        int off = 0;
        bytes[off++] = BC_OBJECT;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 2);
        off = putAscii(bytes, off, "id");
        bytes[off++] = 1;
        bytes[off++] = (byte) (BC_STR_ASCII_FIX_MIN + 5);
        off = putAscii(bytes, off, "extra");
        System.arraycopy(extra, 0, bytes, off, extra.length);
        off += extra.length;
        bytes[off] = BC_OBJECT_END;
        IdBean bean = JSONB.parseObject(bytes, IdBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void dumpScaleOverflow() {
        JSONException error = assertThrows(
                JSONException.class,
                () -> JSONB.toJSONString(decimal(2049), true));
        assertEquals("scale overflow : 2049", error.getMessage());
    }

    @Test
    public void validScales() {
        for (int scale : new int[]{-2048, -2047, -10, -1, 0, 1, 10, 2047, 2048}) {
            for (BigInteger unscaled : new BigInteger[]{
                    BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(-123),
                    BigInteger.valueOf(Integer.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE),
                    BigInteger.ONE.shiftLeft(80)
            }) {
                BigDecimal expected = new BigDecimal(unscaled, scale);
                byte[] bytes = JSONB.toBytes(expected);
                assertEquals(expected, JSONB.parse(bytes));
                assertEquals(expected, JSONB.parseObject(bytes, BigDecimal.class));
                byte[] scaleBytes = JSONB.toBytes(scale);
                byte[] unscaledBytes = JSONB.toBytes(unscaled);
                bytes = new byte[1 + scaleBytes.length + unscaledBytes.length];
                bytes[0] = BC_DECIMAL;
                System.arraycopy(scaleBytes, 0, bytes, 1, scaleBytes.length);
                System.arraycopy(unscaledBytes, 0, bytes, 1 + scaleBytes.length, unscaledBytes.length);
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected, reader.readNumber());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.toBigInteger(), reader.readBigInteger());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.intValue(), reader.readInt32Value());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.longValue(), reader.readInt64Value());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.toString(), reader.readString());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.doubleValue(), reader.readDoubleValue(), 0.0);
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.floatValue(), reader.readFloatValue(), 0.0f);
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    reader.skipValue();
                    assertEquals(bytes.length, reader.getOffset());
                }
            }
        }
    }

    private static void assertRejected(
            byte[] bytes,
            String expectedMessage,
            boolean skipRejects,
            String description) {
        assertRejected(bytes, expectedMessage, expectedMessage, skipRejects, description);
    }

    private static void assertRejected(
            byte[] bytes,
            String expectedMessage,
            String skipMessage,
            boolean skipRejects,
            String description) {
        JSONException parseError = assertThrows(JSONException.class, () -> JSONB.parse(bytes), description);
        assertEquals(expectedMessage, parseError.getMessage(), description);
        for (int i = 0; i < OPERATIONS.size(); i++) {
            Consumer<JSONReader> operation = OPERATIONS.get(i);
            String name = description + " / " + OPERATION_NAMES[i];
            try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                JSONException error = assertThrows(JSONException.class, () -> operation.accept(reader), name);
                assertEquals(expectedMessage, error.getMessage(), name);
            }
        }
        try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
            if (skipRejects) {
                JSONException error = assertThrows(
                        JSONException.class, reader::skipValue, description + " / skipValue");
                assertEquals(skipMessage, error.getMessage(), description + " / skipValue");
            } else {
                reader.skipValue();
                assertEquals(bytes.length, reader.getOffset(), description + " / skipValue");
            }
        }
    }

    private static byte[] decimal(int scale) {
        return decimal(
                new byte[]{
                        BC_INT32,
                        (byte) (scale >>> 24), (byte) (scale >>> 16), (byte) (scale >>> 8), (byte) scale},
                new byte[]{BC_BIGINT_LONG, 1});
    }

    private static byte[] decimal(byte[] scaleEncoding, byte[] unscaledEncoding) {
        byte[] bytes = new byte[1 + scaleEncoding.length + unscaledEncoding.length];
        bytes[0] = BC_DECIMAL;
        System.arraycopy(scaleEncoding, 0, bytes, 1, scaleEncoding.length);
        System.arraycopy(unscaledEncoding, 0, bytes, 1 + scaleEncoding.length, unscaledEncoding.length);
        return bytes;
    }

    private static byte[] nestedDecimal(int scale) {
        return decimal(new byte[]{0}, decimal(scale));
    }

    private static byte[] nestedScaleDecimal(int scale) {
        return decimal(decimal(scale), new byte[]{BC_BIGINT_LONG, 1});
    }

    private static byte[] int8(int value) {
        return new byte[]{BC_INT8, (byte) value};
    }

    private static int putAscii(byte[] bytes, int off, String str) {
        for (int i = 0; i < str.length(); i++) {
            bytes[off++] = (byte) str.charAt(i);
        }
        return off;
    }

    public static class NullOnErrorBean {
        @JSONField(deserializeFeatures = JSONReader.Feature.NullOnError)
        public BigDecimal amount;
        public String name;
    }

    public static class IdBean {
        public int id;
    }
}
