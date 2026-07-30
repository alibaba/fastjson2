package com.alibaba.fastjson2.internal.asm;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.function.ObjLongConsumer;

public final class ASMUtils {
    public static final String TYPE_UNSAFE_UTILS = JDKUtils.class.getName().replace('.', '/');

    public static final String TYPE_OBJECT_WRITER_ADAPTER
            = "com/alibaba/fastjson2/writer/ObjectWriterAdapter".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_1 = "com/alibaba/fastjson2/writer/ObjectWriter1".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_2 = "com/alibaba/fastjson2/writer/ObjectWriter2".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_3 = "com/alibaba/fastjson2/writer/ObjectWriter3".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_4 = "com/alibaba/fastjson2/writer/ObjectWriter4".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_5 = "com/alibaba/fastjson2/writer/ObjectWriter5".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_6 = "com/alibaba/fastjson2/writer/ObjectWriter6".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_7 = "com/alibaba/fastjson2/writer/ObjectWriter7".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_8 = "com/alibaba/fastjson2/writer/ObjectWriter8".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_9 = "com/alibaba/fastjson2/writer/ObjectWriter9".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_10 = "com/alibaba/fastjson2/writer/ObjectWriter10".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_11 = "com/alibaba/fastjson2/writer/ObjectWriter11".replace('.', '/');
    public static final String TYPE_OBJECT_WRITER_12 = "com/alibaba/fastjson2/writer/ObjectWriter12".replace('.', '/');
    public static final String TYPE_FIELD_READE = "com/alibaba/fastjson2/reader/FieldReader".replace('.', '/');
    public static final String TYPE_JSON_READER = JSONReader.class.getName().replace('.', '/');

    public static final String TYPE_OBJECT_READER = "com/alibaba/fastjson2/reader/ObjectReader".replace('.', '/');
    public static final String TYPE_OBJECT_READER_ADAPTER
            = "com/alibaba/fastjson2/reader/ObjectReaderAdapter".replace('.', '/');
    public static final String TYPE_OBJECT_READER_1 = "com/alibaba/fastjson2/reader/ObjectReader1".replace('.', '/');
    public static final String TYPE_OBJECT_READER_2 = "com/alibaba/fastjson2/reader/ObjectReader2".replace('.', '/');
    public static final String TYPE_OBJECT_READER_3 = "com/alibaba/fastjson2/reader/ObjectReader3".replace('.', '/');
    public static final String TYPE_OBJECT_READER_4 = "com/alibaba/fastjson2/reader/ObjectReader4".replace('.', '/');
    public static final String TYPE_OBJECT_READER_5 = "com/alibaba/fastjson2/reader/ObjectReader5".replace('.', '/');
    public static final String TYPE_OBJECT_READER_6 = "com/alibaba/fastjson2/reader/ObjectReader6".replace('.', '/');
    public static final String TYPE_OBJECT_READER_7 = "com/alibaba/fastjson2/reader/ObjectReader7".replace('.', '/');
    public static final String TYPE_OBJECT_READER_8 = "com/alibaba/fastjson2/reader/ObjectReader8".replace('.', '/');
    public static final String TYPE_OBJECT_READER_9 = "com/alibaba/fastjson2/reader/ObjectReader9".replace('.', '/');
    public static final String TYPE_OBJECT_READER_10 = "com/alibaba/fastjson2/reader/ObjectReader10".replace('.', '/');
    public static final String TYPE_OBJECT_READER_11 = "com/alibaba/fastjson2/reader/ObjectReader11".replace('.', '/');
    public static final String TYPE_OBJECT_READER_12 = "com/alibaba/fastjson2/reader/ObjectReader12".replace('.', '/');
    public static final String TYPE_OBJECT_READER_NONE_DEFAULT_CONSTRUCTOR = "com/alibaba/fastjson2/reader/ObjectReaderNoneDefaultConstructor".replace('.', '/');
    public static final String TYPE_BYTE_ARRAY_VALUE_CONSUMER = "com/alibaba/fastjson2/reader/ByteArrayValueConsumer".replace('.', '/');
    public static final String TYPE_CHAR_ARRAY_VALUE_CONSUMER = "com/alibaba/fastjson2/reader/CharArrayValueConsumer".replace('.', '/');
    public static final String TYPE_TYPE_UTILS = TypeUtils.class.getName().replace('.', '/');
    public static final String TYPE_DATE_UTILS = DateUtils.class.getName().replace('.', '/');

    public static final String TYPE_OBJECT_WRITER = "com/alibaba/fastjson2/writer/ObjectWriter".replace('.', '/');
    public static final String TYPE_JSON_WRITER = JSONWriter.class.getName().replace('.', '/');
    public static final String TYPE_JSONB_IO = "com/alibaba/fastjson2/JSONB$IO".replace('.', '/');
    public static final String TYPE_FIELD_WRITER = "com/alibaba/fastjson2/writer/FieldWriter".replace('.', '/');
    public static final String TYPE_IO_UTILS = IOUtils.class.getName().replace('.', '/');
    public static final String TYPE_OBJECT = "java/lang/Object";

    public static final String DESC_FIELD_WRITER = 'L' + "com/alibaba/fastjson2/writer/FieldWriter".replace('.', '/') + ';';
    public static final String DESC_FIELD_WRITER_ARRAY = "[" + DESC_FIELD_WRITER;
    public static final String DESC_FIELD_READER = 'L' + "com/alibaba/fastjson2/reader/FieldReader".replace('.', '/') + ';';
    public static final String DESC_FIELD_READER_ARRAY = "[" + DESC_FIELD_READER;
    public static final String DESC_JSON_READER = 'L' + TYPE_JSON_READER + ';';
    public static final String DESC_JSON_WRITER = 'L' + TYPE_JSON_WRITER + ';';
    public static final String DESC_OBJECT_READER = 'L' + TYPE_OBJECT_READER + ';';
    public static final String DESC_OBJECT_READER_ADAPTER = 'L' + TYPE_OBJECT_READER_ADAPTER + ';';
    public static final String DESC_OBJECT_WRITER = 'L' + TYPE_OBJECT_WRITER + ';';
    public static final String DESC_SUPPLIER = "Ljava/util/function/Supplier;";
    public static final String DESC_JSONSCHEMA = 'L' + "com/alibaba/fastjson2/schema/JSONSchema".replace('.', '/') + ';';

    static final Map<MethodInfo, String[]> paramMapping = new HashMap<>();

    static final Map<Class, String> descMapping = new HashMap<>();
    static final Map<Class, String> typeMapping = new HashMap<>();

    static {
        paramMapping.put(
                new MethodInfo(
                        ParameterizedTypeImpl.class.getName(),
                        "<init>",
                        new String[]{"[Ljava.lang.reflect.Type;", "java.lang.reflect.Type", "java.lang.reflect.Type"}
                ),
                new String[]{"actualTypeArguments", "ownerType", "rawType"}
        );

        paramMapping.put(
                new MethodInfo(
                        "org.apache.commons.lang3.tuple.Triple",
                        "of",
                        new String[]{"java.lang.Object", "java.lang.Object", "java.lang.Object"}
                ),
                new String[]{"left", "middle", "right"}
        );
        paramMapping.put(
                new MethodInfo(
                        "org.apache.commons.lang3.tuple.MutableTriple",
                        "<init>",
                        new String[]{"java.lang.Object", "java.lang.Object", "java.lang.Object"}
                ),
                new String[]{"left", "middle", "right"}
        );

        paramMapping.put(
                new MethodInfo(
                        "org.javamoney.moneta.Money",
                        "<init>",
                        new String[]{"java.math.BigDecimal", "javax.money.CurrencyUnit", "javax.money.MonetaryContext"}
                ),
                new String[]{"number", "currency", "monetaryContext"}
        );
        paramMapping.put(
                new MethodInfo(
                        "org.javamoney.moneta.Money",
                        "<init>",
                        new String[]{"java.math.BigDecimal", "javax.money.CurrencyUnit"}
                ),
                new String[]{"number", "currency"}
        );

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
                JDKUtils.class,
                JSONWriter.Context.class,
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
        // ObjectWriter reference removed
        // ObjectWriter reference removed

        // FieldWriter[] removed
        // FieldReader[] removed
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

        String[] paramNames = paramMapping.get(new MethodInfo(declaringClass.getName(), name, types));
        if (paramNames != null) {
            return paramNames;
        }

        ClassLoader classLoader = declaringClass.getClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        String className = declaringClass.getName();
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = classLoader.getResourceAsStream(resourceName);

        if (is != null) {
            try {
                ClassReader reader = new ClassReader(is);
                TypeCollector visitor = new TypeCollector(name, types);
                reader.accept(visitor);

                paramNames = visitor.getParameterNamesForMethod();
                if (paramNames != null && paramNames.length == paramCount - 1) {
                    Class<?> dd = declaringClass.getDeclaringClass();
                    if (dd != null && dd.equals(types[0])) {
                        String[] strings = new String[paramCount];
                        strings[0] = "this$0";
                        System.arraycopy(paramNames, 0, strings, 1, paramNames.length);
                        paramNames = strings;
                    }
                }
                return paramNames;
            } catch (IOException | ArrayIndexOutOfBoundsException ignored) {
                // ignored
            } finally {
                IOUtils.close(is);
            }
        }

        paramNames = new String[paramCount];
        int i;
        if (types[0] == declaringClass.getDeclaringClass()
                && !Modifier.isStatic(declaringClass.getModifiers())) {
            paramNames[0] = "this.$0";
            i = 1;
        } else {
            i = 0;
        }
        for (; i < paramNames.length; i++) {
            paramNames[i] = "arg" + i;
        }
        return paramNames;
    }

    static final class MethodInfo {
        final String className;
        final String methodName;
        final String[] paramTypeNames;
        int hash;

        public MethodInfo(String className, String methodName, String[] paramTypeNames) {
            this.className = className;
            this.methodName = methodName;
            this.paramTypeNames = paramTypeNames;
        }

        public MethodInfo(String className, String methodName, Class[] paramTypes) {
            this.className = className;
            this.methodName = methodName;
            this.paramTypeNames = new String[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypeNames[i] = paramTypes[i].getName();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MethodInfo that = (MethodInfo) o;
            return Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName) && Arrays.equals(paramTypeNames, that.paramTypeNames);
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                int result = Objects.hash(className, methodName);
                result = 31 * result + Arrays.hashCode(paramTypeNames);
                hash = result;
            }
            return hash;
        }
    }
}
