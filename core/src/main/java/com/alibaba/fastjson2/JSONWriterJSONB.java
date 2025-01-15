package com.alibaba.fastjson2;

import com.alibaba.fastjson2.internal.trove.map.hash.TLongIntHashMap;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.*;
import java.util.*;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.DateUtils.OFFSET_8_ZONE_ID_NAME;
import static com.alibaba.fastjson2.util.DateUtils.SHANGHAI_ZONE_ID_NAME;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.*;

final class JSONWriterJSONB
        extends JSONWriter {
    // optimize for write ZonedDateTime
    static final byte[] SHANGHAI_ZONE_ID_NAME_BYTES = JSONB.toBytes(SHANGHAI_ZONE_ID_NAME);
    static final byte[] OFFSET_8_ZONE_ID_NAME_BYTES = JSONB.toBytes(OFFSET_8_ZONE_ID_NAME);
    static final long WRITE_ENUM_USING_STRING_MASK = WriteEnumUsingToString.mask | WriteEnumsUsingName.mask;

    private final CacheItem cacheItem;
    byte[] bytes;
    private TLongIntHashMap symbols;
    private int symbolIndex;

    private long rootTypeNameHash;

    JSONWriterJSONB(Context ctx, SymbolTable symbolTable) {
        super(ctx, symbolTable, true, StandardCharsets.UTF_8);
        cacheItem = CACHE_ITEMS[System.identityHashCode(Thread.currentThread()) & (CACHE_ITEMS.length - 1)];
        byte[] bytes = BYTES_UPDATER.getAndSet(cacheItem, null);
        if (bytes == null) {
            bytes = new byte[8192];
        }
        this.bytes = bytes;
    }

    @Override
    public void close() {
        final byte[] bytes = this.bytes;
        if (bytes.length < CACHE_THRESHOLD) {
            BYTES_UPDATER.lazySet(cacheItem, bytes);
        }
    }

    @Override
    public void writeAny(Object value) {
        if (value == null) {
            writeNull();
            return;
        }

        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;

        Class<?> valueClass = value.getClass();
        ObjectWriter objectWriter = context.provider.getObjectWriter(valueClass, valueClass, fieldBased);

        if (isBeanToArray()) {
            objectWriter.writeArrayMappingJSONB(this, value, null, null, 0);
        } else {
            objectWriter.writeJSONB(this, value, null, null, 0);
        }
    }

    @Override
    public void startObject() {
        if (level >= context.maxLevel) {
            throw new JSONException("level too large : " + level);
        }

        level++;
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }
        bytes[off] = BC_OBJECT;
        this.off = off + 1;
    }

    @Override
    public void endObject() {
        level--;
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }
        bytes[off] = BC_OBJECT_END;
        this.off = off + 1;
    }

    @Override
    public void startArray() {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void startArray(Object array, int size) {
        if (isWriteTypeInfo(array)) {
            writeTypeName(array.getClass().getName());
        }

        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        boolean tinyInt = size <= ARRAY_FIX_LEN;
        bytes[off] = tinyInt ? (byte) (BC_ARRAY_FIX_MIN + size) : BC_ARRAY;
        this.off = off + 1;
        if (!tinyInt) {
            writeInt32(size);
        }
    }

    @Override
    public void startArray(int size) {
        int off = this.off;
        if (off + 1 >= bytes.length) {
            grow0(off + 2);
        }

        boolean tinyInt = size <= ARRAY_FIX_LEN;
        bytes[off] = tinyInt ? (byte) (BC_ARRAY_FIX_MIN + size) : BC_ARRAY;
        this.off = off + 1;
        if (!tinyInt) {
            writeInt32(size);
        }
    }

    @Override
    public void startArray0() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = BC_ARRAY_FIX_MIN;
        this.off = off + 1;
    }

    @Override
    public void startArray1() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 1);
        this.off = off + 1;
    }

    @Override
    public void startArray2() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 2);
        this.off = off + 1;
    }

    @Override
    public void startArray3() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 3);
        this.off = off + 1;
    }

    @Override
    public void startArray4() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 4);
        this.off = off + 1;
    }

    @Override
    public void startArray5() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 5);
        this.off = off + 1;
    }

    @Override
    public void startArray6() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 6);
        this.off = off + 1;
    }

    @Override
    public void startArray7() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 7);
        this.off = off + 1;
    }

    @Override
    public void startArray8() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 8);
        this.off = off + 1;
    }

    @Override
    public void startArray9() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 9);
        this.off = off + 1;
    }

    @Override
    public void startArray10() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 10);
        this.off = off + 1;
    }

    @Override
    public void startArray11() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 11);
        this.off = off + 1;
    }

    @Override
    public void startArray12() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 12);
        this.off = off + 1;
    }

    @Override
    public void startArray13() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 13);
        this.off = off + 1;
    }

    @Override
    public void startArray14() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 14);
        this.off = off + 1;
    }

    @Override
    public void startArray15() {
        int off = this.off;
        if (off == bytes.length) {
            grow0(off + 1);
        }

        bytes[off] = (byte) (BC_ARRAY_FIX_MIN + 15);
        this.off = off + 1;
    }

    @Override
    public void writeRaw(byte b) {
        if (off == bytes.length) {
            grow0(off + 1);
        }
        bytes[off++] = b;
    }

    @Override
    public void writeChar(char ch) {
        if (off == bytes.length) {
            grow0(off + 1);
        }
        bytes[off++] = BC_CHAR;
        writeInt32(ch);
    }

    @Override
    public void writeName(String name) {
        writeString(name);
    }

    @Override
    public void writeNull() {
        if (off == bytes.length) {
            grow0(off + 1);
        }
        bytes[off++] = BC_NULL;
    }

    @Override
    public void writeStringNull() {
        if (off == bytes.length) {
            grow0(off + 1);
        }
        bytes[off++] = BC_NULL;
    }

    @Override
    public void endArray() {
    }

    @Override
    public void writeComma() {
        throw new JSONException("unsupported operation");
    }

    @Override
    protected void write0(char ch) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void writeString(boolean value) {
        writeString(Boolean.toString(value));
    }

    @Override
    public void writeString(byte value) {
        writeString(Integer.toString(value));
    }

    @Override
    public void writeString(short value) {
        writeString(Integer.toString(value));
    }

    @Override
    public void writeString(int value) {
        writeString(Integer.toString(value));
    }

    @Override
    public void writeString(long value) {
        writeString(Long.toString(value));
    }

    @Override
    public void writeString(boolean[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeString(value[i]);
        }
    }

    @Override
    public void writeString(byte[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeString(value[i]);
        }
    }

    @Override
    public void writeString(short[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeString(value[i]);
        }
    }

    @Override
    public void writeString(int[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeString(value[i]);
        }
    }

    @Override
    public void writeString(long[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeString(value[i]);
        }
    }

    @Override
    public void writeString(float[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeString(value[i]);
        }
    }

    @Override
    public void writeString(double[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeString(value[i]);
        }
    }

    @Override
    public void writeString(char[] chars, int coff, int len, boolean quote) {
        if (chars == null) {
            writeNull();
            return;
        }

        boolean ascii = true;
        for (int i = 0; i < len; ++i) {
            if (chars[i + coff] > 0x00FF) {
                ascii = false;
                break;
            }
        }

        if (ascii) {
            int off = this.off;
            byte[] bytes = this.bytes;
            if (len <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (len + BC_STR_ASCII_FIX_MIN);
            } else {
                bytes[off++] = BC_STR_ASCII;
                off += writeInt32(bytes, off, len);
            }
            for (int i = 0; i < len; ++i) {
                bytes[off++] = (byte) chars[coff + i];
            }
            this.off = off;
            return;
        }

        writeString(new String(chars, coff, len));
    }

    public void writeStringLatin1(final byte[] value) {
        if (value == null) {
            writeStringNull();
            return;
        }

        int off = this.off;
        int strlen = value.length;
        int minCapacity = value.length
                + off
                + 5 /*max str len*/
                + 1;

        if (minCapacity - bytes.length > 0) {
            grow0(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (strlen <= STR_ASCII_FIX_LEN) {
            bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
        } else if (strlen <= INT32_BYTE_MAX) {
            putStringSizeSmall(bytes, off, strlen);
            off += 3;
        } else {
            off += putStringSizeLarge(bytes, off, strlen);
        }
        System.arraycopy(value, 0, bytes, off, value.length);
        this.off = off + strlen;
    }

    private static void putStringSizeSmall(byte[] bytes, int off, int val) {
        bytes[off] = BC_STR_ASCII;
        bytes[off + 1] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
        bytes[off + 2] = (byte) (val);
    }

    private static int putStringSizeLarge(byte[] bytes, int off, int strlen) {
        if (strlen <= INT32_SHORT_MAX) {
            bytes[off] = BC_STR_ASCII;
            bytes[off + 1] = (byte) (BC_INT32_SHORT_ZERO + (strlen >> 16));
            bytes[off + 2] = (byte) (strlen >> 8);
            bytes[off + 3] = (byte) (strlen);
            return 4;
        }

        bytes[off] = BC_STR_ASCII;
        bytes[off + 1] = BC_INT32;
        IOUtils.putIntBE(
                bytes,
                off + 2,
                strlen
        );
        return 6;
    }

    @Override
    public void writeString(final char[] chars) {
        if (chars == null) {
            writeNull();
            return;
        }

        int off = this.off;
        boolean ascii = true;
        int strlen = chars.length;
        byte[] bytes = this.bytes;
        if (chars.length < STR_ASCII_FIX_LEN) {
            int minCapacity = off + 1 + strlen;
            if (minCapacity > bytes.length) {
                bytes = grow(minCapacity);
            }

            bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            for (int i = 0; i < chars.length; i++) {
                char ch = chars[i];
                if (ch > 0x00FF) {
                    ascii = false;
                    break;
                }
                bytes[off++] = (byte) ch;
            }

            if (ascii) {
                this.off = off;
                return;
            } else {
                off = this.off;
            }
        }

        {
            int i = 0;
            int upperBound = chars.length & ~3;
            for (; i < upperBound; i += 4) {
                char c0 = chars[i];
                char c1 = chars[i + 1];
                char c2 = chars[i + 2];
                char c3 = chars[i + 3];
                if (c0 > 0x00FF || c1 > 0x00FF || c2 > 0x00FF || c3 > 0x00FF) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                for (; i < chars.length; ++i) {
                    if (chars[i] > 0x00FF) {
                        ascii = false;
                        break;
                    }
                }
            }
        }

        int minCapacity = (ascii ? strlen : strlen * 3)
                + off
                + 5 /*max str len*/
                + 1;

        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        if (ascii) {
            if (strlen <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            } else if (strlen <= INT32_BYTE_MAX) {
                putStringSizeSmall(bytes, off, strlen);
                off += 3;
            } else {
                off += putStringSizeLarge(bytes, off, strlen);
            }
            for (int i = 0; i < chars.length; i++) {
                bytes[off++] = (byte) chars[i];
            }
        } else {
            int maxSize = chars.length * 3;
            int lenByteCnt = sizeOfInt(maxSize);
            minCapacity = off + maxSize + lenByteCnt + 1;
            if (minCapacity > bytes.length) {
                bytes = grow(minCapacity);
            }
            int result = IOUtils.encodeUTF8(chars, 0, chars.length, bytes, off + lenByteCnt + 1);

            int utf8len = result - off - lenByteCnt - 1;
            int utf8lenByteCnt = sizeOfInt(utf8len);
            if (lenByteCnt != utf8lenByteCnt) {
                System.arraycopy(bytes, off + lenByteCnt + 1, bytes, off + utf8lenByteCnt + 1, utf8len);
            }
            bytes[off++] = BC_STR_UTF8;
            if (utf8len >= BC_INT32_NUM_MIN && utf8len <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) utf8len;
            } else if (utf8len >= INT32_BYTE_MIN && utf8len <= INT32_BYTE_MAX) {
                bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (utf8len >> 8));
                bytes[off + 1] = (byte) (utf8len);
                off += 2;
            } else {
                off += writeInt32(bytes, off, utf8len);
            }
            off += utf8len;
        }
        this.off = off;
    }

    @Override
    public void writeString(final char[] chars, final int charsOff, final int len) {
        if (chars == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        boolean ascii = true;

        if (len < STR_ASCII_FIX_LEN) {
            int minCapacity = off + 1 + len;
            if (minCapacity > bytes.length) {
                bytes = grow(minCapacity);
            }

            bytes[off++] = (byte) (len + BC_STR_ASCII_FIX_MIN);
            for (int i = charsOff; i < len; i++) {
                char ch = chars[i];
                if (ch > 0x00FF) {
                    ascii = false;
                    break;
                }
                bytes[off++] = (byte) ch;
            }

            if (ascii) {
                this.off = off;
                return;
            }

            off = this.off;
        }

        {
            int i = charsOff;
            int upperBound = chars.length & ~3;
            for (; i < upperBound; i += 4) {
                char c0 = chars[i];
                char c1 = chars[i + 1];
                char c2 = chars[i + 2];
                char c3 = chars[i + 3];
                if (c0 > 0x00FF || c1 > 0x00FF || c2 > 0x00FF || c3 > 0x00FF) {
                    ascii = false;
                    break;
                }
            }
            if (ascii) {
                for (; i < chars.length; ++i) {
                    if (chars[i] > 0x00FF) {
                        ascii = false;
                        break;
                    }
                }
            }
        }

        int minCapacity = (ascii ? len : len * 3)
                + off
                + 5 /*max str len*/
                + 1;

        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        if (ascii) {
            if (len <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (len + BC_STR_ASCII_FIX_MIN);
            } else if (len <= INT32_BYTE_MAX) {
                bytes[off] = BC_STR_ASCII;
                bytes[off + 1] = (byte) (BC_INT32_BYTE_ZERO + (len >> 8));
                bytes[off + 2] = (byte) (len);
                off += 3;
            } else {
                bytes[off++] = BC_STR_ASCII;
                off += writeInt32(bytes, off, len);
            }
            for (int i = 0; i < chars.length; i++) {
                bytes[off++] = (byte) chars[i];
            }
        } else {
            int maxSize = chars.length * 3;
            int lenByteCnt = sizeOfInt(maxSize);
            minCapacity = off + maxSize + lenByteCnt + 1;
            if (minCapacity > bytes.length) {
                bytes = grow(minCapacity);
            }
            int result = IOUtils.encodeUTF8(chars, 0, chars.length, bytes, off + lenByteCnt + 1);

            int utf8len = result - off - lenByteCnt - 1;
            int utf8lenByteCnt = sizeOfInt(utf8len);
            if (lenByteCnt != utf8lenByteCnt) {
                System.arraycopy(bytes, off + lenByteCnt + 1, bytes, off + utf8lenByteCnt + 1, utf8len);
            }
            bytes[off++] = BC_STR_UTF8;
            if (utf8len >= BC_INT32_NUM_MIN && utf8len <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) utf8len;
            } else if (utf8len >= INT32_BYTE_MIN && utf8len <= INT32_BYTE_MAX) {
                bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (utf8len >> 8));
                bytes[off + 1] = (byte) (utf8len);
                off += 2;
            } else {
                off += writeInt32(bytes, off, utf8len);
            }
            off += utf8len;
        }
        this.off = off;
    }

    public void writeString(String[] strings) {
        if (strings == null) {
            writeArrayNull();
            return;
        }

        startArray(strings.length);
        for (int i = 0; i < strings.length; i++) {
            String item = strings[i];
            if (item == null) {
                writeStringNull();
                continue;
            }
            writeString(item);
        }
    }

    @Override
    public void writeSymbol(String str) {
        if (str == null) {
            writeNull();
            return;
        }

        if (symbolTable != null) {
            int ordinal = symbolTable.getOrdinal(str);
            if (ordinal >= 0) {
                writeRaw(BC_SYMBOL);
                writeInt32(-ordinal);
                return;
            }
        }

        writeString(str);
    }

    @Override
    public void writeTypeName(String typeName) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off == bytes.length) {
            bytes = grow(off + 1);
        }
        bytes[off++] = BC_TYPED_ANY;

        long hash = Fnv.hashCode64(typeName);

        int symbol = -1;
        if (symbolTable != null) {
            symbol = symbolTable.getOrdinalByHashCode(hash);
            if (symbol == -1 && symbols != null) {
                symbol = symbols.get(hash);
            }
        } else if (symbols != null) {
            symbol = symbols.get(hash);
        }

        if (symbol == -1) {
            if (symbols == null) {
                symbols = new TLongIntHashMap();
            }
            symbols.put(hash, symbol = symbolIndex++);
        } else {
            if (off == bytes.length) {
                bytes = grow(off + 1);
            }

            this.off = off + writeInt32(bytes, off, symbol);
            return;
        }
        this.off = off;

        writeString(typeName);
        writeInt32(symbol);
    }

    @Override
    public boolean writeTypeName(byte[] typeName, long hash) {
        if (symbolTable != null) {
            int symbol = symbolTable.getOrdinalByHashCode(hash);
            if (symbol != -1) {
                return writeTypeNameSymbol(symbol);
            }
        }

        boolean symbolExists = false;
        int symbol;
        if (rootTypeNameHash == hash) {
            symbolExists = true;
            symbol = 0;
        } else if (symbols != null) {
            symbol = symbols.putIfAbsent(hash, symbolIndex);
            if (symbol != symbolIndex) {
                symbolExists = true;
            } else {
                symbolIndex++;
            }
        } else {
            symbol = symbolIndex++;
            if (symbol == 0) {
                rootTypeNameHash = hash;
            }
            if (symbol != 0 || (context.features & WriteNameAsSymbol.mask) != 0) {
                symbols = new TLongIntHashMap(hash, symbol);
            }
        }

        if (symbolExists) {
            writeTypeNameSymbol(-symbol);
            return false;
        }

        int off = this.off;
        int minCapacity = off + 2 + typeName.length;
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = BC_TYPED_ANY;
        System.arraycopy(typeName, 0, bytes, off, typeName.length);
        off += typeName.length;
        if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
            bytes[off++] = (byte) symbol;
        } else {
            off += writeInt32(bytes, off, symbol);
        }
        this.off = off;
        return false;
    }

    private boolean writeTypeNameSymbol(int symbol) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 7 > bytes.length) {
            bytes = grow(off + 7);
        }

        bytes[off++] = BC_TYPED_ANY;
        this.off = off + writeInt32(bytes, off, -symbol);
        return false;
    }

    static int sizeOfInt(int i) {
        if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
            return 1;
        }

        if (i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX) {
            return 2;
        }

        if (i >= INT32_SHORT_MIN && i <= INT32_SHORT_MAX) {
            return 3;
        }

        return 5;
    }

    public void writeString(List<String> list) {
        if (list == null) {
            writeArrayNull();
            return;
        }

        final int size = list.size();
        startArray(size);

        if (STRING_VALUE != null && STRING_CODER != null) {
            int off = this.off;
            byte[] bytes = this.bytes;
            final int LATIN = 0;
            boolean latinAll = true;
            for (int i = 0; i < list.size(); i++) {
                String str = list.get(i);
                if (str == null) {
                    if (off == bytes.length) {
                        bytes = grow(off + 1);
                    }
                    bytes[off++] = BC_NULL;
                    continue;
                }
                int coder = STRING_CODER.applyAsInt(str);
                if (coder != LATIN) {
                    latinAll = false;
                    break;
                }
                int strlen = str.length();
                if (off + strlen + 6 > bytes.length) {
                    bytes = grow(off + strlen + 6);
                }
                if (strlen <= STR_ASCII_FIX_LEN) {
                    bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                } else if (strlen <= INT32_BYTE_MAX) {
                    bytes[off] = BC_STR_ASCII;
                    bytes[off + 1] = (byte) (BC_INT32_BYTE_ZERO + (strlen >> 8));
                    bytes[off + 2] = (byte) (strlen);
                    off += 3;
                } else {
                    bytes[off++] = BC_STR_ASCII;
                    off += writeInt32(bytes, off, strlen);
                }
                byte[] value = STRING_VALUE.apply(str);
                System.arraycopy(value, 0, bytes, off, value.length);
                off += strlen;
            }
            if (latinAll) {
                this.off = off;
                return;
            }
        }

        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i);
            writeString(str);
        }
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeNull();
            return;
        }

        if (STRING_VALUE != null) {
            int coder = STRING_CODER.applyAsInt(str);
            byte[] value = STRING_VALUE.apply(str);

            if (coder == 0) {
                int off = this.off;
                int strlen = value.length;
                int minCapacity = value.length + off + 6;

                if (minCapacity - bytes.length > 0) {
                    ensureCapacity(minCapacity);
                }

                final byte[] bytes = this.bytes;
                if (strlen <= STR_ASCII_FIX_LEN) {
                    bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                } else if (strlen <= INT32_BYTE_MAX) {
                    putStringSizeSmall(bytes, off, strlen);
                    off += 3;
                } else {
                    off += putStringSizeLarge(bytes, off, strlen);
                }
                System.arraycopy(value, 0, bytes, off, value.length);
                this.off = off + strlen;
                return;
            } else {
                if (tryWriteStringUTF16(value)) {
                    return;
                }
            }
        }

        writeString(
                JDKUtils.getCharArray(str)
        );
    }

    public void writeStringUTF16(byte[] value) {
        int off = this.off;
        final int strlen = value.length;
        int minCapacity = off + strlen + 6;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = JDKUtils.BIG_ENDIAN ? BC_STR_UTF16BE : BC_STR_UTF16LE;
        off += writeInt32(bytes, off, strlen);
        System.arraycopy(value, 0, bytes, off, strlen);
        this.off = off + strlen;
    }

    protected boolean tryWriteStringUTF16(byte[] value) {
        int check_cnt = 128;
        if (check_cnt > value.length) {
            check_cnt = value.length;
        }
        if ((check_cnt & 1) == 1) {
            check_cnt -= 1;
        }

        int asciiCount = 0;
        for (int i = 0; i + 2 <= check_cnt; i += 2) {
            byte b0 = value[i];
            byte b1 = value[i + 1];
            if (b0 == 0 || b1 == 0) {
                asciiCount++;
            }
        }

        boolean utf16 = value.length != 0 && (asciiCount == 0 || (check_cnt >> 1) / asciiCount >= 3); // utf16字符占比>=1/3
        int off = this.off;
        int minCapacity = off + 6 + value.length * 2 + 1;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (!utf16) {
            int maxSize = value.length + (value.length >> 2);
            int lenByteCnt = sizeOfInt(maxSize);
            int result = IOUtils.encodeUTF8(value, 0, value.length, bytes, off + lenByteCnt + 1);
            int utf8len = result - off - lenByteCnt - 1;
            if (utf8len > value.length) {
                utf16 = true;
            } else if (result != -1) {
                this.off = off + writeUTF8(bytes, off, value, utf8len, asciiCount, lenByteCnt);
                return true;
            }
        }

        if (utf16) {
            this.off = off + writeUTF16(bytes, off, value);
            return true;
        }
        return false;
    }

    private static int writeUTF8(
            byte[] bytes,
            int off,
            byte[] value,
            int utf8len,
            int asciiCount,
            int lenByteCnt
    ) {
        final byte strtype;
        if (utf8len * 2 == value.length) {
            if (asciiCount <= STR_ASCII_FIX_LEN) {
                bytes[off] = (byte) (BC_STR_ASCII_FIX_MIN + utf8len);
                System.arraycopy(bytes, off + 1 + lenByteCnt, bytes, off + 1, utf8len);
                return utf8len + 1;
            }
            strtype = BC_STR_ASCII;
        } else {
            strtype = BC_STR_UTF8;
        }
        int utf8lenByteCnt = sizeOfInt(utf8len);
        if (lenByteCnt != utf8lenByteCnt) {
            System.arraycopy(bytes, off + lenByteCnt + 1, bytes, off + utf8lenByteCnt + 1, utf8len);
        }
        bytes[off] = strtype;
        return writeInt32(bytes, off + 1, utf8len) + utf8len + 1;
    }

    private static int writeUTF16(byte[] bytes, int off, byte[] value) {
        bytes[off] = JDKUtils.BIG_ENDIAN ? BC_STR_UTF16BE : BC_STR_UTF16LE;
        int size = writeInt32(bytes, off + 1, value.length);
        System.arraycopy(value, 0, bytes, off + size + 1, value.length);
        return value.length + size + 1;
    }

    void ensureCapacity(int minCapacity) {
        if (minCapacity >= bytes.length) {
            grow0(minCapacity);
        }
    }

    private byte[] grow(int minCapacity) {
        grow0(minCapacity);
        return bytes;
    }

    private void grow0(int minCapacity) {
        bytes = Arrays.copyOf(bytes, newCapacity(minCapacity, bytes.length));
    }

    @Override
    public void writeMillis(long millis) {
        int off = this.off;
        int minCapacity = off + 9;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (millis % 1000 == 0) {
            long seconds = (millis / 1000);
            if (seconds >= Integer.MIN_VALUE && seconds <= Integer.MAX_VALUE) {
                int secondsInt = (int) seconds;

                bytes[off] = BC_TIMESTAMP_SECONDS;
                IOUtils.putIntBE(
                        bytes,
                        off + 1,
                        secondsInt
                );
                this.off = off + 5;
                return;
            }

            if (seconds % 60 == 0) {
                long minutes = seconds / 60;
                if (minutes >= Integer.MIN_VALUE && minutes <= Integer.MAX_VALUE) {
                    int minutesInt = (int) minutes;
                    bytes[off] = BC_TIMESTAMP_MINUTES;
                    IOUtils.putIntBE(
                            bytes,
                            off + 1,
                            minutesInt
                    );
                    this.off = off + 5;
                    return;
                }
            }
        }

        bytes[off] = BC_TIMESTAMP_MILLIS;
        IOUtils.putLongBE(
                bytes,
                off + 1,
                millis
        );
        this.off = off + 9;
    }

    static final long WRITE_NUM_NULL_MASK = Feature.NullAsDefaultValue.mask | Feature.WriteNullNumberAsZero.mask;

    @Override
    public void writeInt64(Long i) {
        int minCapacity = off + 9;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        int off = this.off;
        if (i == null) {
            bytes[off++] = (this.context.features & WRITE_NUM_NULL_MASK) == 0
                    ? BC_NULL
                    : (byte) (BC_INT64_NUM_MIN - INT64_NUM_LOW_VALUE);
        } else {
            off = writeInt64(bytes, off, i);
        }

        this.off = off;
    }

    private static int writeInt64(byte[] bytes, int off, long i) {
        if (i >= INT64_NUM_LOW_VALUE && i <= INT64_NUM_HIGH_VALUE) {
            bytes[off++] = (byte) (BC_INT64_NUM_MIN + (i - INT64_NUM_LOW_VALUE));
        } else if (i >= INT64_BYTE_MIN && i <= INT64_BYTE_MAX) {
            IOUtils.putShortBE(bytes, off, (short) ((BC_INT64_BYTE_ZERO << 8) + i));
            off += 2;
        } else if (i >= INT64_SHORT_MIN && i <= INT64_SHORT_MAX) {
            bytes[off] = (byte) (BC_INT64_SHORT_ZERO + (i >> 16));
            IOUtils.putShortBE(bytes, off + 1, (short) i);
            off += 3;
        } else if (i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE) {
            bytes[off] = BC_INT64_INT;
            IOUtils.putIntBE(
                    bytes,
                    off + 1,
                    (int) i
            );
            off += 5;
        } else {
            bytes[off] = BC_INT64;
            IOUtils.putLongBE(bytes, off + 1, i);
            off += 9;
        }
        return off;
    }

    @Override
    public void writeInt64(long val) {
        int minCapacity = off + 9;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = writeInt64(bytes, this.off, val);
    }

    @Override
    public void writeInt64(long[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }

        int size = value.length;

        int off = this.off;
        int minCapacity = off + size * 9 + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off] = BC_ARRAY;
            off += writeInt32(bytes, off + 1, size) + 1;
        }

        for (int i = 0; i < value.length; i++) {
            off = writeInt64(bytes, off, value[i]);
        }
        this.off = off;
    }

    @Override
    public void writeListInt64(List<Long> values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        int size = values.size();

        int off = this.off;
        int minCapacity = off + size * 9 + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off] = BC_ARRAY;
            off += writeInt32(bytes, off + 1, size) + 1;
        }

        for (int i = 0; i < size; i++) {
            Long item = values.get(i);
            if (item == null) {
                bytes[off++] = BC_NULL;
                continue;
            }
            off = writeInt64(bytes, off, item);
        }
        this.off = off;
    }

    @Override
    public void writeFloat(float value) {
        int off = this.off;
        int minCapacity = off + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        int i = (int) value;
        if (i == value && value >= BC_INT32_NUM_MIN && value <= BC_INT32_NUM_MAX) {
            bytes[off] = BC_FLOAT_INT;
            bytes[off + 1] = (byte) i;
            off += 2;
        } else {
            bytes[off] = BC_FLOAT;
            i = Float.floatToIntBits(value);
            IOUtils.putIntBE(bytes, off + 1, i);
            off += 5;
        }
        this.off = off;
    }

    @Override
    public void writeFloat(float[] values) {
        if (values == null) {
            writeNull();
            return;
        }
        startArray(values.length);
        for (int i = 0; i < values.length; i++) {
            writeFloat(values[i]);
        }
        endArray();
    }

    @Override
    public void writeDouble(double value) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (value == 0 || value == 1) {
            if (off == bytes.length) {
                bytes = grow(off + 1);
            }
            bytes[off] = value == 0 ? BC_DOUBLE_NUM_0 : BC_DOUBLE_NUM_1;
            this.off = off + 1;
            return;
        }

        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            long longValue = (long) value;
            if (longValue == value) {
                if (off == bytes.length) {
                    bytes = grow(off + 1);
                }
                bytes[off] = BC_DOUBLE_LONG;
                this.off = off + 1;
                writeInt64(longValue);
                return;
            }
        }

        if (off + 9 > bytes.length) {
            bytes = grow(off + 9);
        }
        bytes[off] = BC_DOUBLE;
        long i = Double.doubleToLongBits(value);
        IOUtils.putLongBE(bytes, off + 1, i);
        this.off = off + 9;
    }

    @Override
    public void writeDouble(double[] values) {
        if (values == null) {
            writeNull();
            return;
        }
        startArray(values.length);
        for (int i = 0; i < values.length; i++) {
            writeDouble(values[i]);
        }
        endArray();
    }

    @Override
    public void writeInt16(short[] values) {
        if (values == null) {
            writeNull();
            return;
        }
        startArray(values.length);
        for (int i = 0; i < values.length; i++) {
            writeInt32(values[i]);
        }
        endArray();
    }

    @Override
    public void writeInt32(int[] values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        // inline startArray(value.length);
        int size = values.length;
        int minCapacity = off + 6 + values.length * 5;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off++] = BC_ARRAY;
            off += writeInt32(bytes, off, size);
        }

        for (int i = 0; i < values.length; i++) {
            int val = values[i];
            if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) val;
                continue;
            }

            if (val >= INT32_BYTE_MIN && val <= INT32_BYTE_MAX) {
                bytes[off++] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
                bytes[off++] = (byte) (val);
                continue;
            }

            if (val >= INT32_SHORT_MIN && val <= INT32_SHORT_MAX) {
                bytes[off] = (byte) (BC_INT32_SHORT_ZERO + (val >> 16));
                bytes[off + 1] = (byte) (val >> 8);
                bytes[off + 2] = (byte) (val);
                off += 3;
                continue;
            }

            bytes[off] = BC_INT32;
            IOUtils.putIntBE(bytes, off + 1, val);
            off += 5;
        }
        this.off = off;
    }

    @Override
    public void writeInt8(byte[] values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        int off = this.off;
        int size = values.length;
        int minCapacity = off + 6 + values.length * 2;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off++] = BC_ARRAY;
            off += writeInt32(bytes, off, size);
        }

        for (int val : values) {
            if (val < BC_INT32_NUM_MIN || val > BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
            }
            bytes[off++] = (byte) val;
        }
        this.off = off;
    }

    @Override
    public void writeInt8(byte val) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 2 > bytes.length) {
            bytes = grow(off + 2);
        }
        bytes[off] = BC_INT8;
        bytes[off + 1] = val;
        this.off = off + 2;
    }

    @Override
    public void writeInt16(short val) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 3 > bytes.length) {
            bytes = grow(off + 3);
        }
        bytes[off] = BC_INT16;
        bytes[off + 1] = (byte) (val >>> 8);
        bytes[off + 2] = (byte) val;
        this.off = off + 3;
    }

    @Override
    public void writeEnum(Enum e) {
        if (e == null) {
            writeNull();
            return;
        }

        if ((context.features & WRITE_ENUM_USING_STRING_MASK) != 0) {
            writeString(
                    (context.features & WriteEnumUsingToString.mask) != 0
                            ? e.toString()
                            : e.name()
            );
        } else {
            int ordinal = e.ordinal();
            byte[] bytes = this.bytes;
            int off = this.off;
            if (off + 5 > bytes.length) {
                bytes = grow(off + 5);
            }
            this.off = off + writeInt32(bytes, off, ordinal);
        }
    }

    @Override
    public void writeInt32(Integer i) {
        int minCapacity = off + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        int size;
        int off = this.off;
        if (i == null) {
            if ((this.context.features & (Feature.NullAsDefaultValue.mask | Feature.WriteNullNumberAsZero.mask)) == 0) {
                bytes[off] = BC_NULL;
            } else {
                bytes[off] = 0;
            }
            size = 1;
        } else {
            int val = i;
            if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
                bytes[off] = (byte) val;
                size = 1;
            } else if (val >= INT32_BYTE_MIN && val <= INT32_BYTE_MAX) {
                bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
                bytes[off + 1] = (byte) (val);
                size = 2;
            } else if (val >= INT32_SHORT_MIN && val <= INT32_SHORT_MAX) {
                bytes[off] = (byte) (BC_INT32_SHORT_ZERO + (val >> 16));
                bytes[off + 1] = (byte) (val >> 8);
                bytes[off + 2] = (byte) (val);
                size = 3;
            } else {
                bytes[off] = BC_INT32;
                IOUtils.putIntBE(bytes, off + 1, val);
                size = 5;
            }
        }
        this.off += size;
    }

    @Override
    public void writeInt32(int val) {
        int minCapacity = off + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        int size;
        int off = this.off;
        if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
            bytes[off] = (byte) val;
            size = 1;
        } else if (val >= INT32_BYTE_MIN && val <= INT32_BYTE_MAX) {
            bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
            bytes[off + 1] = (byte) (val);
            size = 2;
        } else if (val >= INT32_SHORT_MIN && val <= INT32_SHORT_MAX) {
            bytes[off] = (byte) (BC_INT32_SHORT_ZERO + (val >> 16));
            bytes[off + 1] = (byte) (val >> 8);
            bytes[off + 2] = (byte) (val);
            size = 3;
        } else {
            bytes[off] = BC_INT32;
            IOUtils.putIntBE(bytes, off + 1, val);
            size = 5;
        }
        this.off += size;
    }

    @Override
    public void writeListInt32(List<Integer> values) {
        if (values == null) {
            writeArrayNull();
            return;
        }

        int size = values.size();

        int off = this.off;
        int minCapacity = off + size * 5 + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off] = BC_ARRAY;
            off += writeInt32(bytes, off + 1, size) + 1;
        }

        for (int i = 0; i < size; i++) {
            Number item = values.get(i);
            if (item == null) {
                bytes[off++] = BC_NULL;
                continue;
            }

            int val = item.intValue();
            if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) val;
            } else if (val >= INT32_BYTE_MIN && val <= INT32_BYTE_MAX) {
                bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
                bytes[off + 1] = (byte) (val);
                off += 2;
            } else if (val >= INT32_SHORT_MIN && val <= INT32_SHORT_MAX) {
                bytes[off] = (byte) (BC_INT32_SHORT_ZERO + (val >> 16));
                bytes[off + 1] = (byte) (val >> 8);
                bytes[off + 2] = (byte) (val);
                off += 3;
            } else {
                bytes[off] = BC_INT32;
                IOUtils.putIntBE(bytes, off + 1, val);
                off += 5;
            }
        }
        this.off = off;
    }

    public static int writeInt32(byte[] bytes, int off, int val) {
        if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
            bytes[off] = (byte) val;
            return 1;
        } else if (val >= INT32_BYTE_MIN && val <= INT32_BYTE_MAX) {
            bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
            bytes[off + 1] = (byte) (val);
            return 2;
        } else if (val >= INT32_SHORT_MIN && val <= INT32_SHORT_MAX) {
            bytes[off] = (byte) (BC_INT32_SHORT_ZERO + (val >> 16));
            bytes[off + 1] = (byte) (val >> 8);
            bytes[off + 2] = (byte) (val);
            return 3;
        } else {
            bytes[off] = BC_INT32;
            IOUtils.putIntBE(bytes, off + 1, val);
            return 5;
        }
    }

    @Override
    public void writeArrayNull() {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }

        bytes[off++] = (this.context.features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
    }

    @Override
    public void writeRaw(String str) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void writeRaw(byte[] raw) {
        int off = this.off;
        int minCapacity = off + raw.length;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        System.arraycopy(raw, 0, bytes, off, raw.length);
        this.off = off + raw.length;
    }

    public void writeSymbol(int symbol) {
        int off = this.off;
        int minCapacity = off + 6;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = BC_SYMBOL;

        if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
            bytes[off++] = (byte) symbol;
        } else if (symbol >= INT32_BYTE_MIN && symbol <= INT32_BYTE_MAX) {
            bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (symbol >> 8));
            bytes[off + 1] = (byte) (symbol);
            off += 2;
        } else {
            off += writeInt32(bytes, off, symbol);
        }
        this.off = off;
    }

    @Override
    public void writeNameRaw(byte[] name, long nameHash) {
        int off = this.off;
        int minCapacity = off + 6 + name.length;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        int symbol;
        if (symbolTable == null
                || (symbol = symbolTable.getOrdinalByHashCode(nameHash)) == -1
        ) {
            if ((context.features & WriteNameAsSymbol.mask) == 0) {
                System.arraycopy(name, 0, bytes, off, name.length);
                this.off = off + name.length;
                return;
            }

            boolean symbolExists = false;
            if (symbols != null) {
                if ((symbol = symbols.putIfAbsent(nameHash, symbolIndex)) != symbolIndex) {
                    symbolExists = true;
                } else {
                    symbolIndex++;
                }
            } else {
                (symbols = new TLongIntHashMap())
                        .put(nameHash, symbol = symbolIndex++);
            }

            if (!symbolExists) {
                bytes[off++] = BC_SYMBOL;
                System.arraycopy(name, 0, bytes, off, name.length);
                off += name.length;

                if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
                    bytes[off++] = (byte) symbol;
                } else {
                    off += writeInt32(bytes, off, symbol);
                }
                this.off = off;
                return;
            }
            symbol = -symbol;
        }

        bytes[off++] = BC_SYMBOL;
        int intValue = -symbol;
        if (intValue >= BC_INT32_NUM_MIN && intValue <= BC_INT32_NUM_MAX) {
            bytes[off++] = (byte) intValue;
        } else {
            off += writeInt32(bytes, off, intValue);
        }
        this.off = off;
    }

    @Override
    public void writeLocalDate(LocalDate date) {
        if (date == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 5 > bytes.length) {
            bytes = grow(off + 5);
        }

        bytes[off] = BC_LOCAL_DATE;
        int year = date.getYear();
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) date.getMonthValue();
        bytes[off + 4] = (byte) date.getDayOfMonth();
        this.off = off + 5;
    }

    @Override
    public void writeLocalTime(LocalTime time) {
        if (time == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 9 > bytes.length) {
            bytes = grow(off + 9);
        }

        bytes[off] = BC_LOCAL_TIME;
        bytes[off + 1] = (byte) time.getHour();
        bytes[off + 2] = (byte) time.getMinute();
        bytes[off + 3] = (byte) time.getSecond();
        off += 4;

        this.off = off + writeInt32(bytes, off, time.getNano());
    }

    @Override
    public void writeLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 13 > bytes.length) {
            bytes = grow(off + 13);
        }

        bytes[off] = BC_LOCAL_DATETIME;
        int year = dateTime.getYear();
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) dateTime.getMonthValue();
        bytes[off + 4] = (byte) dateTime.getDayOfMonth();
        bytes[off + 5] = (byte) dateTime.getHour();
        bytes[off + 6] = (byte) dateTime.getMinute();
        bytes[off + 7] = (byte) dateTime.getSecond();
        off += 8;

        this.off = off + writeInt32(bytes, off, dateTime.getNano());
    }

    @Override
    public void writeZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 13 > bytes.length) {
            bytes = grow(off + 13);
        }

        bytes[off] = BC_TIMESTAMP_WITH_TIMEZONE;
        int year = dateTime.getYear();
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) dateTime.getMonthValue();
        bytes[off + 4] = (byte) dateTime.getDayOfMonth();
        bytes[off + 5] = (byte) dateTime.getHour();
        bytes[off + 6] = (byte) dateTime.getMinute();
        bytes[off + 7] = (byte) dateTime.getSecond();
        off += 8;

        this.off = off + writeInt32(bytes, off, dateTime.getNano());

        ZoneId zoneId = dateTime.getZone();
        String zoneIdStr = zoneId.getId();
        if (zoneIdStr.equals(SHANGHAI_ZONE_ID_NAME)) {
            writeRaw(SHANGHAI_ZONE_ID_NAME_BYTES);
        } else {
            writeString(zoneIdStr);
        }
    }

    @Override
    public void writeOffsetDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        String zoneIdStr = dateTime.getOffset().getId();
        int strlen = zoneIdStr.length();

        int off = this.off;
        byte[] bytes = this.bytes;
        int minCapacity = off + 14 + strlen;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        bytes[off] = BC_TIMESTAMP_WITH_TIMEZONE;
        int year = dateTime.getYear();
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) dateTime.getMonthValue();
        bytes[off + 4] = (byte) dateTime.getDayOfMonth();
        bytes[off + 5] = (byte) dateTime.getHour();
        bytes[off + 6] = (byte) dateTime.getMinute();
        bytes[off + 7] = (byte) dateTime.getSecond();
        off += 8;

        off += writeInt32(bytes, off, dateTime.getNano());

        bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
        zoneIdStr.getBytes(0, strlen, bytes, off);
        this.off = off + strlen;
    }

    @Override
    public void writeOffsetTime(OffsetTime offsetTime) {
        if (offsetTime == null) {
            writeNull();
            return;
        }

        writeOffsetDateTime(
                OffsetDateTime.of(DateUtils.LOCAL_DATE_19700101, offsetTime.toLocalTime(), offsetTime.getOffset())
        );
    }

    @Override
    public void writeInstant(Instant instant) {
        if (instant == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 15 > bytes.length) {
            bytes = grow(off + 15);
        }

        bytes[off] = BC_TIMESTAMP;
        off = writeInt64(bytes, off + 1, instant.getEpochSecond());
        this.off = off + writeInt32(bytes, off, instant.getNano());
    }

    @Override
    public void writeUUID(UUID value) {
        if (value == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 18 > bytes.length) {
            bytes = grow(off + 18);
        }

        bytes[off] = BC_BINARY;
        bytes[off + 1] = BC_INT32_NUM_16;
        IOUtils.putLongBE(bytes, off + 2, value.getMostSignificantBits());
        IOUtils.putLongBE(bytes, off + 10, value.getLeastSignificantBits());
        this.off = off + 18;
    }

    @Override
    public void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNull();
            return;
        }

        int off = this.off;
        byte[] bytes = this.bytes;
        if (isInt64(value)) {
            if (off + 10 > bytes.length) {
                bytes = grow(off + 10);
            }
            bytes[off] = BC_BIGINT_LONG;
            this.off = writeInt64(bytes, off + 1, value.longValue());
            return;
        }

        byte[] valueBytes = value.toByteArray();
        int minCapacity = off + 5 + valueBytes.length;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        bytes[off++] = BC_BIGINT;
        off += writeInt32(bytes, off, valueBytes.length);
        System.arraycopy(valueBytes, 0, bytes, off, valueBytes.length);
        this.off = off + valueBytes.length;
    }

    @Override
    public void writeBinary(byte[] binary) {
        if (binary == null) {
            writeNull();
            return;
        }

        int off = this.off;
        int len = binary.length;
        int minCapacity = off + 6 + len;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        bytes[off++] = BC_BINARY;
        off += writeInt32(bytes, off, len);

        System.arraycopy(binary, 0, bytes, off, len);
        this.off = off + len;
    }

    @Override
    public void writeDecimal(BigDecimal value, long features, DecimalFormat format) {
        if (value == null) {
            writeNull();
            return;
        }

        int precision = value.precision();
        int scale = value.scale();

        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 15 > bytes.length) {
            bytes = grow(off + 15);
        }
        if (precision < 19 && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1) {
            long intCompact = UNSAFE.getLong(value, FIELD_DECIMAL_INT_COMPACT_OFFSET);
            if (scale == 0) {
                bytes[off] = BC_DECIMAL_LONG;
                this.off = writeInt64(bytes, off + 1, intCompact);
                return;
            }

            bytes[off++] = BC_DECIMAL;
            off += writeInt32(bytes, off, scale);
            if (intCompact >= Integer.MIN_VALUE && intCompact <= Integer.MAX_VALUE) {
                off += writeInt32(bytes, off, (int) intCompact);
            } else {
                off = writeInt64(bytes, off, intCompact);
            }
            this.off = off;
            return;
        }

        BigInteger unscaledValue = value.unscaledValue();
        if (scale == 0
                && isInt64(unscaledValue)) {
            bytes[off] = BC_DECIMAL_LONG;
            this.off = writeInt64(bytes, off + 1, unscaledValue.longValue());
            return;
        }

        bytes[off++] = BC_DECIMAL;
        off += writeInt32(bytes, off, scale);

        if (isInt32(unscaledValue)) {
            off += writeInt32(bytes, off, unscaledValue.intValue());
        } else if (isInt64(unscaledValue)) {
            off = writeInt64(bytes, off, unscaledValue.longValue());
        } else {
            this.off = off;
            writeBigInt(unscaledValue, 0);
            return;
        }
        this.off = off;
    }

    @Override
    public void writeBool(boolean value) {
        int off = this.off;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        this.bytes[off] = value ? BC_TRUE : BC_FALSE;
        this.off = off + 1;
    }

    @Override
    public void writeBool(boolean[] values) {
        if (values == null) {
            writeNull();
            return;
        }

        startArray(values.length);
        for (int i = 0; i < values.length; i++) {
            writeBool(values[i]);
        }
        endArray();
    }

    @Override
    public void writeReference(String path) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off == bytes.length) {
            bytes = grow(off + 1);
        }
        bytes[off] = BC_REFERENCE;
        this.off = off + 1;
        writeString(path == this.lastReference ? "#-1" : path);
        this.lastReference = path;
    }

    @Override
    public void writeDateTime14(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second
    ) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 9 > bytes.length) {
            bytes = grow(off + 9);
        }

        bytes[off] = BC_LOCAL_DATETIME;
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) month;
        bytes[off + 4] = (byte) dayOfMonth;
        bytes[off + 5] = (byte) hour;
        bytes[off + 6] = (byte) minute;
        bytes[off + 7] = (byte) second;
        bytes[off + 8] = BC_INT32_NUM_0;
        this.off = off + 9;
    }

    @Override
    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second
    ) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 9 > bytes.length) {
            bytes = grow(off + 9);
        }

        bytes[off] = BC_LOCAL_DATETIME;
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) month;
        bytes[off + 4] = (byte) dayOfMonth;
        bytes[off + 5] = (byte) hour;
        bytes[off + 6] = (byte) minute;
        bytes[off + 7] = (byte) second;
        bytes[off + 8] = BC_INT32_NUM_0;
        this.off = off + 9;
    }

    @Override
    public void writeDateTimeISO8601(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second,
            int millis,
            int offsetSeconds,
            boolean timeZone
    ) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void writeDateYYYMMDD8(int year, int month, int dayOfMonth) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 5 > bytes.length) {
            bytes = grow(off + 5);
        }

        bytes[off] = BC_LOCAL_DATE;
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) month;
        bytes[off + 4] = (byte) dayOfMonth;
        this.off = off + 5;
    }

    @Override
    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        writeDateYYYMMDD8(year, month, dayOfMonth);
    }

    @Override
    public void writeTimeHHMMSS8(int hour, int minute, int second) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void writeBase64(byte[] bytes) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void writeHex(byte[] bytes) {
        writeBinary(bytes);
    }

    @Override
    public void writeRaw(char ch) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void writeNameRaw(byte[] bytes) {
        writeRaw(bytes);
    }

    @Override
    public void writeName2Raw(long name) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 2;
    }

    @Override
    public void writeName3Raw(long name) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 3;
    }

    @Override
    public void writeName4Raw(long name) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 4;
    }

    @Override
    public void writeName5Raw(long name) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 5;
    }

    @Override
    public void writeName6Raw(long name) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 6;
    }

    @Override
    public void writeName7Raw(long name) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 7;
    }

    @Override
    public void writeName8Raw(long name) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 8 > bytes.length) {
            bytes = grow(off + 8);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name);
        this.off = off + 8;
    }

    @Override
    public void writeName9Raw(long name0, int name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 12 > bytes.length) {
            bytes = grow(off + 12);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 9;
    }

    @Override
    public void writeName10Raw(long name0, long name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 16 > bytes.length) {
            bytes = grow(off + 16);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 10;
    }

    @Override
    public void writeName11Raw(long name0, long name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 16 > bytes.length) {
            bytes = grow(off + 16);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 11;
    }

    @Override
    public void writeName12Raw(long name0, long name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 16 > bytes.length) {
            bytes = grow(off + 16);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 12;
    }

    @Override
    public void writeName13Raw(long name0, long name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 16 > bytes.length) {
            bytes = grow(off + 16);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 13;
    }

    @Override
    public void writeName14Raw(long name0, long name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 16 > bytes.length) {
            bytes = grow(off + 16);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 14;
    }

    @Override
    public void writeName15Raw(long name0, long name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 16 > bytes.length) {
            bytes = grow(off + 16);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 15;
    }

    @Override
    public void writeName16Raw(long name0, long name1) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 16 > bytes.length) {
            bytes = grow(off + 16);
        }

        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off, name0);
        UNSAFE.putLong(bytes, ARRAY_BYTE_BASE_OFFSET + off + 8, name1);
        this.off = off + 16;
    }

    @Override
    public void writeNameRaw(char[] chars) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void writeNameRaw(char[] bytes, int offset, int len) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void writeColon() {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void write(List array) {
        if (array == null) {
            writeArrayNull();
            return;
        }

        final int size = array.size();
        startArray(size);
        for (int i = 0; i < array.size(); i++) {
            Object item = array.get(i);
            writeAny(item);
        }
    }

    @Override
    public void write(Map map) {
        if (map == null) {
            writeNull();
            return;
        }

        startObject();
        for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
            writeAny(entry.getKey());
            writeAny(entry.getValue());
        }
        endObject();
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, off);
    }

    @Override
    public int size() {
        return off;
    }

    @Override
    public byte[] getBytes(Charset charset) {
        throw new JSONException("not support operator");
    }

    @Override
    public int flushTo(OutputStream to) throws IOException {
        int len = off;
        to.write(bytes, 0, off);
        off = 0;
        return len;
    }

    @Override
    public int flushTo(OutputStream out, Charset charset) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public String toString() {
        if (off == 0) {
            return "<empty>";
        }

        byte[] jsonbBytes = getBytes();
        JSONReader reader = JSONReader.ofJSONB(jsonbBytes);
        JSONWriter writer = JSONWriter.of();
        try {
            Object object = reader.readAny();
            writer.writeAny(object);
            return writer.toString();
        } catch (Exception ex) {
            return JSONB.typeName(bytes[0]) + ", bytes length " + off;
        }
    }

    @Override
    public void println() {
    }
}
