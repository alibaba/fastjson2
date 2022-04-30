package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import static com.alibaba.fastjson2.JSONFactory.Utils.STRING_CREATOR_JDK8;
import static com.alibaba.fastjson2.JSONFactory.Utils.STRING_CREATOR_ERROR;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.alibaba.fastjson2.JSONFactory.*;

public abstract class JSONWriter implements Closeable {
    protected final Context context;
    protected final Charset charset;
    protected final boolean utf8;
    protected final boolean utf16;

    protected boolean startObject = false;
    protected int level;
    protected int off;
    protected Object rootObject;
    protected IdentityHashMap<Object, Path> refs;
    protected Path path;
    protected String lastReference;

    protected JSONWriter(Context context, Charset charset) {
        this.context = context;
        this.charset = charset;
        this.utf8 = charset == StandardCharsets.UTF_8;
        this.utf16 = charset == StandardCharsets.UTF_16;
    }

    public boolean isUTF8() {
        return utf8;
    }

    public boolean isUTF16() {
        return utf16;
    }

    public boolean isJSONB() {
        return false;
    }

    public boolean isIgnoreNoneSerializable() {
        return (context.features & Feature.IgnoreNoneSerializable.mask) != 0;
    }

    public JSONB.SymbolTable getSymbolTable() {
        throw new UnsupportedOperationException();
    }

    public void config(Feature... features) {
        context.config(features);
    }

    public void config(Feature feature, boolean state) {
        context.config(feature, state);
    }

    public Context getContext() {
        return context;
    }

    public int level() {
        return level;
    }

    public void setRootObject(Object rootObject) {
        this.rootObject = rootObject;
        if ((context.features & JSONWriter.Feature.ReferenceDetection.mask) != 0) {
            if (refs == null) {
                refs = new IdentityHashMap(8);
            }

            refs.putIfAbsent(rootObject, this.path = Path.ROOT);
        }
    }

    public String setPath(String name, Object object) {
        if ((context.features & Feature.ReferenceDetection.mask) == 0) {
            return null;
        }

        this.path = new Path(this.path, name);

        if (refs == null) {
            refs = new IdentityHashMap(8);
        }

        Path previous = refs.get(object);
        if (previous == null) {
            refs.put(object, this.path);
            return null;
        }
        return previous.toString();
    }

    public String setPath(int index, Object object) {
        if ((context.features & Feature.ReferenceDetection.mask) == 0) {
            return null;
        }

        if (this.path == Path.ROOT) {
            if (index == 0) {
                this.path = Path.ROOT_0;
            } else if (index == 1) {
                this.path = Path.ROOT_1;
            } else {
                this.path = new Path(Path.ROOT, index);
            }
        } else {
            this.path = new Path(this.path, index);
        }

        if (refs == null) {
            refs = new IdentityHashMap(8);
        }

        Path previous = refs.get(object);
        if (previous == null) {
            refs.put(object, this.path);
            return null;
        }
        return previous.toString();
    }

    public void popPath(Object object) {
        if (this.path == null) {
            return;
        }

        if ((context.features & Feature.ReferenceDetection.mask) == 0) {
            return;
        }

        this.path = this.path.parent;
    }


    public boolean hasFilter() {
        return context.propertyPreFilter != null
                || context.propertyFilter != null
                || context.nameFilter != null
                || context.valueFilter != null
                ;
    }

    public boolean isWriteNulls() {
        return (context.features & (Feature.WriteNulls.mask | Feature.NullAsDefaultValue.mask)) != 0;
    }

    public boolean isNotWriteDefaultValue() {
        return (context.features & Feature.NotWriteDefaultValue.mask) != 0;
    }

    public boolean isRefDetect() {
        return (context.features & Feature.ReferenceDetection.mask) != 0;
    }

    public boolean isRefDetect(Object object) {
        return (context.features & Feature.ReferenceDetection.mask) != 0
                && object != null
                && !ObjectWriterProvider.isNotReferenceDetect(object.getClass())
                ;
    }

    public boolean isBeanToArray() {
        return (context.features & Feature.BeanToArray.mask) != 0;
    }

    public boolean isBeanToArray(long features) {
        return ((context.features | features) & Feature.BeanToArray.mask) != 0;
    }

    public boolean isEnabled(Feature feature) {
        return (context.features & feature.mask) != 0;
    }

    public boolean isEnabled(long feature) {
        return (context.features & feature) != 0;
    }

    public long getFeatures() {
        return context.features;
    }

    public long getFeatures(long features) {
        return context.features | features;
    }

    public boolean isIgnoreErrorGetter() {
        return (context.features & Feature.IgnoreErrorGetter.mask) != 0;
    }

    public boolean isWriteTypeInfo(Object object, Type fieldType) {
        long features = context.features;
        if ((features & Feature.WriteClassName.mask) == 0) {
            return false;
        }

        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        Class fieldClass = null;
        if (fieldType instanceof Class) {
            fieldClass = (Class) fieldType;
        } else if (fieldType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) fieldType).getRawType();
            if (rawType instanceof Class) {
                fieldClass = (Class) rawType;
            }
        }
        if (objectClass == fieldClass) {
            return false;
        }

        if ((features & Feature.NotWriteHashMapArrayListClassName.mask) != 0) {
            if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & Feature.NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    public boolean isWriteTypeInfo(Object object) {
        long features = context.features;
        if ((features & Feature.WriteClassName.mask) == 0) {
            return false;
        }

        if ((features & Feature.NotWriteHashMapArrayListClassName.mask) != 0
                && object != null) {
            Class objectClass = object.getClass();
            if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & Feature.NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    public boolean isWriteTypeInfo(Object object, Type fieldType, long features) {
        features |= context.features;

        if ((features & Feature.WriteClassName.mask) == 0) {
            return false;
        }

        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        Class fieldClass = null;
        if (fieldType instanceof Class) {
            fieldClass = (Class) fieldType;
        } else if (fieldType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) fieldType).getRawType();
            if (rawType instanceof Class) {
                fieldClass = (Class) rawType;
            }
        }
        if (objectClass == fieldClass) {
            return false;
        }

        if ((features & Feature.NotWriteHashMapArrayListClassName.mask) != 0) {
            if (objectClass == HashMap.class) {
                if (fieldClass == null || fieldClass == Object.class || fieldClass == Map.class || fieldClass == AbstractMap.class) {
                    return false;
                }
            } else if (objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & Feature.NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    public boolean isWriteTypeInfo(Object object, Class fieldClass, long features) {
        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        if (objectClass == fieldClass) {
            return false;
        }

        features |= context.features;

        if ((features & Feature.WriteClassName.mask) == 0) {
            return false;
        }

        if ((features & Feature.NotWriteHashMapArrayListClassName.mask) != 0) {
            if (objectClass == HashMap.class) {
                if (fieldClass == null || fieldClass == Object.class || fieldClass == Map.class || fieldClass == AbstractMap.class) {
                    return false;
                }
            } else if (objectClass == ArrayList.class) {
                return false;
            }
        }

        return (features & Feature.NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    public boolean isWriteMapTypeInfo(Object object, Class fieldClass, long features) {
        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
        if (objectClass == fieldClass) {
            return false;
        }

        features |= context.features;

        if ((features & Feature.WriteClassName.mask) == 0) {
            return false;
        }

        if ((features & Feature.NotWriteHashMapArrayListClassName.mask) != 0) {
            if (objectClass == HashMap.class) {
                return false;
            }
        }

        return (features & Feature.NotWriteRootClassName.mask) == 0 || object != this.rootObject;
    }

    public boolean isWriteTypeInfo(Object object, long features) {
        features |= context.features;

        if ((features & Feature.WriteClassName.mask) == 0) {
            return false;
        }

        if ((features & Feature.NotWriteHashMapArrayListClassName.mask) != 0) {
            if (object != null) {
                Class objectClass = object.getClass();
                if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                    return false;
                }
            }
        }

        return (features & Feature.NotWriteRootClassName.mask) == 0
                || object != this.rootObject;
    }

    public ObjectWriter getObjectWriter(Class objectClass) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        return context.provider.getObjectWriter(objectClass, objectClass, fieldBased);
    }

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        boolean fieldBased = (context.features & Feature.FieldBased.mask) != 0;
        return context.provider.getObjectWriter(objectType, objectClass, fieldBased);
    }

    public static JSONWriter of() {
        Context writeContext = createWriteContext();
        return JDKUtils.JVM_VERSION == 8 ? new JSONWriterUTF16JDK8(writeContext) : new JSONWriterUTF16(writeContext);
    }

    public static JSONWriter of(Feature... features) {
        Context writeContext = JSONFactory.createWriteContext(features);
        JSONWriterUTF16 jsonWriter = JDKUtils.JVM_VERSION == 8 ? new JSONWriterUTF16JDK8(writeContext) : new JSONWriterUTF16(writeContext);
        for (int i = 0; i < features.length; i++) {
            if (features[i] == Feature.PrettyFormat) {
                return new JSONWriterPretty(jsonWriter);
            }
        }
        return jsonWriter;
    }

    public static JSONWriter ofJSONB() {
        return new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider)
                , null
        );
    }

    public static JSONWriter ofJSONB(JSONB.SymbolTable symbolTable) {
        return new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider)
                , symbolTable
        );
    }

    public static JSONWriter ofPretty() {
        return ofPretty(
                of());
    }

    public static JSONWriter ofPretty(JSONWriter writer) {
        return new JSONWriterPretty(writer);
    }

    public static JSONWriter ofUTF8() {
        if (JDKUtils.JVM_VERSION >= 9) {
            return new JSONWriterUTF8JDK9(
                    JSONFactory.createWriteContext());
        } else {
            return new JSONWriterUTF8(
                    JSONFactory.createWriteContext());
        }
    }

    public static JSONWriter ofUTF8(Feature... features) {
        Context writeContext = createWriteContext(features);

        JSONWriter jsonWriter;
        if (JDKUtils.JVM_VERSION >= 9) {
            jsonWriter = new JSONWriterUTF8JDK9(
                    writeContext);
        } else {
            jsonWriter = new JSONWriterUTF8(
                    writeContext);
        }

        boolean pretty = (writeContext.features & JSONWriter.Feature.PrettyFormat.mask) != 0;
        if (pretty) {
            jsonWriter = new JSONWriterPretty(jsonWriter);
        }
        return jsonWriter;
    }

    public void writeBinary(byte[] bytes) {
        if (bytes == null) {
            writeNull();
            return;
        }
        startArray();
        for (int i = 0; i < bytes.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeInt32(bytes[i]);
        }
        endArray();
    }

    public void writeBase64(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    protected abstract void write0(char ch);

    public abstract void writeRaw(String str);

    public void writeRaw(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public void writeRaw(byte b) {
        throw new UnsupportedOperationException();
    }

    public void writeNameRaw(byte[] bytes, int offset, int len) {
        throw new UnsupportedOperationException();
    }

    public void writeRaw(char[] chars) {
        throw new UnsupportedOperationException();
    }

    public void writeNameRaw(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    public void writeNameRaw(byte[] name, long nameHash) {
        throw new UnsupportedOperationException();
    }

    public void writeNameRaw(char[] chars) {
        throw new UnsupportedOperationException();
    }

    public void writeNameRaw(char[] bytes, int offset, int len) {
        throw new UnsupportedOperationException();
    }

    public void writeName(String name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeString(name);
    }

    public abstract void startObject();

    public abstract void endObject();

    public abstract void startArray();

    public void startArray(int size) {
        throw new UnsupportedOperationException();
    }

    public void startArray(Object array, int size) {
        throw new UnsupportedOperationException();
    }

    public abstract void endArray();

    public abstract void writeComma();

    public void writeColon() {
        write0(':');
    }

    public void writeInt16(short[] value) {
        if (value == null) {
            writeNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeInt16(value[i]);
        }
        endArray();
    }

    public void writeInt8(byte value) {
        writeInt32(value);
    }

    public void writeInt16(short value) {
        writeInt32(value);
    }

    public void writeInt32(int[] value) {
        if (value == null) {
            writeNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeInt32(value[i]);
        }
        endArray();
    }

    public abstract void writeInt32(int value);

    public abstract void writeInt64(long i);

    public void writeMillis(long i) {
        writeInt64(i);
    }

    public void writeInt64(long[] value) {
        if (value == null) {
            writeNull();
            return;
        }
        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeInt64(value[i]);
        }
        endArray();
    }

    public abstract void writeFloat(float value);

    public void writeFloat(float[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeFloat(value[i]);
        }
        endArray();
    }

    public abstract void writeDouble(double value);

    public void writeDouble(double[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeDouble(value[i]);
        }
        endArray();
    }

    public void writeBool(boolean value) {
        if ((context.features & Feature.WriteBooleanAsNumber.mask) != 0) {
            write0(value ? '1' : '0');
        } else {
            writeRaw(value ? "true" : "false");
        }
    }

    public void writeBool(boolean[] value) {
        if (value == null) {
            writeNull();
            return;
        }

        startArray();
        for (int i = 0; i < value.length; i++) {
            if (i != 0) {
                writeComma();
            }
            writeBool(value[i]);
        }
        endArray();
    }

    public void writeNull() {
        writeRaw("null");
    }

    public void writeStringNull() {
        String raw;
        if ((this.context.features & Feature.NullAsDefaultValue.mask) != 0) {
            raw = "";
        } else {
            raw = "null";
        }
        writeRaw(raw);
    }

    public void writeArrayNull() {
        String raw;
        if ((this.context.features & Feature.NullAsDefaultValue.mask) != 0) {
            raw = "[]";
        } else {
            raw = "null";
        }
        writeRaw(raw);
    }

    public void writeNumberNull() {
        String raw;
        if ((this.context.features & Feature.NullAsDefaultValue.mask) != 0) {
            writeInt32(0);
        } else {
            writeNull();
        }
    }

    public void writeBooleanNull() {
        if ((this.context.features & Feature.NullAsDefaultValue.mask) != 0) {
            writeBool(false);
        } else {
            writeNull();
        }
    }

    public abstract void writeDecimal(BigDecimal value);

    public void writeDecimal(BigDecimal value, long features) {
        if (value == null) {
            writeNumberNull();
            return;
        }

        features |= context.features;

        if ((features & Feature.WriteBigDecimalAsPlain.mask) != 0) {
            String str = value.toPlainString();
            writeRaw(str);
            return;
        }

        String str = value.toString();

        if ((features & Feature.BrowserCompatible.mask) != 0
                && (value.compareTo(LOW) < 0 || value.compareTo(HIGH) > 0)) {
            write0('"');
            writeRaw(str);
            write0('"');
        } else {
            writeRaw(str);
        }
    }

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
            writeInt32(e.ordinal());
        }
    }

    public void writeBigInt(BigInteger value) {
        writeBigInt(value, 0);
    }

    public abstract void writeBigInt(BigInteger value, long features);

    public abstract void writeUUID(UUID value);

    public void writeTypeName(String typeName) {
        throw new UnsupportedOperationException();
    }

    public boolean writeTypeName(byte[] typeName, long typeNameHash) {
        throw new UnsupportedOperationException();
    }

    public abstract void writeString(String str);

    public void writeSymbol(String string) {
        writeString(string);
    }

    public void writeString(char ch) {
        write0('"');
        write0(ch);
        write0('"');
    }

    public void writeString(char[] str) {
        if (str == null) {
            writeNull();
            return;
        }

        write0('"');
        boolean special = false;
        for (int i = 0; i < str.length; ++i) {
            if (str[i] == '\\' || str[i] == '"') {
                special = true;
                break;
            }
        }

        if (!special) {
            writeRaw(str);
        } else {
            for (int i = 0; i < str.length; ++i) {
                char ch = str[i];
                if (ch == '\\' || ch == '"') {
                    write0('\\');
                }
                write0(ch);
            }
        }
        write0('"');
    }

    public void writeLocalDate(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int dayOfMonth = date.getDayOfMonth();

        int yearSize = IOUtils.stringSize(year);
        int len = 8 + yearSize;
        char[] chars = new char[len];
        chars[0] = '"';
        Arrays.fill(chars, 1, len - 1, '0');
        IOUtils.getChars(year, yearSize + 1, chars);
        chars[yearSize + 1] = '-';
        IOUtils.getChars(month, yearSize + 4, chars);
        chars[yearSize + 4] = '-';
        IOUtils.getChars(dayOfMonth, yearSize + 7, chars);
        chars[len - 1] = '"';

        writeRaw(chars);
    }

    public void writeLocalDateTime(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int dayOfMonth = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond();
        int nano = dateTime.getNano();

        int yearSize = IOUtils.stringSize(year);
        int len = 17 + yearSize;
        int small;
        if (nano % 1000_000_000 == 0) {
            small = 0;
        } else if (nano % 1000_000_00 == 0) {
            len += 2;
            small = nano / 1000_000_00;
        } else if (nano % 1000_000_0 == 0) {
            len += 3;
            small = nano / 1000_000_0;
        } else if (nano % 1000_000 == 0) {
            len += 4;
            small = nano / 1000_000;
        } else if (nano % 1000_00 == 0) {
            len += 5;
            small = nano / 1000_00;
        } else if (nano % 1000_0 == 0) {
            len += 6;
            small = nano / 1000_0;
        } else if (nano % 1000 == 0) {
            len += 7;
            small = nano / 1000;
        } else if (nano % 100 == 0) {
            len += 8;
            small = nano / 100;
        } else if (nano % 10 == 0) {
            len += 9;
            small = nano / 10;
        } else {
            len += 10;
            small = nano;
        }

        char[] chars = new char[len];
        chars[0] = '"';
        Arrays.fill(chars, 1, len - 1, '0');
        IOUtils.getChars(year, yearSize + 1, chars);
        chars[yearSize + 1] = '-';
        IOUtils.getChars(month, yearSize + 4, chars);
        chars[yearSize + 4] = '-';
        IOUtils.getChars(dayOfMonth, yearSize + 7, chars);
        chars[yearSize + 7] = 'T';
        IOUtils.getChars(hour, yearSize + 10, chars);
        chars[yearSize + 10] = ':';
        IOUtils.getChars(minute, yearSize + 13, chars);
        chars[yearSize + 13] = ':';
        IOUtils.getChars(second, yearSize + 16, chars);
        if (small != 0) {
            chars[yearSize + 16] = '.';
            IOUtils.getChars(small, len - 1, chars);
        }
        chars[len - 1] = '"';

        writeRaw(chars);
    }

    public void writeLocalTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();
        int nano = time.getNano();

        int len = 10;
        int small;
        if (nano % 1000_000_000 == 0) {
            small = 0;
        } else if (nano % 1000_000_00 == 0) {
            len += 2;
            small = nano / 1000_000_00;
        } else if (nano % 1000_000_0 == 0) {
            len += 3;
            small = nano / 1000_000_0;
        } else if (nano % 1000_000 == 0) {
            len += 4;
            small = nano / 1000_000;
        } else if (nano % 1000_00 == 0) {
            len += 5;
            small = nano / 1000_00;
        } else if (nano % 1000_0 == 0) {
            len += 6;
            small = nano / 1000_0;
        } else if (nano % 1000 == 0) {
            len += 7;
            small = nano / 1000;
        } else if (nano % 100 == 0) {
            len += 8;
            small = nano / 100;
        } else if (nano % 10 == 0) {
            len += 9;
            small = nano / 10;
        } else {
            len += 10;
            small = nano;
        }

        char[] chars = new char[len];
        chars[0] = '"';
        Arrays.fill(chars, 1, chars.length - 1, '0');
        IOUtils.getChars(hour, 3, chars);
        chars[3] = ':';
        IOUtils.getChars(minute, 6, chars);
        chars[6] = ':';
        IOUtils.getChars(second, 9, chars);
        if (small != 0) {
            chars[9] = '.';
            IOUtils.getChars(small, len - 1, chars);
        }
        chars[len - 1] = '"';

        writeRaw(chars);
    }

    public void writeZonedDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            writeNull();
            return;
        }

        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int dayOfMonth = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond();
        int nano = dateTime.getNano();
        String zoneId = dateTime.getZone().getId();

        int len = 17;

        int zoneSize;
        if (zoneId.equals("UTC")) {
            zoneId = "Z";
            zoneSize = 1;
        } else {
            zoneSize = 2 + zoneId.length();
        }
        len += zoneSize;

        int yearSize = IOUtils.stringSize(year);
        len += yearSize;
        int small;
        if (nano % 1000_000_000 == 0) {
            small = 0;
        } else if (nano % 1000_000_00 == 0) {
            len += 2;
            small = nano / 1000_000_00;
        } else if (nano % 1000_000_0 == 0) {
            len += 3;
            small = nano / 1000_000_0;
        } else if (nano % 1000_000 == 0) {
            len += 4;
            small = nano / 1000_000;
        } else if (nano % 1000_00 == 0) {
            len += 5;
            small = nano / 1000_00;
        } else if (nano % 1000_0 == 0) {
            len += 6;
            small = nano / 1000_0;
        } else if (nano % 1000 == 0) {
            len += 7;
            small = nano / 1000;
        } else if (nano % 100 == 0) {
            len += 8;
            small = nano / 100;
        } else if (nano % 10 == 0) {
            len += 9;
            small = nano / 10;
        } else {
            len += 10;
            small = nano;
        }

        char[] chars = new char[len];
        chars[0] = '"';
        Arrays.fill(chars, 1, chars.length - 1, '0');
        IOUtils.getChars(year, yearSize + 1, chars);
        chars[yearSize + 1] = '-';
        IOUtils.getChars(month, yearSize + 4, chars);
        chars[yearSize + 4] = '-';
        IOUtils.getChars(dayOfMonth, yearSize + 7, chars);
        chars[yearSize + 7] = 'T';
        IOUtils.getChars(hour, yearSize + 10, chars);
        chars[yearSize + 10] = ':';
        IOUtils.getChars(minute, yearSize + 13, chars);
        chars[yearSize + 13] = ':';
        IOUtils.getChars(second, yearSize + 16, chars);
        if (small != 0) {
            chars[yearSize + 16] = '.';
            IOUtils.getChars(small, len - 1 - zoneSize, chars);
        }
        if (zoneSize == 1) {
            chars[len - 2] = 'Z';
        } else {
            chars[len - zoneSize - 1] = '[';
            zoneId.getChars(0, zoneId.length(), chars, len - zoneSize);
            chars[len - 2] = ']';
        }
        chars[len - 1] = '"';

        writeRaw(chars);
    }

    public void writeInstant(Instant instant) {
        if (instant == null) {
            writeNull();
            return;
        }

        String str = DateTimeFormatter.ISO_INSTANT.format(instant);
        writeString(str);
    }

    public abstract void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second);

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
        int millislen;
        if (millis == 0) {
            millislen = 0;
        } else if (millis < 10) {
            millislen = 4;
        } else {
            if (millis % 100 == 0) {
                millislen = 2;
            } else if (millis % 10 == 0) {
                millislen = 3;
            } else {
                millislen = 4;
            }
        }
        int zonelen = offsetSeconds == 0 ? 1 : 6;
        int offset = offsetSeconds / 3600;
        int len = 21 + millislen + zonelen;
        char[] chars = new char[len];

        chars[0] = '"';
        chars[1] = (char) (year / 1000 + '0');
        chars[2] = (char) ((year / 100) % 10 + '0');
        chars[3] = (char) ((year / 10) % 10 + '0');
        chars[4] = (char) (year % 10 + '0');
        chars[5] = '-';
        chars[6] = (char) (month / 10 + '0');
        chars[7] = (char) (month % 10 + '0');
        chars[8] = '-';
        chars[9] = (char) (dayOfMonth / 10 + '0');
        chars[10] = (char) (dayOfMonth % 10 + '0');
        chars[11] = 'T';
        chars[12] = (char) (hour / 10 + '0');
        chars[13] = (char) (hour % 10 + '0');
        chars[14] = ':';
        chars[15] = (char) (minute / 10 + '0');
        chars[16] = (char) (minute % 10 + '0');
        chars[17] = ':';
        chars[18] = (char) (second / 10 + '0');
        chars[19] = (char) (second % 10 + '0');
        if (millislen > 0) {
            chars[20] = '.';
            Arrays.fill(chars, 21, 20 + millislen, '0');
            if (millis < 10) {
                IOUtils.getChars(millis, 20 + millislen, chars);
            } else {
                if (millis % 100 == 0) {
                    IOUtils.getChars(millis / 100, 20 + millislen, chars);
                } else if (millis % 10 == 0) {
                    IOUtils.getChars(millis / 10, 20 + millislen, chars);
                } else {
                    IOUtils.getChars(millis, 20 + millislen, chars);
                }
            }
        }
        if (offsetSeconds == 0) {
            chars[20 + millislen] = 'Z';
        } else {
            int offsetAbs = Math.abs(offset);
            int offsetlen = IOUtils.stringSize(offsetAbs);

            if (offset >= 0) {
                chars[20 + millislen] = '+';
            } else {
                chars[20 + millislen] = '-';
            }
            chars[20 + millislen + 1] = '0';
            IOUtils.getChars(offsetAbs, 20 + millislen + 3, chars);
            chars[20 + millislen + 3] = ':';
            chars[20 + millislen + 4] = '0';
            int offsetMinutes = (offsetSeconds - offset * 3600) / 60;
            if (offsetMinutes < 0) {
                offsetMinutes = -offsetMinutes;
            }
            IOUtils.getChars(offsetMinutes, 20 + millislen + zonelen, chars);
        }
        chars[chars.length - 1] = '"';

        writeRaw(chars);
    }

    public void writeDateYYYMMDD10(int year, int month, int dayOfMonth) {
        char[] chars = new char[10];

        chars[0] = (char) (year / 1000 + '0');
        chars[1] = (char) ((year / 100) % 10 + '0');
        chars[2] = (char) ((year / 10) % 10 + '0');
        chars[3] = (char) (year % 10 + '0');
        chars[4] = '-';
        chars[5] = (char) (month / 10 + '0');
        chars[6] = (char) (month % 10 + '0');
        chars[7] = '-';
        chars[8] = (char) (dayOfMonth / 10 + '0');
        chars[9] = (char) (dayOfMonth % 10 + '0');

        writeString(chars);
    }

    public void writeTimeHHMMSS8(int hour, int minute, int second) {
        char[] chars = new char[8];

        chars[0] = (char) (hour / 10 + '0');
        chars[1] = (char) (hour % 10 + '0');
        chars[2] = ':';
        chars[3] = (char) (minute / 10 + '0');
        chars[4] = (char) (minute % 10 + '0');
        chars[5] = ':';
        chars[6] = (char) (second / 10 + '0');
        chars[7] = (char) (second % 10 + '0');

        writeString(chars);
    }

    public void write(List array) {
        write0('[');
        boolean first = true;
        for (Object item : array) {
            if (!first) {
                write0(',');
            }
            writeAny(item);
            first = false;
        }
        write0(']');
    }

    public void write(Map map) {
        write0('{');
        boolean first = true;
        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext(); ) {
            if (!first) {
                write0(',');
            }

            Map.Entry next = it.next();
            writeAny(
                    next.getKey());
            write0(':');
            writeAny(
                    next.getValue());

            first = false;
        }
        write0('}');
    }

    public void writeAny(Object value) {
        if (value == null) {
            writeNull();
            return;
        }

        Class<?> valueClass = value.getClass();
        ObjectWriter objectWriter = context.provider.getObjectWriter(valueClass, valueClass);
        if (isJSONB()) {
            objectWriter.writeJSONB(this, value, null, null, 0);
        } else {
            objectWriter.write(this, value, null, null, 0);
        }
    }

    public abstract void writeReference(String path);

    @Override
    public void close() {

    }

    public byte[] getBytes() {
        throw new UnsupportedOperationException();
    }

    public void flushTo(java.io.Writer to) {
        throw new UnsupportedOperationException();
    }

    public int flushTo(OutputStream to) throws IOException {
        throw new UnsupportedOperationException();
    }

    static int MAX_ARRAY_SIZE = 1024 * 1024 * 64; // 64M

    public static class Context {
        static ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

        final ObjectWriterProvider provider;
        DateTimeFormatter dateFormatter;
        String dateFormat;
        boolean dateFormatMillis;
        boolean dateFormatISO8601;
        boolean dateFormatUnixTime;
        String numberFormat;
        long features;
        ZoneId zoneId;

        PropertyPreFilter propertyPreFilter;
        PropertyFilter propertyFilter;
        NameFilter nameFilter;
        ValueFilter valueFilter;

        public Context(ObjectWriterProvider provider) {
            if (provider == null) {
                throw new IllegalArgumentException("objectWriterProvider must not null");
            }

            this.provider = provider;
        }

        public Context(ObjectWriterProvider provider, Feature... features) {
            if (provider == null) {
                throw new IllegalArgumentException("objectWriterProvider must not null");
            }

            this.provider = provider;

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        public long getFeatures() {
            return features;
        }

        public boolean isEnabled(Feature feature) {
            return (this.features & feature.mask) != 0;
        }

        public void config(Feature... features) {
            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        public void config(Feature feature, boolean state) {
            if (state) {
                features |= feature.mask;
            } else {
                features &= ~feature.mask;
            }
        }

        protected void configFilter(Filter... filters) {
            for (Filter filter : filters) {
                if (filter instanceof NameFilter) {
                    setNameFilter((NameFilter) filter);
                } else if (filter instanceof ValueFilter) {
                    setValueFilter((ValueFilter) filter);
                } else if (filter instanceof PropertyFilter) {
                    setPropertyFilter((PropertyFilter) filter);
                } else if (filter instanceof PropertyPreFilter) {
                    setPropertyPreFilter((PropertyPreFilter) filter);
                }
            }
        }

        public <T> ObjectWriter<T> getObjectWriter(Class<T> objectType) {
            boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
            ObjectWriter objectWriter = provider.getObjectWriter(objectType, objectType, fieldBased);
            return objectWriter;
        }

        public <T> ObjectWriter<T> getObjectWriter(Type objectType, Class<T> objectClass) {
            boolean fieldBased = (features & Feature.FieldBased.mask) != 0;
            return provider.getObjectWriter(objectType, objectClass, fieldBased);
        }

        public ObjectWriterProvider getProvider() {
            return provider;
        }

        public ZoneId getZoneId() {
            if (zoneId == null) {
                zoneId = DEFAULT_ZONE_ID;
            }
            return zoneId;
        }

        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public boolean isDateFormatMillis() {
            return dateFormatMillis;
        }

        public boolean isDateFormatUnixTime() {
            return dateFormatUnixTime;
        }

        public boolean isDateFormatISO8601() {
            return dateFormatISO8601;
        }

        public DateTimeFormatter getDateFormatter() {
            if (dateFormatter == null && dateFormat != null && !dateFormatMillis && !dateFormatISO8601 && !dateFormatUnixTime) {
                dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
            }
            return dateFormatter;
        }

        public void setDateFormat(String dateFormat) {
            if (dateFormat == null || !dateFormat.equals(this.dateFormat)) {
                dateFormatter = null;
            }

            if (dateFormat != null && !dateFormat.isEmpty()) {
                switch (dateFormat) {
                    case "millis":
                        dateFormatMillis = true;
                        break;
                    case "iso8601":
                        dateFormatMillis = false;
                        dateFormatISO8601 = true;
                        break;
                    case "unixtime":
                        dateFormatMillis = false;
                        dateFormatUnixTime = true;
                        break;
                    default:
                        dateFormatMillis = false;
                        break;
                }
            }

            this.dateFormat = dateFormat;
        }

        public PropertyPreFilter getPropertyPreFilter() {
            return propertyPreFilter;
        }

        public void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
            this.propertyPreFilter = propertyPreFilter;
        }

        public NameFilter getNameFilter() {
            return nameFilter;
        }

        public void setNameFilter(NameFilter nameFilter) {
            this.nameFilter = nameFilter;
        }

        public ValueFilter getValueFilter() {
            return valueFilter;
        }

        public void setValueFilter(ValueFilter valueFilter) {
            this.valueFilter = valueFilter;
        }

        public PropertyFilter getPropertyFilter() {
            return propertyFilter;
        }

        public void setPropertyFilter(PropertyFilter propertyFilter) {
            this.propertyFilter = propertyFilter;
        }
    }

    public enum Feature {
        FieldBased(1),
        IgnoreNoneSerializable(1 << 1),
        BeanToArray(1 << 2),
        WriteNulls(1 << 3),
        BrowserCompatible(1 << 4),
        NullAsDefaultValue(1 << 5),
        WriteBooleanAsNumber(1 << 6),
        WriteNonStringValueAsString(1 << 7),
        WriteClassName(1 << 8),
        NotWriteRootClassName(1 << 9),
        NotWriteHashMapArrayListClassName(1 << 10),
        NotWriteDefaultValue(1 << 11),
        WriteEnumsUsingName(1 << 12),
        WriteEnumUsingToString(1 << 13),
        IgnoreErrorGetter(1 << 14),
        PrettyFormat(1 << 15),
        ReferenceDetection(1 << 16),
        WriteNameAsSymbol(1 << 17),
        WriteBigDecimalAsPlain(1 << 18),
        ;

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }

    public final static class Path {
        public final static Path ROOT = new Path(null, "$");
        public final static Path ROOT_0 = new Path(ROOT, 0);
        public final static Path ROOT_1 = new Path(ROOT, 1);

        final Path parent;
        final String name;
        final int index;
        String fullPath;

        public Path(Path parent, String name) {
            this.parent = parent;
            this.name = name;
            this.index = -1;
        }

        public Path(Path parent, int index) {
            this.parent = parent;
            this.name = null;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Path path = (Path) o;
            return index == path.index && Objects.equals(parent, path.parent) && Objects.equals(name, path.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, name, index);
        }

        @Override
        public String toString() {
            if (fullPath != null) {
                return fullPath;
            }

            byte[] buf = new byte[16];
            int off = 0;

            int level = 0;
            Path[] items = new Path[4];
            for (Path p = this; p != null; p = p.parent) {
                if (items.length == level) {
                    items = Arrays.copyOf(items, items.length + 4);
                }
                items[level] = p;
                level++;
            }

            boolean ascii = true;

            for (int i = level - 1; i >= 0; i--) {
                Path item = items[i];
                String name = item.name;
                if (name == null) {
                    int intValue = item.index;
                    int intValueSize = IOUtils.stringSize(intValue);
                    while (off + intValueSize + 2 >= buf.length) {
                        int newCapacity = buf.length + (buf.length >> 1);
                        buf = Arrays.copyOf(buf, newCapacity);
                    }

                    buf[off++] = '[';
                    IOUtils.getChars(intValue, off + intValueSize, buf);
                    off += intValueSize;
                    buf[off++] = ']';
                } else {
                    if (off + 1 >= buf.length) {
                        int newCapacity = buf.length + (buf.length >> 1);
                        buf = Arrays.copyOf(buf, newCapacity);
                    }

                    if (i != level - 1) {
                        buf[off++] = '.';
                    }

                    if (JDKUtils.JVM_VERSION == 8) {
                        char[] chars = JDKUtils.getCharArray(name);
                        for (int j = 0; j < chars.length; j++) {
                            char ch = chars[j];
                            switch (ch) {
                                case '/':
                                case ':':
                                case ';':
                                case '`':
                                case '.':
                                case '~':
                                case '!':
                                case '@':
                                case '#':
                                case '%':
                                case '^':
                                case '&':
                                case '*':
                                case '[':
                                case ']':
                                case '<':
                                case '>':
                                case '?':
                                case '(':
                                case ')':
                                case '-':
                                case '+':
                                case '=':
                                case '\\':
                                case '"':
                                case '\'':
                                    if (off + 1 >= buf.length) {
                                        int newCapacity = buf.length + (buf.length >> 1);
                                        buf = Arrays.copyOf(buf, newCapacity);
                                    }
                                    buf[off++] = '\\';
                                    buf[off++] = (byte) ch;
                                    break;
                                default:
                                    if ((ch >= 0x0001) && (ch <= 0x007F)) {
                                        if (off == buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        buf[off++] = (byte) ch;
                                    } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                                        if (off + 2 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }

                                        ascii = false;
                                        final int uc;
                                        if (ch >= '\uD800' && ch < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
                                            if (name.length() - i < 2) {
                                                uc = -1;
                                            } else {
                                                char d = name.charAt(i + 1);
                                                // d >= '\uDC00' && d < ('\uDFFF' + 1)
                                                if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                                    uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                                                } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                                    buf[off++] = (byte) '?';
                                                    continue;
                                                }
                                            }
                                        } else {
                                            //
                                            if (ch >= '\uDC00' && ch < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                                                buf[off++] = (byte) '?';
                                                continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                            } else {
                                                uc = ch;
                                            }
                                        }

                                        if (uc < 0) {
                                            buf[off++] = (byte) '?';
                                        } else {
                                            buf[off++] = (byte) (0xf0 | ((uc >> 18)));
                                            buf[off++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                                            buf[off++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                                            buf[off++] = (byte) (0x80 | (uc & 0x3f));
                                            i++; // 2 chars
                                        }
                                    } else if (ch > 0x07FF) {
                                        if (off + 2 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off++] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                                        buf[off++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                                        buf[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
                                    } else {
                                        if (off + 1 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off++] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                                        buf[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
                                    }
                                    break;
                            }
                        }
                    } else {
                        for (int j = 0; j < name.length(); j++) {
                            char ch = name.charAt(j);
                            switch (ch) {
                                case '/':
                                case ':':
                                case ';':
                                case '`':
                                case '.':
                                case '~':
                                case '!':
                                case '@':
                                case '#':
                                case '%':
                                case '^':
                                case '&':
                                case '*':
                                case '[':
                                case ']':
                                case '<':
                                case '>':
                                case '?':
                                case '(':
                                case ')':
                                case '-':
                                case '+':
                                case '=':
                                case '\\':
                                case '"':
                                case '\'':
                                    if (off + 1 >= buf.length) {
                                        int newCapacity = buf.length + (buf.length >> 1);
                                        buf = Arrays.copyOf(buf, newCapacity);
                                    }
                                    buf[off++] = '\\';
                                    buf[off++] = (byte) ch;
                                    break;
                                default:
                                    if ((ch >= 0x0001) && (ch <= 0x007F)) {
                                        if (off == buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        buf[off++] = (byte) ch;
                                    } else if (ch >= '\uD800' && ch < ('\uDFFF' + 1)) { //  //Character.isSurrogate(c)
                                        if (off + 2 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }

                                        ascii = false;
                                        final int uc;
                                        if (ch >= '\uD800' && ch < ('\uDBFF' + 1)) { // Character.isHighSurrogate(c)
                                            if (name.length() - i < 2) {
                                                uc = -1;
                                            } else {
                                                char d = name.charAt(i + 1);
                                                // d >= '\uDC00' && d < ('\uDFFF' + 1)
                                                if (d >= '\uDC00' && d < ('\uDFFF' + 1)) { // Character.isLowSurrogate(d)
                                                    uc = ((ch << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00'); // Character.toCodePoint(c, d)
                                                } else {
//                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                                    buf[off++] = (byte) '?';
                                                    continue;
                                                }
                                            }
                                        } else {
                                            //
                                            if (ch >= '\uDC00' && ch < ('\uDFFF' + 1)) { // Character.isLowSurrogate(c)
                                                buf[off++] = (byte) '?';
                                                continue;
//                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                                            } else {
                                                uc = ch;
                                            }
                                        }

                                        if (uc < 0) {
                                            buf[off++] = (byte) '?';
                                        } else {
                                            buf[off++] = (byte) (0xf0 | ((uc >> 18)));
                                            buf[off++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                                            buf[off++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                                            buf[off++] = (byte) (0x80 | (uc & 0x3f));
                                            i++; // 2 chars
                                        }
                                    } else if (ch > 0x07FF) {
                                        if (off + 2 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off++] = (byte) (0xE0 | ((ch >> 12) & 0x0F));
                                        buf[off++] = (byte) (0x80 | ((ch >> 6) & 0x3F));
                                        buf[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
                                    } else {
                                        if (off + 1 >= buf.length) {
                                            int newCapacity = buf.length + (buf.length >> 1);
                                            buf = Arrays.copyOf(buf, newCapacity);
                                        }
                                        ascii = false;

                                        buf[off++] = (byte) (0xC0 | ((ch >> 6) & 0x1F));
                                        buf[off++] = (byte) (0x80 | ((ch >> 0) & 0x3F));
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

            if (ascii) {
                if (JDKUtils.UNSAFE_ASCII_CREATOR != null) {
                    byte[] bytes;
                    if (off == buf.length) {
                        bytes = buf;
                    } else {
                        bytes = new byte[off];
                        System.arraycopy(buf, 0, bytes, 0, off);
                    }
                    return fullPath = JDKUtils.UNSAFE_ASCII_CREATOR.apply(bytes);
                }

                if (JDKUtils.JVM_VERSION == 8) {
                    if (STRING_CREATOR_JDK8 == null && !STRING_CREATOR_ERROR) {
                        try {
                            STRING_CREATOR_JDK8 = JDKUtils.getStringCreatorJDK8();
                        } catch (Throwable e) {
                            STRING_CREATOR_ERROR = true;
                        }
                    }

                    char[] chars = new char[off];
                    for (int i = 0; i < off; i++) {
                        chars[i] = (char) buf[i];
                    }
                    if (STRING_CREATOR_JDK8 == null) {
                        fullPath = new String(chars);
                    } else {
                        fullPath = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                    }

                    return fullPath;
                }
            }

            return fullPath = new String(buf, 0, off, ascii ? StandardCharsets.US_ASCII : StandardCharsets.UTF_8);
        }
    }
}
