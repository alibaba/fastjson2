package com.alibaba.fastjson2.internal.asm;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderAdapter;
import com.alibaba.fastjson2.util.IOUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

        Class[] classes = new Class[] {
                java.util.List.class,
                java.util.Collection.class,
                ObjectReader.class,
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
                com.alibaba.fastjson2.writer.FieldWriter.class,
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
            return "[" + desc(clazz.getComponentType());
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
        final Class<?>[] types;
        final Class<?> declaringClass;
        final String name;

        if (methodOrCtor instanceof Method) {
            Method method = (Method) methodOrCtor;
            types = method.getParameterTypes();
            name = method.getName();
            declaringClass = method.getDeclaringClass();
        } else {
            Constructor<?> constructor = (Constructor<?>) methodOrCtor;
            types = constructor.getParameterTypes();
            declaringClass = constructor.getDeclaringClass();
            name = "<init>";
        }

        if (types.length == 0) {
            return new String[0];
        }

        ClassLoader classLoader = declaringClass.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        String className = declaringClass.getName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = classLoader.getResourceAsStream(resourceName);

        if (is == null) {
            return new String[0];
        }

        try {
            ClassReader reader = new ClassReader(is, false);
            TypeCollector visitor = new TypeCollector(name, types);
            reader.accept(visitor);

            return visitor.getParameterNamesForMethod();
        } catch (IOException e) {
            return new String[0];
        } finally {
            IOUtils.close(is);
        }
    }
}
