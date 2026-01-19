package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.*;

class FieldWriterEnum<T>
        extends FieldWriter<T> {
    final byte[][] valueNameCacheUTF8;
    final char[][] valueNameCacheUTF16;

    final byte[][] utf8ValueCache;
    final char[][] utf16ValueCache;

    final Class enumType;
    final Enum[] enumConstants;
    final long[] hashCodes;
    final long[] hashCodesSymbolCache;

    FieldWriterEnum(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class<? extends Enum> enumClass,
            Field field,
            Method method,
            Function<T, Enum> function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, enumClass, field, method, function);

        this.enumType = enumClass;
        this.enumConstants = enumClass.getEnumConstants();
        this.hashCodes = new long[enumConstants.length];
        this.hashCodesSymbolCache = new long[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            hashCodes[i] = Fnv.hashCode64(enumConstants[i].name());
        }

        valueNameCacheUTF8 = new byte[enumConstants.length][];
        valueNameCacheUTF16 = new char[enumConstants.length][];

        utf8ValueCache = new byte[enumConstants.length][];
        utf16ValueCache = new char[enumConstants.length][];
    }

    public final int writeEnumValueJSONB(byte[] bytes, int off, Enum e, SymbolTable symbolTable, long features) {
        if (e == null) {
            bytes[off] = JSONB.Constants.BC_NULL;
            return off + 1;
        }

        features |= this.features;
        boolean usingOrdinal = (features & (JSONWriter.Feature.WriteEnumUsingToString.mask | JSONWriter.Feature.WriteEnumsUsingName.mask)) == 0;
        boolean usingToString = (features & JSONWriter.Feature.WriteEnumUsingToString.mask) != 0;
        String str = usingToString ? e.toString() : e.name();
        if (IOUtils.isASCII(str)) {
            return JSONWriterJSONB.IO.writeSymbol(bytes, off, str, symbolTable);
        }

        if (usingOrdinal) {
            return JSONWriterJSONB.IO.writeInt32(bytes, off, e.ordinal());
        }

        return JSONWriterJSONB.IO.writeString(bytes, off, str);
    }

    @Override
    public final void writeEnumJSONB(JSONWriterJSONB jsonWriter, Enum e, long features) {
        if (e == null) {
            return;
        }

        boolean usingOrdinal = (features & (JSONWriter.Feature.WriteEnumUsingToString.mask | JSONWriter.Feature.WriteEnumsUsingName.mask)) == 0;
        boolean usingToString = (features & JSONWriter.Feature.WriteEnumUsingToString.mask) != 0;

        int ordinal = e.ordinal();
        SymbolTable symbolTable = jsonWriter.symbolTable;
        if (symbolTable != null && usingOrdinal && !usingToString) {
            if (writeSymbolNameOrdinal(jsonWriter, ordinal, symbolTable)) {
                return;
            }
        }

        if (usingToString) {
            writeJSONBToString(jsonWriter, e, symbolTable);
            return;
        }

        if (usingOrdinal) {
            int symbol;
            if (symbolTable != null) {
                int symbolTableIdentity = System.identityHashCode(symbolTable);
                if (nameSymbolCache == 0) {
                    symbol = symbolTable.getOrdinalByHashCode(hashCode);
                    nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                } else {
                    int identity = (int) nameSymbolCache;
                    if (identity == symbolTableIdentity) {
                        symbol = (int) (nameSymbolCache >> 32);
                    } else {
                        symbol = symbolTable.getOrdinalByHashCode(hashCode);
                        nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                    }
                }
            } else {
                symbol = -1;
            }

            if (symbol != -1) {
                jsonWriter.writeSymbol(-symbol);
            } else {
                jsonWriter.writeNameRaw(nameJSONB, hashCode);
            }

            jsonWriter.writeInt32(ordinal);
            return;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeString(e.name());
    }

    private boolean writeSymbolNameOrdinal(JSONWriter jsonWriter, int ordinal, SymbolTable symbolTable) {
        int symbolTableIdentity = System.identityHashCode(symbolTable);
        long enumNameCache = hashCodesSymbolCache[ordinal];
        int enumSymbol;
        if (enumNameCache == 0) {
            enumSymbol = symbolTable.getOrdinalByHashCode(hashCodes[ordinal]);
            hashCodesSymbolCache[ordinal] = ((long) enumSymbol << 32) | symbolTableIdentity;
        } else {
            int identity = (int) enumNameCache;
            if (identity == symbolTableIdentity) {
                enumSymbol = (int) (enumNameCache >> 32);
            } else {
                enumSymbol = symbolTable.getOrdinalByHashCode(hashCodes[ordinal]);
                hashCodesSymbolCache[ordinal] = ((long) enumSymbol << 32) | symbolTableIdentity;
            }
        }

        int namingOrdinal = enumSymbol;
        if (namingOrdinal >= 0) {
            int symbol;
            if (nameSymbolCache == 0) {
                symbol = symbolTable.getOrdinalByHashCode(hashCode);
                if (symbol != -1) {
                    nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                }
            } else {
                int identity = (int) nameSymbolCache;
                if (identity == symbolTableIdentity) {
                    symbol = (int) (nameSymbolCache >> 32);
                } else {
                    symbol = symbolTable.getOrdinalByHashCode(hashCode);
                    nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                }
            }

            if (symbol != -1) {
                jsonWriter.writeSymbol(-symbol);
            } else {
                jsonWriter.writeNameRaw(nameJSONB, hashCode);
            }

            jsonWriter.writeRaw(JSONB.Constants.BC_STR_ASCII);
            jsonWriter.writeInt32(-namingOrdinal);
            return true;
        }
        return false;
    }

    private void writeJSONBToString(JSONWriter jsonWriter, Enum e, SymbolTable symbolTable) {
        int symbol;
        if (symbolTable != null) {
            int symbolTableIdentity = System.identityHashCode(symbolTable);

            if (nameSymbolCache == 0) {
                symbol = symbolTable.getOrdinalByHashCode(hashCode);
                nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
            } else {
                int identity = (int) nameSymbolCache;
                if (identity == symbolTableIdentity) {
                    symbol = (int) (nameSymbolCache >> 32);
                } else {
                    symbol = symbolTable.getOrdinalByHashCode(hashCode);
                    nameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                }
            }
        } else {
            symbol = -1;
        }

        if (symbol != -1) {
            jsonWriter.writeSymbol(-symbol);
        } else {
            jsonWriter.writeNameRaw(nameJSONB, hashCode);
        }

        jsonWriter.writeString(e.toString());
    }

    @Override
    public final void writeEnumUTF8(JSONWriterUTF8 jsonWriter, Enum e, long features) {
        if ((features & MASK_WRITE_ENUM_USING_TO_STRING) == 0) {
            final int ordinal = e.ordinal();
            if ((features & MASK_WRITE_ENUM_USING_ORDINAL) != 0) {
                writeEnumUsingOrdinal(jsonWriter, ordinal);
                return;
            }

            if ((features & MASK_UNQUOTE_FIELD_NAME) == 0) {
                byte[] bytes = valueNameCacheUTF8[ordinal];

                if (bytes == null) {
                    valueNameCacheUTF8[ordinal] = bytes = getNameBytes(ordinal);
                }
                jsonWriter.writeNameRaw(bytes);
                return;
            }
        }

        writeFieldNameUTF8(jsonWriter);
        jsonWriter.writeString(e.toString());
    }

    @Override
    public final void writeEnumUTF16(JSONWriterUTF16 jsonWriter, Enum e, long features) {
        if ((features & MASK_WRITE_ENUM_USING_TO_STRING) == 0) {
            final int ordinal = e.ordinal();
            if ((features & MASK_WRITE_ENUM_USING_ORDINAL) != 0) {
                writeEnumUsingOrdinal(jsonWriter, ordinal);
                return;
            }

            if ((features & MASK_UNQUOTE_FIELD_NAME) == 0) {
                char[] chars = valueNameCacheUTF16[ordinal];
                if (chars == null) {
                    valueNameCacheUTF16[ordinal] = chars = getNameChars(ordinal);
                }
                jsonWriter.writeNameRaw(chars);
                return;
            }
        }

        writeFieldNameUTF16(jsonWriter);
        jsonWriter.writeString(e.toString());
    }

    private void writeEnumUsingOrdinal(JSONWriterUTF8 jsonWriter, int ordinal) {
        if ((features & MASK_UNQUOTE_FIELD_NAME) == 0) {
            byte[] bytes = utf8ValueCache[ordinal];
            if (bytes == null) {
                utf8ValueCache[ordinal] = bytes = getBytes(ordinal);
            }
            jsonWriter.writeNameRaw(bytes);
            return;
        }

        writeFieldNameUTF8(jsonWriter);
        jsonWriter.writeInt32(ordinal);
    }

    private void writeEnumUsingOrdinal(JSONWriterUTF16 jsonWriter, int ordinal) {
        if ((features & MASK_UNQUOTE_FIELD_NAME) == 0) {
            char[] chars = utf16ValueCache[ordinal];
            if (chars == null) {
                utf16ValueCache[ordinal] = chars = getChars(ordinal);
            }
            jsonWriter.writeNameRaw(chars);
            return;
        }

        writeFieldNameUTF16(jsonWriter);
        jsonWriter.writeInt32(ordinal);
    }

    private char[] getNameChars(int ordinal) {
        char[] chars;
        String name = enumConstants[ordinal].name();
        chars = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + name.length() + 2);
        chars[nameWithColonUTF16.length] = '"';
        name.getChars(0, name.length(), chars, nameWithColonUTF16.length + 1);
        chars[chars.length - 1] = '"';
        return chars;
    }

    private byte[] getNameBytes(int ordinal) {
        byte[] bytes;
        byte[] nameUft8Bytes = enumConstants[ordinal].name().getBytes(StandardCharsets.UTF_8);
        bytes = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + nameUft8Bytes.length + 2);
        bytes[nameWithColonUTF8.length] = '"';
        int index = nameWithColonUTF8.length + 1;
        for (byte b : nameUft8Bytes) {
            bytes[index++] = b;
        }
        bytes[bytes.length - 1] = '"';
        return bytes;
    }

    private char[] getChars(int ordinal) {
        char[] chars;
        int size = IOUtils.stringSize(ordinal);
        char[] original = Arrays.copyOf(nameWithColonUTF16, nameWithColonUTF16.length + size);
        chars = Arrays.copyOf(original, original.length);
        IOUtils.getChars(ordinal, chars.length, chars);
        return chars;
    }

    private byte[] getBytes(int ordinal) {
        byte[] bytes;
        int size = IOUtils.stringSize(ordinal);
        byte[] original = Arrays.copyOf(nameWithColonUTF8, nameWithColonUTF8.length + size);
        bytes = Arrays.copyOf(original, original.length);
        IOUtils.getChars(ordinal, bytes.length, bytes);
        return bytes;
    }

    @Override
    public final void writeValue(JSONWriter jsonWriter, T object) {
        Enum value = (Enum) propertyAccessor.getObject(object);
        jsonWriter.writeEnum(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Enum value = (Enum) propertyAccessor.getObject(object);
        long features = this.features | jsonWriter.getFeatures();
        if (value == null) {
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        if (jsonWriter instanceof JSONWriterJSONB) {
            writeEnumJSONB((JSONWriterJSONB) jsonWriter, value, features);
        } else {
            writeEnum(jsonWriter, value);
        }
        return true;
    }
}
