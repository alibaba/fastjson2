package com.alibaba.fastjson2.autoType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.SymbolTable;
import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the scan-time detection of illegal autoType type name characters
 * ({@code ':'} and {@code '!'}), see {@link JSONReader#isTypeNameIllegal()}.
 */
@Tag("autotype")
public class AutoTypeIllegalTypeNameTest {
    static final String BEAN_NAME = "com.alibaba.fastjson2.autoType.AutoTypeIllegalTypeNameTest$Bean";

    // =========================================================================
    // IOUtils.containsTypeNameSpecialChar boundary coverage
    // =========================================================================

    @Test
    public void testContainsTypeNameSpecialCharBytes() {
        for (int len = 1; len <= 33; len++) {
            byte[] buf = new byte[len];
            for (int pos = 0; pos < len; pos++) {
                Arrays.fill(buf, (byte) 'a');

                buf[pos] = ':';
                assertTrue(IOUtils.containsTypeNameSpecialChar(buf, 0, len), "len=" + len + ", pos=" + pos);

                buf[pos] = '!';
                assertTrue(IOUtils.containsTypeNameSpecialChar(buf, 0, len), "len=" + len + ", pos=" + pos);

                // near-miss characters around ':' (0x3A) and '!' (0x21)
                for (byte b : new byte[] {'9', ';', ' ', '"', '@', '/', (byte) 0x80, (byte) 0xFF}) {
                    buf[pos] = b;
                    assertFalse(IOUtils.containsTypeNameSpecialChar(buf, 0, len), "len=" + len + ", pos=" + pos + ", b=" + b);
                }
            }
        }
    }

    @Test
    public void testContainsTypeNameSpecialCharChars() {
        for (int len = 1; len <= 17; len++) {
            char[] buf = new char[len];
            for (int pos = 0; pos < len; pos++) {
                Arrays.fill(buf, 'a');

                buf[pos] = ':';
                assertTrue(IOUtils.containsTypeNameSpecialChar(buf, 0, len), "len=" + len + ", pos=" + pos);

                buf[pos] = '!';
                assertTrue(IOUtils.containsTypeNameSpecialChar(buf, 0, len), "len=" + len + ", pos=" + pos);

                for (char c : new char[] {'9', ';', ' ', '"', '@', '/', 0x3A00, 0x2100, 0xFF3A, 0xFF01}) {
                    buf[pos] = c;
                    assertFalse(IOUtils.containsTypeNameSpecialChar(buf, 0, len), "len=" + len + ", pos=" + pos + ", c=" + (int) c);
                }
            }
        }
    }

    // =========================================================================
    // readTypeHashCode scan-time detection, one test per reader implementation
    // =========================================================================

    @Test
    public void testDetectUtf8() {
        assertFalse(readTypeNameIllegalUtf8("com.example.Bean"));
        assertTrue(readTypeNameIllegalUtf8("com.example.Bean:bad"));
        assertTrue(readTypeNameIllegalUtf8("com.example.Bean!bad"));
        assertTrue(readTypeNameIllegalUtf8(":com.example.Bean"));
        assertTrue(readTypeNameIllegalUtf8("com.example.Bean:"));
        assertTrue(readTypeNameIllegalUtf8("a:b"));
        // multibyte characters never alias the raw bytes of ':' or '!'
        assertFalse(readTypeNameIllegalUtf8("com.example.Bean中"));
        assertTrue(readTypeNameIllegalUtf8("com.example.Bean中:bad"));
    }

    @Test
    public void testDetectAscii() {
        assertFalse(readTypeNameIllegalOfString("com.example.Bean"));
        assertTrue(readTypeNameIllegalOfString("com.example.Bean:bad"));
        assertTrue(readTypeNameIllegalOfString("com.example.Bean!bad"));
        assertTrue(readTypeNameIllegalOfString("a!b"));
    }

    @Test
    public void testDetectUtf16() {
        assertFalse(readTypeNameIllegalUtf16("com.example.Bean"));
        assertTrue(readTypeNameIllegalUtf16("com.example.Bean:bad"));
        assertTrue(readTypeNameIllegalUtf16("com.example.Bean!bad"));
        assertTrue(readTypeNameIllegalUtf16(":com.example.Bean"));
        assertTrue(readTypeNameIllegalUtf16("com.example.Bean!"));
    }

    private static boolean readTypeNameIllegalUtf8(String typeName) {
        JSONReader reader = JSONReader.of(quote(typeName).getBytes(StandardCharsets.UTF_8));
        reader.readTypeHashCode();
        return reader.isTypeNameIllegal();
    }

    private static boolean readTypeNameIllegalOfString(String typeName) {
        JSONReader reader = JSONReader.of(quote(typeName));
        reader.readTypeHashCode();
        return reader.isTypeNameIllegal();
    }

    private static boolean readTypeNameIllegalUtf16(String typeName) {
        JSONReader reader = JSONReader.of(quote(typeName).toCharArray());
        reader.readTypeHashCode();
        return reader.isTypeNameIllegal();
    }

    private static String quote(String typeName) {
        return '"' + typeName + '"';
    }

    /**
     * Escaped characters are not seen by the scan-time check; the type name is still
     * rejected downstream after unescaping.
     */
    @Test
    public void testEscapedSpecialCharNotFlagged() {
        JSONReader reader = JSONReader.of("\"com.example.Bean\\u003Abad\"");
        reader.readTypeHashCode();
        assertFalse(reader.isTypeNameIllegal());
        assertEquals("com.example.Bean:bad", reader.getString());
    }

    // =========================================================================
    // JSONB
    // =========================================================================

    @Test
    public void testDetectJsonb() {
        assertTrue(readTypeNameIllegalJsonb("com.example.Bean:bad"));
        assertTrue(readTypeNameIllegalJsonb("com.example.Bean!bad"));
        assertFalse(readTypeNameIllegalJsonb("com.example.Bean"));
    }

    private static boolean readTypeNameIllegalJsonb(String typeName) {
        byte[] jsonbBytes = writeJsonbTypedObject(typeName);
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes);
        assertTrue(reader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY));
        reader.readTypeHashCode();
        return reader.isTypeNameIllegal();
    }

    private static byte[] writeJsonbTypedObject(String typeName) {
        SymbolTable symbolTable = JSONB.symbolTable("abc");
        JSONWriter.Context writeContext = JSONFactory.createWriteContext(JSONWriter.Feature.WriteNameAsSymbol);
        JSONWriter writer = JSONWriter.ofJSONB(writeContext, symbolTable);
        writer.writeTypeName(typeName);
        writer.startObject();
        writer.endObject();
        return writer.getBytes();
    }

    @Test
    public void testJsonbCheckAutoTypeIllegalName() {
        byte[] jsonbBytes = writeJsonbTypedObject("com.example.Bean:bad");

        JSONReader reader = JSONReader.ofJSONB(jsonbBytes);
        assertNull(reader.checkAutoType(Object.class, 0, JSONReader.Feature.SupportAutoType.mask));

        JSONReader reader2 = JSONReader.ofJSONB(jsonbBytes);
        assertThrows(JSONException.class, () -> reader2.checkAutoType(
                Object.class,
                0,
                JSONReader.Feature.SupportAutoType.mask | JSONReader.Feature.ErrorOnNotSupportAutoType.mask));
    }

    /**
     * Short type names (up to 8 bytes) take the conservative path in JSONB and are
     * still rejected by the downstream validation.
     */
    @Test
    public void testJsonbCheckAutoTypeShortIllegalName() {
        byte[] jsonbBytes = writeJsonbTypedObject("A:B");
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes);
        assertNull(reader.checkAutoType(Object.class, 0, JSONReader.Feature.SupportAutoType.mask));
    }

    // =========================================================================
    // end-to-end behaviour of typed bean reads
    // =========================================================================

    /**
     * A type name carrying {@code ':'} is unresolvable, so a typed bean read falls back
     * to the expected class and keeps reading, the same as before the scan-time check.
     */
    @Test
    public void testTypedBeanIllegalNameFallsBack() {
        String json = "{\"@type\":\"com.example.Bean:bad\",\"id\":101}";
        assertEquals(101, JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportAutoType).id);
        assertEquals(101, JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), Bean.class, JSONReader.Feature.SupportAutoType).id);
        assertEquals(101, JSON.parseObject(json.toCharArray(), Bean.class, JSONReader.Feature.SupportAutoType).id);
    }

    /**
     * An escaped {@code ':'} slips past the scan-time check and must take the downstream
     * validation path, which rejects it the same way.
     */
    @Test
    public void testTypedBeanEscapedIllegalNameFallsBack() {
        String json = "{\"@type\":\"com.example.Bean\\u003Abad\",\"id\":101}";
        assertEquals(101, JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportAutoType).id);
    }

    /**
     * Names at or above the 192 length limit keep the downstream length error even when
     * they also carry an illegal character.
     */
    @Test
    public void testLongIllegalNameKeepsLengthError() {
        StringBuilder sb = new StringBuilder("{\"@type\":\"");
        for (int i = 0; i < 199; i++) {
            sb.append('a');
        }
        sb.append(":\"}");
        String json = sb.toString();
        JSONException e = assertThrows(JSONException.class,
                () -> JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportAutoType));
        assertTrue(e.getMessage().contains("autoType is not support"));
    }

    /**
     * A user-provided AutoTypeBeforeHandler still receives illegal type names and may
     * resolve them; the scan-time check must not bypass the handler.
     */
    @Test
    public void testCustomHandlerStillReceivesIllegalName() {
        String json = "{\"@type\":\"custom:Bean\",\"id\":101}";
        JSONReader.AutoTypeBeforeHandler handler = (typeName, expectClass, features) -> {
            if ("custom:Bean".equals(typeName)) {
                return Bean.class;
            }
            return null;
        };

        JSONReader.Context context = JSONFactory.createReadContext(handler, JSONReader.Feature.SupportAutoType);
        Bean bean = JSON.parseObject(json, Bean.class, context);
        assertEquals(101, bean.id);
    }

    @Test
    public void testTypedBeanLegalNameNotAffected() {
        String json = "{\"@type\":\"" + BEAN_NAME + "\",\"id\":101}";
        Bean bean = JSON.parseObject(json, Bean.class, JSONReader.Feature.SupportAutoType);
        assertEquals(101, bean.id);
        assertEquals(101, JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), Bean.class, JSONReader.Feature.SupportAutoType).id);
        assertEquals(101, JSON.parseObject(json.toCharArray(), Bean.class, JSONReader.Feature.SupportAutoType).id);
    }

    public static class Bean {
        public int id;
    }
}
