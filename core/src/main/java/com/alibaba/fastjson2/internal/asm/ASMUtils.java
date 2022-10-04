package com.alibaba.fastjson2.internal.asm;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONPathCompilerReflect;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.reader.*;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;
import com.alibaba.fastjson2.writer.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;

public class ASMUtils {
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
        descMapping.put(java.util.List.class, "Ljava/util/List;");
        typeMapping.put(java.util.Collection.class, "Ljava/util/Collection;");

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
                ObjectWriter.class,
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
                JSONWriter.class,
                JSONWriter.Context.class,
                JSONB.class
        };
        for (Class objectType : classes) {
            typeMapping.put(objectType, objectType.getName().replace('.', '/'));
        }
    }

    public static String type(Class<?> clazz) {
        String type = typeMapping.get(clazz);
        if (type != null) {
            return type;
        }

        if (clazz.isArray()) {
            return "[" + desc(clazz.getComponentType());
        }

        if (clazz.isPrimitive()) {
            return typeMapping.get(clazz);
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

        if (clazz.isPrimitive()) {
            return typeMapping.get(clazz);
        }

        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            switch (componentType.getName()) {
                case "com.alibaba.fastjson2.writer.FieldWriter":
                    return "[Lcom/alibaba/fastjson2/writer/FieldWriter;";
                case "com.alibaba.fastjson2.reader.FieldReader":
                    return "[Lcom/alibaba/fastjson2/reader/FieldReader;";
                default:
                    return "[" + desc(componentType);
            }
        }

        String className = clazz.getName();
        switch (className) {
            case "java.util.Date":
                return "Ljava/util/Date;";
            case "java.lang.String":
                return "Ljava/lang/String;";
            case "com.alibaba.fastjson2.writer.ObjectWriter":
                return "Lcom/alibaba/fastjson2/writer/ObjectWriter;";
            case "com.alibaba.fastjson2.JSONWriter":
                return "Lcom/alibaba/fastjson2/JSONWriter;";
            case "com.alibaba.fastjson2.writer.FieldWriter":
                return "Lcom/alibaba/fastjson2/writer/FieldWriter;";
            case "com.alibaba.fastjson2.JSONReader":
                return "Lcom/alibaba/fastjson2/JSONReader;";
            case "com.alibaba.fastjson2.reader.FieldReader":
                return "Lcom/alibaba/fastjson2/reader/FieldReader;";
            case "com.alibaba.fastjson2.reader.ObjectReader":
                return "Lcom/alibaba/fastjson2/reader/ObjectReader;";
            case "java.util.function.Supplier":
                return "Ljava/util/function/Supplier;";
            case "com.alibaba.fastjson2.schema.JSONSchema":
                return "Lcom/alibaba/fastjson2/schema/JSONSchema;";
            case "com.alibaba.fastjson2.annotation.JSONType":
                return "Lcom/alibaba/fastjson2/annotation/JSONType;";
            default:
                break;
        }

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
            ClassReader reader = new ClassReader(is, false);
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
