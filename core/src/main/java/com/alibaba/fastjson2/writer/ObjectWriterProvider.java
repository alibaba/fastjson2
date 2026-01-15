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

/**
 * ObjectWriterProvider is responsible for providing and managing ObjectWriter instances
 * for serializing Java objects into JSON format. It handles object writer creation, caching,
 * type conversion, and various serialization features.
 *
 * <p>This provider supports various features including:
 * <ul>
 *   <li>Object writer caching for performance optimization</li>
 *   <li>Mixin support for modifying serialization behavior</li>
 *   <li>Module-based extensibility</li>
 *   <li>Custom type writer registration</li>
 *   <li>Property naming strategy configuration</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Get default provider
 * ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
 *
 * // Get object writer for a specific type
 * ObjectWriter&lt;User&gt; writer = provider.getObjectWriter(User.class);
 *
 * // Serialize object to JSON string
 * User user = new User(1, "John");
 * String jsonString = writer.toJSONString(user);
 *
 * // Register custom object writer
 * provider.register(User.class, new CustomUserWriter());
 * </pre>
 *
 * @since 2.0.0
 */
@SuppressWarnings("ALL")
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

    /**
     * Constructs an ObjectWriterProvider with default settings.
     */
    public ObjectWriterProvider() {
        this((PropertyNamingStrategy) null);
    }

    /**
     * Constructs an ObjectWriterProvider with the specified naming strategy.
     *
     * @param namingStrategy the property naming strategy to use
     */
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

    /**
     * Constructs an ObjectWriterProvider with the specified ObjectWriterCreator.
     *
     * @param creator the ObjectWriterCreator to use for creating ObjectWriter instances
     */
    public ObjectWriterProvider(ObjectWriterCreator creator) {
        init();
        this.creator = creator;
    }

    /**
     * Gets the property naming strategy used by this provider.
     *
     * @return the property naming strategy, or null if none is set
     */
    public PropertyNamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    /**
     * Sets whether to use compatible field name behavior.
     *
     * @param stat true to enable compatible field name behavior, false to disable
     * @deprecated only use compatible with fastjson 1.x
     */
    public void setCompatibleWithFieldName(boolean stat) {
        if (stat) {
            userDefineMask |= NAME_COMPATIBLE_WITH_FILED;
        } else {
            userDefineMask &= ~NAME_COMPATIBLE_WITH_FILED;
        }
    }

    /**
     * Sets the property naming strategy used by this provider.
     *
     * @param namingStrategy the property naming strategy to set
     */
    public void setNamingStrategy(PropertyNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }

    /**
     * Registers a mixin mapping between a target class and a mixin source class.
     * Mixin allows modifying the serialization behavior of a class by applying
     * annotations from another class.
     *
     * @param target the target class to which the mixin will be applied
     * @param mixinSource the source class from which annotations will be copied, or null to remove the mixin
     */
    public void mixIn(Class target, Class mixinSource) {
        if (mixinSource == null) {
            mixInCache.remove(target);
        } else {
            mixInCache.put(target, mixinSource);
        }
        cache.remove(target);
    }

    /**
     * Clears all mixin mappings.
     */
    public void cleanupMixIn() {
        mixInCache.clear();
    }

    /**
     * Gets the ObjectWriterCreator used by this provider. If a context-specific creator
     * is available, it will be returned; otherwise, the default creator for this provider
     * will be returned.
     *
     * @return the ObjectWriterCreator
     */
    public ObjectWriterCreator getCreator() {
        ObjectWriterCreator contextCreator = JSONFactory.getContextWriterCreator();
        if (contextCreator != null) {
            return contextCreator;
        }
        return creator;
    }

    /**
     * Registers an ObjectWriter for the specified type using the default field-based setting.
     *
     * @param type the type for which to register the ObjectWriter
     * @param objectWriter the ObjectWriter to register, or null to unregister
     * @return the previous ObjectWriter for the type, or null if there was no previous ObjectWriter
     */
    public ObjectWriter register(Type type, ObjectWriter objectWriter) {
        boolean fieldBased = (JSONFactory.getDefaultWriterFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
        return register(type, objectWriter, fieldBased);
    }

    public boolean isDefaultWriter(Type type) {
        boolean fieldBased = (JSONFactory.getDefaultWriterFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;
        if (type == Long[].class || type == long[].class) {
            ObjectWriter cached = cache.get(Long.class);
            return cached == null || cached == ObjectWriterImplInt64.INSTANCE;
        }
        ObjectWriter cached = cache.get(type);
        if (cached == null) {
            return true;
        }
        if (type == LocalDate.class) {
            return cached == ObjectWriterImplLocalDate.INSTANCE;
        }
        if (type == LocalDate.class) {
            return cached == ObjectWriterImplLocalDate.INSTANCE;
        }
        if (type == LocalDateTime.class) {
            return cached == ObjectWriterImplLocalDateTime.INSTANCE;
        }
        if (type == LocalTime.class) {
            return cached == ObjectWriterImplLocalTime.INSTANCE;
        }
        if (type == Long.class) {
            return cached == ObjectWriterImplInt64.INSTANCE;
        }
        return true;
    }

    /**
     * Registers an ObjectWriter for the specified type.
     *
     * @param type the type for which to register the ObjectWriter
     * @param objectWriter the ObjectWriter to register, or null to unregister
     * @param fieldBased whether the ObjectWriter is field-based
     * @return the previous ObjectWriter for the type, or null if there was no previous ObjectWriter
     */
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

    /**
     * Registers an ObjectWriter for the specified type using method-based writing
     * if it is not already registered.
     *
     * @param type the type for which to register the ObjectWriter
     * @param objectWriter the ObjectWriter to register
     * @return the previous ObjectWriter for the type, or null if there was no previous ObjectWriter
     */
    public ObjectWriter registerIfAbsent(Type type, ObjectWriter objectWriter) {
        return registerIfAbsent(type, objectWriter, false);
    }

    /**
     * Registers an ObjectWriter for the specified type if it is not already registered.
     *
     * @param type the type for which to register the ObjectWriter
     * @param objectWriter the ObjectWriter to register
     * @param fieldBased whether the ObjectWriter is field-based
     * @return the previous ObjectWriter for the type, or null if there was no previous ObjectWriter
     */
    public ObjectWriter registerIfAbsent(Type type, ObjectWriter objectWriter, boolean fieldBased) {
        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.putIfAbsent(type, objectWriter);
    }

    /**
     * Unregisters the ObjectWriter for the specified type using method-based writing.
     *
     * @param type the type for which to unregister the ObjectWriter
     * @return the unregistered ObjectWriter, or null if there was no ObjectWriter for the type
     */
    public ObjectWriter unregister(Type type) {
        return unregister(type, false);
    }

    /**
     * Unregisters the ObjectWriter for the specified type.
     *
     * @param type the type for which to unregister the ObjectWriter
     * @param fieldBased whether the ObjectWriter is field-based
     * @return the unregistered ObjectWriter, or null if there was no ObjectWriter for the type
     */
    public ObjectWriter unregister(Type type, boolean fieldBased) {
        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type);
    }

    /**
     * Unregisters the specified ObjectWriter for the given type, but only if the currently
     * registered writer matches the specified writer.
     *
     * @param type the type for which to unregister the ObjectWriter
     * @param objectWriter the ObjectWriter to unregister
     * @return true if the ObjectWriter was unregistered, false otherwise
     */
    public boolean unregister(Type type, ObjectWriter objectWriter) {
        return unregister(type, objectWriter, false);
    }

    /**
     * Unregisters the specified ObjectWriter for the given type, but only if the currently
     * registered writer matches the specified writer.
     *
     * @param type the type for which to unregister the ObjectWriter
     * @param objectWriter the ObjectWriter to unregister
     * @param fieldBased whether the ObjectWriter is field-based
     * @return true if the ObjectWriter was unregistered, false otherwise
     */
    public boolean unregister(Type type, ObjectWriter objectWriter, boolean fieldBased) {
        ConcurrentMap<Type, ObjectWriter> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type, objectWriter);
    }

    /**
     * Registers an ObjectWriterModule. If the module is already registered, this method
     * does nothing and returns false.
     *
     * @param module the module to register
     * @return true if the module was registered, false if it was already registered
     */
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

    /**
     * Unregisters an ObjectWriterModule.
     *
     * @param module the module to unregister
     * @return true if the module was unregistered, false if it was not registered
     */
    public boolean unregister(ObjectWriterModule module) {
        return modules.remove(module);
    }

    /**
     * Gets the mixin source class for the specified target class.
     *
     * @param target the target class
     * @return the mixin source class, or null if no mixin is registered for the target
     */
    public Class getMixIn(Class target) {
        return mixInCache.get(target);
    }

    /**
     * Initializes this provider with the base module.
     */
    public void init() {
        modules.add(new ObjectWriterBaseModule(this));
    }

    /**
     * Gets the list of registered ObjectWriter modules.
     *
     * @return the list of modules
     */
    public List<ObjectWriterModule> getModules() {
        return modules;
    }

    /**
     * Gets field information for the specified field of a class.
     *
     * @param beanInfo the BeanInfo object to populate with bean information
     * @param fieldInfo the FieldInfo object to populate with field information
     * @param objectClass the class containing the field
     * @param field the field for which to get information
     */
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

    /**
     * Gets field information for the specified method of a class.
     *
     * @param beanInfo the BeanInfo object to populate with bean information
     * @param fieldInfo the FieldInfo object to populate with field information
     * @param objectClass the class containing the method
     * @param method the method for which to get information
     */
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

    /**
     * Gets bean information for the specified class by delegating to registered modules.
     *
     * @param beanInfo the BeanInfo object to populate with bean information
     * @param objectClass the class for which to get bean information
     */
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

    /**
     * Gets an ObjectWriter for the specified type with formatting.
     *
     * @param objectType the type for which to get an ObjectWriter
     * @param format the format string to use
     * @param locale the locale to use
     * @return the ObjectWriter for the specified type
     */
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

    /**
     * Gets an ObjectWriter for the specified class.
     *
     * @param objectClass the class for which to get an ObjectWriter
     * @return the ObjectWriter for the specified class
     */
    public ObjectWriter getObjectWriter(Class objectClass) {
        return getObjectWriter(objectClass, objectClass, false);
    }

    /**
     * Gets an ObjectWriter for the specified type and class.
     *
     * @param objectType the type for which to get an ObjectWriter
     * @param objectClass the class for which to get an ObjectWriter
     * @return the ObjectWriter for the specified type and class
     */
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        return getObjectWriter(objectType, objectClass, false);
    }

    /**
     * Gets an ObjectWriter for the specified type.
     *
     * @param objectType the type for which to get an ObjectWriter
     * @return the ObjectWriter for the specified type
     */
    public ObjectWriter getObjectWriter(Type objectType) {
        Class objectClass = TypeUtils.getClass(objectType);
        return getObjectWriter(objectType, objectClass, false);
    }

    /**
     * Gets an ObjectWriter from the cache for the specified type and class.
     *
     * @param objectType the type for which to get an ObjectWriter
     * @param objectClass the class for which to get an ObjectWriter
     * @param fieldBased whether to use field-based writing
     * @return the ObjectWriter from the cache, or null if not found
     */
    public ObjectWriter getObjectWriterFromCache(Type objectType, Class objectClass, boolean fieldBased) {
        return fieldBased
                ? cacheFieldBased.get(objectType)
                : cache.get(objectType);
    }

    /**
     * Gets an ObjectWriter for the specified type, class, and format with field-based option.
     *
     * @param objectType the type for which to get an ObjectWriter
     * @param objectClass the class for which to get an ObjectWriter
     * @param format the format string to use
     * @param fieldBased whether to use field-based writing
     * @return the ObjectWriter for the specified type, class, and format
     */
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass, String format, boolean fieldBased) {
        ObjectWriter objectWriter = getObjectWriter(objectType, objectClass, fieldBased);
        if (format != null) {
            if (objectType == LocalDateTime.class && objectWriter == ObjectWriterImplLocalDateTime.INSTANCE) {
                return ObjectWriterImplLocalDateTime.of(format, null);
            }
        }
        return objectWriter;
    }

    /**
     * Gets an ObjectWriter for the specified type, class, and field-based option.
     * If an ObjectWriter for the type is already cached, it will be returned directly.
     * Otherwise, a new ObjectWriter will be created and cached.
     *
     * @param objectType the type for which to get an ObjectWriter
     * @param objectClass the class for which to get an ObjectWriter
     * @param fieldBased whether to use field-based writing
     * @return the ObjectWriter for the specified type and class
     */
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

    /**
     * Checks if the specified class is a primitive type or an enum.
     *
     * @param clazz the class to check
     * @return true if the class is a primitive type or an enum, false otherwise
     */
    public static boolean isPrimitiveOrEnum(final Class<?> clazz) {
        return Arrays.binarySearch(PRIMITIVE_HASH_CODES, System.identityHashCode(clazz)) >= 0
                || ((clazz.getModifiers() & ENUM) != 0 && clazz.getSuperclass() == Enum.class);
    }

    /**
     * Checks if the specified class is a type that should not have reference detection.
     *
     * @param clazz the class to check
     * @return true if the class should not have reference detection, false otherwise
     */
    public static boolean isNotReferenceDetect(final Class<?> clazz) {
        return Arrays.binarySearch(NOT_REFERENCES_TYPE_HASH_CODES, System.identityHashCode(clazz)) >= 0
                || ((clazz.getModifiers() & ENUM) != 0 && clazz.getSuperclass() == Enum.class);
    }

    /**
     * Clears all cached ObjectWriters and mixin mappings.
     *
     * @since 2.0.53
     */
    public void clear() {
        mixInCache.clear();
        cache.clear();
        cacheFieldBased.clear();
    }

    /**
     * Cleans up cached ObjectWriters and mixin mappings associated with the specified class.
     *
     * @param objectClass the class for which to clean up cached ObjectWriters
     */
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

    /**
     * Cleans up cached ObjectWriters associated with the specified ClassLoader.
     * This method removes all cached writers that are related to classes loaded
     * by the given ClassLoader.
     *
     * @param classLoader the ClassLoader for which to clean up cached ObjectWriters
     */
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

    /**
     * Checks if reference detection is disabled.
     *
     * @return true if reference detection is disabled, false otherwise
     */
    public boolean isDisableReferenceDetect() {
        return disableReferenceDetect;
    }

    /**
     * Checks if auto-type support is disabled.
     *
     * @return true if auto-type support is disabled, false otherwise
     */
    public boolean isDisableAutoType() {
        return disableAutoType;
    }

    /**
     * Checks if JSONB support is disabled.
     *
     * @return true if JSONB support is disabled, false otherwise
     */
    public boolean isDisableJSONB() {
        return disableJSONB;
    }

    /**
     * Checks if array mapping is disabled.
     *
     * @return true if array mapping is disabled, false otherwise
     */
    public boolean isDisableArrayMapping() {
        return disableArrayMapping;
    }

    /**
     * Sets whether reference detection is disabled.
     *
     * @param disableReferenceDetect true to disable reference detection, false to enable it
     */
    public void setDisableReferenceDetect(boolean disableReferenceDetect) {
        this.disableReferenceDetect = disableReferenceDetect;
    }

    /**
     * Sets whether array mapping is disabled.
     *
     * @param disableArrayMapping true to disable array mapping, false to enable it
     */
    public void setDisableArrayMapping(boolean disableArrayMapping) {
        this.disableArrayMapping = disableArrayMapping;
    }

    /**
     * Sets whether JSONB support is disabled.
     *
     * @param disableJSONB true to disable JSONB support, false to enable it
     */
    public void setDisableJSONB(boolean disableJSONB) {
        this.disableJSONB = disableJSONB;
    }

    /**
     * Sets whether auto-type support is disabled.
     *
     * @param disableAutoType true to disable auto-type support, false to enable it
     */
    public void setDisableAutoType(boolean disableAutoType) {
        this.disableAutoType = disableAutoType;
    }

    /**
     * Checks if alphabetic ordering is enabled.
     *
     * @return true if alphabetic ordering is enabled, false otherwise
     */
    public boolean isAlphabetic() {
        return alphabetic;
    }

    /**
     * Sets whether alphabetic ordering is enabled.
     *
     * @param alphabetic true to enable alphabetic ordering, false to disable it
     */
    public void setAlphabetic(boolean alphabetic) {
        this.alphabetic = alphabetic;
    }

    /**
     * Checks if transient fields should be skipped.
     *
     * @return true if transient fields should be skipped, false otherwise
     */
    public boolean isSkipTransient() {
        return skipTransient;
    }

    /**
     * Sets whether transient fields should be skipped.
     *
     * @param skipTransient true to skip transient fields, false to include them
     */
    public void setSkipTransient(boolean skipTransient) {
        this.skipTransient = skipTransient;
    }

    /**
     * Creates a new BeanInfo instance.
     *
     * @return a new BeanInfo instance
     */
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
