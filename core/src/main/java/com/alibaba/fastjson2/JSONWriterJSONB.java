package com.alibaba.fastjson2;

import com.alibaba.fastjson2.internal.trove.map.hash.TLongIntHashMap;
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
import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNameAsSymbol;
import static com.alibaba.fastjson2.util.DateUtils.OFFSET_8_ZONE_ID_NAME;
import static com.alibaba.fastjson2.util.DateUtils.SHANGHAI_ZONE_ID_NAME;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.*;

final class JSONWriterJSONB
        extends JSONWriter {
    // optimize for write ZonedDateTime
    static final byte[] SHANGHAI_ZONE_ID_NAME_BYTES = JSONB.toBytes(SHANGHAI_ZONE_ID_NAME);
    static final byte[] OFFSET_8_ZONE_ID_NAME_BYTES = JSONB.toBytes(OFFSET_8_ZONE_ID_NAME);

    private final CacheItem cacheItem;
    private byte[] bytes;
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
            ensureCapacity(off + 1);
        }
        bytes[off] = BC_OBJECT;
        this.off = off + 1;
    }

    @Override
    public void endObject() {
        level--;
        int off = this.off;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
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
            ensureCapacity(off + 1);
        }

        final byte[] bytes = this.bytes;
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
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }

        final byte[] bytes = this.bytes;
        boolean tinyInt = size <= ARRAY_FIX_LEN;
        bytes[off] = tinyInt ? (byte) (BC_ARRAY_FIX_MIN + size) : BC_ARRAY;
        this.off = off + 1;
        if (!tinyInt) {
            writeInt32(size);
        }
    }

    @Override
    public void writeRaw(byte b) {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = b;
    }

    @Override
    public void writeChar(char ch) {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
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
            ensureCapacity(off + 1);
        }
        bytes[off++] = BC_NULL;
    }

    @Override
    public void writeStringNull() {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
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
    public void writeString(char[] chars, int off, int len, boolean quote) {
        if (chars == null) {
            writeNull();
            return;
        }

        boolean ascii = true;
        for (int i = 0; i < len; ++i) {
            if (chars[i + off] > 0x00FF) {
                ascii = false;
                break;
            }
        }

        if (ascii) {
            if (len <= STR_ASCII_FIX_LEN) {
                bytes[this.off++] = (byte) (len + BC_STR_ASCII_FIX_MIN);
            } else {
                bytes[this.off++] = BC_STR_ASCII;
                writeInt32(len);
            }
            for (int i = 0; i < len; ++i) {
                bytes[this.off++] = (byte) chars[off + i];
            }
            return;
        }

        writeString(new String(chars, off, len));
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
        bytes[off + 2] = (byte) (strlen >>> 24);
        bytes[off + 3] = (byte) (strlen >>> 16);
        bytes[off + 4] = (byte) (strlen >>> 8);
        bytes[off + 5] = (byte) strlen;
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
        if (chars.length < STR_ASCII_FIX_LEN) {
            int minCapacity = off + 1 + strlen;
            if (minCapacity - bytes.length > 0) {
                ensureCapacity(minCapacity);
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

        if (minCapacity - bytes.length > 0) {
            ensureCapacity(minCapacity);
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
            ensureCapacity(off + maxSize + lenByteCnt + 1);
            int result = IOUtils.encodeUTF8(chars, 0, chars.length, bytes, off + lenByteCnt + 1);

            int utf8len = result - off - lenByteCnt - 1;
            int utf8lenByteCnt = sizeOfInt(utf8len);
            if (lenByteCnt != utf8lenByteCnt) {
                System.arraycopy(bytes, off + lenByteCnt + 1, bytes, off + utf8lenByteCnt + 1, utf8len);
            }
            final byte[] bytes = this.bytes;
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

        boolean ascii = true;

        if (len < STR_ASCII_FIX_LEN) {
            final int mark = this.off;

            int minCapacity = this.off + 1 + len;
            if (minCapacity - bytes.length > 0) {
                ensureCapacity(minCapacity);
            }

            bytes[this.off++] = (byte) (len + BC_STR_ASCII_FIX_MIN);
            for (int i = charsOff; i < len; i++) {
                char ch = chars[i];
                if (ch > 0x00FF) {
                    ascii = false;
                    break;
                }
                bytes[this.off++] = (byte) ch;
            }

            if (ascii) {
                return;
            }

            this.off = mark;
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
                + this.off
                + 5 /*max str len*/
                + 1;

        if (minCapacity - bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        if (ascii) {
            byte[] bytes = this.bytes;
            if (len <= STR_ASCII_FIX_LEN) {
                bytes[this.off++] = (byte) (len + BC_STR_ASCII_FIX_MIN);
            } else if (len <= INT32_BYTE_MAX) {
                int off = this.off;
                bytes[off] = BC_STR_ASCII;
                bytes[off + 1] = (byte) (BC_INT32_BYTE_ZERO + (len >> 8));
                bytes[off + 2] = (byte) (len);
                this.off += 3;
            } else {
                bytes[this.off++] = BC_STR_ASCII;
                writeInt32(len);
            }
            for (int i = 0; i < chars.length; i++) {
                bytes[this.off++] = (byte) chars[i];
            }
        } else {
            int maxSize = chars.length * 3;
            int lenByteCnt = sizeOfInt(maxSize);
            ensureCapacity(this.off + maxSize + lenByteCnt + 1);
            int result = IOUtils.encodeUTF8(chars, 0, chars.length, bytes, this.off + lenByteCnt + 1);

            int utf8len = result - this.off - lenByteCnt - 1;
            int utf8lenByteCnt = sizeOfInt(utf8len);
            if (lenByteCnt != utf8lenByteCnt) {
                System.arraycopy(bytes, this.off + lenByteCnt + 1, bytes, this.off + utf8lenByteCnt + 1, utf8len);
            }
            bytes[this.off++] = BC_STR_UTF8;
            if (utf8len >= BC_INT32_NUM_MIN && utf8len <= BC_INT32_NUM_MAX) {
                bytes[this.off++] = (byte) utf8len;
            } else if (utf8len >= INT32_BYTE_MIN && utf8len <= INT32_BYTE_MAX) {
                bytes[this.off] = (byte) (BC_INT32_BYTE_ZERO + (utf8len >> 8));
                bytes[this.off + 1] = (byte) (utf8len);
                this.off += 2;
            } else {
                writeInt32(utf8len);
            }
            this.off += utf8len;
        }
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
                if (isEnabled(Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) {
                    writeString("");
                } else {
                    writeNull();
                }
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
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        this.bytes[off++] = BC_TYPED_ANY;

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
                ensureCapacity(off + 1);
            }

            writeInt32(symbol);
            return;
        }

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
            bytes[off] = (byte) symbol;
            this.off = off + 1;
        } else {
            this.off = off;
            writeInt32(symbol);
        }

        return false;
    }

    private boolean writeTypeNameSymbol(int symbol) {
        int off = this.off;
        if (off + 2 >= bytes.length) {
            ensureCapacity(off + 2);
        }

        this.bytes[off] = BC_TYPED_ANY;
        this.off = off + 1;
        writeInt32(-symbol);
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
            int mark = off;
            final int LATIN = 0;
            boolean latinAll = true;
            for (int i = 0; i < list.size(); i++) {
                String str = list.get(i);
                if (str == null) {
                    writeNull();
                }
                int coder = STRING_CODER.applyAsInt(str);
                if (coder != LATIN) {
                    latinAll = false;
                    off = mark;
                    break;
                }
                int strlen = str.length();
                if (strlen + 3 > bytes.length) {
                    ensureCapacity(strlen + 3);
                }
                if (strlen <= STR_ASCII_FIX_LEN) {
                    bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                } else if (strlen <= INT32_BYTE_MAX) {
                    int off = this.off;
                    bytes[off] = BC_STR_ASCII;
                    bytes[off + 1] = (byte) (BC_INT32_BYTE_ZERO + (strlen >> 8));
                    bytes[off + 2] = (byte) (strlen);
                    this.off += 3;
                } else {
                    bytes[off++] = BC_STR_ASCII;
                    writeInt32(strlen);
                }
                byte[] value = STRING_VALUE.apply(str);
                System.arraycopy(value, 0, bytes, off, value.length);
                off += strlen;
            }
            if (latinAll) {
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
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = JDKUtils.BIG_ENDIAN ? BC_STR_UTF16BE : BC_STR_UTF16LE;
        off += writeInt32(bytes, off, strlen);
        System.arraycopy(value, 0, bytes, off, strlen);
        this.off = off + strlen;
    }

    private boolean tryWriteStringUTF16(byte[] value) {
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
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
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
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity > maxArraySize) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
    }

    @Override
    public void writeMillis(long millis) {
        int off = this.off;
        int minCapacity = off + 9;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (millis % 1000 == 0) {
            long seconds = (millis / 1000);
            if (seconds >= Integer.MIN_VALUE && seconds <= Integer.MAX_VALUE) {
                int secondsInt = (int) seconds;

                bytes[off] = BC_TIMESTAMP_SECONDS;
                bytes[off + 1] = (byte) (secondsInt >>> 24);
                bytes[off + 2] = (byte) (secondsInt >>> 16);
                bytes[off + 3] = (byte) (secondsInt >>> 8);
                bytes[off + 4] = (byte) secondsInt;
                this.off = off + 5;
                return;
            }

            if (seconds % 60 == 0) {
                long minutes = seconds / 60;
                if (minutes >= Integer.MIN_VALUE && minutes <= Integer.MAX_VALUE) {
                    int minutesInt = (int) minutes;
                    bytes[off] = BC_TIMESTAMP_MINUTES;
                    bytes[off + 1] = (byte) (minutesInt >>> 24);
                    bytes[off + 2] = (byte) (minutesInt >>> 16);
                    bytes[off + 3] = (byte) (minutesInt >>> 8);
                    bytes[off + 4] = (byte) minutesInt;
                    this.off = off + 5;
                    return;
                }
            }
        }

        bytes[off] = BC_TIMESTAMP_MILLIS;
        bytes[off + 1] = (byte) (millis >>> 56);
        bytes[off + 2] = (byte) (millis >>> 48);
        bytes[off + 3] = (byte) (millis >>> 40);
        bytes[off + 4] = (byte) (millis >>> 32);
        bytes[off + 5] = (byte) (millis >>> 24);
        bytes[off + 6] = (byte) (millis >>> 16);
        bytes[off + 7] = (byte) (millis >>> 8);
        bytes[off + 8] = (byte) millis;
        this.off = off + 9;
    }

    @Override
    public void writeInt64(Long i) {
        int minCapacity = off + 9;
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        int off = this.off;
        int size;
        if (i == null) {
            if ((this.context.features & (Feature.NullAsDefaultValue.mask | Feature.WriteNullNumberAsZero.mask)) == 0) {
                bytes[off] = BC_NULL;
            } else {
                bytes[off] = (byte) (BC_INT64_NUM_MIN + (0 - INT64_NUM_LOW_VALUE));
            }
            size = 1;
        } else {
            long val = i.longValue();
            if (val >= INT64_NUM_LOW_VALUE && val <= INT64_NUM_HIGH_VALUE) {
                bytes[off] = (byte) (BC_INT64_NUM_MIN + (val - INT64_NUM_LOW_VALUE));
                size = 1;
            } else if (val >= INT64_BYTE_MIN && val <= INT64_BYTE_MAX) {
                bytes[off] = (byte) (BC_INT64_BYTE_ZERO + (val >> 8));
                bytes[off + 1] = (byte) (val);
                size = 2;
            } else if (val >= INT64_SHORT_MIN && val <= INT64_SHORT_MAX) {
                bytes[off] = (byte) (BC_INT64_SHORT_ZERO + (val >> 16));
                bytes[off + 1] = (byte) (val >> 8);
                bytes[off + 2] = (byte) (val);
                size = 3;
            } else if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                bytes[off] = BC_INT64_INT;
                bytes[off + 1] = (byte) (val >>> 24);
                bytes[off + 2] = (byte) (val >>> 16);
                bytes[off + 3] = (byte) (val >>> 8);
                bytes[off + 4] = (byte) val;
                size = 5;
            } else {
                bytes[off] = BC_INT64;
                putLong(bytes, off + 1, val);
                size = 9;
            }
        }

        this.off = off + size;
    }

    @Override
    public void writeInt64(long val) {
        int minCapacity = off + 9;
        if (minCapacity > bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        int off = this.off;
        int size;
        if (val >= INT64_NUM_LOW_VALUE && val <= INT64_NUM_HIGH_VALUE) {
            bytes[off] = (byte) (BC_INT64_NUM_MIN + (val - INT64_NUM_LOW_VALUE));
            size = 1;
        } else if (val >= INT64_BYTE_MIN && val <= INT64_BYTE_MAX) {
            bytes[off] = (byte) (BC_INT64_BYTE_ZERO + (val >> 8));
            bytes[off + 1] = (byte) (val);
            size = 2;
        } else if (val >= INT64_SHORT_MIN && val <= INT64_SHORT_MAX) {
            bytes[off] = (byte) (BC_INT64_SHORT_ZERO + (val >> 16));
            bytes[off + 1] = (byte) (val >> 8);
            bytes[off + 2] = (byte) (val);
            size = 3;
        } else if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
            bytes[off] = BC_INT64_INT;
            bytes[off + 1] = (byte) (val >>> 24);
            bytes[off + 2] = (byte) (val >>> 16);
            bytes[off + 3] = (byte) (val >>> 8);
            bytes[off + 4] = (byte) val;
            size = 5;
        } else {
            bytes[off] = BC_INT64;
            putLong(bytes, off + 1, val);
            size = 9;
        }
        this.off = off + size;
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
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off] = BC_ARRAY;
            off += writeInt32(bytes, off + 1, size) + 1;
        }

        for (int i = 0; i < value.length; i++) {
            long val = value[i];
            if (val >= INT64_NUM_LOW_VALUE && val <= INT64_NUM_HIGH_VALUE) {
                bytes[off++] = (byte) (BC_INT64_NUM_MIN + (val - INT64_NUM_LOW_VALUE));
                continue;
            }

            if (val >= INT64_BYTE_MIN && val <= INT64_BYTE_MAX) {
                bytes[off] = (byte) (BC_INT64_BYTE_ZERO + (val >> 8));
                bytes[off + 1] = (byte) (val);
                off += 2;
                continue;
            }

            if (val >= INT64_SHORT_MIN && val <= INT64_SHORT_MAX) {
                bytes[off] = (byte) (BC_INT64_SHORT_ZERO + (val >> 16));
                bytes[off + 1] = (byte) (val >> 8);
                bytes[off + 2] = (byte) (val);
                off += 3;
                continue;
            }

            if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                bytes[off] = BC_INT64_INT;
                putInt(bytes, off + 1, (int) val);
                off += 5;
                continue;
            }

            bytes[off] = BC_INT64;
            putLong(bytes, off + 1, val);
            off += 9;
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
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
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

            long val = item.longValue();
            if (val >= INT64_NUM_LOW_VALUE && val <= INT64_NUM_HIGH_VALUE) {
                bytes[off++] = (byte) (BC_INT64_NUM_MIN + (val - INT64_NUM_LOW_VALUE));
                continue;
            }

            if (val >= INT64_BYTE_MIN && val <= INT64_BYTE_MAX) {
                bytes[off] = (byte) (BC_INT64_BYTE_ZERO + (val >> 8));
                bytes[off + 1] = (byte) (val);
                off += 2;
                continue;
            }

            if (val >= INT64_SHORT_MIN && val <= INT64_SHORT_MAX) {
                bytes[off] = (byte) (BC_INT64_SHORT_ZERO + (val >> 16));
                bytes[off + 1] = (byte) (val >> 8);
                bytes[off + 2] = (byte) (val);
                off += 3;
                continue;
            }

            if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                bytes[off] = BC_INT64_INT;
                putInt(bytes, off + 1, (int) val);
                off += 5;
                continue;
            }

            bytes[off] = BC_INT64;
            putLong(bytes, off + 1, val);
            off += 9;
        }
        this.off = off;
    }

    private static void putInt(byte[] bytes, int off, int val) {
        bytes[off] = (byte) (val >>> 24);
        bytes[off + 1] = (byte) (val >>> 16);
        bytes[off + 2] = (byte) (val >>> 8);
        bytes[off + 3] = (byte) val;
    }

    private static void putLong(byte[] bytes, int off, long val) {
        bytes[off] = (byte) (val >>> 56);
        bytes[off + 1] = (byte) (val >>> 48);
        bytes[off + 2] = (byte) (val >>> 40);
        bytes[off + 3] = (byte) (val >>> 32);
        bytes[off + 4] = (byte) (val >>> 24);
        bytes[off + 5] = (byte) (val >>> 16);
        bytes[off + 6] = (byte) (val >>> 8);
        bytes[off + 7] = (byte) val;
    }

    @Override
    public void writeFloat(float value) {
        int off = this.off;
        int minCapacity = off + 5;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        int i = (int) value;
        if (i == value && value >= INT32_SHORT_MIN && value <= INT32_SHORT_MAX) {
            bytes[off] = BC_FLOAT_INT;
            off += writeInt32(bytes, off + 1, i) + 1;
        } else {
            bytes[off] = BC_FLOAT;
            i = Float.floatToIntBits(value);
            bytes[off + 1] = (byte) (i >>> 24);
            bytes[off + 2] = (byte) (i >>> 16);
            bytes[off + 3] = (byte) (i >>> 8);
            bytes[off + 4] = (byte) i;
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
        if (value == 0) {
            ensureCapacity(off + 1);
            bytes[off++] = BC_DOUBLE_NUM_0;
            return;
        }

        int off = this.off;
        if (value == 1) {
            ensureCapacity(off + 1);
            bytes[off] = BC_DOUBLE_NUM_1;
            this.off = off + 1;
            return;
        }

        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            long longValue = (long) value;
            if (longValue == value) {
                ensureCapacity(off + 1);
                bytes[off] = BC_DOUBLE_LONG;
                this.off = off + 1;
                writeInt64(longValue);
                return;
            }
        }

        ensureCapacity(off + 9);
        final byte[] bytes = this.bytes;
        bytes[off] = BC_DOUBLE;
        long i = Double.doubleToLongBits(value);
        bytes[off + 1] = (byte) (i >>> 56);
        bytes[off + 2] = (byte) (i >>> 48);
        bytes[off + 3] = (byte) (i >>> 40);
        bytes[off + 4] = (byte) (i >>> 32);
        bytes[off + 5] = (byte) (i >>> 24);
        bytes[off + 6] = (byte) (i >>> 16);
        bytes[off + 7] = (byte) (i >>> 8);
        bytes[off + 8] = (byte) i;
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

        // inline startArray(value.length);
        int size = values.length;
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }

        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off++] = BC_ARRAY;
            writeInt32(size);
        }

        int off = this.off;
        int minCapacity = off + values.length * 5;
        if (minCapacity - bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
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
            putInt(bytes, off + 1, val);
            off += 5;
        }
        this.off = off;
    }

    @Override
    public void writeInt8(byte val) {
        int off = this.off;
        int minCapacity = off + 2;
        if (minCapacity - bytes.length > 0) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off] = BC_INT8;
        bytes[off + 1] = val;
        this.off = off + 2;
    }

    @Override
    public void writeInt16(short val) {
        int off = this.off;
        int minCapacity = off + 3;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }
        final byte[] bytes = this.bytes;
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

        if ((context.features & Feature.WriteEnumUsingToString.mask) != 0) {
            writeString(e.toString());
        } else if ((context.features & Feature.WriteEnumsUsingName.mask) != 0) {
            writeString(e.name());
        } else {
            int val = e.ordinal();
            if (val <= BC_INT32_NUM_MAX) {
                if (off == bytes.length) {
                    ensureCapacity(off + 1);
                }

                bytes[off++] = (byte) val;
                return;
            }
            writeInt32(val);
        }
    }

    @Override
    public void writeInt32(Integer i) {
        int minCapacity = off + 5;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
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
            int val = i.intValue();
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
                putInt(bytes, off + 1, val);
                size = 5;
            }
        }
        this.off += size;
    }

    @Override
    public void writeInt32(int val) {
        int minCapacity = off + 5;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
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
            putInt(bytes, off + 1, val);
            size = 5;
        }
        this.off += size;
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
            putInt(bytes, off + 1, val);
            return 5;
        }
    }

    @Override
    public void writeArrayNull() {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }

        if ((this.context.features & (Feature.NullAsDefaultValue.mask | Feature.WriteNullListAsEmpty.mask)) != 0) {
            bytes[off++] = BC_ARRAY_FIX_MIN;
        } else {
            bytes[off++] = BC_NULL;
        }
    }

    @Override
    public void writeRaw(String str) {
        throw new JSONException("unsupported operation");
    }

    @Override
    public void writeRaw(byte[] bytes) {
        int minCapacity = this.off + bytes.length;
        if (minCapacity - this.bytes.length > 0) {
            ensureCapacity(minCapacity);
        }
        System.arraycopy(bytes, 0, this.bytes, off, bytes.length);
        off += bytes.length;
    }

    public void writeSymbol(int symbol) {
        int minCapacity = off + 3;
        if (minCapacity >= bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
        bytes[off++] = BC_SYMBOL;

        if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
            bytes[off++] = (byte) symbol;
            return;
        }

        if (symbol >= INT32_BYTE_MIN && symbol <= INT32_BYTE_MAX) {
            bytes[off] = (byte) (BC_INT32_BYTE_ZERO + (symbol >> 8));
            bytes[off + 1] = (byte) (symbol);
            off += 2;
            return;
        }

        writeInt32(symbol);
    }

    @Override
    public void writeNameRaw(byte[] name, long nameHash) {
        int off = this.off;
        int minCapacity = off + name.length + 2;
        if (minCapacity >= this.bytes.length) {
            ensureCapacity(minCapacity);
        }

        final byte[] bytes = this.bytes;
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
                this.off = off + name.length;

                if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
                    bytes[this.off++] = (byte) symbol;
                } else {
                    writeInt32(symbol);
                }
                return;
            }
            symbol = -symbol;
        }

        bytes[off++] = BC_SYMBOL;
        int intValue = -symbol;
        if (intValue >= BC_INT32_NUM_MIN && intValue <= BC_INT32_NUM_MAX) {
            bytes[off] = (byte) intValue;
            this.off = off + 1;
        } else {
            this.off = off;
            writeInt32(intValue);
        }
    }

    @Override
    public void writeLocalDate(LocalDate date) {
        if (date == null) {
            writeNull();
            return;
        }

        int off = this.off;
        ensureCapacity(off + 5);

        final byte[] bytes = this.bytes;
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
        ensureCapacity(off + 4);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_LOCAL_TIME;
        bytes[off + 1] = (byte) time.getHour();
        bytes[off + 2] = (byte) time.getMinute();
        bytes[off + 3] = (byte) time.getSecond();
        this.off = off + 4;

        int nano = time.getNano();
        writeInt32(nano);
    }

    @Override
    public void writeLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        int off = this.off;
        ensureCapacity(off + 8);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_LOCAL_DATETIME;
        int year = dateTime.getYear();
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) dateTime.getMonthValue();
        bytes[off + 4] = (byte) dateTime.getDayOfMonth();
        bytes[off + 5] = (byte) dateTime.getHour();
        bytes[off + 6] = (byte) dateTime.getMinute();
        bytes[off + 7] = (byte) dateTime.getSecond();
        this.off = off + 8;

        int nano = dateTime.getNano();
        writeInt32(nano);
    }

    @Override
    public void writeZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        int off = this.off;
        ensureCapacity(off + 8);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_TIMESTAMP_WITH_TIMEZONE;
        int year = dateTime.getYear();
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) dateTime.getMonthValue();
        bytes[off + 4] = (byte) dateTime.getDayOfMonth();
        bytes[off + 5] = (byte) dateTime.getHour();
        bytes[off + 6] = (byte) dateTime.getMinute();
        bytes[off + 7] = (byte) dateTime.getSecond();
        this.off = off + 8;

        int nano = dateTime.getNano();
        writeInt32(nano);

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

        int off = this.off;
        ensureCapacity(off + 8);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_TIMESTAMP_WITH_TIMEZONE;
        int year = dateTime.getYear();
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) dateTime.getMonthValue();
        bytes[off + 4] = (byte) dateTime.getDayOfMonth();
        bytes[off + 5] = (byte) dateTime.getHour();
        bytes[off + 6] = (byte) dateTime.getMinute();
        bytes[off + 7] = (byte) dateTime.getSecond();
        this.off = off + 8;

        int nano = dateTime.getNano();
        writeInt32(nano);

        ZoneId zoneId = dateTime.getOffset();
        String zoneIdStr = zoneId.getId();
        if (zoneIdStr.equals(OFFSET_8_ZONE_ID_NAME)) {
            writeRaw(OFFSET_8_ZONE_ID_NAME_BYTES);
        } else {
            writeString(zoneIdStr);
        }
    }

    @Override
    public void writeInstant(Instant instant) {
        if (instant == null) {
            writeNull();
            return;
        }

        ensureCapacity(off + 1);
        bytes[off++] = BC_TIMESTAMP;
        long second = instant.getEpochSecond();
        int nano = instant.getNano();
        writeInt64(second);
        writeInt32(nano);
    }

    @Override
    public void writeUUID(UUID value) {
        if (value == null) {
            writeNull();
            return;
        }

        long msb = value.getMostSignificantBits();
        long lsb = value.getLeastSignificantBits();

        int off = this.off;
        ensureCapacity(off + 18);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_BINARY;
        bytes[off + 1] = BC_INT32_NUM_16;
        bytes[off + 2] = (byte) (msb >>> 56);
        bytes[off + 3] = (byte) (msb >>> 48);
        bytes[off + 4] = (byte) (msb >>> 40);
        bytes[off + 5] = (byte) (msb >>> 32);
        bytes[off + 6] = (byte) (msb >>> 24);
        bytes[off + 7] = (byte) (msb >>> 16);
        bytes[off + 8] = (byte) (msb >>> 8);
        bytes[off + 9] = (byte) msb;
        bytes[off + 10] = (byte) (lsb >>> 56);
        bytes[off + 11] = (byte) (lsb >>> 48);
        bytes[off + 12] = (byte) (lsb >>> 40);
        bytes[off + 13] = (byte) (lsb >>> 32);
        bytes[off + 14] = (byte) (lsb >>> 24);
        bytes[off + 15] = (byte) (lsb >>> 16);
        bytes[off + 16] = (byte) (lsb >>> 8);
        bytes[off + 17] = (byte) lsb;
        this.off = off + 18;
    }

    @Override
    public void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNull();
            return;
        }

        if (isInt64(value)) {
            if (off == bytes.length) {
                ensureCapacity(off + 1);
            }
            bytes[off++] = BC_BIGINT_LONG;
            long int64Value = value.longValue();
            writeInt64(int64Value);
            return;
        }

        byte[] valueBytes = value.toByteArray();
        ensureCapacity(off + 5 + valueBytes.length);

        bytes[off++] = BC_BIGINT;
        writeInt32(valueBytes.length);
        System.arraycopy(valueBytes, 0, bytes, off, valueBytes.length);
        off += valueBytes.length;
    }

    @Override
    public void writeBinary(byte[] bytes) {
        if (bytes == null) {
            writeNull();
            return;
        }

        ensureCapacity(off + 6 + bytes.length);
        this.bytes[off++] = BC_BINARY;
        writeInt32(bytes.length);

        System.arraycopy(bytes, 0, this.bytes, off, bytes.length);
        off += bytes.length;
    }

    @Override
    public void writeDecimal(BigDecimal value, long features, DecimalFormat format) {
        if (value == null) {
            writeNull();
            return;
        }

        int precision = value.precision();
        int scale = value.scale();

        if (precision < 19 && FIELD_DECIMAL_INT_COMPACT_OFFSET != -1) {
            long intCompact = UNSAFE.getLong(value, FIELD_DECIMAL_INT_COMPACT_OFFSET);
            if (scale == 0) {
                ensureCapacity(off + 1);
                this.bytes[off++] = BC_DECIMAL_LONG;
                writeInt64(intCompact);
                return;
            }

            ensureCapacity(off + 1);
            this.bytes[off++] = BC_DECIMAL;
            writeInt32(scale);
            if (intCompact >= Integer.MIN_VALUE && intCompact <= Integer.MAX_VALUE) {
                writeInt32((int) intCompact);
            } else {
                writeInt64(intCompact);
            }
            return;
        }

        BigInteger unscaledValue = value.unscaledValue();
        if (scale == 0
                && isInt64(unscaledValue)) {
            ensureCapacity(off + 1);
            this.bytes[off++] = BC_DECIMAL_LONG;
            long longValue = unscaledValue.longValue();
            writeInt64(longValue);
            return;
        }

        ensureCapacity(off + 1);
        this.bytes[off++] = BC_DECIMAL;
        writeInt32(scale);

        if (isInt32(unscaledValue)) {
            int intValue = unscaledValue.intValue();
            writeInt32(intValue);
        } else if (isInt64(unscaledValue)) {
            long longValue = unscaledValue.longValue();
            writeInt64(longValue);
        } else {
            writeBigInt(unscaledValue, 0);
        }
    }

    @Override
    public void writeBool(boolean value) {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        this.bytes[off++] = value ? BC_TRUE : BC_FALSE;
    }

    @Override
    public void writeBool(boolean[] valeus) {
        if (valeus == null) {
            writeNull();
            return;
        }

        startArray(valeus.length);
        for (int i = 0; i < valeus.length; i++) {
            writeBool(valeus[i]);
        }
        endArray();
    }

    @Override
    public void writeReference(String path) {
        if (off == bytes.length) {
            ensureCapacity(off + 1);
        }
        bytes[off++] = BC_REFERENCE;

        if (path == this.lastReference) {
            writeString("#-1");
        } else {
            writeString(path);
        }

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
        ensureCapacity(off + 8);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_LOCAL_DATETIME;
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) month;
        bytes[off + 4] = (byte) dayOfMonth;
        bytes[off + 5] = (byte) hour;
        bytes[off + 6] = (byte) minute;
        bytes[off + 7] = (byte) second;
        this.off = off + 8;

        int nano = 0;
        writeInt32(nano);
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
        ensureCapacity(off + 8);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_LOCAL_DATETIME;
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) month;
        bytes[off + 4] = (byte) dayOfMonth;
        bytes[off + 5] = (byte) hour;
        bytes[off + 6] = (byte) minute;
        bytes[off + 7] = (byte) second;
        this.off = off + 8;

        int nano = 0;
        writeInt32(nano);
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
        ensureCapacity(off + 5);

        final byte[] bytes = this.bytes;
        bytes[off] = BC_LOCAL_DATE;
        bytes[off + 1] = (byte) (year >>> 8);
        bytes[off + 2] = (byte) year;
        bytes[off + 3] = (byte) month;
        bytes[off + 4] = (byte) dayOfMonth;
        this.off = off + 5;
    }

    @Override
    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        throw new JSONException("unsupported operation");
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
    public void write(JSONObject object) {
        if (object == null) {
            writeNull();
            return;
        }

        startObject();
        for (Map.Entry entry : object.entrySet()) {
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
        if (bytes.length == 0) {
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
}
