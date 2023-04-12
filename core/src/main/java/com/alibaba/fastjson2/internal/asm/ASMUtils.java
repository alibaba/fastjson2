package com.alibaba.fastjson2.internal.asm;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;
import com.alibaba.fastjson2.writer.*;
import com.alibaba.fastjson2.writer.FieldWriter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class ASMUtils {
    public static final String TYPE_UNSAFE_UTILS = UnsafeUtils.class.getName().replace('.', '/');

    public static final String TYPE_OBJECT_WRITER_ADAPTER
            = ObjectWriterAdapter.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_1 = ObjectWriter1.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_2 = ObjectWriter2.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_3 = ObjectWriter3.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_4 = ObjectWriter4.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_5 = ObjectWriter5.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_6 = ObjectWriter6.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_7 = ObjectWriter7.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_8 = ObjectWriter8.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_9 = ObjectWriter9.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_10 = ObjectWriter10.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_11 = ObjectWriter11.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_12 = ObjectWriter12.class.getName().replace('.', '/');
    public static final String TYPE_FIELD_READE = FieldReader.class.getName().replace('.', '/');
    public static final String TYPE_JSON_READER = JSONReader.class.getName().replace('.', '/');

    public static final String TYPE_OBJECT_READER = ObjectReader.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_ADAPTER
            = ObjectReaderAdapter.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_1 = ObjectReader1.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_2 = ObjectReader2.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_3 = ObjectReader3.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_4 = ObjectReader4.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_5 = ObjectReader5.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_6 = ObjectReader6.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_7 = ObjectReader7.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_8 = ObjectReader8.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_9 = ObjectReader9.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_10 = ObjectReader10.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_11 = ObjectReader11.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT_READER_12 = ObjectReader12.class.getName().replace('.', '/');
    public static final String TYPE_BYTE_ARRAY_VALUE_CONSUMER = ByteArrayValueConsumer.class.getName().replace('.', '/');
    public static final String TYPE_CHAR_ARRAY_VALUE_CONSUMER = CharArrayValueConsumer.class.getName().replace('.', '/');
    public static final String TYPE_TYPE_UTILS = TypeUtils.class.getName().replace('.', '/');
    public static final String TYPE_DATE_UTILS = DateUtils.class.getName().replace('.', '/');

    public static final String TYPE_OBJECT_WRITER = ObjectWriter.class.getName().replace('.', '/');
    public static final String TYPE_JSON_WRITER = JSONWriter.class.getName().replace('.', '/');
    public static final String TYPE_FIELD_WRITER = FieldWriter.class.getName().replace('.', '/');

    public static final String DESC_FIELD_WRITER = 'L' + FieldWriter.class.getName().replace('.', '/') + ';';
    public static final String DESC_FIELD_WRITER_ARRAY = "[" + DESC_FIELD_WRITER;
    public static final String DESC_FIELD_READER = 'L' + FieldReader.class.getName().replace('.', '/') + ';';
    public static final String DESC_FIELD_READER_ARRAY = "[" + DESC_FIELD_READER;
    public static final String DESC_JSON_READER = 'L' + TYPE_JSON_READER + ';';
    public static final String DESC_JSON_WRITER = 'L' + TYPE_JSON_WRITER + ';';
    public static final String DESC_OBJECT_READER = 'L' + TYPE_OBJECT_READER + ';';
    public static final String DESC_OBJECT_WRITER = 'L' + TYPE_OBJECT_WRITER + ';';
    public static final String DESC_SUPPLIER = "Ljava/util/function/Supplier;";
    public static final String DESC_JSONSCHEMA = 'L' + JSONSchema.class.getName().replace('.', '/') + ';';

    static Map<Class, String> descMapping = new HashMap<>();
    static Map<Class, String> typeMapping = new HashMap<>();

    static {
        descMapping.put(int.class, "I");
        descMapping.put(void.class, "V");
        descMapping.put(boolean.class, "Z");
        descMapping.put(char.class, "C");
        descMapping.put(byte.class, "B");
        descMapping.put(short.class, "S");
        descMapping.put(float.class, "F");
        descMapping.put(long.class, "J");
        descMapping.put(double.class, "D");

        typeMapping.put(int.class, "I");
        typeMapping.put(void.class, "V");
        typeMapping.put(boolean.class, "Z");
        typeMapping.put(char.class, "C");
        typeMapping.put(byte.class, "B");
        typeMapping.put(short.class, "S");
        typeMapping.put(float.class, "F");
        typeMapping.put(long.class, "J");
        typeMapping.put(double.class, "D");

        Class[] classes = new Class[]{
                String.class,
                java.util.List.class,
                java.util.Collection.class,
                ObjectReader.class,
                ObjectReader1.class,
                ObjectReader2.class,
                ObjectReader3.class,
                ObjectReader4.class,
                ObjectReader5.class,
                ObjectReader6.class,
                ObjectReader7.class,
                ObjectReader8.class,
                ObjectReader9.class,
                ObjectReader10.class,
                ObjectReader11.class,
                ObjectReader12.class,
                ObjectReaderAdapter.class,
                FieldReader.class,
                JSONReader.class,
                ObjBoolConsumer.class,
                ObjCharConsumer.class,
                ObjByteConsumer.class,
                ObjShortConsumer.class,
                ObjIntConsumer.class,
                ObjLongConsumer.class,
                ObjFloatConsumer.class,
                ObjDoubleConsumer.class,
                BiConsumer.class,
                UnsafeUtils.class,
                ObjectWriterAdapter.class,
                ObjectWriter1.class,
                ObjectWriter2.class,
                ObjectWriter3.class,
                ObjectWriter4.class,
                ObjectWriter5.class,
                ObjectWriter6.class,
                ObjectWriter7.class,
                ObjectWriter8.class,
                ObjectWriter9.class,
                ObjectWriter10.class,
                ObjectWriter11.class,
                ObjectWriter12.class,
                com.alibaba.fastjson2.writer.FieldWriter.class,
                JSONPathCompilerReflect.SingleNamePathTyped.class,
                JSONWriter.Context.class,
                JSONB.class,
                JSONSchema.class,
                JSONType.class,
                java.util.Date.class,
                java.util.function.Supplier.class
        };
        for (Class objectType : classes) {
            String type = objectType.getName().replace('.', '/');
            typeMapping.put(objectType, type);
            String desc = 'L' + type + ';';
            descMapping.put(objectType, desc);
        }

        typeMapping.put(JSONWriter.class, TYPE_JSON_WRITER);
        descMapping.put(JSONWriter.class, DESC_JSON_WRITER);
        typeMapping.put(ObjectWriter.class, TYPE_OBJECT_WRITER);
        descMapping.put(ObjectWriter.class, DESC_OBJECT_WRITER);

        descMapping.put(FieldWriter[].class, DESC_FIELD_WRITER_ARRAY);
        descMapping.put(FieldReader[].class, DESC_FIELD_READER_ARRAY);
    }

    public static String type(Class<?> clazz) {
        String type = typeMapping.get(clazz);
        if (type != null) {
            return type;
        }

        if (clazz.isArray()) {
            return "[" + desc(clazz.getComponentType());
        }

        // 直接基于字符串替换，不使用正则替换
        return clazz.getName().replace('.', '/');
    }

    static final AtomicReference<char[]> descCacheRef = new AtomicReference<>();

    public static String desc(Class<?> clazz) {
        String desc = descMapping.get(clazz);
        if (desc != null) {
            return desc;
        }

        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            return "[" + desc(componentType);
        }

        String className = clazz.getName();
        char[] chars = descCacheRef.getAndSet(null);
        if (chars == null) {
            chars = new char[512];
        }
        chars[0] = 'L';
        className.getChars(0, className.length(), chars, 1);
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == '.') {
                chars[i] = '/';
            }
        }
        chars[className.length() + 1] = ';';

        String str = new String(chars, 0, className.length() + 2);
        descCacheRef.compareAndSet(null, chars);
        return str;
    }

    public static String[] lookupParameterNames(AccessibleObject methodOrCtor) {
        if (methodOrCtor instanceof Constructor) {
            Constructor constructor = (Constructor) methodOrCtor;
            Class[] parameterTypes = constructor.getParameterTypes();

            Class declaringClass = constructor.getDeclaringClass();
            if (declaringClass == DateTimeParseException.class) {
                if (parameterTypes.length == 3) {
                    if (parameterTypes[0] == String.class && parameterTypes[1] == CharSequence.class && parameterTypes[2] == int.class) {
                        return new String[]{"message", "parsedString", "errorIndex"};
                    }
                } else if (parameterTypes.length == 4) {
                    if (parameterTypes[0] == String.class && parameterTypes[1] == CharSequence.class && parameterTypes[2] == int.class && parameterTypes[3] == Throwable.class) {
                        return new String[]{"message", "parsedString", "errorIndex", "cause"};
                    }
                }
            }

            if (Throwable.class.isAssignableFrom(declaringClass)) {
                switch (parameterTypes.length) {
                    case 1:
                        if (parameterTypes[0] == String.class) {
                            return new String[]{"message"};
                        }

                        if (Throwable.class.isAssignableFrom(parameterTypes[0])) {
                            return new String[]{"cause"};
                        }
                        break;
                    case 2:
                        if (parameterTypes[0] == String.class && Throwable.class.isAssignableFrom(parameterTypes[1])) {
                            return new String[]{"message", "cause"};
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        final Class<?>[] types;
        final Class<?> declaringClass;
        final String name;

        int paramCount;
        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
            paramCount = method.getParameterCount();
        } else {
            Constructor<?> constructor = (Constructor<?>) methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
            paramCount = constructor.getParameterCount();
        }

        if (types.length == 0) {
            return new String[paramCount];
        }

        ClassLoader classLoader = declaringClass.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        String className = declaringClass.getName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = classLoader.getResourceAsStream(resourceName);

        if (is == null) {
            return new String[paramCount];
        }

        try {
            ClassReader reader = new ClassReader(is);
            TypeCollector visitor = new TypeCollector(name, types);
            reader.accept(visitor);

            String[] params = visitor.getParameterNamesForMethod();
            if (params != null && params.length == paramCount - 1) {
                Class<?> dd = declaringClass.getDeclaringClass();
                if (dd != null && dd.equals(types[0])) {
                    String[] strings = new String[paramCount];
                    strings[0] = "this$0";
                    System.arraycopy(params, 0, strings, 1, params.length);
                    params = strings;
                }
            }
            return params;
        } catch (IOException e) {
            return new String[paramCount];
        } finally {
            IOUtils.close(is);
        }
    }
}
