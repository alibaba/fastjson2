package com.alibaba.fastjson2;

import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.*;
import java.lang.reflect.GenericArrayType;
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
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.NotWriteDefaultValue;
import static com.alibaba.fastjson2.util.JDKUtils.*;

public abstract class JSONWriter
        implements Closeable {
    static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public final Context context;
    public final boolean utf8;
    public final boolean utf16;
    public final boolean jsonb;
    public final boolean useSingleQuote;
    public final SymbolTable symbolTable;

    protected final Charset charset;
    protected final char quote;
    protected final int maxArraySize;

    protected boolean startObject;
    protected int level;
    protected int off;
    protected Object rootObject;
    protected IdentityHashMap<Object, Path> refs;
    protected Path path;
    protected String lastReference;

    protected JSONWriter(
            Context context,
            SymbolTable symbolTable,
            boolean jsonb,
            Charset charset
    ) {
        this.context = context;
        this.symbolTable = symbolTable;
        this.charset = charset;
        this.jsonb = jsonb;
        this.utf8 = !jsonb && charset == StandardCharsets.UTF_8;
        this.utf16 = !jsonb && charset == StandardCharsets.UTF_16;
        this.useSingleQuote = !jsonb && (context.features & Feature.UseSingleQuotes.mask) != 0;

        quote = useSingleQuote ? '\'' : '"';

        // 64M or 1G
        maxArraySize = (context.features & LargeObject.mask) != 0 ? 1073741824 : 67108864;
    }

    public Charset getCharset() {
        return charset;
    }

    public final boolean isUTF8() {
        return utf8;
    }

    public final boolean isUTF16() {
        return utf16;
    }

    public boolean isIgnoreNoneSerializable() {
        return (context.features & Feature.IgnoreNoneSerializable.mask) != 0;
    }

    public boolean isIgnoreNoneSerializable(Object object) {
        return (context.features & Feature.IgnoreNoneSerializable.mask) != 0
                && object != null
                && !Serializable.class.isAssignableFrom(object.getClass());
    }

    public final SymbolTable getSymbolTable() {
        return symbolTable;
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
        this.path = JSONWriter.Path.ROOT;
    }

    public String setPath(String name, Object object) {
        if ((context.features & Feature.ReferenceDetection.mask) == 0) {
            return null;
        }

        this.path = new Path(this.path, name);

        Path previous;
        if (object == rootObject) {
            previous = Path.ROOT;
        } else {
            if (refs == null) {
                refs = new IdentityHashMap(8);
                refs.put(object, this.path);
                return null;
            }

            previous = refs.get(object);
            if (previous == null) {
                refs.put(object, this.path);
                return null;
            }
        }

        return previous.toString();
    }

    public String setPath(FieldWriter fieldWriter, Object object) {
        if ((context.features & Feature.ReferenceDetection.mask) == 0) {
            return null;
        }

        if (object == Collections.EMPTY_LIST || object == Collections.EMPTY_SET) {
            return null;
        }

        if (this.path == Path.ROOT) {
            this.path = fieldWriter.getRootParentPath();
        } else {
            this.path = fieldWriter.getPath(path);
        }

        Path previous;
        if (object == rootObject) {
            previous = Path.ROOT;
        } else {
            if (refs == null) {
                refs = new IdentityHashMap(8);
                refs.put(object, this.path);
                return null;
            }

            previous = refs.get(object);
            if (previous == null) {
                refs.put(object, this.path);
                return null;
            }
        }

        return previous.toString();
    }

    public String setPath(int index, Object object) {
        if ((context.features & Feature.ReferenceDetection.mask) == 0) {
            return null;
        }

        if (index == 0) {
            if (path.child0 != null) {
                this.path = path.child0;
            } else {
                this.path = path.child0 = new Path(path, index);
            }
        } else if (index == 1) {
            if (path.child1 != null) {
                this.path = path.child1;
            } else {
                this.path = path.child1 = new Path(path, index);
            }
        } else {
            this.path = new Path(path, index);
        }

        Path previous;
        if (object == rootObject) {
            previous = Path.ROOT;
        } else {
            if (refs == null) {
                refs = new IdentityHashMap(8);
                refs.put(object, this.path);
                return null;
            }

            previous = refs.get(object);
            if (previous == null) {
                refs.put(object, this.path);
                return null;
            }
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

    public final boolean hasFilter() {
        return context.hasFilter;
    }

    public final boolean hasFilter(long feature) {
        return context.hasFilter || (context.features & feature) != 0;
    }

    public boolean isWriteNulls() {
        return (context.features & Feature.WriteNulls.mask) != 0;
    }

    public boolean isRefDetect() {
        return (context.features & Feature.ReferenceDetection.mask) != 0;
    }

    public boolean isUseSingleQuotes() {
        return useSingleQuote;
    }

    public boolean isRefDetect(Object object) {
        return (context.features & Feature.ReferenceDetection.mask) != 0
                && object != null
                && !ObjectWriterProvider.isNotReferenceDetect(object.getClass());
    }

    public boolean containsReference(Object value) {
        return refs != null && refs.containsKey(value);
    }

    public boolean removeReference(Object value) {
        return this.refs != null && this.refs.remove(value) != null;
    }

    public boolean isBeanToArray() {
        return (context.features & Feature.BeanToArray.mask) != 0;
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

    public boolean isWriteTypeInfo(Object object, Class fieldClass) {
        long features = context.features;
        if ((features & Feature.WriteClassName.mask) == 0) {
            return false;
        }

        if (object == null) {
            return false;
        }

        Class objectClass = object.getClass();
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
        } else if (fieldType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) fieldType;
            Type componentType = genericArrayType.getGenericComponentType();
            if (componentType instanceof ParameterizedType) {
                componentType = ((ParameterizedType) componentType).getRawType();
            }
            if (objectClass.isArray()) {
                if (objectClass.getComponentType().equals(componentType)) {
                    return false;
                }
            }
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
        if (JVM_VERSION == 8) {
            return new JSONWriterUTF16JDK8(writeContext);
        }

        if ((defaultWriterFeatures & Feature.OptimizedForAscii.mask) != 0) {
            return new JSONWriterUTF8JDK9(writeContext);
        }

        return new JSONWriterUTF16(writeContext);
    }

    public static JSONWriter of(ObjectWriterProvider provider, Feature... features) {
        Context context = new Context(provider);
        context.config(features);
        return of(context);
    }

    public static JSONWriter of(Context writeContext) {
        if (writeContext == null) {
            writeContext = JSONFactory.createWriteContext();
        }

        JSONWriter jsonWriter;
        if (JVM_VERSION == 8) {
            jsonWriter = new JSONWriterUTF16JDK8(writeContext);
        } else if ((writeContext.features & Feature.OptimizedForAscii.mask) != 0) {
            jsonWriter = new JSONWriterUTF8JDK9(writeContext);
        } else {
            jsonWriter = new JSONWriterUTF16(writeContext);
        }

        if (writeContext.isEnabled(Feature.PrettyFormat)) {
            jsonWriter = new JSONWriterPretty(jsonWriter);
        }
        return jsonWriter;
    }

    public static JSONWriter of(Feature... features) {
        Context writeContext = JSONFactory.createWriteContext(features);
        JSONWriter jsonWriter;
        if (JVM_VERSION == 8) {
            jsonWriter = new JSONWriterUTF16JDK8(writeContext);
        } else if ((writeContext.features & Feature.OptimizedForAscii.mask) != 0) {
            jsonWriter = new JSONWriterUTF8JDK9(writeContext);
        } else {
            jsonWriter = new JSONWriterUTF16(writeContext);
        }

        for (int i = 0; i < features.length; i++) {
            if (features[i] == Feature.PrettyFormat) {
                return new JSONWriterPretty(jsonWriter);
            }
        }
        return jsonWriter;
    }

    public static JSONWriter ofUTF16(Feature... features) {
        Context writeContext = JSONFactory.createWriteContext(features);
        JSONWriter jsonWriter = JVM_VERSION == 8
                ? new JSONWriterUTF16JDK8(writeContext)
                : new JSONWriterUTF16(writeContext);

        for (int i = 0; i < features.length; i++) {
            if (features[i] == Feature.PrettyFormat) {
                return new JSONWriterPretty(jsonWriter);
            }
        }
        return jsonWriter;
    }

    public static JSONWriter ofJSONB() {
        return new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider),
                null
        );
    }

    public static JSONWriter ofJSONB(JSONWriter.Context context) {
        return new JSONWriterJSONB(context, null);
    }

    public static JSONWriter ofJSONB(JSONWriter.Context context, SymbolTable symbolTable) {
        return new JSONWriterJSONB(context, symbolTable);
    }

    public static JSONWriter ofJSONB(Feature... features) {
        return new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider, features),
                null
        );
    }

    public static JSONWriter ofJSONB(SymbolTable symbolTable) {
        return new JSONWriterJSONB(
                new JSONWriter.Context(JSONFactory.defaultObjectWriterProvider),
                symbolTable
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
        if (JVM_VERSION >= 9) {
            return new JSONWriterUTF8JDK9(
                    JSONFactory.createWriteContext());
        } else {
            return new JSONWriterUTF8(
                    JSONFactory.createWriteContext());
        }
    }

    public static JSONWriter ofUTF8(JSONWriter.Context context) {
        if (JVM_VERSION >= 9) {
            return new JSONWriterUTF8JDK9(context);
        } else {
            return new JSONWriterUTF8(context);
        }
    }

    public static JSONWriter ofUTF8(Feature... features) {
        Context writeContext = createWriteContext(features);

        JSONWriter jsonWriter;
        if (JVM_VERSION >= 9) {
            jsonWriter = new JSONWriterUTF8JDK9(writeContext);
        } else {
            jsonWriter = new JSONWriterUTF8(writeContext);
        }

        boolean pretty = (writeContext.features & JSONWriter.Feature.PrettyFormat.mask) != 0;
        if (pretty) {
            jsonWriter = new JSONWriterPretty(jsonWriter);
        }
        return jsonWriter;
    }

    public void writeBinary(byte[] bytes) {
        if (bytes == null) {
            writeArrayNull();
            return;
        }

        if ((context.features & Feature.WriteByteArrayAsBase64.mask) != 0) {
            writeBase64(bytes);
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

    public abstract void writeBase64(byte[] bytes);

    public abstract void writeHex(byte[] bytes);

    protected abstract void write0(char ch);

    public abstract void writeRaw(String str);

    public abstract void writeRaw(byte[] bytes);

    public void writeRaw(byte b) {
        throw new JSONException("UnsupportedOperation");
    }

    public void writeNameRaw(byte[] bytes, int offset, int len) {
        throw new JSONException("UnsupportedOperation");
    }

    public void writeRaw(char[] chars) {
        writeRaw(chars, 0, chars.length);
    }

    public void writeRaw(char[] chars, int off, int charslen) {
        throw new JSONException("UnsupportedOperation");
    }

    public abstract void writeChar(char ch);

    public abstract void writeRaw(char ch);

    public void writeRaw(char c0, char c1) {
        writeRaw(c0);
        writeRaw(c1);
    }

    public abstract void writeNameRaw(byte[] bytes);

    public void writeSymbol(int symbol) {
        throw new JSONException("UnsupportedOperation");
    }

    public void writeNameRaw(byte[] name, long nameHash) {
        throw new JSONException("UnsupportedOperation");
    }

    public abstract void writeNameRaw(char[] chars);

    public abstract void writeNameRaw(char[] bytes, int offset, int len);

    public void writeName(String name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeString(name);
    }

    public void writeName(long name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeInt64(name);

        if (name >= Integer.MIN_VALUE && name <= Integer.MAX_VALUE && (context.features & Feature.WriteClassName.mask) != 0) {
            writeRaw('L');
        }
    }

    public void writeName(int name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeInt32(name);
    }

    public void writeNameAny(Object name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }

        writeAny(name);
    }

    public abstract void startObject();

    public abstract void endObject();

    public abstract void startArray();

    public void startArray(int size) {
        throw new JSONException("UnsupportedOperation");
    }

    public void startArray(Object array, int size) {
        throw new JSONException("UnsupportedOperation");
    }

    public abstract void endArray();

    public abstract void writeComma();

    public abstract void writeColon();

    public void writeInt16(short[] value) {
        if (value == null) {
            writeArrayNull();
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

    public void writeDoubleArray(double value0, double value1) {
        startArray();
        writeDouble(value0);
        writeComma();
        writeDouble(value1);
        endArray();
    }

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
            writeArrayNull();
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
        if ((this.context.features & (Feature.NullAsDefaultValue.mask | Feature.WriteNullStringAsEmpty.mask)) != 0) {
            raw = (this.context.features & Feature.UseSingleQuotes.mask) != 0 ? "''" : "\"\"";
        } else {
            raw = "null";
        }
        writeRaw(raw);
    }

    public void writeArrayNull() {
        String raw;
        if ((this.context.features & (Feature.NullAsDefaultValue.mask | Feature.WriteNullListAsEmpty.mask)) != 0) {
            raw = "[]";
        } else {
            raw = "null";
        }
        writeRaw(raw);
    }

    public void writeNumberNull() {
        String raw;
        if ((this.context.features & (Feature.NullAsDefaultValue.mask | Feature.WriteNullNumberAsZero.mask)) != 0) {
            writeInt32(0);
        } else {
            writeNull();
        }
    }

    public void writeBooleanNull() {
        if ((this.context.features & (Feature.NullAsDefaultValue.mask | Feature.WriteNullBooleanAsFalse.mask)) != 0) {
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

    public void checkAndWriteTypeName(Object object, Class fieldClass) {
        long features = context.features;
        if ((features & Feature.WriteClassName.mask) == 0) {
            return;
        }

        if (object == null) {
            return;
        }

        Class objectClass = object.getClass();
        if (objectClass == fieldClass) {
            return;
        }

        if ((features & Feature.NotWriteHashMapArrayListClassName.mask) != 0) {
            if (objectClass == HashMap.class || objectClass == ArrayList.class) {
                return;
            }
        }

        if ((features & Feature.NotWriteRootClassName.mask) != 0 && object == this.rootObject) {
            return;
        }

        writeTypeName(TypeUtils.getTypeName(objectClass));
    }

    public void writeTypeName(String typeName) {
        throw new JSONException("UnsupportedOperation");
    }

    public boolean writeTypeName(byte[] typeName, long typeNameHash) {
        throw new JSONException("UnsupportedOperation");
    }

    public void writeString(Reader reader) {
        writeRaw(quote);

        try {
            char[] chars = new char[2048];
            for (; ; ) {
                int len = reader.read(chars, 0, chars.length);
                if (len < 0) {
                    break;
                }

                if (len > 0) {
                    writeString(chars, 0, len, false);
                }
            }
        } catch (Exception ex) {
            throw new JSONException("read string from reader error", ex);
        }

        writeRaw(quote);
    }

    public abstract void writeString(String str);

    public void writeString(List<String> list) {
        startArray();
        for (int i = 0, size = list.size(); i < size; i++) {
            if (i != 0) {
                writeComma();
            }

            String str = list.get(i);
            writeString(str);
        }
        endArray();
    }

    public void writeSymbol(String string) {
        writeString(string);
    }

    public void writeString(char[] chars) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        writeString(chars, 0, chars.length);
    }

    public void writeString(char[] chars, int off, int charslen) {
        if (chars == null) {
            writeStringNull();
            return;
        }

        write0('"');
        boolean special = false;
        for (int i = off; i < charslen; ++i) {
            if (chars[i] == '\\' || chars[i] == '"') {
                special = true;
                break;
            }
        }

        if (!special) {
            writeRaw(chars, off, charslen);
        } else {
            for (int i = off; i < charslen; ++i) {
                char ch = chars[i];
                if (ch == '\\' || ch == '"') {
                    write0('\\');
                }
                write0(ch);
            }
        }
        write0('"');
    }

    public abstract void writeString(char[] chars, int off, int len, boolean quote);

    public abstract void writeLocalDate(LocalDate date);

    public abstract void writeLocalDateTime(LocalDateTime dateTime);

    public abstract void writeLocalTime(LocalTime time);

    public abstract void writeZonedDateTime(ZonedDateTime dateTime);

    public void writeInstant(Instant instant) {
        if (instant == null) {
            writeNull();
            return;
        }

        String str = DateTimeFormatter.ISO_INSTANT.format(instant);
        writeString(str);
    }

    public abstract void writeDateTime14(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second);

    public abstract void writeDateTime19(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second);

    public abstract void writeDateTimeISO8601(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minute,
            int second,
            int millis,
            int offsetSeconds,
            boolean timeZone
    );

    public abstract void writeDateYYYMMDD8(int year, int month, int dayOfMonth);

    public abstract void writeDateYYYMMDD10(int year, int month, int dayOfMonth);

    public abstract void writeTimeHHMMSS8(int hour, int minute, int second);

    public void write(List array) {
        if (array == null) {
            this.writeArrayNull();
            return;
        }

        final long NONE_DIRECT_FEATURES = ReferenceDetection.mask
                | PrettyFormat.mask
                | NotWriteEmptyArray.mask
                | NotWriteDefaultValue.mask;

        if ((context.features & NONE_DIRECT_FEATURES) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(array.getClass());
            objectWriter.write(this, array, null, null, 0);
            return;
        }

        write0('[');
        for (int i = 0; i < array.size(); i++) {
            Object item = array.get(i);
            if (i != 0) {
                write0(',');
            }
            writeAny(item);
        }
        write0(']');
    }

    public void write(Map map) {
        if (map == null) {
            this.writeNull();
            return;
        }

        final long NONE_DIRECT_FEATURES = ReferenceDetection.mask
                | PrettyFormat.mask
                | NotWriteEmptyArray.mask
                | NotWriteDefaultValue.mask;

        if ((context.features & NONE_DIRECT_FEATURES) != 0) {
            ObjectWriter objectWriter = context.getObjectWriter(map.getClass());
            objectWriter.write(this, map, null, null, 0);
            return;
        }

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

    public void write(JSONObject map) {
        write((Map) map);
    }

    public void writeAny(Object value) {
        if (value == null) {
            writeNull();
            return;
        }

        Class<?> valueClass = value.getClass();
        ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
        objectWriter.write(this, value, null, null, 0);
    }

    public abstract void writeReference(String path);

    @Override
    public void close() {
    }

    public abstract int size();

    public abstract byte[] getBytes();

    public abstract byte[] getBytes(Charset charset);

    public void flushTo(java.io.Writer to) {
        try {
            String json = this.toString();
            to.write(json);
            off = 0;
        } catch (IOException e) {
            throw new JSONException("flushTo error", e);
        }
    }

    public abstract int flushTo(OutputStream to) throws IOException;

    public abstract int flushTo(OutputStream out, Charset charset) throws IOException;

    public static class Context {
        static ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

        public final ObjectWriterProvider provider;
        DateTimeFormatter dateFormatter;
        String dateFormat;
        Locale locale;
        boolean dateFormatMillis;
        boolean dateFormatISO8601;
        boolean dateFormatUnixTime;
        boolean formatyyyyMMddhhmmss19;
        boolean formatHasDay;
        boolean formatHasHour;
        long features;
        ZoneId zoneId;

        boolean hasFilter;
        PropertyPreFilter propertyPreFilter;
        PropertyFilter propertyFilter;
        NameFilter nameFilter;
        ValueFilter valueFilter;
        BeforeFilter beforeFilter;
        AfterFilter afterFilter;
        LabelFilter labelFilter;
        ContextValueFilter contextValueFilter;
        ContextNameFilter contextNameFilter;

        public Context(ObjectWriterProvider provider) {
            if (provider == null) {
                throw new IllegalArgumentException("objectWriterProvider must not null");
            }

            this.features = defaultWriterFeatures;
            this.provider = provider;
        }

        public Context(Feature... features) {
            this.features = defaultWriterFeatures;
            this.provider = JSONFactory.getDefaultObjectWriterProvider();

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
        }

        public Context(String format, Feature... features) {
            this.features = defaultWriterFeatures;
            this.provider = JSONFactory.getDefaultObjectWriterProvider();

            for (int i = 0; i < features.length; i++) {
                this.features |= features[i].mask;
            }
            setDateFormat(format);
        }

        public Context(ObjectWriterProvider provider, Feature... features) {
            if (provider == null) {
                throw new IllegalArgumentException("objectWriterProvider must not null");
            }

            this.features = defaultWriterFeatures;
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

        public boolean isEnabled(long feature) {
            return (this.features & feature) != 0;
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
                    this.nameFilter = (NameFilter) filter;
                }

                if (filter instanceof ValueFilter) {
                    this.valueFilter = (ValueFilter) filter;
                }

                if (filter instanceof PropertyFilter) {
                    this.propertyFilter = (PropertyFilter) filter;
                }

                if (filter instanceof PropertyPreFilter) {
                    this.propertyPreFilter = (PropertyPreFilter) filter;
                }

                if (filter instanceof BeforeFilter) {
                    this.beforeFilter = (BeforeFilter) filter;
                }

                if (filter instanceof AfterFilter) {
                    this.afterFilter = (AfterFilter) filter;
                }

                if (filter instanceof LabelFilter) {
                    this.labelFilter = (LabelFilter) filter;
                }

                if (filter instanceof ContextValueFilter) {
                    this.contextValueFilter = (ContextValueFilter) filter;
                }

                if (filter instanceof ContextNameFilter) {
                    this.contextNameFilter = (ContextNameFilter) filter;
                }
            }

            hasFilter = propertyPreFilter != null
                    || propertyFilter != null
                    || nameFilter != null
                    || valueFilter != null
                    || beforeFilter != null
                    || afterFilter != null
                    || labelFilter != null
                    || contextValueFilter != null
                    || contextNameFilter != null;
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

        public boolean isDateFormatHasDay() {
            return formatHasDay;
        }

        public boolean isDateFormatHasHour() {
            return formatHasHour;
        }

        public boolean isFormatyyyyMMddhhmmss19() {
            return formatyyyyMMddhhmmss19;
        }

        public DateTimeFormatter getDateFormatter() {
            if (dateFormatter == null && dateFormat != null && !dateFormatMillis && !dateFormatISO8601 && !dateFormatUnixTime) {
                dateFormatter = locale == null
                        ? DateTimeFormatter.ofPattern(dateFormat)
                        : DateTimeFormatter.ofPattern(dateFormat, locale);
            }
            return dateFormatter;
        }

        public void setDateFormat(String dateFormat) {
            if (dateFormat == null || !dateFormat.equals(this.dateFormat)) {
                dateFormatter = null;
            }

            if (dateFormat != null && !dateFormat.isEmpty()) {
                boolean dateFormatMillis = false, dateFormatISO8601 = false, dateFormatUnixTime = false, formatHasDay = false, formatHasHour = false, formatyyyyMMddhhmmss19 = false;
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
                    case "yyyy-MM-ddTHH:mm:ss":
                        dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
                        formatHasDay = true;
                        formatHasHour = true;
                        break;
                    case "yyyy-MM-dd HH:mm:ss":
                        formatyyyyMMddhhmmss19 = true;
                        formatHasDay = true;
                        formatHasHour = true;
                        break;
                    default:
                        dateFormatMillis = false;
                        formatHasDay = dateFormat.indexOf("d") != -1;
                        formatHasHour = dateFormat.indexOf("H") != -1;
                        break;
                }
                this.dateFormatMillis = dateFormatMillis;
                this.dateFormatISO8601 = dateFormatISO8601;
                this.dateFormatUnixTime = dateFormatUnixTime;
                this.formatHasDay = formatHasDay;
                this.formatHasHour = formatHasHour;
                this.formatyyyyMMddhhmmss19 = formatyyyyMMddhhmmss19;
            }

            this.dateFormat = dateFormat;
        }

        public PropertyPreFilter getPropertyPreFilter() {
            return propertyPreFilter;
        }

        public void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
            this.propertyPreFilter = propertyPreFilter;
            if (propertyPreFilter != null) {
                hasFilter = true;
            }
        }

        public NameFilter getNameFilter() {
            return nameFilter;
        }

        public void setNameFilter(NameFilter nameFilter) {
            this.nameFilter = nameFilter;
            if (nameFilter != null) {
                hasFilter = true;
            }
        }

        public ValueFilter getValueFilter() {
            return valueFilter;
        }

        public void setValueFilter(ValueFilter valueFilter) {
            this.valueFilter = valueFilter;
            if (valueFilter != null) {
                hasFilter = true;
            }
        }

        public ContextValueFilter getContextValueFilter() {
            return contextValueFilter;
        }

        public void setContextValueFilter(ContextValueFilter contextValueFilter) {
            this.contextValueFilter = contextValueFilter;
            if (contextValueFilter != null) {
                hasFilter = true;
            }
        }

        public ContextNameFilter getContextNameFilter() {
            return contextNameFilter;
        }

        public void setContextNameFilter(ContextNameFilter contextNameFilter) {
            this.contextNameFilter = contextNameFilter;
            if (contextNameFilter != null) {
                hasFilter = true;
            }
        }

        public PropertyFilter getPropertyFilter() {
            return propertyFilter;
        }

        public void setPropertyFilter(PropertyFilter propertyFilter) {
            this.propertyFilter = propertyFilter;
            if (propertyFilter != null) {
                hasFilter = true;
            }
        }

        public AfterFilter getAfterFilter() {
            return afterFilter;
        }

        public void setAfterFilter(AfterFilter afterFilter) {
            this.afterFilter = afterFilter;
            if (afterFilter != null) {
                hasFilter = true;
            }
        }

        public BeforeFilter getBeforeFilter() {
            return beforeFilter;
        }

        public void setBeforeFilter(BeforeFilter beforeFilter) {
            this.beforeFilter = beforeFilter;
            if (beforeFilter != null) {
                hasFilter = true;
            }
        }

        public LabelFilter getLabelFilter() {
            return labelFilter;
        }

        public void setLabelFilter(LabelFilter labelFilter) {
            this.labelFilter = labelFilter;
            if (labelFilter != null) {
                hasFilter = true;
            }
        }
    }

    public enum Feature {
        FieldBased(1),
        IgnoreNoneSerializable(1 << 1),
        ErrorOnNoneSerializable(1 << 2),
        BeanToArray(1 << 3),
        WriteNulls(1 << 4),
        WriteMapNullValue(1 << 4),
        BrowserCompatible(1 << 5),
        NullAsDefaultValue(1 << 6),
        WriteBooleanAsNumber(1 << 7),
        WriteNonStringValueAsString(1 << 8),
        WriteClassName(1 << 9),
        NotWriteRootClassName(1 << 10),
        NotWriteHashMapArrayListClassName(1 << 11),
        NotWriteDefaultValue(1 << 12),
        WriteEnumsUsingName(1 << 13),
        WriteEnumUsingToString(1 << 14),
        IgnoreErrorGetter(1 << 15),
        PrettyFormat(1 << 16),
        ReferenceDetection(1 << 17),
        WriteNameAsSymbol(1 << 18),
        WriteBigDecimalAsPlain(1 << 19),
        UseSingleQuotes(1 << 20),
        MapSortField(1 << 21),
        WriteNullListAsEmpty(1 << 22),
        /**
         * @since 1.1
         */
        WriteNullStringAsEmpty(1 << 23),
        /**
         * @since 1.1
         */
        WriteNullNumberAsZero(1 << 24),
        /**
         * @since 1.1
         */
        WriteNullBooleanAsFalse(1 << 25),

        /**
         * @since 2.0.7
         */
        NotWriteEmptyArray(1 << 26),
        WriteNonStringKeyAsString(1 << 27),
        /**
         * @since 2.0.11
         */
        WritePairAsJavaBean(1L << 28),

        /**
         * @since 2.0.12
         */
        OptimizedForAscii(1L << 29),

        /**
         * @since 2.0.12
         * Feature that specifies that all characters beyond 7-bit ASCII range (i.e. code points of 128 and above) need to be output using format-specific escapes (for JSON, backslash escapes),
         * if format uses escaping mechanisms (which is generally true for textual formats but not for binary formats).
         * Feature is disabled by default.
         */
        EscapeNoneAscii(1L << 30),
        /**
         * @since 2.0.13
         */
        WriteByteArrayAsBase64(1L << 31),

        /**
         * @since 2.0.13
         */
        IgnoreNonFieldGetter(1L << 32),

        /**
         * @since 2.0.16
         */
        LargeObject(1L << 33),

        /**
         * @since 2.0.17
         */
        WriteLongAsString(1L << 34),

        /**
         * @since 2.0.20
         */
        BrowserSecure(1L << 35);

        public final long mask;

        Feature(long mask) {
            this.mask = mask;
        }
    }

    public static final class Path {
        public static final Path ROOT = new Path(null, "$");

        public final Path parent;
        final String name;
        final int index;
        String fullPath;

        Path child0;
        Path child1;

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

                    if (JVM_VERSION == 8) {
                        char[] chars = getCharArray(name);
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
                                            if (off == buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }
                                            buf[off++] = (byte) '?';
                                        } else {
                                            if (off + 3 >= buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }
                                            buf[off++] = (byte) (0xf0 | ((uc >> 18)));
                                            buf[off++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                                            buf[off++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                                            buf[off++] = (byte) (0x80 | (uc & 0x3f));
                                            j++; // 2 chars
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
                                            if (off == buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }

                                            buf[off++] = (byte) '?';
                                        } else {
                                            if (off + 3 >= buf.length) {
                                                int newCapacity = buf.length + (buf.length >> 1);
                                                buf = Arrays.copyOf(buf, newCapacity);
                                            }

                                            buf[off++] = (byte) (0xf0 | ((uc >> 18)));
                                            buf[off++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                                            buf[off++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                                            buf[off++] = (byte) (0x80 | (uc & 0x3f));
                                            j++; // 2 chars
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
                if (STRING_CREATOR_JDK11 != null) {
                    byte[] bytes;
                    if (off == buf.length) {
                        bytes = buf;
                    } else {
                        bytes = new byte[off];
                        System.arraycopy(buf, 0, bytes, 0, off);
                    }
                    return fullPath = STRING_CREATOR_JDK11.apply(bytes, LATIN1);
                }

                if (STRING_CREATOR_JDK8 != null) {
                    char[] chars = new char[off];
                    for (int i = 0; i < off; i++) {
                        chars[i] = (char) buf[i];
                    }
                    return fullPath = STRING_CREATOR_JDK8.apply(chars, Boolean.TRUE);
                }
            }

            return fullPath = new String(buf, 0, off, ascii ? StandardCharsets.US_ASCII : StandardCharsets.UTF_8);
        }
    }
}
