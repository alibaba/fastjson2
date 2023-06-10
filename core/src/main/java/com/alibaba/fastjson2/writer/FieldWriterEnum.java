package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.SymbolTable;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class FieldWriterEnum
        extends FieldWriter {
    final byte[][] valueNameCacheUTF8;
    final char[][] valueNameCacheUTF16;

    final byte[][] utf8ValueCache;
    final char[][] utf16ValueCache;

    final Class enumType;
    final Enum[] enumConstants;
    final long[] hashCodes;
    final long[] hashCodesSymbolCache;

    protected FieldWriterEnum(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class<? extends Enum> enumClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldType, enumClass, field, method);

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

    @Override
    public final void writeEnumJSONB(JSONWriter jsonWriter, Enum e) {
        if (e == null) {
            return;
        }

        long features = jsonWriter.getFeatures(this.features);
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
    public final void writeEnum(JSONWriter jsonWriter, Enum e) {
        long features = jsonWriter.getFeatures(this.features);

        if ((features & JSONWriter.Feature.WriteEnumUsingToString.mask) == 0) {
            if (jsonWriter.jsonb) {
                writeEnumJSONB(jsonWriter, e);
                return;
            }

            boolean unquoteName = (features & JSONWriter.Feature.UnquoteFieldName.mask) != 0;
            final boolean utf8 = jsonWriter.utf8;
            final boolean utf16 = !jsonWriter.utf8 && jsonWriter.utf16;
            final int ordinal = e.ordinal();

            if ((features & JSONWriter.Feature.WriteEnumUsingOrdinal.mask) != 0) {
                if (!unquoteName) {
                    if (utf8) {
                        byte[] bytes = utf8ValueCache[ordinal];
                        if (bytes == null) {
                            utf8ValueCache[ordinal] = bytes = getBytes(ordinal);
                        }
                        jsonWriter.writeNameRaw(bytes);
                        return;
                    }

                    if (utf16) {
                        char[] chars = utf16ValueCache[ordinal];
                        if (chars == null) {
                            utf16ValueCache[ordinal] = chars = getChars(ordinal);
                        }
                        jsonWriter.writeNameRaw(chars);
                        return;
                    }
                }

                writeFieldName(jsonWriter);
                jsonWriter.writeInt32(ordinal);
                return;
            }

            if (!unquoteName) {
                if (utf8) {
                    byte[] bytes = valueNameCacheUTF8[ordinal];

                    if (bytes == null) {
                        valueNameCacheUTF8[ordinal] = bytes = getNameBytes(ordinal);
                    }
                    jsonWriter.writeNameRaw(bytes);
                    return;
                }

                if (utf16) {
                    char[] chars = valueNameCacheUTF16[ordinal];
                    if (chars == null) {
                        valueNameCacheUTF16[ordinal] = chars = getNameChars(ordinal);
                    }
                    jsonWriter.writeNameRaw(chars);
                    return;
                }
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeString(e.toString());
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
    public final void writeValue(JSONWriter jsonWriter, Object object) {
        Enum value = (Enum) getFieldValue(object);
        jsonWriter.writeEnum(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        Enum value = (Enum) getFieldValue(object);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        if (jsonWriter.jsonb) {
            writeEnumJSONB(jsonWriter, value);
        } else {
            writeEnum(jsonWriter, value);
        }
        return true;
    }
}
