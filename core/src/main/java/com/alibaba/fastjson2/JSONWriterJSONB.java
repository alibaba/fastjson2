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
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.util.DateUtils.OFFSET_8_ZONE_ID_NAME;
import static com.alibaba.fastjson2.util.DateUtils.SHANGHAI_ZONE_ID_NAME;
import static com.alibaba.fastjson2.util.IOUtils.*;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.*;

public final class JSONWriterJSONB
        extends JSONWriter {
    // optimize for write ZonedDateTime
    static final byte[] SHANGHAI_ZONE_ID_NAME_BYTES = JSONB.toBytes(SHANGHAI_ZONE_ID_NAME);
    static final byte[] OFFSET_8_ZONE_ID_NAME_BYTES = JSONB.toBytes(OFFSET_8_ZONE_ID_NAME);

    private final CacheItem cacheItem;
    byte[] bytes;
    TLongIntHashMap symbols;
    int symbolIndex;

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
        if (++level > context.maxLevel) {
            overflowLevel();
        }

        writeRaw(BC_OBJECT);
    }

    @Override
    public void endObject() {
        level--;
        writeRaw(BC_OBJECT_END);
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

        startArray(size);
    }

    @Override
    public void startArray(int size) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 6 > bytes.length) {
            bytes = grow(off + 6);
        }

        this.off = IO.startArray(bytes, off, size);
    }

    @Override
    public void startArray0() {
        writeRaw(BC_ARRAY_FIX_MIN);
    }

    @Override
    public void startArray1() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 1));
    }

    @Override
    public void startArray2() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 2));
    }

    @Override
    public void startArray3() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 3));
    }

    @Override
    public void startArray4() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 4));
    }

    @Override
    public void startArray5() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 5));
    }

    @Override
    public void startArray6() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 6));
    }

    @Override
    public void startArray7() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 7));
    }

    @Override
    public void startArray8() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 8));
    }

    @Override
    public void startArray9() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 9));
    }

    @Override
    public void startArray10() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 10));
    }

    @Override
    public void startArray11() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 11));
    }

    @Override
    public void startArray12() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 12));
    }

    @Override
    public void startArray13() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 13));
    }

    @Override
    public void startArray14() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 14));
    }

    @Override
    public void startArray15() {
        writeRaw((byte) (BC_ARRAY_FIX_MIN + 15));
    }

    @Override
    public void writeRaw(byte b) {
        int off = this.off;
        grow1(off)[off] = b;
        this.off = off + 1;
    }

    @Override
    public void writeChar(char ch) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 6 > bytes.length) {
            bytes = grow(off + 6);
        }
        bytes[off] = BC_CHAR;
        this.off = IO.writeInt32(bytes, off + 1, ch);
    }

    @Override
    public void writeName(String name) {
        writeString(name);
    }

    @Override
    public void writeNull() {
        writeRaw(BC_NULL);
    }

    @Override
    public void writeStringNull() {
        writeRaw(BC_NULL);
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
                bytes[off] = BC_STR_ASCII;
                off = IO.writeInt32(bytes, off + 1, len);
            }
            for (int i = 0; i < len; ++i) {
                bytes[off++] = (byte) chars[coff + i];
            }
            this.off = off;
            return;
        }

        writeString(new String(chars, coff, len));
    }

    public void writeStringLatin1(byte[] value) {
        byte[] bytes = this.bytes;
        int off = this.off;
        int minCapacity = off + value.length + 6;
        if (minCapacity - bytes.length > 0) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeStringLatin1(bytes, off, value);
    }

    @Override
    public void writeString(char[] chars) {
        if (chars == null) {
            writeNull();
            return;
        }

        writeString0(chars, 0, chars.length);
    }

    @Override
    public void writeString(char[] chars, int charsOff, int len) {
        if (chars == null) {
            writeNull();
            return;
        }

        writeString0(chars, charsOff, len);
    }

    private void writeString0(char[] chars, int coff, int strlen) {
        int off = this.off;
        byte[] bytes = this.bytes;
        boolean ascii = true;

        if (strlen < STR_ASCII_FIX_LEN) {
            int minCapacity = off + 1 + strlen;
            if (minCapacity > bytes.length) {
                bytes = grow(minCapacity);
            }

            bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            for (int i = coff, end = coff + strlen; i < end; i++) {
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
        } else {
            ascii = isLatin1(chars, coff, strlen);
        }

        int minCapacity = (ascii ? strlen : strlen * 3) + off + 6;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        if (ascii) {
            off = writeStringLatin1(bytes, off, chars, coff, strlen);
        } else {
            off = writeUTF8(bytes, off, chars, coff, strlen);
        }
        this.off = off;
    }

    private static int writeStringLatin1(byte[] bytes, int off, char[] chars, int coff, int strlen) {
        if (strlen <= STR_ASCII_FIX_LEN) {
            bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
        } else {
            bytes[off] = BC_STR_ASCII;
            if (strlen <= INT32_BYTE_MAX) {
                putShortBE(bytes, off + 1, (short) ((BC_INT32_BYTE_ZERO << 8) + strlen));
                off += 3;
            } else {
                off = IO.writeInt32(bytes, off + 1, strlen);
            }
        }
        for (int i = 0; i < strlen; i++) {
            bytes[off++] = (byte) chars[coff + i];
        }
        return off;
    }

    private static int writeUTF8(byte[] bytes, int off, char[] chars, int coff, int strlen) {
        int maxSize = strlen * 3;
        int lenByteCnt = sizeOfInt(maxSize);
        int result = IOUtils.encodeUTF8(chars, coff, strlen, bytes, off + lenByteCnt + 1);

        int utf8len = result - off - lenByteCnt - 1;
        int utf8lenByteCnt = sizeOfInt(utf8len);
        if (lenByteCnt != utf8lenByteCnt) {
            System.arraycopy(bytes, off + lenByteCnt + 1, bytes, off + utf8lenByteCnt + 1, utf8len);
        }
        bytes[off] = BC_STR_UTF8;
        return IO.writeInt32(bytes, off + 1, utf8len) + utf8len;
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
        byte[] bytes = grow1(off);
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

            this.off = IO.writeInt32(bytes, off, symbol);
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

        byte[] bytes = this.bytes;
        int off = this.off;
        int minCapacity = off + 2 + typeName.length;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        bytes[off] = BC_TYPED_ANY;
        System.arraycopy(typeName, 0, bytes, off + 1, typeName.length);
        off += typeName.length + 1;
        if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
            bytes[off++] = (byte) symbol;
        } else {
            off = IO.writeInt32(bytes, off, symbol);
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

        bytes[off] = BC_TYPED_ANY;
        this.off = IO.writeInt32(bytes, off + 1, -symbol);
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
            for (int i = 0; i < size; i++) {
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
                byte[] value = STRING_VALUE.apply(str);
                if (off + value.length + 6 >= bytes.length) {
                    bytes = grow(off + value.length + 6);
                }
                off = IO.writeStringLatin1(bytes, off, value);
            }
            if (latinAll) {
                this.off = off;
                return;
            }
        }

        for (int i = 0; i < size; i++) {
            writeString(
                    list.get(i)
            );
        }
    }

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeNull();
            return;
        }

        if (STRING_VALUE != null) {
            byte[] value = STRING_VALUE.apply(str);
            if (STRING_CODER.applyAsInt(str) == 0) {
                writeStringLatin1(value);
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
        this.off = IO.writeStringUTF16(bytes, off, value);
    }

    boolean tryWriteStringUTF16(byte[] value) {
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
        int start = off;
        bytes[off] = strtype;
        off = IO.writeInt32(bytes, off + 1, utf8len);
        return (off - start) + utf8len;
    }

    private static int writeUTF16(byte[] bytes, int off, byte[] value) {
        int start = off;
        bytes[off] = JDKUtils.BIG_ENDIAN ? BC_STR_UTF16BE : BC_STR_UTF16LE;
        off = IO.writeInt32(bytes, off + 1, value.length);
        System.arraycopy(value, 0, bytes, off, value.length);
        return value.length + off - start;
    }

    @Override
    public final byte[] ensureCapacity(int minCapacity) {
        byte[] bytes = this.bytes;
        if (minCapacity >= bytes.length) {
            this.bytes = bytes = Arrays.copyOf(bytes, newCapacity(minCapacity, bytes.length));
        }
        return bytes;
    }

    private byte[] grow1(int off) {
        byte[] bytes = this.bytes;
        if (off == bytes.length) {
            bytes = grow(off + 1);
        }
        return bytes;
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
                putIntBE(bytes, off + 1, secondsInt);
                this.off = off + 5;
                return;
            }

            if (seconds % 60 == 0) {
                long minutes = seconds / 60;
                if (minutes >= Integer.MIN_VALUE && minutes <= Integer.MAX_VALUE) {
                    int minutesInt = (int) minutes;
                    bytes[off] = BC_TIMESTAMP_MINUTES;
                    putIntBE(bytes, off + 1, minutesInt);
                    this.off = off + 5;
                    return;
                }
            }
        }

        bytes[off] = BC_TIMESTAMP_MILLIS;
        putLongBE(bytes, off + 1, millis);
        this.off = off + 9;
    }

    @Override
    public void writeInt64(Long i) {
        int minCapacity = off + 9;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        this.off = IO.writeInt64(bytes, off, i, context.features);
    }

    @Override
    public void writeInt64(long val) {
        int minCapacity = off + 9;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeInt64(bytes, this.off, val);
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
            off = IO.writeInt32(bytes, off + 1, size);
        }

        for (int i = 0; i < value.length; i++) {
            off = IO.writeInt64(bytes, off, value[i]);
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
            off = IO.writeInt32(bytes, off + 1, size);
        }

        for (int i = 0; i < size; i++) {
            Long item = values.get(i);
            if (item == null) {
                bytes[off++] = BC_NULL;
                continue;
            }
            off = IO.writeInt64(bytes, off, item);
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
        this.off = IO.writeFloat(bytes, off, value);
    }

    @Override
    public void writeFloat(float[] values) {
        int off = this.off;
        byte[] bytes = this.bytes;
        int minCapacity = off + (values == null ? 1 : (5 + values.length * 5));
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeFloat(bytes, off, values);
    }

    @Override
    public void writeDouble(double value) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 9 > bytes.length) {
            bytes = grow(off + 9);
        }
        this.off = IO.writeDouble(bytes, off, value);
    }

    @Override
    public void writeDouble(double[] values) {
        int off = this.off;
        byte[] bytes = this.bytes;
        int minCapacity = off + (values == null ? 1 : (5 + values.length * 9));
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeDouble(bytes, off, values);
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
            bytes[off] = BC_ARRAY;
            off = IO.writeInt32(bytes, off + 1, size);
        }

        for (int i = 0; i < values.length; i++) {
            off = IO.writeInt32(bytes, off, values[i]);
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
            bytes[off] = BC_ARRAY;
            off = IO.writeInt32(bytes, off + 1, size);
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
        this.off = IO.writeInt8(bytes, off, val);
    }

    @Override
    public void writeInt16(short val) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 3 > bytes.length) {
            bytes = grow(off + 3);
        }
        this.off = IO.writeInt16(bytes, off, val);
    }

    @Override
    public void writeEnum(Enum e) {
        if (e == null) {
            writeNull();
            return;
        }

        if ((context.features & (MASK_WRITE_ENUM_USING_TO_STRING | MASK_WRITE_ENUMS_USING_NAME)) != 0) {
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
            this.off = IO.writeInt32(bytes, off, ordinal);
        }
    }

    @Override
    public void writeInt32(Integer i) {
        int off = this.off;
        int minCapacity = off + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeInt32(bytes, off, i, context.features);
    }

    @Override
    public void writeInt32(int val) {
        int off = this.off;
        int minCapacity = off + 5;
        byte[] bytes = this.bytes;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeInt32(bytes, off, val);
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
            off = IO.writeInt32(bytes, off + 1, size);
        }

        for (int i = 0; i < size; i++) {
            Integer item = values.get(i);
            if (item == null) {
                bytes[off++] = BC_NULL;
                continue;
            }

            off = IO.writeInt32(bytes, off, item);
        }
        this.off = off;
    }

    @Override
    public void writeArrayNull() {
        writeRaw((this.context.features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL);
    }

    public void writeArrayNull(long features) {
        writeRaw((features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL);
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
            putShortBE(bytes, off, (short) ((BC_INT32_BYTE_ZERO << 8) + symbol));
            off += 2;
        } else {
            off = IO.writeInt32(bytes, off, symbol);
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
                    off = IO.writeInt32(bytes, off, symbol);
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
            off = IO.writeInt32(bytes, off, intValue);
        }
        this.off = off;
    }

    @Override
    public void writeLocalDate(LocalDate date) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 5 > bytes.length) {
            bytes = grow(off + 5);
        }
        this.off = IO.writeLocalDate(bytes, off, date);
    }

    @Override
    public void writeLocalTime(LocalTime time) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 9 > bytes.length) {
            bytes = grow(off + 9);
        }
        this.off = IO.writeLocalTime(bytes, off, time);
    }

    @Override
    public void writeLocalDateTime(LocalDateTime dateTime) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 13 > bytes.length) {
            bytes = grow(off + 13);
        }
        this.off = IO.writeLocalDateTime(bytes, off, dateTime);
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

        putIntBE(bytes,
                off,
                (BC_TIMESTAMP_WITH_TIMEZONE << 24) | (dateTime.getYear() << 8) | dateTime.getMonthValue());
        putIntBE(bytes,
                off + 4,
                (dateTime.getDayOfMonth() << 24)
                        | (dateTime.getHour() << 16)
                        | (dateTime.getMinute() << 8)
                        | dateTime.getSecond());
        this.off = IO.writeInt32(bytes, off + 8, dateTime.getNano());

        String zoneIdStr = dateTime.getZone().getId();
        if (zoneIdStr.equals(SHANGHAI_ZONE_ID_NAME)) {
            writeRaw(SHANGHAI_ZONE_ID_NAME_BYTES);
        } else {
            writeString(zoneIdStr);
        }
    }

    @Override
    public void writeOffsetDateTime(OffsetDateTime dateTime) {
        int off = this.off;
        byte[] bytes = this.bytes;
        int minCapacity = off + 21;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeOffsetDateTime(bytes, off, dateTime);
    }

    @Override
    public void writeOffsetTime(OffsetTime offsetTime) {
        int off = this.off;
        byte[] bytes = this.bytes;
        int minCapacity = off + 21;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeOffsetTime(bytes, off, offsetTime);
    }

    @Override
    public void writeInstant(Instant instant) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 15 > bytes.length) {
            bytes = grow(off + 15);
        }
        this.off = IO.writeInstant(bytes, off, instant);
    }

    @Override
    public void writeUUID(UUID value) {
        int off = this.off;
        byte[] bytes = this.bytes;
        if (off + 18 > bytes.length) {
            bytes = grow(off + 18);
        }
        this.off = IO.writeUUID(bytes, off, value);
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
            this.off = IO.writeInt64(bytes, off + 1, value.longValue());
            return;
        }

        byte[] valueBytes = value.toByteArray();
        int minCapacity = off + 5 + valueBytes.length;
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }

        bytes[off] = BC_BIGINT;
        off = IO.writeInt32(bytes, off + 1, valueBytes.length);
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
        bytes[off] = BC_BINARY;
        off = IO.writeInt32(bytes, off + 1, len);

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
                this.off = IO.writeInt64(bytes, off + 1, intCompact);
                return;
            }

            bytes[off] = BC_DECIMAL;
            off = IO.writeInt32(bytes, off + 1, scale);
            if (intCompact >= Integer.MIN_VALUE && intCompact <= Integer.MAX_VALUE) {
                off = IO.writeInt32(bytes, off, (int) intCompact);
            } else {
                off = IO.writeInt64(bytes, off, intCompact);
            }
            this.off = off;
            return;
        }

        BigInteger unscaledValue = value.unscaledValue();
        if (scale == 0
                && isInt64(unscaledValue)) {
            bytes[off] = BC_DECIMAL_LONG;
            this.off = IO.writeInt64(bytes, off + 1, unscaledValue.longValue());
            return;
        }

        bytes[off] = BC_DECIMAL;
        off = IO.writeInt32(bytes, off + 1, scale);

        if (isInt32(unscaledValue)) {
            off = IO.writeInt32(bytes, off, unscaledValue.intValue());
        } else if (isInt64(unscaledValue)) {
            off = IO.writeInt64(bytes, off, unscaledValue.longValue());
        } else {
            this.off = off;
            writeBigInt(unscaledValue, 0);
            return;
        }
        this.off = off;
    }

    @Override
    public void writeBool(boolean value) {
        writeRaw(value ? BC_TRUE : BC_FALSE);
    }

    @Override
    public void writeBool(boolean[] values) {
        int off = this.off;
        byte[] bytes = this.bytes;
        int minCapacity = off + (values == null ? 1 : (5 + values.length));
        if (minCapacity > bytes.length) {
            bytes = grow(minCapacity);
        }
        this.off = IO.writeBoolean(bytes, off, values);
    }

    @Override
    public void writeReference(String path) {
        int off = this.off;
        grow1(off)[off] = BC_REFERENCE;
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

        putIntBE(bytes, off, (BC_LOCAL_DATETIME << 24) | ((year & 0xFFFF) << 8) | month);
        putIntBE(bytes, off + 4, (dayOfMonth << 24) | (hour << 16) | (minute << 8) | second);
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
        writeDateTime14(year, month, dayOfMonth, hour, minute, second);
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
        putIntBE(bytes, off + 1, (year << 16) | (month << 8) | dayOfMonth);
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

    /**
     * IO utility methods for JSONB serialization
     */
    public abstract static class IO {
        /**
         * Calculates the capacity needed for an enum
         *
         * @param e the enum value
         * @param features the features to apply
         * @return the capacity needed
         */
        public static int enumCapacity(Enum e, long features) {
            if ((features & (MASK_WRITE_ENUM_USING_TO_STRING | MASK_WRITE_ENUMS_USING_NAME)) != 0) {
                return ((features & MASK_WRITE_ENUM_USING_TO_STRING) != 0
                        ? e.toString()
                        : e.name()).length() * 3 + 6;
            }
            return 5;
        }

        /**
         * Writes an enum value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param e the enum value to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeEnum(byte[] bytes, int off, Enum e, long features) {
            if ((features & (MASK_WRITE_ENUM_USING_TO_STRING | MASK_WRITE_ENUMS_USING_NAME)) != 0) {
                return writeString(bytes, off,
                        (features & MASK_WRITE_ENUM_USING_TO_STRING) != 0
                                ? e.toString()
                                : e.name()
                );
            } else {
                return writeInt32(bytes, off, e.ordinal());
            }
        }

        /**
         * Writes a Boolean value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Boolean value to write
         * @return the new offset
         */
        public static int writeBoolean(byte[] bytes, int off, Boolean value) {
            bytes[off] = value == null ? BC_NULL : value ? BC_TRUE : BC_FALSE;
            return off + 1;
        }

        /**
         * Writes a boolean value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the boolean value to write
         * @return the new offset
         */
        public static int writeBoolean(byte[] bytes, int off, boolean value) {
            bytes[off] = value ? BC_TRUE : BC_FALSE;
            return off + 1;
        }

        /**
         * Writes a boolean array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the boolean array to write
         * @return the new offset
         */
        public static int writeBoolean(byte[] bytes, int off, boolean[] values) {
            if (values == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            off = startArray(bytes, off, values.length);
            for (int i = 0; i < values.length; i++) {
                bytes[off + i] = values[i] ? BC_TRUE : BC_FALSE;
            }
            return off + values.length;
        }

        /**
         * Writes a Float value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Float value to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeFloat(byte[] bytes, int off, Float value, long features) {
            float floatValue;
            if (value == null) {
                if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0) {
                    bytes[off] = BC_NULL;
                    return off + 1;
                }
                floatValue = 0;
            } else {
                floatValue = value;
            }
            return IO.writeFloat(bytes, off, floatValue);
        }

        /**
         * Writes a float array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the float array to write
         * @return the new offset
         */
        public static int writeFloat(byte[] bytes, int off, float[] values) {
            if (values == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            off = startArray(bytes, off, values.length);
            for (float value : values) {
                off = IO.writeFloat(bytes, off, value);
            }
            return off;
        }

        /**
         * Writes a float value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the float value to write
         * @return the new offset
         */
        public static int writeFloat(byte[] bytes, int off, float value) {
            int intValue = (int) value;
            if (intValue == value && ((intValue + 0x40000) & ~0x7ffff) == 0) {
                bytes[off] = BC_FLOAT_INT;
                return IO.writeInt32(bytes, off + 1, intValue);
            }

            bytes[off] = BC_FLOAT;
            IOUtils.putIntBE(bytes, off + 1, Float.floatToIntBits(value));
            return off + 5;
        }

        /**
         * Writes a Double value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Double value to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeDouble(byte[] bytes, int off, Double value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                        ? BC_NULL
                        : BC_DOUBLE_NUM_0;
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : BC_DOUBLE_NUM_0;
                return off + 1;
            }
            return IO.writeDouble(bytes, off, value);
        }

        /**
         * Writes a double value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the double value to write
         * @return the new offset
         */
        public static int writeDouble(byte[] bytes, int off, double value) {
            if (value == 0 || value == 1) {
                bytes[off] = value == 0 ? BC_DOUBLE_NUM_0 : BC_DOUBLE_NUM_1;
                return off + 1;
            }

            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                long longValue = (long) value;
                if (longValue == value) {
                    bytes[off] = BC_DOUBLE_LONG;
                    return IO.writeInt64(bytes, off + 1, longValue);
                }
            }

            bytes[off] = BC_DOUBLE;
            IOUtils.putLongBE(bytes, off + 1, Double.doubleToLongBits(value));
            return off + 9;
        }

        /**
         * Writes a double array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the double array to write
         * @return the new offset
         */
        public static int writeDouble(byte[] bytes, int off, double[] values) {
            if (values == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            off = startArray(bytes, off, values.length);
            for (double value : values) {
                off = writeDouble(bytes, off, value);
            }
            return off;
        }

        /**
         * Writes a Byte value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the Byte value to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeInt8(byte[] bytes, int off, Byte val, long features) {
            if (val == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            putShortLE(bytes, off, (short) ((val << 8) | (BC_INT8 & 0xFF)));
            return off + 2;
        }

        /**
         * Writes a byte value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the byte value to write
         * @return the new offset
         */
        public static int writeInt8(byte[] bytes, int off, byte val) {
            putShortLE(bytes, off, (short) ((val << 8) | (BC_INT8 & 0xFF)));
            return off + 2;
        }

        /**
         * Writes a Short value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the Short value to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeInt16(byte[] bytes, int off, Short val, long features) {
            if (val == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            bytes[off] = BC_INT16;
            putShortBE(bytes, off + 1, val);
            return off + 3;
        }

        /**
         * Writes a short value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the short value to write
         * @return the new offset
         */
        public static int writeInt16(byte[] bytes, int off, short val) {
            bytes[off] = BC_INT16;
            putShortBE(bytes, off + 1, val);
            return off + 3;
        }

        /**
         * Writes an Integer value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Integer value to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeInt32(byte[] bytes, int off, Integer value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0 ? BC_NULL : 0;
                return off + 1;
            }
            return IO.writeInt32(bytes, off, value);
        }

        /**
         * Writes a string with a symbol table to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param str the string to write
         * @param symbolTable the symbol table to use
         * @return the new offset
         */
        public static int writeSymbol(byte[] bytes, int off, String str, SymbolTable symbolTable) {
            if (str == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            int ordinal = symbolTable.getOrdinal(str);
            if (ordinal >= 0) {
                bytes[off] = BC_STR_ASCII;
                return writeInt32(bytes, off + 1, -ordinal);
            }
            return writeString(bytes, off, str);
        }

        /**
         * Writes a symbol to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param symbol the symbol to write
         * @return the new offset
         */
        public static int writeSymbol(byte[] bytes, int off, int symbol) {
            bytes[off++] = BC_SYMBOL;

            if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) symbol;
            } else if (symbol >= INT32_BYTE_MIN && symbol <= INT32_BYTE_MAX) {
                putShortBE(bytes, off, (short) ((BC_INT32_BYTE_ZERO << 8) + symbol));
                off += 2;
            } else {
                off = writeInt32(bytes, off, symbol);
            }
            return off;
        }

        /**
         * Checks and writes a type name to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param object the object to check
         * @param fieldClass the field class
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
        public static int checkAndWriteTypeName(byte[] bytes, int off, Object object, Class<?> fieldClass, JSONWriter jsonWriter) {
            long features = jsonWriter.getFeatures();
            Class<?> objectClass;
            if ((features & MASK_WRITE_CLASS_NAME) == 0
                    || object == null
                    || (objectClass = object.getClass()) == fieldClass
                    || ((features & MASK_NOT_WRITE_HASHMAP_ARRAY_LIST_CLASS_NAME) != 0 && (objectClass == HashMap.class || objectClass == ArrayList.class))
                    || ((features & MASK_NOT_WRITE_ROOT_CLASSNAME) != 0 && object == jsonWriter.rootObject)
            ) {
                return off;
            }

            return writeTypeName(bytes, off, getTypeName(objectClass), jsonWriter);
        }

        /**
         * Writes a type name to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param typeName the type name to write
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
        public static int writeTypeName(byte[] bytes, int off, String typeName, JSONWriter jsonWriter) {
            JSONWriterJSONB jsonWriterJSONB = (JSONWriterJSONB) jsonWriter;
            SymbolTable symbolTable = jsonWriter.symbolTable;
            bytes[off++] = BC_TYPED_ANY;

            long hash = Fnv.hashCode64(typeName);

            int symbol = -1;
            if (symbolTable != null) {
                symbol = symbolTable.getOrdinalByHashCode(hash);
                if (symbol == -1 && jsonWriterJSONB.symbols != null) {
                    symbol = jsonWriterJSONB.symbols.get(hash);
                }
            } else if (jsonWriterJSONB.symbols != null) {
                symbol = jsonWriterJSONB.symbols.get(hash);
            }

            if (symbol == -1) {
                if (jsonWriterJSONB.symbols == null) {
                    jsonWriterJSONB.symbols = new TLongIntHashMap();
                }
                jsonWriterJSONB.symbols.put(hash, symbol = jsonWriterJSONB.symbolIndex++);
            } else {
                return writeInt32(bytes, off, symbol);
            }

            off = writeString(bytes, off, typeName);
            return writeInt32(bytes, off, symbol);
        }

        /**
         * Writes an integer value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the integer value to write
         * @return the new offset
         */
        public static int writeInt32(byte[] bytes, int off, int value) {
            if (((value + 0x10) & ~0x3f) == 0) {
                bytes[off++] = (byte) value;
            } else if (((value + 0x800) & ~0xfff) == 0) {
                putShortBE(bytes, off, (short) ((BC_INT32_BYTE_ZERO << 8) + value));
                off += 2;
            } else if (((value + 0x40000) & ~0x7ffff) == 0) {
                bytes[off] = (byte) (BC_INT32_SHORT_ZERO + (value >> 16));
                putShortBE(bytes, off + 1, (short) value);
                off += 3;
            } else {
                bytes[off] = BC_INT32;
                putIntBE(bytes, off + 1, value);
                off += 5;
            }
            return off;
        }

        /**
         * Writes a collection of Long values to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param values the collection of Long values to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeInt64(byte[] bytes, int off, Collection<Long> values, long features) {
            if (values == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = values.size();
            off = startArray(bytes, off, size);
            for (Long value : values) {
                if (value == null) {
                    bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                            ? BC_NULL
                            : (byte) (BC_INT64_NUM_MIN - INT64_NUM_LOW_VALUE);
                    off++;
                } else {
                    off = IO.writeInt64(bytes, off, value);
                }
            }
            return off;
        }

        public static int writeInt64(byte[] bytes, int off, List<Long> values, long features) {
            if (values == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = values.size();
            off = startArray(bytes, off, size);
            for (int i = 0; i < values.size(); i++) {
                Long value = values.get(i);
                if (value == null) {
                    bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                            ? BC_NULL
                            : (byte) (BC_INT64_NUM_MIN - INT64_NUM_LOW_VALUE);
                    off++;
                } else {
                    off = IO.writeInt64(bytes, off, value);
                }
            }
            return off;
        }

        /**
         * Writes a Long value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Long value to write
         * @param features the features to apply
         * @return the new offset
         */
        public static int writeInt64(byte[] bytes, int off, Long value, long features) {
            if (value == null) {
                bytes[off] = (features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) == 0
                        ? BC_NULL
                        : (byte) (BC_INT64_NUM_MIN - INT64_NUM_LOW_VALUE);
                return off + 1;
            }
            return IO.writeInt64(bytes, off, value);
        }

        /**
         * Writes a long value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the long value to write
         * @return the new offset
         */
        public static int writeInt64(byte[] bytes, int off, long value) {
            if (value >= INT64_NUM_LOW_VALUE && value <= INT64_NUM_HIGH_VALUE) {
                bytes[off++] = (byte) (BC_INT64_NUM_MIN + (value - INT64_NUM_LOW_VALUE));
            } else if (((value + 0x800) & ~0xfffL) == 0) {
                putShortBE(bytes, off, (short) ((BC_INT64_BYTE_ZERO << 8) + value));
                off += 2;
            } else if (((value + 0x40000) & ~0x7ffffL) == 0) {
                bytes[off] = (byte) (BC_INT64_SHORT_ZERO + (value >> 16));
                putShortBE(bytes, off + 1, (short) value);
                off += 3;
            } else if ((((value + 0x80000000L) & ~0xffffffffL) == 0)) {
                bytes[off] = BC_INT64_INT;
                putIntBE(bytes, off + 1, (int) value);
                off += 5;
            } else {
                bytes[off] = BC_INT64;
                putLongBE(bytes, off + 1, value);
                off += 9;
            }
            return off;
        }

        /**
         * Starts an array in a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param size the size of the array
         * @return the new offset
         */
        public static int startArray(byte[] bytes, int off, int size) {
            boolean tinyInt = size <= ARRAY_FIX_LEN;
            bytes[off++] = tinyInt ? (byte) (BC_ARRAY_FIX_MIN + size) : BC_ARRAY;
            if (!tinyInt) {
                off = writeInt32(bytes, off, size);
            }
            return off;
        }

        public static int writeStringJDK8(byte[] bytes, int off, Collection<String> strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.size();
            off = startArray(bytes, off, size);
            for (String string : strings) {
                off = writeStringJDK8(bytes, off, string);
            }
            return off;
        }

        public static int writeStringJDK11(byte[] bytes, int off, Collection<String> strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.size();
            off = startArray(bytes, off, size);
            for (String string : strings) {
                off = writeStringJDK11(bytes, off, string);
            }
            return off;
        }

        public static int writeStringJDK8(byte[] bytes, int off, List<String> strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.size();
            off = startArray(bytes, off, size);
            for (int i = 0; i < strings.size(); i++) {
                off = writeStringJDK8(bytes, off, strings.get(i));
            }
            return off;
        }

        public static int writeStringJDK11(byte[] bytes, int off, List<String> strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.size();
            off = startArray(bytes, off, size);
            for (int i = 0; i < strings.size(); i++) {
                off = writeStringJDK11(bytes, off, strings.get(i));
            }
            return off;
        }

        public static int writeStringJDK8(byte[] bytes, int off, String[] strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.length;
            off = startArray(bytes, off, size);
            for (String string : strings) {
                off = writeStringJDK8(bytes, off, string);
            }
            return off;
        }

        public static int writeStringJDK11(byte[] bytes, int off, String[] strings, long features) {
            if (strings == null) {
                bytes[off] = (features & WRITE_ARRAY_NULL_MASK) != 0 ? BC_ARRAY_FIX_MIN : BC_NULL;
                return off + 1;
            }
            int size = strings.length;
            off = startArray(bytes, off, size);
            for (String string : strings) {
                off = writeStringJDK11(bytes, off, string);
            }
            return off;
        }

        /**
         * Writes a string to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param str the string to write
         * @return the new offset
         */
        public static int writeString(byte[] bytes, int off, String str) {
            if (str == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            if (STRING_CODER != null && STRING_VALUE != null) {
                int coder = STRING_CODER.applyAsInt(str);
                byte[] value = STRING_VALUE.apply(str);
                if (coder == 0) {
                    return writeStringLatin1(bytes, off, value);
                } else {
                    return writeStringUTF16(bytes, off, value);
                }
            } else {
                return writeString(bytes, off, JDKUtils.getCharArray(str));
            }
        }

        public static int writeStringJDK8(byte[] bytes, int off, String str) {
            if (str == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            return writeString(bytes, off, JDKUtils.getCharArray(str));
        }

        public static int writeStringJDK11(byte[] bytes, int off, String str) {
            if (str == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            int coder = STRING_CODER.applyAsInt(str);
            byte[] value = STRING_VALUE.apply(str);
            if (coder == 0) {
                return writeStringLatin1(bytes, off, value);
            } else {
                return writeStringUTF16(bytes, off, value);
            }
        }

        /**
         * Writes a UTF-16 string to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the UTF-16 byte array to write
         * @return the new offset
         */
        public static int writeStringUTF16(byte[] bytes, int off, byte[] value) {
            final int strlen = value.length;
            bytes[off] = JDKUtils.BIG_ENDIAN ? BC_STR_UTF16BE : BC_STR_UTF16LE;
            off = writeInt32(bytes, off + 1, strlen);
            System.arraycopy(value, 0, bytes, off, strlen);
            return off + strlen;
        }

        /**
         * Writes a Latin-1 string to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Latin-1 byte array to write
         * @return the new offset
         */
        public static int writeStringLatin1(byte[] bytes, int off, byte[] value) {
            int strlen = value.length;
            if (strlen <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            } else if (strlen <= INT32_BYTE_MAX) {
                off = putStringSizeSmall(bytes, off, strlen);
            } else {
                off = putStringSizeLarge(bytes, off, strlen);
            }
            System.arraycopy(value, 0, bytes, off, value.length);
            return off + strlen;
        }

        public static int stringCapacityJDK8(List<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.getClass().getName().length() * 3 + 13;
            for (int i = 0; i < strings.size(); i++) {
                String str = strings.get(i);
                size += str == null ? 0 : str.length() * 3 + 6;
            }
            return size;
        }

        public static int stringCapacityJDK11(List<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.getClass().getName().length() * 3 + 13;
            for (int i = 0; i < strings.size(); i++) {
                String str = strings.get(i);
                size += str == null ? 0 : (str.length() << STRING_CODER.applyAsInt(str)) + 6;
            }
            return size;
        }

        public static int stringCapacityJDK8(Collection<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.getClass().getName().length() * 3 + 13;
            for (String str : strings) {
                size += str == null ? 0 : str.length() * 3 + 6;
            }
            return size;
        }

        public static int stringCapacityJDK11(Collection<String> strings) {
            if (strings == null) {
                return 1;
            }
            int size = strings.getClass().getName().length() * 3 + 13;
            for (String str : strings) {
                size += str == null ? 0 : (str.length() << STRING_CODER.applyAsInt(str)) + 6;
            }
            return size;
        }

        public static int stringCapacityJDK8(String[] strings) {
            if (strings == null) {
                return 1;
            }
            int size = 6;
            for (String str : strings) {
                size += str == null ? 0 : str.length() * 3 + 6;
            }
            return size;
        }

        public static int stringCapacityJDK11(String[] strings) {
            if (strings == null) {
                return 1;
            }
            int size = 6;
            for (String str : strings) {
                size += str == null ? 0 : (str.length() << STRING_CODER.applyAsInt(str)) + 6;
            }
            return size;
        }

        /**
         * Calculates the capacity needed for a collection of Long values
         *
         * @param values the collection of Long values
         * @return the capacity needed
         */
        public static int int64Capacity(Collection<Long> values) {
            return values == null ? 1 : values.getClass().getName().length() * 3 + 13 + values.size() * 9;
        }

        /**
         * Calculates the capacity needed for a string
         *
         * @param str the string
         * @return the capacity needed
         */
        public static int stringCapacity(String str) {
            return JVM_VERSION > 8 ? stringCapacityJDK11(str) : stringCapacityJDK8(str);
        }

        public static int stringCapacityJDK8(String str) {
            return str == null ? 0 : str.length() * 3 + 6;
        }

        public static int stringCapacityJDK11(String str) {
            return str == null ? 0 : (str.length() << STRING_CODER.applyAsInt(str)) + 6;
        }

        /**
         * Puts a small string size to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param val the size value to write
         * @return the new offset
         */
        public static int putStringSizeSmall(byte[] bytes, int off, int val) {
            bytes[off] = BC_STR_ASCII;
            putShortBE(bytes, off + 1, (short) ((BC_INT32_BYTE_ZERO << 8) + val));
            return off + 3;
        }

        /**
         * Puts a large string size to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param strlen the size value to write
         * @return the new offset
         */
        public static int putStringSizeLarge(byte[] bytes, int off, int strlen) {
            if (strlen <= INT32_SHORT_MAX) {
                putIntBE(bytes, off, (BC_STR_ASCII << 24) + (BC_INT32_SHORT_ZERO << 16) + strlen);
                return off + 4;
            }

            putShortBE(bytes, off, (short) ((BC_STR_ASCII << 8) | BC_INT32));
            putIntBE(bytes, off + 2, strlen);
            return off + 6;
        }

        /**
         * Writes a character array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @return the new offset
         */
        public static int writeString(byte[] bytes, int off, char[] chars) {
            return writeString(bytes, off, chars, 0, chars.length);
        }

        /**
         * Writes a character array with offset and length to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @param coff the offset in the character array
         * @param strlen the length of characters to write
         * @return the new offset
         */
        public static int writeString(byte[] bytes, int off, char[] chars, int coff, int strlen) {
            int start = off;
            boolean ascii = true;
            if (strlen < STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
                for (int i = coff, end = coff + strlen; i < end; i++) {
                    char ch = chars[i];
                    if (ch > 0x00FF) {
                        ascii = false;
                        break;
                    }
                    bytes[off++] = (byte) ch;
                }

                if (ascii) {
                    return off;
                }

                off = start;
            } else {
                ascii = isLatin1(chars, coff, strlen);
            }

            if (ascii) {
                off = writeStringLatin1(bytes, off, chars, coff, strlen);
            } else {
                off = writeUTF8(bytes, off, chars, coff, strlen);
            }
            return off;
        }

        /**
         * Writes a Latin-1 character array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @param coff the offset in the character array
         * @param strlen the length of characters to write
         * @return the new offset
         */
        public static int writeStringLatin1(byte[] bytes, int off, char[] chars, int coff, int strlen) {
            if (strlen <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            } else {
                bytes[off] = BC_STR_ASCII;
                if (strlen <= INT32_BYTE_MAX) {
                    putShortBE(bytes, off + 1, (short) ((BC_INT32_BYTE_ZERO << 8) + strlen));
                    off += 3;
                } else {
                    off = writeInt32(bytes, off + 1, strlen);
                }
            }
            for (int i = 0; i < strlen; i++) {
                bytes[off++] = (byte) chars[coff + i];
            }
            return off;
        }

        /**
         * Writes a UTF-8 character array to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param chars the character array to write
         * @param coff the offset in the character array
         * @param strlen the length of characters to write
         * @return the new offset
         */
        public static int writeUTF8(byte[] bytes, int off, char[] chars, int coff, int strlen) {
            int maxSize = strlen * 3;
            int lenByteCnt = sizeOfInt(maxSize);
            int result = IOUtils.encodeUTF8(chars, coff, strlen, bytes, off + lenByteCnt + 1);

            int utf8len = result - off - lenByteCnt - 1;
            int utf8lenByteCnt = sizeOfInt(utf8len);
            if (lenByteCnt != utf8lenByteCnt) {
                System.arraycopy(bytes, off + lenByteCnt + 1, bytes, off + utf8lenByteCnt + 1, utf8len);
            }
            bytes[off] = BC_STR_UTF8;
            return writeInt32(bytes, off + 1, utf8len) + utf8len;
        }

        /**
         * Calculates the size needed for an integer value
         *
         * @param i the integer value
         * @return the size needed
         */
        public static int sizeOfInt(int i) {
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

        /**
         * Writes a UUID value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the UUID value to write
         * @return the new offset
         */
        public static int writeUUID(byte[] bytes, int off, UUID value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putShortLE(bytes, off, (short) ((BC_BINARY & 0xFF) | ((BC_INT32_NUM_16 & 0xFF) << 8)));
            putLongBE(bytes, off + 2, value.getMostSignificantBits());
            putLongBE(bytes, off + 10, value.getLeastSignificantBits());
            return off + 18;
        }

        /**
         * Writes an Instant value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the Instant value to write
         * @return the new offset
         */
        public static int writeInstant(byte[] bytes, int off, Instant value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            bytes[off] = BC_TIMESTAMP;
            off = writeInt64(bytes, off + 1, value.getEpochSecond());
            return writeInt32(bytes, off, value.getNano());
        }

        /**
         * Writes a LocalDate value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the LocalDate value to write
         * @return the new offset
         */
        public static int writeLocalDate(byte[] bytes, int off, LocalDate value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            bytes[off] = BC_LOCAL_DATE;
            int year = value.getYear();
            putIntBE(bytes, off + 1, (year << 16) | (value.getMonthValue() << 8) | value.getDayOfMonth());
            return off + 5;
        }

        /**
         * Writes a LocalTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the LocalTime value to write
         * @return the new offset
         */
        public static int writeLocalTime(byte[] bytes, int off, LocalTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putIntBE(bytes,
                    off,
                    (BC_LOCAL_TIME << 24) | (value.getHour() << 16) | (value.getMinute() << 8) | value.getSecond());
            return writeInt32(bytes, off + 4, value.getNano());
        }

        /**
         * Writes a LocalDateTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the LocalDateTime value to write
         * @return the new offset
         */
        public static int writeLocalDateTime(byte[] bytes, int off, LocalDateTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putIntBE(bytes,
                    off,
                    (BC_LOCAL_DATETIME << 24) | (value.getYear() << 8) | value.getMonthValue());
            putIntBE(bytes,
                    off + 4,
                    (value.getDayOfMonth() << 24)
                            | (value.getHour() << 16)
                            | (value.getMinute() << 8)
                            | value.getSecond());
            return writeInt32(bytes, off + 8, value.getNano());
        }

        /**
         * Writes an OffsetDateTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the OffsetDateTime value to write
         * @return the new offset
         */
        public static int writeOffsetDateTime(byte[] bytes, int off, OffsetDateTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }
            putIntBE(bytes,
                    off,
                    (BC_TIMESTAMP_WITH_TIMEZONE << 24) | (value.getYear() << 8) | value.getMonthValue());
            putIntBE(bytes,
                    off + 4,
                    (value.getDayOfMonth() << 24)
                            | (value.getHour() << 16)
                            | (value.getMinute() << 8)
                            | value.getSecond());

            off = writeInt32(bytes, off + 8, value.getNano());

            String zoneIdStr = value.getOffset().getId();
            int strlen = zoneIdStr.length();
            bytes[off] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            zoneIdStr.getBytes(0, strlen, bytes, off + 1);
            return off + strlen + 1;
        }

        /**
         * Writes an OffsetTime value to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param value the OffsetTime value to write
         * @return the new offset
         */
        public static int writeOffsetTime(byte[] bytes, int off, OffsetTime value) {
            if (value == null) {
                bytes[off] = BC_NULL;
                return off + 1;
            }

            int year = 1970, month = 1, dayOfMonth = 1;
            putIntBE(bytes,
                    off,
                    (BC_TIMESTAMP_WITH_TIMEZONE << 24) | (year << 8) | month);
            putIntBE(bytes,
                    off + 4,
                    (dayOfMonth << 24)
                            | (value.getHour() << 16)
                            | (value.getMinute() << 8)
                            | value.getSecond());

            off = writeInt32(bytes, off + 8, value.getNano());

            String zoneIdStr = value.getOffset().getId();
            int strlen = zoneIdStr.length();
            bytes[off] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            zoneIdStr.getBytes(0, strlen, bytes, off + 1);
            return off + strlen + 1;
        }

        /**
         * Writes a reference to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param path the reference path
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
        public static int writeReference(byte[] bytes, int off, String path, JSONWriter jsonWriter) {
            if (jsonWriter.lastReference == path) {
                path = "#-1";
            } else {
                jsonWriter.lastReference = path;
            }
            bytes[off] = BC_REFERENCE;
            return writeString(bytes, off + 1, path);
        }

        /**
         * Writes a raw name to a byte array
         *
         * @param bytes the byte array to write to
         * @param off the offset in the byte array
         * @param name the name byte array to write
         * @param nameHash the name hash
         * @param jsonWriter the JSON writer
         * @return the new offset
         */
        public static int writeNameRaw(byte[] bytes, int off, byte[] name, long nameHash, JSONWriter jsonWriter) {
            SymbolTable symbolTable = jsonWriter.symbolTable;
            JSONWriterJSONB jsonWriterJSONB = (JSONWriterJSONB) jsonWriter;
            int symbol;
            if (symbolTable == null
                    || (symbol = symbolTable.getOrdinalByHashCode(nameHash)) == -1
            ) {
                if ((jsonWriter.context.features & WriteNameAsSymbol.mask) == 0) {
                    System.arraycopy(name, 0, bytes, off, name.length);
                    return off + name.length;
                }

                boolean symbolExists = false;
                if (jsonWriterJSONB.symbols != null) {
                    if ((symbol = jsonWriterJSONB.symbols.putIfAbsent(nameHash, jsonWriterJSONB.symbolIndex)) != jsonWriterJSONB.symbolIndex) {
                        symbolExists = true;
                    } else {
                        jsonWriterJSONB.symbolIndex++;
                    }
                } else {
                    (jsonWriterJSONB.symbols = new TLongIntHashMap())
                            .put(nameHash, symbol = jsonWriterJSONB.symbolIndex++);
                }

                if (!symbolExists) {
                    bytes[off++] = BC_SYMBOL;
                    System.arraycopy(name, 0, bytes, off, name.length);
                    off += name.length;

                    if (symbol >= BC_INT32_NUM_MIN && symbol <= BC_INT32_NUM_MAX) {
                        bytes[off++] = (byte) symbol;
                    } else {
                        off = writeInt32(bytes, off, symbol);
                    }
                    return off;
                }
                symbol = -symbol;
            }

            bytes[off++] = BC_SYMBOL;
            int intValue = -symbol;
            if (intValue >= BC_INT32_NUM_MIN && intValue <= BC_INT32_NUM_MAX) {
                bytes[off++] = (byte) intValue;
            } else {
                off = writeInt32(bytes, off, intValue);
            }
            return off;
        }
    }
}
