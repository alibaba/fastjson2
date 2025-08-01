package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.modules.ObjectCodecProvider;
import com.alibaba.fastjson2.modules.ObjectWriterAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ObjectWriterProvider
        implements ObjectCodecProvider {
    static final int TYPE_INT32_MASK = 1 << 1;
    static final int TYPE_INT64_MASK = 1 << 2;
    static final int TYPE_DECIMAL_MASK = 1 << 3;
    static final int TYPE_DATE_MASK = 1 << 4;
    static final int TYPE_ENUM_MASK = 1 << 5;
    static final int NAME_COMPATIBLE_WITH_FILED = 1 << 6; // compatibleWithFieldName 1.x

    final ConcurrentMap<Type, ObjectWriter> cache = new ConcurrentHashMap<>();
    final ConcurrentMap<Type, ObjectWriter> cacheFieldBased = new ConcurrentHashMap<>();
    final ConcurrentMap<Class, Class> mixInCache = new ConcurrentHashMap<>();
    final ObjectWriterCreator creator;
    final List<ObjectWriterModule> modules = new ArrayList<>();
    PropertyNamingStrategy namingStrategy;

    boolean disableReferenceDetect = JSONFactory.isDisableReferenceDetect();
    boolean disableArrayMapping = JSONFactory.isDisableArrayMapping();
    boolean disableJSONB = JSONFactory.isDisableJSONB();
    boolean disableAutoType = JSONFactory.isDisableAutoType();
    boolean skipTransient = JSONFactory.isDefaultSkipTransient();

    volatile long userDefineMask;
    boolean alphabetic = JSONFactory.isDefaultWriterAlphabetic();

    public ObjectWriterProvider() {
        this((PropertyNamingStrategy) null);
    }

    public ObjectWriterProvider(PropertyNamingStrategy namingStrategy) {
        init();

        ObjectWriterCreator creator = null;
        switch (JSONFactory.CREATOR) {
            case "reflect":
            case "lambda":
                creator = ObjectWriterCreator.INSTANCE;
                break;
            case "asm":
            default:
                try {
                    if (!JDKUtils.ANDROID && !JDKUtils.GRAAL) {
                        creator = ObjectWriterCreatorASM.INSTANCE;
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
                if (creator == null) {
                    creator = ObjectWriterCreator.INSTANCE;
                }
                break;
        }
        this.creator = creator;
        this.namingStrategy = namingStrategy;
    }

    public ObjectWriterProvider(ObjectWriterCreator creator) {
        init();
        this.creator = creator;
    }

    public PropertyNamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    /**
     * @deprecated only use compatible with fastjson 1.x
     */
    public void setCompatibleWithFieldName(boolean stat) {
        if (stat) {
            userDefineMask |= NAME_COMPATIBLE_WITH_FILED;
        } else {
            userDefineMask &= ~NAME_COMPATIBLE_WITH_FILED;
        }
    }

    public void setNamingStrategy(PropertyNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
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

    public ObjectWriterCreator getCreator() {
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        if (contextCreator != null) {
            return contextCreator;
        }
        return creator;
    }

    public ObjectWriter register(Type type, ObjectWriter objectWriter) {
        boolean fieldBased = (JSONFactory.getDefaultWriterFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
        return register(type, objectWriter, fieldBased);
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

        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;

        if (objectWriter == null) {
            return cache.remove(type);
        }

        return cache.put(type, objectWriter);
    }

    public ObjectWriter registerIfAbsent(Type type, ObjectWriter objectWriter) {
        return registerIfAbsent(type, objectWriter, false);
    }

    public ObjectWriter registerIfAbsent(Type type, ObjectWriter objectWriter, boolean fieldBased) {
        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.putIfAbsent(type, objectWriter);
    }

    public ObjectWriter unregister(Type type) {
        return unregister(type, false);
    }

    public ObjectWriter unregister(Type type, boolean fieldBased) {
        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type);
    }

    public boolean unregister(Type type, ObjectWriter objectWriter) {
        return unregister(type, objectWriter, false);
    }

    public boolean unregister(Type type, ObjectWriter objectWriter, boolean fieldBased) {
        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type, objectWriter);
    }

    public boolean register(ObjectWriterModule module) {
        for (int i = modules.size() - 1; i >= 0; i--) {
            if (modules.get(i) == module) {
                return false;
            }
        }

        module.init(this);

        modules.add(0, module);
        return true;
    }

    public boolean unregister(ObjectWriterModule module) {
        return modules.remove(module);
    }

    public Class getMixIn(Class target) {
        return mixInCache.get(target);
    }

    public void init() {
        modules.add(new ObjectWriterBaseModule(this));
    }

    public List<ObjectWriterModule> getModules() {
        return modules;
    }

    public void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectClass, Field field) {
        for (int i = 0; i < modules.size(); i++) {
            ObjectWriterModule module = modules.get(i);
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getFieldInfo(beanInfo, fieldInfo, objectClass, field);
        }
    }

    public void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectClass, Method method) {
        for (int i = 0; i < modules.size(); i++) {
            ObjectWriterModule module = modules.get(i);
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getFieldInfo(beanInfo, fieldInfo, objectClass, method);
        }
    }

    public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
        if (namingStrategy != null && namingStrategy != PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue) {
            beanInfo.namingStrategy = namingStrategy.name();
        }

        for (int i = 0; i < modules.size(); i++) {
            ObjectWriterModule module = modules.get(i);
            ObjectWriterAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getBeanInfo(beanInfo, objectClass);
        }
    }

    public ObjectWriter getObjectWriter(Type objectType, String format, Locale locale) {
        if (objectType == Double.class) {
            return new ObjectWriterImplDouble(new DecimalFormat(format));
        }

        if (objectType == Float.class) {
            return new ObjectWriterImplFloat(new DecimalFormat(format));
        }

        if (objectType == BigDecimal.class) {
            return new ObjectWriterImplBigDecimal(new DecimalFormat(format), null);
        }

        if (objectType == LocalDate.class) {
            return ObjectWriterImplLocalDate.of(format, null);
        }

        if (objectType == LocalDateTime.class) {
            return new ObjectWriterImplLocalDateTime(format, null);
        }

        if (objectType == LocalTime.class) {
            return new ObjectWriterImplLocalTime(format, null);
        }

        if (objectType == Date.class) {
            return new ObjectWriterImplDate(format, null);
        }

        if (objectType == OffsetDateTime.class) {
            return ObjectWriterImplOffsetDateTime.of(format, null);
        }

        if (objectType == ZonedDateTime.class) {
            return new ObjectWriterImplZonedDateTime(format, null);
        }

        return getObjectWriter(objectType);
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

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass, String format, boolean fieldBased) {
        ObjectWriter objectWriter = getObjectWriter(objectType, objectClass, fieldBased);
        if (format != null) {
            if (objectType == LocalDateTime.class && objectWriter == ObjectWriterImplLocalDateTime.INSTANCE) {
                return ObjectWriterImplLocalDateTime.of(format, null);
            }
        }
        return objectWriter;
    }

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass, boolean fieldBased) {
        ObjectWriter objectWriter = fieldBased
                ? cacheFieldBased.get(objectType)
                : cache.get(objectType);
        return objectWriter != null
                ? objectWriter
                : getObjectWriterInternal(objectType, objectClass, fieldBased);
    }

    private ObjectWriter getObjectWriterInternal(Type objectType, Class objectClass, boolean fieldBased) {
        Class superclass = objectClass.getSuperclass();
        if (!objectClass.isEnum()
                && superclass != null
                && superclass.isEnum()
        ) {
            return getObjectWriter(superclass, superclass, fieldBased);
        }

        final String className = objectClass.getName();
        if (fieldBased) {
            if (superclass != null
                    && superclass != Object.class
                    && "com.google.protobuf.GeneratedMessageV3".equals(superclass.getName())) {
                fieldBased = false;
            } else {
                switch (className) {
                    case "springfox.documentation.spring.web.json.Json":
                    case "cn.hutool.json.JSONArray":
                    case "cn.hutool.json.JSONObject":
                    case "cn.hutool.core.map.CaseInsensitiveMap":
                    case "cn.hutool.core.map.CaseInsensitiveLinkedMap":
                        fieldBased = false;
                        break;
                    default:
                        break;
                }
            }
        } else {
            switch (className) {
                case "org.springframework.core.ResolvableType":
                    fieldBased = true;
                    break;
                default:
                    break;
            }
        }

        ObjectWriter objectWriter = fieldBased
                ? cacheFieldBased.get(objectType)
                : cache.get(objectType);

        if (objectWriter != null) {
            return objectWriter;
        }

        if (TypeUtils.isProxy(objectClass)) {
            Class<?> proxyTarget = superclass;
            if (proxyTarget == Object.class) {
                Class[] interfaces = objectClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    if (!TypeUtils.isProxy(i)) {
                        proxyTarget = i;
                        break;
                    }
                }
            }
            if (objectClass == objectType) {
                objectType = proxyTarget;
            }
            objectClass = proxyTarget;
            if (fieldBased) {
                fieldBased = false;
                objectWriter = cacheFieldBased.get(objectType);
            } else {
                objectWriter = cache.get(objectType);
            }
            if (objectWriter != null) {
                return objectWriter;
            }
        }

        boolean useModules = true;
        if (fieldBased) {
            if (Iterable.class.isAssignableFrom(objectClass)
                    && !Collection.class.isAssignableFrom(objectClass)) {
                useModules = false;
            }
        }

        if (useModules) {
            for (int i = 0; i < modules.size(); i++) {
                ObjectWriterModule module = modules.get(i);
                objectWriter = module.getObjectWriter(objectType, objectClass);
                if (objectWriter != null) {
                    ObjectWriter previous = fieldBased
                            ? cacheFieldBased.putIfAbsent(objectType, objectWriter)
                            : cache.putIfAbsent(objectType, objectWriter);

                    if (previous != null) {
                        objectWriter = previous;
                    }
                    return objectWriter;
                }
            }
        }

        switch (className) {
            case "com.google.common.collect.HashMultimap":
            case "com.google.common.collect.LinkedListMultimap":
            case "com.google.common.collect.LinkedHashMultimap":
            case "com.google.common.collect.ArrayListMultimap":
            case "com.google.common.collect.TreeMultimap":
                objectWriter = GuavaSupport.createAsMapWriter(objectClass);
                break;
            case "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList":
                objectWriter = ObjectWriterImplList.INSTANCE;
                break;
            case "com.alibaba.fastjson.JSONObject":
                objectWriter = ObjectWriterImplMap.of(objectClass);
                break;
            case "android.net.Uri$OpaqueUri":
            case "android.net.Uri$HierarchicalUri":
            case "android.net.Uri$StringUri":
                objectWriter = ObjectWriterImplToString.INSTANCE;
                break;
            case "com.clickhouse.data.value.UnsignedLong":
                objectWriter = new ObjectWriterImplToString(true);
            default:
                break;
        }

        if (objectWriter == null
                && (!fieldBased)
                && Map.class.isAssignableFrom(objectClass)
                && BeanUtils.isExtendedMap(objectClass)) {
            return ObjectWriterImplMap.of(objectClass);
        }

        if (objectWriter == null) {
            ObjectWriterCreator creator = getCreator();
            objectWriter = creator.createObjectWriter(
                    objectClass,
                    fieldBased ? JSONWriter.Feature.FieldBased.mask : 0,
                    this
            );
            ObjectWriter previous = fieldBased
                    ? cacheFieldBased.putIfAbsent(objectType, objectWriter)
                    : cache.putIfAbsent(objectType, objectWriter);

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
                java.util.Calendar.class,
                java.util.UUID.class,
                java.util.Locale.class,
                java.time.LocalTime.class,
                java.time.LocalDate.class,
                java.time.LocalDateTime.class,
                java.time.Instant.class,
                java.time.ZoneId.class,
                java.time.ZonedDateTime.class,
                java.time.OffsetDateTime.class,
                java.time.OffsetTime.class,
                AtomicInteger.class,
                AtomicLong.class,
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

    /**
     * @since 2.0.53
     */
    public void clear() {
        mixInCache.clear();
        cache.clear();
        cacheFieldBased.clear();
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
        } else if (objectWriter instanceof ObjectWriterImplOptional) {
            Class itemClass = TypeUtils.getClass(((ObjectWriterImplOptional) objectWriter).valueType);
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
        mixInCache.entrySet().removeIf
                (entry -> entry.getKey().getClassLoader() == classLoader
                );

        IdentityHashMap<ObjectWriter, Object> checkedMap = new IdentityHashMap();

        cache.entrySet().removeIf(
                entry -> match(entry.getKey(), entry.getValue(), classLoader, checkedMap)
        );

        cacheFieldBased.entrySet().removeIf(
                entry -> match(entry.getKey(), entry.getValue(), classLoader, checkedMap)
        );

        BeanUtils.cleanupCache(classLoader);
    }

    public boolean isDisableReferenceDetect() {
        return disableReferenceDetect;
    }

    public boolean isDisableAutoType() {
        return disableAutoType;
    }

    public boolean isDisableJSONB() {
        return disableJSONB;
    }

    public boolean isDisableArrayMapping() {
        return disableArrayMapping;
    }

    public void setDisableReferenceDetect(boolean disableReferenceDetect) {
        this.disableReferenceDetect = disableReferenceDetect;
    }

    public void setDisableArrayMapping(boolean disableArrayMapping) {
        this.disableArrayMapping = disableArrayMapping;
    }

    public void setDisableJSONB(boolean disableJSONB) {
        this.disableJSONB = disableJSONB;
    }

    public void setDisableAutoType(boolean disableAutoType) {
        this.disableAutoType = disableAutoType;
    }

    public boolean isAlphabetic() {
        return alphabetic;
    }

    public boolean isSkipTransient() {
        return skipTransient;
    }

    public void setSkipTransient(boolean skipTransient) {
        this.skipTransient = skipTransient;
    }

    protected BeanInfo createBeanInfo() {
        return new BeanInfo(this);
    }

    /**
     * Configure the Enum classes as a JavaBean
     * @since 2.0.55
     * @param enumClasses enum classes
     */
    @SuppressWarnings("rawtypes")
    @SafeVarargs
    public final void configEnumAsJavaBean(Class<? extends Enum>... enumClasses) {
        for (Class<? extends Enum> enumClass : enumClasses) {
            register(enumClass, getCreator().createObjectWriter(enumClass));
        }
    }
}
