package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ObjectWriterProvider {
    static final int TYPE_INT32_MASK = 1 << 1;
    static final int TYPE_INT64_MASK = 1 << 2;
    static final int TYPE_DECIMAL_MASK = 1 << 3;
    static final int TYPE_DATE_MASK = 1 << 4;
    static final int TYPE_ENUM_MASK = 1 << 5;

    final ConcurrentMap<Type, ObjectWriter> cache = new ConcurrentHashMap<>();
    final ConcurrentMap<Type, ObjectWriter> cacheFieldBased = new ConcurrentHashMap<>();
    final ConcurrentMap<Class, Class> mixInCache = new ConcurrentHashMap<>();
    final ObjectWriterCreator creator;
    final ObjectWriterModule module = new ObjectWriterModule(this);
    final PropertyNamingStrategy namingStrategy;

    volatile long userDefineMask;

    public ObjectWriterProvider() {
        this((PropertyNamingStrategy) null);
    }

    public ObjectWriterProvider(PropertyNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
        init();

        this.creator = ObjectWriterCreator.INSTANCE;
    }

    public ObjectWriterProvider(ObjectWriterCreator creator) {
        init();
        this.creator = creator;
        this.namingStrategy = null;
    }

    public void mixIn(Class target, Class mixinSource) {
        if (mixinSource == null) {
            mixInCache.remove(target);
        } else {
            mixInCache.put(target, mixinSource);
        }
        cache.remove(target);
    }

    public void cleanupMixIn() {
        mixInCache.clear();
    }

    public ObjectWriter register(Type type, ObjectWriter objectWriter) {
        return register(type, objectWriter, false);
    }

    public ObjectWriter register(Type type, ObjectWriter objectWriter, boolean fieldBased) {
        if (type == Integer.class) {
            if (objectWriter == null || objectWriter == ObjectWriterImplInt32.INSTANCE) {
                userDefineMask &= ~TYPE_INT32_MASK;
            } else {
                userDefineMask |= TYPE_INT32_MASK;
            }
        } else if (type == Long.class || type == long.class) {
            if (objectWriter == null || objectWriter == ObjectWriterImplInt64.INSTANCE) {
                userDefineMask &= ~TYPE_INT64_MASK;
            } else {
                userDefineMask |= TYPE_INT64_MASK;
            }
        } else if (type == BigDecimal.class) {
            if (objectWriter == null || objectWriter == ObjectWriterImplBigDecimal.INSTANCE) {
                userDefineMask &= ~TYPE_DECIMAL_MASK;
            } else {
                userDefineMask |= TYPE_DECIMAL_MASK;
            }
        } else if (type == Date.class) {
            if (objectWriter == null || objectWriter == ObjectWriterImplDate.INSTANCE) {
                userDefineMask &= ~TYPE_DATE_MASK;
            } else {
                userDefineMask |= TYPE_DATE_MASK;
            }
        } else if (type == Enum.class) {
            if (objectWriter == null) {
                userDefineMask &= ~TYPE_ENUM_MASK;
            } else {
                userDefineMask |= TYPE_ENUM_MASK;
            }
        }

        if (objectWriter == null) {
            if (fieldBased) {
                return cacheFieldBased.remove(type);
            } else {
                return cache.remove(type);
            }
        }

        if (fieldBased) {
            return cacheFieldBased.put(type, objectWriter);
        } else {
            return cache.put(type, objectWriter);
        }
    }

    public ObjectWriter registerIfAbsent(Type type, ObjectWriter objectWriter) {
        ObjectWriter origin = cache.get(type);
        if (origin == null) {
            cache.put(type, objectWriter);
        }
        return origin;
    }

    public ObjectWriter unregister(Type type) {
        return cache.remove(type);
    }

    public boolean unregister(Type type, ObjectWriter objectWriter) {
        return cache.remove(type, objectWriter);
    }

    public Class getMixIn(Class target) {
        return mixInCache.get(target);
    }

    public void init() {
    }

    public void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectClass, Field field) {
        module.getFieldInfo(beanInfo, fieldInfo, objectClass, field);
    }

    public void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectClass, Method method) {
        module.getFieldInfo(beanInfo, fieldInfo, objectClass, method);
    }

    public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
        if (namingStrategy != null && namingStrategy != PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue) {
            beanInfo.namingStrategy = namingStrategy.name();
        }

        module.getBeanInfo(beanInfo, objectClass);
    }

    public ObjectWriter getObjectWriter(Class objectClass) {
        return getObjectWriter(objectClass, objectClass, false);
    }

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        return getObjectWriter(objectType, objectClass, false);
    }

    public ObjectWriter getObjectWriter(Type objectType) {
        Class objectClass = TypeUtils.getClass(objectType);
        return getObjectWriter(objectType, objectClass, false);
    }

    public ObjectWriter getObjectWriterFromCache(Type objectType, Class objectClass, boolean fieldBased) {
        return fieldBased
                ? cacheFieldBased.get(objectType)
                : cache.get(objectType);
    }

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass, boolean fieldBased) {
        Class superclass = objectClass.getSuperclass();
        if (!objectClass.isEnum()
                && superclass != null
                && superclass.isEnum()
        ) {
            return getObjectWriter(superclass, superclass, fieldBased);
        }

        if (fieldBased) {
            if (superclass != null
                    && superclass != Object.class
                    && superclass.getName().equals("com.google.protobuf.GeneratedMessageV3")) {
                fieldBased = false;
            }
        }

        ObjectWriter objectWriter = fieldBased
                ? cacheFieldBased.get(objectType)
                : cache.get(objectType);

        if (objectWriter != null) {
            return objectWriter;
        }

        boolean useModules = true;
        if (fieldBased) {
            if (Iterable.class.isAssignableFrom(objectClass)
                    && !Collection.class.isAssignableFrom(objectClass)) {
                useModules = false;
            }
        }

        if (useModules) {
            objectWriter = module.getObjectWriter(objectType, objectClass);
            if (objectWriter != null) {
                ObjectWriter previous = fieldBased
                        ? cacheFieldBased.put(objectType, objectWriter)
                        : cache.put(objectType, objectWriter);

                if (previous != null) {
                    objectWriter = previous;
                }
                return objectWriter;
            }
        }

        String className = objectClass.getName();
        switch (className) {
            case "com.alibaba.fastjson.JSONObject":
                objectWriter = ObjectWriterImplMap.of(objectClass);
                break;
            case "android.net.Uri$OpaqueUri":
            case "android.net.Uri$HierarchicalUri":
            case "android.net.Uri$StringUri":
                objectWriter = ObjectWriterImplToString.INSTANCE;
                break;
            default:
                break;
        }

        if (objectWriter == null) {
            objectWriter = creator.createObjectWriter(
                    objectClass,
                    fieldBased ? JSONWriter.Feature.FieldBased.mask : 0,
                    this
            );
            ObjectWriter previous = fieldBased
                    ? cacheFieldBased.put(objectType, objectWriter)
                    : cache.put(objectType, objectWriter);

            if (previous != null) {
                objectWriter = previous;
            }
        }
        return objectWriter;
    }

    static final int ENUM = 0x00004000;
    static final int[] PRIMITIVE_HASH_CODES;
    static final int[] NOT_REFERENCES_TYPE_HASH_CODES;

    static {
        Class<?>[] classes = new Class[]{
                boolean.class,
                Boolean.class,
                Character.class,
                char.class,
                Byte.class,
                byte.class,
                Short.class,
                short.class,
                Integer.class,
                int.class,
                Long.class,
                long.class,
                Float.class,
                float.class,
                Double.class,
                double.class,
                BigInteger.class,
                BigDecimal.class,
                String.class,
                java.util.Currency.class,
                java.util.Date.class,
                java.util.UUID.class,
                java.util.Locale.class,
                String.class,
                StackTraceElement.class,
                Collections.emptyList().getClass(),
                Collections.emptyMap().getClass(),
                Collections.emptySet().getClass()
        };

        int[] codes = new int[classes.length];
        for (int i = 0; i < classes.length; i++) {
            codes[i] = System.identityHashCode(classes[i]);
        }
        Arrays.sort(codes);
        PRIMITIVE_HASH_CODES = codes;

        int[] codes2 = Arrays.copyOf(codes, codes.length + 3);
        codes2[codes2.length - 1] = System.identityHashCode(Class.class);
        codes2[codes2.length - 2] = System.identityHashCode(int[].class);
        codes2[codes2.length - 3] = System.identityHashCode(long[].class);
        Arrays.sort(codes2);
        NOT_REFERENCES_TYPE_HASH_CODES = codes2;
    }

    public static boolean isPrimitiveOrEnum(final Class<?> clazz) {
        return Arrays.binarySearch(PRIMITIVE_HASH_CODES, System.identityHashCode(clazz)) >= 0
                || ((clazz.getModifiers() & ENUM) != 0 && clazz.getSuperclass() == Enum.class);
    }

    public static boolean isNotReferenceDetect(final Class<?> clazz) {
        return Arrays.binarySearch(NOT_REFERENCES_TYPE_HASH_CODES, System.identityHashCode(clazz)) >= 0
                || ((clazz.getModifiers() & ENUM) != 0 && clazz.getSuperclass() == Enum.class);
    }

    public void cleanup(Class objectClass) {
        mixInCache.remove(objectClass);
        cache.remove(objectClass);
        cacheFieldBased.remove(objectClass);

        BeanUtils.cleanupCache(objectClass);
    }

    static boolean match(Type objectType, ObjectWriter objectWriter, ClassLoader classLoader, IdentityHashMap<ObjectWriter, Object> checkedMap) {
        Class<?> objectClass = TypeUtils.getClass(objectType);
        if (objectClass != null && objectClass.getClassLoader() == classLoader) {
            return true;
        }

        if (checkedMap.containsKey(objectWriter)) {
            return false;
        }

        if (objectWriter instanceof ObjectWriterImplMap) {
            ObjectWriterImplMap mapTyped = (ObjectWriterImplMap) objectWriter;
            Class valueClass = TypeUtils.getClass(mapTyped.valueType);
            if (valueClass != null && valueClass.getClassLoader() == classLoader) {
                return true;
            }
            Class keyClass = TypeUtils.getClass(mapTyped.keyType);
            return keyClass != null && keyClass.getClassLoader() == classLoader;
        } else if (objectWriter instanceof ObjectWriterImplCollection) {
            Class itemClass = TypeUtils.getClass(((ObjectWriterImplCollection) objectWriter).itemType);
            return itemClass != null && itemClass.getClassLoader() == classLoader;
        } else if (objectWriter instanceof ObjectWriterAdapter) {
            checkedMap.put(objectWriter, null);
            List<FieldWriter> fieldWriters = ((ObjectWriterAdapter<?>) objectWriter).fieldWriters;
            for (int i = 0; i < fieldWriters.size(); i++) {
                FieldWriter fieldWriter = fieldWriters.get(i);
                if (fieldWriter instanceof FieldWriterObject) {
                    ObjectWriter initObjectWriter = ((FieldWriterObject<?>) fieldWriter).initObjectWriter;
                    if (match(null, initObjectWriter, classLoader, checkedMap)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void cleanup(ClassLoader classLoader) {
        for (Iterator<Map.Entry<Class, Class>> it = mixInCache.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Class, Class> entry = it.next();
            if (entry.getKey().getClassLoader() == classLoader) {
                it.remove();
            }
        }

        IdentityHashMap<ObjectWriter, Object> checkedMap = new IdentityHashMap();

        for (Iterator<Map.Entry<Type, ObjectWriter>> it = cache.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Type, ObjectWriter> entry = it.next();
            if (match(entry.getKey(), entry.getValue(), classLoader, checkedMap)) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<Type, ObjectWriter>> it = cacheFieldBased.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Type, ObjectWriter> entry = it.next();
            if (match(entry.getKey(), entry.getValue(), classLoader, checkedMap)) {
                it.remove();
            }
        }

        BeanUtils.cleanupCache(classLoader);
    }
}
