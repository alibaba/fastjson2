package com.alibaba.fastjson2;

import com.alibaba.fastjson2.internal.trove.map.hash.TLongIntHashMap;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Arrays;
import java.util.UUID;

import static com.alibaba.fastjson2.JSONB.Constants.*;

final class JSONWriterJSONB
        extends JSONWriter {
    static final BigInteger BIGINT_INT64_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    static final BigInteger BIGINT_INT64_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private final int cachedIndex;

    private byte[] bytes;
    private SymbolTable symbolTable;

    TLongIntHashMap symbols;
    private int symbolIndex;

    JSONWriterJSONB(Context ctx, SymbolTable symbolTable) {
        super(ctx, StandardCharsets.UTF_8);
        cachedIndex = JSONFactory.cacheIndex();
        bytes = JSONFactory.allocateByteArray(cachedIndex);
        this.symbolTable = symbolTable;
    }

    @Override
    public void close() {
        JSONFactory.releaseByteArray(cachedIndex, bytes);
    }

    @Override
    public boolean isUTF8() {
        return false;
    }

    @Override
    public boolean isUTF16() {
        return false;
    }

    @Override
    public boolean isJSONB() {
        return true;
    }

    @Override
    public SymbolTable getSymbolTable() {
        return symbolTable;
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
        level++;
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
        bytes[off++] = BC_OBJECT;
    }

    @Override
    public void endObject() {
        level--;
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
        bytes[off++] = BC_OBJECT_END;
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

        level++;
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off++] = BC_ARRAY;
            writeInt32(size);
        }
    }

    @Override
    public void startArray(int size) {
        level++;
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off++] = BC_ARRAY;
            writeInt32(size);
        }
    }

    @Override
    public void writeRaw(byte b) {
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
        bytes[off++] = b;
    }

    @Override
    public void writeChar(char ch) {
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
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
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
        bytes[off++] = BC_NULL;
    }

    @Override
    public void endArray() {
        level--;
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
        throw new JSONException("unsupported operation");
    }

    @Override
    public void writeString(char[] str) {
        if (str == null) {
            writeNull();
            return;
        }

        final int strlen = str.length;
        boolean ascii = true;
        for (int i = 0; i < strlen; ++i) {
            if (str[i] > 0x007F) {
                ascii = false;
                break;
            }
        }

        if (ascii) {
            if (strlen <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            } else {
                bytes[off++] = BC_STR_ASCII;
                writeInt32(strlen);
            }
            for (int i = 0; i < strlen; ++i) {
                bytes[off++] = (byte) str[i];
            }
            return;
        }

        writeString(new String(str));
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
                writeRaw(BC_STR_ASCII);
                writeInt32(-ordinal);
                return;
            }
        }

        writeString(str);
    }

    @Override
    public void writeTypeName(String typeName) {
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
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
                int minCapacity = off + 1;
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
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
                int minCapacity = off + 2;
                if (minCapacity - bytes.length > 0) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                this.bytes[off++] = BC_TYPED_ANY;
                writeInt32(-symbol);
                return false;
            }
        }

        int symbol = -1;
        if (symbols != null) {
            symbol = symbols.get(hash);
        }

        if (symbol != -1) {
            if (off == bytes.length) {
                int minCapacity = off + 1;
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            this.bytes[off++] = BC_TYPED_ANY;
            writeInt32(symbol);
            return false;
        }

        if (symbols == null) {
            symbols = new TLongIntHashMap();
        }
        symbols.put(hash, symbol = symbolIndex++);

        int minCapacity = off + 1 + typeName.length;
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        this.bytes[off++] = BC_TYPED_ANY;
        System.arraycopy(typeName, 0, this.bytes, off, typeName.length);
        off += typeName.length;
        writeInt32(symbol);

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

    @Override
    public void writeString(String str) {
        if (str == null) {
            writeNull();
            return;
        }

        char[] chars = str.toCharArray();

        boolean ascii = true;
        {
            int i = 0;
            while (i + 4 <= chars.length) {
                char c0 = chars[i];
                char c1 = chars[i + 1];
                char c2 = chars[i + 2];
                char c3 = chars[i + 3];
                if (c0 > 0x007F || c1 > 0x007F || c2 > 0x007F || c3 > 0x007F) {
                    ascii = false;
                    break;
                }
                i += 4;
            }
            if (ascii) {
                for (; i < chars.length; ++i) {
                    if (chars[i] > 0x007F) {
                        ascii = false;
                        break;
                    }
                }
            }
        }

        final int strlen = chars.length;
        int minCapacity = (ascii ? strlen : strlen * 3)
                + off
                + 5 /*max str len*/
                + 1;

        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        if (ascii) {
            if (strlen <= STR_ASCII_FIX_LEN) {
                bytes[off++] = (byte) (strlen + BC_STR_ASCII_FIX_MIN);
            } else {
                bytes[off++] = BC_STR_ASCII;
                writeInt32(strlen);
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
            bytes[off++] = BC_STR_UTF8;
            writeInt32(utf8len);
            off += utf8len;
        }
    }

    void ensureCapacity(int minCapacity) {
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
    }

    @Override
    public void writeMillis(long millis) {
        if (millis % 1000 == 0) {
            long seconds = (millis / 1000);
            if (seconds >= Integer.MIN_VALUE && seconds <= Integer.MAX_VALUE) {
                int secondsInt = (int) seconds;

                int minCapacity = off + 5;
                if (minCapacity - bytes.length > 0) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = BC_TIMESTAMP_SECONDS;
                bytes[off++] = (byte) (secondsInt >>> 24);
                bytes[off++] = (byte) (secondsInt >>> 16);
                bytes[off++] = (byte) (secondsInt >>> 8);
                bytes[off++] = (byte) secondsInt;
                return;
            }

            if (seconds % 60000 == 0) {
                long minutes = seconds / 60;
                if (minutes >= Integer.MIN_VALUE && minutes <= Integer.MAX_VALUE) {
                    int minutesInt = (int) minutes;

                    int minCapacity = off + 5;
                    if (minCapacity - bytes.length > 0) {
                        int oldCapacity = bytes.length;
                        int newCapacity = oldCapacity + (oldCapacity >> 1);
                        if (newCapacity - minCapacity < 0) {
                            newCapacity = minCapacity;
                        }
                        if (newCapacity - MAX_ARRAY_SIZE > 0) {
                            throw new OutOfMemoryError();
                        }

                        // minCapacity is usually close to size, so this is a win:
                        bytes = Arrays.copyOf(bytes, newCapacity);
                    }

                    bytes[off++] = BC_TIMESTAMP_MINUTES;
                    bytes[off++] = (byte) (minutesInt >>> 24);
                    bytes[off++] = (byte) (minutesInt >>> 16);
                    bytes[off++] = (byte) (minutesInt >>> 8);
                    bytes[off++] = (byte) minutesInt;
                    return;
                }
            }
        }

        int minCapacity = off + 9;
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        bytes[off++] = BC_TIMESTAMP_MILLIS;
        bytes[off++] = (byte) (millis >>> 56);
        bytes[off++] = (byte) (millis >>> 48);
        bytes[off++] = (byte) (millis >>> 40);
        bytes[off++] = (byte) (millis >>> 32);
        bytes[off++] = (byte) (millis >>> 24);
        bytes[off++] = (byte) (millis >>> 16);
        bytes[off++] = (byte) (millis >>> 8);
        bytes[off++] = (byte) millis;
    }

    @Override
    public void writeInt64(long val) {
        if (val >= INT64_NUM_LOW_VALUE && val <= INT64_NUM_HIGH_VALUE) {
            // inline ensureCapacity(off + 1);
            if (off == bytes.length) {
                int minCapacity = off + 1;

                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = (byte) (BC_INT64_NUM_MIN + (val - INT64_NUM_LOW_VALUE));
            return;
        }

        if (val >= INT64_BYTE_MIN && val <= INT64_BYTE_MAX) {
            int minCapacity = off + 2;
            if (minCapacity - bytes.length > 0) {
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = (byte) (BC_INT64_BYTE_ZERO + (val >> 8));
            bytes[off++] = (byte) (val);
            return;
        }

        if (val >= INT64_SHORT_MIN && val <= INT64_SHORT_MAX) {
            int minCapacity = off + 3;
            if (minCapacity - bytes.length > 0) {
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = (byte) (BC_INT64_SHORT_ZERO + (val >> 16));
            bytes[off++] = (byte) (val >> 8);
            bytes[off++] = (byte) (val);
            return;
        }

        if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
            int minCapacity = off + 5;
            if (minCapacity - bytes.length > 0) {
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = BC_INT64_INT;
            bytes[off++] = (byte) (val >>> 24);
            bytes[off++] = (byte) (val >>> 16);
            bytes[off++] = (byte) (val >>> 8);
            bytes[off++] = (byte) val;
            return;
        }

        int minCapacity = off + 9;
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
        bytes[off++] = BC_INT64;
        bytes[off++] = (byte) (val >>> 56);
        bytes[off++] = (byte) (val >>> 48);
        bytes[off++] = (byte) (val >>> 40);
        bytes[off++] = (byte) (val >>> 32);
        bytes[off++] = (byte) (val >>> 24);
        bytes[off++] = (byte) (val >>> 16);
        bytes[off++] = (byte) (val >>> 8);
        bytes[off++] = (byte) val;
    }

    @Override
    public void writeInt64(long[] value) {
        if (value == null) {
            writeArrayNull();
            return;
        }

        // inline startArray(value.length);
        int size = value.length;
        level++;
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off++] = BC_ARRAY;
            writeInt32(size);
        }

        for (int i = 0; i < value.length; i++) {
            long val = value[i];
            if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
                // inline ensureCapacity(off + 1);
                if (off == bytes.length) {
                    int minCapacity = off + 1;

                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = (byte) val;
                continue;
            }

            if (val >= INT64_BYTE_MIN && val <= INT64_BYTE_MAX) {
                int minCapacity = off + 2;
                if (minCapacity - bytes.length > 0) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = (byte) (BC_INT64_BYTE_ZERO + (val >> 8));
                bytes[off++] = (byte) (val);
                continue;
            }

            if (val >= INT64_SHORT_MIN && val <= INT64_SHORT_MAX) {
                int minCapacity = off + 3;
                if (minCapacity - bytes.length > 0) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = (byte) (BC_INT64_SHORT_ZERO + (val >> 16));
                bytes[off++] = (byte) (val >> 8);
                bytes[off++] = (byte) (val);
                continue;
            }

            int minCapacity = off + 9;
            if (minCapacity - bytes.length > 0) {
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }
            bytes[off++] = BC_INT64;
            bytes[off++] = (byte) (val >>> 56);
            bytes[off++] = (byte) (val >>> 48);
            bytes[off++] = (byte) (val >>> 40);
            bytes[off++] = (byte) (val >>> 32);
            bytes[off++] = (byte) (val >>> 24);
            bytes[off++] = (byte) (val >>> 16);
            bytes[off++] = (byte) (val >>> 8);
            bytes[off++] = (byte) val;
        }

        // inline endArray();
        level--;
    }

    @Override
    public void writeFloat(float value) {
        if (value >= INT32_SHORT_MIN && value <= INT32_SHORT_MAX) {
            int int32Value = (int) value;
            if (int32Value == value) {
                ensureCapacity(off + 1);
                bytes[off++] = BC_FLOAT_INT;
                writeInt32(int32Value);
                return;
            }
        }

        ensureCapacity(off + 5);
        bytes[off++] = BC_FLOAT;
        int i = Float.floatToIntBits(value);
        bytes[off++] = (byte) (i >>> 24);
        bytes[off++] = (byte) (i >>> 16);
        bytes[off++] = (byte) (i >>> 8);
        bytes[off++] = (byte) i;
    }

    @Override
    public void writeFloat(float[] value) {
        if (value == null) {
            writeNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeFloat(value[i]);
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

        if (value == 1) {
            ensureCapacity(off + 1);
            bytes[off++] = BC_DOUBLE_NUM_1;
            return;
        }

        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            long longValue = (long) value;
            if (longValue == value) {
                ensureCapacity(off + 1);
                bytes[off++] = BC_DOUBLE_LONG;
                writeInt64(longValue);
                return;
            }
        }

        ensureCapacity(off + 9);
        bytes[off++] = BC_DOUBLE;
        long i = Double.doubleToLongBits(value);
        bytes[off++] = (byte) (i >>> 56);
        bytes[off++] = (byte) (i >>> 48);
        bytes[off++] = (byte) (i >>> 40);
        bytes[off++] = (byte) (i >>> 32);
        bytes[off++] = (byte) (i >>> 24);
        bytes[off++] = (byte) (i >>> 16);
        bytes[off++] = (byte) (i >>> 8);
        bytes[off++] = (byte) i;
    }

    @Override
    public void writeDouble(double[] value) {
        if (value == null) {
            writeNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeDouble(value[i]);
        }
        endArray();
    }

    @Override
    public void writeInt16(short[] value) {
        if (value == null) {
            writeNull();
            return;
        }
        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeInt32(value[i]);
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
        level++;
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        if (size <= ARRAY_FIX_LEN) {
            bytes[off++] = (byte) (BC_ARRAY_FIX_MIN + size);
        } else {
            bytes[off++] = BC_ARRAY;
            writeInt32(size);
        }

        for (int i = 0; i < values.length; i++) {
            int val = values[i];

            if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
                if (off == bytes.length) {
                    int minCapacity = off + 1;

                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = (byte) val;
                continue;
            }

            if (val >= INT32_BYTE_MIN && val <= INT32_BYTE_MAX) {
                int minCapacity = off + 2;
                if (minCapacity - bytes.length > 0) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
                bytes[off++] = (byte) (val);
                continue;
            }

            if (val >= INT32_SHORT_MIN && val <= INT32_SHORT_MAX) {
                int minCapacity = off + 3;
                if (minCapacity - bytes.length > 0) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = (byte) (BC_INT32_SHORT_ZERO + (val >> 16));
                bytes[off++] = (byte) (val >> 8);
                bytes[off++] = (byte) (val);
                continue;
            }

            int minCapacity = off + 5;
            if (minCapacity - bytes.length > 0) {
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = BC_INT32;
            bytes[off++] = (byte) (val >>> 24);
            bytes[off++] = (byte) (val >>> 16);
            bytes[off++] = (byte) (val >>> 8);
            bytes[off++] = (byte) val;
        }

        // inline endArray();
        level--;
    }

    @Override
    public void writeInt8(byte val) {
        int minCapacity = off + 2;
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        bytes[off++] = BC_INT8;
        bytes[off++] = val;
    }

    @Override
    public void writeInt16(short val) {
        int minCapacity = off + 3;
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        bytes[off++] = BC_INT16;
        bytes[off++] = (byte) (val >>> 8);
        bytes[off++] = (byte) val;
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
            if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
                if (off == bytes.length) {
                    int minCapacity = off + 1;

                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                bytes[off++] = (byte) val;
                return;
            }
            writeInt32(val);
        }
    }

    @Override
    public void writeInt32(int val) {
        if (val >= BC_INT32_NUM_MIN && val <= BC_INT32_NUM_MAX) {
            if (off == bytes.length) {
                int minCapacity = off + 1;

                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = (byte) val;
            return;
        }

        if (val >= INT32_BYTE_MIN && val <= INT32_BYTE_MAX) {
            int minCapacity = off + 2;
            if (minCapacity - bytes.length > 0) {
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = (byte) (BC_INT32_BYTE_ZERO + (val >> 8));
            bytes[off++] = (byte) (val);
            return;
        }

        if (val >= INT32_SHORT_MIN && val <= INT32_SHORT_MAX) {
            int minCapacity = off + 3;
            if (minCapacity - bytes.length > 0) {
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }

            bytes[off++] = (byte) (BC_INT32_SHORT_ZERO + (val >> 16));
            bytes[off++] = (byte) (val >> 8);
            bytes[off++] = (byte) (val);
            return;
        }

        int minCapacity = off + 5;
        if (minCapacity - bytes.length > 0) {
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }

        bytes[off++] = BC_INT32;
        bytes[off++] = (byte) (val >>> 24);
        bytes[off++] = (byte) (val >>> 16);
        bytes[off++] = (byte) (val >>> 8);
        bytes[off++] = (byte) val;
    }

    @Override
    public void writeArrayNull() {
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
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
            int oldCapacity = this.bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            this.bytes = Arrays.copyOf(this.bytes, newCapacity);
        }
        System.arraycopy(bytes, 0, this.bytes, off, bytes.length);
        off += bytes.length;
    }

    @Override
    public void writeNameRaw(byte[] name, long nameHash) {
        if (symbolTable != null) {
            int symbol = symbolTable.getOrdinalByHashCode(nameHash);
            if (symbol != -1) {
                int minCapacity = off + 2;
                if (minCapacity - bytes.length > 0) {
                    int oldCapacity = bytes.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }
                    if (newCapacity - MAX_ARRAY_SIZE > 0) {
                        throw new OutOfMemoryError();
                    }

                    // minCapacity is usually close to size, so this is a win:
                    bytes = Arrays.copyOf(bytes, newCapacity);
                }

                this.bytes[off++] = BC_SYMBOL;
                writeInt32(-symbol);
                return;
            }
        }

        int symbol = -1;
        if (symbols != null) {
            symbol = symbols.get(nameHash);
        }

        if (symbol != -1) {
            if (off == this.bytes.length) {
                int minCapacity = off + 1;
                int oldCapacity = this.bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.bytes = Arrays.copyOf(this.bytes, newCapacity);
            }
            this.bytes[off++] = BC_SYMBOL;
            writeInt32(symbol);
            return;
        }

        if ((context.features & Feature.WriteNameAsSymbol.mask) == 0) {
            int minCapacity = this.off + name.length;
            if (minCapacity - this.bytes.length > 0) {
                int oldCapacity = this.bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                this.bytes = Arrays.copyOf(this.bytes, newCapacity);
            }

            System.arraycopy(name, 0, this.bytes, off, name.length);
            off += name.length;
            return;
        }

        if (symbols == null) {
            symbols = new TLongIntHashMap();
        }
        symbols.put(nameHash, symbol = symbolIndex++);

        int minCapacity = this.off + 1 + name.length;
        if (minCapacity - this.bytes.length > 0) {
            int oldCapacity = this.bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            this.bytes = Arrays.copyOf(this.bytes, newCapacity);
        }
        this.bytes[off++] = BC_SYMBOL;
        System.arraycopy(name, 0, this.bytes, off, name.length);
        off += name.length;
        writeInt32(symbol);
    }

    @Override
    public void writeLocalDate(LocalDate date) {
        if (date == null) {
            writeNull();
            return;
        }

        ensureCapacity(off + 5);

        bytes[off++] = BC_LOCAL_DATE;
        int year = date.getYear();
        bytes[off++] = (byte) (year >>> 8);
        bytes[off++] = (byte) year;
        bytes[off++] = (byte) date.getMonthValue();
        bytes[off++] = (byte) date.getDayOfMonth();
    }

    @Override
    public void writeLocalTime(LocalTime time) {
        if (time == null) {
            writeNull();
            return;
        }

        ensureCapacity(off + 4);

        bytes[off++] = BC_LOCAL_TIME;
        bytes[off++] = (byte) time.getHour();
        bytes[off++] = (byte) time.getMinute();
        bytes[off++] = (byte) time.getSecond();

        int nano = time.getNano();
        writeInt32(nano);
    }

    @Override
    public void writeLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        ensureCapacity(off + 8);

        bytes[off++] = BC_LOCAL_DATETIME;
        int year = dateTime.getYear();
        bytes[off++] = (byte) (year >>> 8);
        bytes[off++] = (byte) year;
        bytes[off++] = (byte) dateTime.getMonthValue();
        bytes[off++] = (byte) dateTime.getDayOfMonth();
        bytes[off++] = (byte) dateTime.getHour();
        bytes[off++] = (byte) dateTime.getMinute();
        bytes[off++] = (byte) dateTime.getSecond();

        int nano = dateTime.getNano();
        writeInt32(nano);
    }

    @Override
    public void writeZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        ensureCapacity(off + 8);

        bytes[off++] = BC_TIMESTAMP_WITH_TIMEZONE;
        int year = dateTime.getYear();
        bytes[off++] = (byte) (year >>> 8);
        bytes[off++] = (byte) year;
        bytes[off++] = (byte) dateTime.getMonthValue();
        bytes[off++] = (byte) dateTime.getDayOfMonth();
        bytes[off++] = (byte) dateTime.getHour();
        bytes[off++] = (byte) dateTime.getMinute();
        bytes[off++] = (byte) dateTime.getSecond();

        int nano = dateTime.getNano();
        writeInt32(nano);

        String zoneId = dateTime.getZone().getId();
        writeString(zoneId);
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

        ensureCapacity(off + 18);
        bytes[off++] = BC_BINARY;
        bytes[off++] = BC_INT32_NUM_16;

        bytes[off++] = (byte) (msb >>> 56);
        bytes[off++] = (byte) (msb >>> 48);
        bytes[off++] = (byte) (msb >>> 40);
        bytes[off++] = (byte) (msb >>> 32);
        bytes[off++] = (byte) (msb >>> 24);
        bytes[off++] = (byte) (msb >>> 16);
        bytes[off++] = (byte) (msb >>> 8);
        bytes[off++] = (byte) msb;

        bytes[off++] = (byte) (lsb >>> 56);
        bytes[off++] = (byte) (lsb >>> 48);
        bytes[off++] = (byte) (lsb >>> 40);
        bytes[off++] = (byte) (lsb >>> 32);
        bytes[off++] = (byte) (lsb >>> 24);
        bytes[off++] = (byte) (lsb >>> 16);
        bytes[off++] = (byte) (lsb >>> 8);
        bytes[off++] = (byte) lsb;
    }

    @Override
    public void writeBigInt(BigInteger value, long features) {
        if (value == null) {
            writeNull();
            return;
        }

        if (value.compareTo(BIGINT_INT64_MIN) >= 0 && value.compareTo(BIGINT_INT64_MAX) <= 0) {
            if (off == bytes.length) {
                int minCapacity = off + 1;
                int oldCapacity = bytes.length;
                int newCapacity = oldCapacity + (oldCapacity >> 1);
                if (newCapacity - minCapacity < 0) {
                    newCapacity = minCapacity;
                }
                if (newCapacity - MAX_ARRAY_SIZE > 0) {
                    throw new OutOfMemoryError();
                }

                // minCapacity is usually close to size, so this is a win:
                bytes = Arrays.copyOf(bytes, newCapacity);
            }
            bytes[off++] = BC_BIGINT_LONG;
            long int64Value = value.longValue();
            writeInt64(int64Value);
            return;
        }

        byte[] bytes = value.toByteArray();
        ensureCapacity(off + 5 + bytes.length);

        this.bytes[off++] = BC_BIGINT;
        writeInt32(bytes.length);
        System.arraycopy(bytes, 0, this.bytes, off, bytes.length);
        off += bytes.length;
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
    public void writeDecimal(BigDecimal value, long features) {
        writeDecimal(value);
    }

    @Override
    public void writeDecimal(BigDecimal value) {
        if (value == null) {
            writeNull();
            return;
        }

        BigInteger unscaledValue = value.unscaledValue();
        int scale = value.scale();

        if (scale == 0
                && unscaledValue.compareTo(BIGINT_INT64_MIN) >= 0
                && unscaledValue.compareTo(BIGINT_INT64_MAX) <= 0) {
            ensureCapacity(off + 1);
            this.bytes[off++] = BC_DECIMAL_LONG;
            writeInt64(
                    unscaledValue.longValue()
            );
            return;
        }

        ensureCapacity(off + 1);
        this.bytes[off++] = BC_DECIMAL;
        writeInt32(scale);
        writeBigInt(unscaledValue);
    }

    @Override
    public void writeBool(boolean value) {
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
        }
        this.bytes[off++] = value ? BC_TRUE : BC_FALSE;
    }

    @Override
    public void writeBool(boolean[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        startArray(value.length);
        for (int i = 0; i < value.length; i++) {
            writeBool(value[i]);
        }
        endArray();
    }

    @Override
    public void writeReference(String path) {
        if (off == bytes.length) {
            int minCapacity = off + 1;
            int oldCapacity = bytes.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                throw new OutOfMemoryError();
            }

            // minCapacity is usually close to size, so this is a win:
            bytes = Arrays.copyOf(bytes, newCapacity);
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
    public void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second) {
        ensureCapacity(off + 8);

        bytes[off++] = BC_LOCAL_DATETIME;
        bytes[off++] = (byte) (year >>> 8);
        bytes[off++] = (byte) year;
        bytes[off++] = (byte) month;
        bytes[off++] = (byte) dayOfMonth;
        bytes[off++] = (byte) hour;
        bytes[off++] = (byte) minute;
        bytes[off++] = (byte) second;

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
            int offsetSeconds
    ) {
        throw new JSONException("unsupported operation");
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
    public void writeRaw(char ch) {
        throw new JSONException("UnsupportedOperation");
    }

    @Override
    public void writeNameRaw(byte[] bytes) {
        throw new JSONException("UnsupportedOperation");
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
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, off);
    }

    @Override
    public int flushTo(OutputStream to) throws IOException {
        int len = off;
        to.write(bytes, 0, off);
        off = 0;
        return len;
    }

    @Override
    public int flushTo(OutputStream out, Charset charset) throws IOException {
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
