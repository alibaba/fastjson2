package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.JSONReader.AutoTypeBeforeHandler;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.FieldBiConsumer;
import com.alibaba.fastjson2.function.FieldConsumer;
import com.alibaba.fastjson2.modules.ObjectCodecProvider;
import com.alibaba.fastjson2.modules.ObjectReaderAnnotationProcessor;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.support.LambdaMiscCodec;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static com.alibaba.fastjson2.util.TypeUtils.loadClass;

/**
 * ObjectReaderProvider is responsible for providing and managing ObjectReader instances
 * for deserializing JSON data into Java objects. It handles object creation, caching,
 * type conversion, and auto-type support.
 *
 * <p>This provider supports various features including:
 * <ul>
 *   <li>Object reader caching for performance optimization</li>
 *   <li>Auto-type support with security controls</li>
 *   <li>Type conversion between different Java types</li>
 *   <li>Mixin support for modifying serialization behavior</li>
 *   <li>Module-based extensibility</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Get default provider
 * ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
 *
 * // Get object reader for a specific type
 * ObjectReader&lt;User&gt; reader = provider.getObjectReader(User.class);
 *
 * // Parse JSON string using the reader
 * User user = reader.readObject(JSONReader.of(jsonString));
 *
 * // Register custom type converter
 * provider.registerTypeConvert(String.class, Integer.class, Integer::valueOf);
 * </pre>
 *
 * @since 2.0.0
 */
public class ObjectReaderProvider
        implements ObjectCodecProvider {
    static final ClassLoader FASTJSON2_CLASS_LOADER = JSON.class.getClassLoader();
    public static final boolean SAFE_MODE;
    static final String[] DENYS;
    static final String[] AUTO_TYPE_ACCEPT_LIST;

    static AutoTypeBeforeHandler DEFAULT_AUTO_TYPE_BEFORE_HANDLER;
    static Consumer<Class> DEFAULT_AUTO_TYPE_HANDLER;
    static boolean DEFAULT_AUTO_TYPE_HANDLER_INIT_ERROR;

    static ObjectReaderCachePair readerCache;

    private static final class ObjectReaderCachePair {
        final long hashCode;
        final ObjectReader reader;
        volatile int missCount;

        public ObjectReaderCachePair(long hashCode, ObjectReader reader) {
            this.hashCode = hashCode;
            this.reader = reader;
        }
    }

    static {
        {
            String property = System.getProperty(PROPERTY_DENY_PROPERTY);
            if (property == null) {
                property = JSONFactory.Conf.getProperty(PROPERTY_DENY_PROPERTY);
            }
            if (property != null && property.length() > 0) {
                DENYS = property.split(",");
            } else {
                DENYS = new String[0];
            }
        }

        {
            String property = System.getProperty(PROPERTY_AUTO_TYPE_ACCEPT);
            if (property == null) {
                property = JSONFactory.Conf.getProperty(PROPERTY_AUTO_TYPE_ACCEPT);
            }
            if (property != null && property.length() > 0) {
                AUTO_TYPE_ACCEPT_LIST = property.split(",");
            } else {
                AUTO_TYPE_ACCEPT_LIST = new String[0];
            }
        }

        {
            String property = System.getProperty(PROPERTY_AUTO_TYPE_BEFORE_HANDLER);
            if (property == null || property.isEmpty()) {
                property = JSONFactory.Conf.getProperty(PROPERTY_AUTO_TYPE_BEFORE_HANDLER);
            }

            if (property != null) {
                property = property.trim();
            }

            if (property != null && !property.isEmpty()) {
                Class handlerClass = TypeUtils.loadClass(property);
                if (handlerClass != null) {
                    try {
                        DEFAULT_AUTO_TYPE_BEFORE_HANDLER = (AutoTypeBeforeHandler) handlerClass.newInstance();
                    } catch (Exception ignored) {
                        DEFAULT_AUTO_TYPE_HANDLER_INIT_ERROR = true;
                        // skip
                    }
                }
            }
        }

        {
            String property = System.getProperty(PROPERTY_AUTO_TYPE_HANDLER);
            if (property == null || property.isEmpty()) {
                property = JSONFactory.Conf.getProperty(PROPERTY_AUTO_TYPE_HANDLER);
            }

            if (property != null) {
                property = property.trim();
            }

            if (property != null && !property.isEmpty()) {
                Class handlerClass = TypeUtils.loadClass(property);
                if (handlerClass != null) {
                    try {
                        DEFAULT_AUTO_TYPE_HANDLER = (Consumer<Class>) handlerClass.newInstance();
                    } catch (Exception ignored) {
                        DEFAULT_AUTO_TYPE_HANDLER_INIT_ERROR = true;
                        // skip
                    }
                }
            }
        }

        {
            String property = System.getProperty("fastjson.parser.safeMode");
            if (property == null || property.isEmpty()) {
                property = JSONFactory.Conf.getProperty("fastjson.parser.safeMode");
            }

            if (property == null || property.isEmpty()) {
                property = System.getProperty("fastjson2.parser.safeMode");
            }
            if (property == null || property.isEmpty()) {
                property = JSONFactory.Conf.getProperty("fastjson2.parser.safeMode");
            }

            if (property != null) {
                property = property.trim();
            }

            SAFE_MODE = "true".equals(property);
        }
    }

    final ConcurrentMap<Type, ObjectReader> cache = new ConcurrentHashMap<>();
    final ConcurrentMap<Type, ObjectReader> cacheFieldBased = new ConcurrentHashMap<>();
    final ConcurrentMap<Integer, ConcurrentHashMap<Long, ObjectReader>> tclHashCaches = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, ObjectReader> hashCache = new ConcurrentHashMap<>();
    final ConcurrentMap<Class, Class> mixInCache = new ConcurrentHashMap<>();

    final LRUAutoTypeCache autoTypeList = new LRUAutoTypeCache(1024);

    private final ConcurrentMap<Type, Map<Type, Function>> typeConverts = new ConcurrentHashMap<>();

    final ObjectReaderCreator creator;
    final List<ObjectReaderModule> modules = new CopyOnWriteArrayList<>();

    boolean disableReferenceDetect = JSONFactory.isDisableReferenceDetect();
    boolean disableArrayMapping = JSONFactory.isDisableArrayMapping();
    boolean disableJSONB = JSONFactory.isDisableJSONB();
    boolean disableAutoType = JSONFactory.isDisableAutoType();
    boolean disableSmartMatch = JSONFactory.isDisableSmartMatch();

    private long[] acceptHashCodes;

    private AutoTypeBeforeHandler autoTypeBeforeHandler = DEFAULT_AUTO_TYPE_BEFORE_HANDLER;
    private Consumer<Class> autoTypeHandler = DEFAULT_AUTO_TYPE_HANDLER;
    PropertyNamingStrategy namingStrategy;

    {
        long[] hashCodes;
        if (AUTO_TYPE_ACCEPT_LIST == null) {
            hashCodes = new long[1];
        } else {
            hashCodes = new long[AUTO_TYPE_ACCEPT_LIST.length + 1];
            for (int i = 0; i < AUTO_TYPE_ACCEPT_LIST.length; i++) {
                hashCodes[i] = Fnv.hashCode64(AUTO_TYPE_ACCEPT_LIST[i]);
            }
        }

        hashCodes[hashCodes.length - 1] = -6293031534589903644L;

        Arrays.sort(hashCodes);
        acceptHashCodes = hashCodes;

        hashCache.put(ObjectArrayReader.TYPE_HASH_CODE, ObjectArrayReader.INSTANCE);
        final long STRING_CLASS_NAME_HASH = -4834614249632438472L; // Fnv.hashCode64(String.class.getName());
        hashCache.put(STRING_CLASS_NAME_HASH, ObjectReaderImplString.INSTANCE);
        hashCache.put(Fnv.hashCode64(TypeUtils.getTypeName(HashMap.class)), ObjectReaderImplMap.INSTANCE);
    }

    /**
     * Registers an ObjectReader for the specified hash code if it is not already registered.
     * This method handles both thread-local and global caching.
     *
     * @param hashCode the hash code for which to register the ObjectReader
     * @param objectReader the ObjectReader to register
     */
    public void registerIfAbsent(long hashCode, ObjectReader objectReader) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl != null && tcl != JSON.class.getClassLoader()) {
            int tclHash = System.identityHashCode(tcl);
            ConcurrentHashMap<Long, ObjectReader> tclHashCache = tclHashCaches.get(tclHash);
            if (tclHashCache == null) {
                tclHashCaches.putIfAbsent(tclHash, new ConcurrentHashMap<>());
                tclHashCache = tclHashCaches.get(tclHash);
            }

            tclHashCache.putIfAbsent(hashCode, objectReader);
        }

        hashCache.putIfAbsent(hashCode, objectReader);
    }

    /**
     * Adds a type name to the auto-type accept list. Types in this list are allowed
     * for auto-type deserialization.
     *
     * @param name the type name to add to the accept list
     */
    public void addAutoTypeAccept(String name) {
        if (name != null && name.length() != 0) {
            long hash = Fnv.hashCode64(name);
            if (Arrays.binarySearch(this.acceptHashCodes, hash) < 0) {
                long[] hashCodes = new long[this.acceptHashCodes.length + 1];
                hashCodes[hashCodes.length - 1] = hash;
                System.arraycopy(this.acceptHashCodes, 0, hashCodes, 0, this.acceptHashCodes.length);
                Arrays.sort(hashCodes);
                this.acceptHashCodes = hashCodes;
            }
        }
    }

    @Deprecated
    public void addAutoTypeDeny(String name) {
    }

    /**
     * Gets the auto-type handler that is invoked when a type is auto-resolved.
     *
     * @return the auto-type handler, or null if none is set
     */
    public Consumer<Class> getAutoTypeHandler() {
        return autoTypeHandler;
    }

    /**
     * Sets the auto-type handler that will be invoked when a type is auto-resolved.
     *
     * @param autoTypeHandler the auto-type handler to set
     */
    public void setAutoTypeHandler(Consumer<Class> autoTypeHandler) {
        this.autoTypeHandler = autoTypeHandler;
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
     * Clears all mixin mappings.
     */
    public void cleanupMixIn() {
        mixInCache.clear();
    }

    /**
     * Registers a mixin mapping between a target class and a mixin source class.
     * Mixin allows modifying the serialization/deserialization behavior of a class
     * by applying annotations from another class.
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
        cacheFieldBased.remove(target);
    }

    /**
     * Registers a subtype class for see-also support. This allows the provider to
     * recognize and handle subtypes of the specified superclass.
     *
     * @param subTypeClass the subtype class to register
     */
    public void registerSeeAlsoSubType(Class subTypeClass) {
        registerSeeAlsoSubType(subTypeClass, null);
    }

    /**
     * Registers a subtype class with a specific name for see-also support.
     *
     * @param subTypeClass the subtype class to register
     * @param subTypeClassName the name of the subtype class, or null to use the class's simple name
     * @throws JSONException if the superclass is null
     */
    public void registerSeeAlsoSubType(Class subTypeClass, String subTypeClassName) {
        Class superClass = subTypeClass.getSuperclass();
        if (superClass == null) {
            throw new JSONException("superclass is null");
        }

        ObjectReader objectReader = getObjectReader(superClass);
        if (objectReader instanceof ObjectReaderSeeAlso) {
            ObjectReaderSeeAlso readerSeeAlso = (ObjectReaderSeeAlso) objectReader;
            ObjectReaderSeeAlso readerSeeAlsoNew = readerSeeAlso.addSubType(subTypeClass, subTypeClassName);
            if (readerSeeAlsoNew != readerSeeAlso) {
                if (cache.containsKey(superClass)) {
                    cache.put(superClass, readerSeeAlsoNew);
                } else {
                    cacheFieldBased.put(subTypeClass, readerSeeAlsoNew);
                }
            }
        }
    }

    /**
     * Registers an ObjectReader for the specified type. If an ObjectReader is already
     * registered for the type, it will be replaced.
     *
     * @param type the type for which to register the ObjectReader
     * @param objectReader the ObjectReader to register, or null to unregister
     * @param fieldBased whether the ObjectReader is field-based
     * @return the previous ObjectReader for the type, or null if there was no previous ObjectReader
     */
    public ObjectReader register(Type type, ObjectReader objectReader, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        if (objectReader == null) {
            return cache.remove(type);
        }

        return cache.put(type, objectReader);
    }

    /**
     * Registers an ObjectReader for the specified type using method-based reading.
     * If an ObjectReader is already registered for the type, it will be replaced.
     *
     * @param type the type for which to register the ObjectReader
     * @param objectReader the ObjectReader to register, or null to unregister
     * @return the previous ObjectReader for the type, or null if there was no previous ObjectReader
     */
    public ObjectReader register(Type type, ObjectReader objectReader) {
        return register(type, objectReader, false);
    }

    /**
     * Registers an ObjectReader for the specified type using method-based reading
     * if it is not already registered.
     *
     * @param type the type for which to register the ObjectReader
     * @param objectReader the ObjectReader to register
     * @return the previous ObjectReader for the type, or null if there was no previous ObjectReader
     */
    public ObjectReader registerIfAbsent(Type type, ObjectReader objectReader) {
        return registerIfAbsent(type, objectReader, false);
    }

    /**
     * Registers an ObjectReader for the specified type if it is not already registered.
     *
     * @param type the type for which to register the ObjectReader
     * @param objectReader the ObjectReader to register
     * @param fieldBased whether the ObjectReader is field-based
     * @return the previous ObjectReader for the type, or null if there was no previous ObjectReader
     */
    public ObjectReader registerIfAbsent(Type type, ObjectReader objectReader, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.putIfAbsent(type, objectReader);
    }

    /**
     * Unregisters the ObjectReader for the specified type using method-based reading.
     *
     * @param type the type for which to unregister the ObjectReader
     * @return the unregistered ObjectReader, or null if there was no ObjectReader for the type
     */
    public ObjectReader unregisterObjectReader(Type type) {
        return unregisterObjectReader(type, false);
    }

    /**
     * Unregisters the ObjectReader for the specified type.
     *
     * @param type the type for which to unregister the ObjectReader
     * @param fieldBased whether the ObjectReader is field-based
     * @return the unregistered ObjectReader, or null if there was no ObjectReader for the type
     */
    public ObjectReader unregisterObjectReader(Type type, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type);
    }

    /**
     * Unregisters the specified ObjectReader for the given type using method-based reading,
     * but only if the currently registered reader matches the specified reader.
     *
     * @param type the type for which to unregister the ObjectReader
     * @param reader the ObjectReader to unregister
     * @return true if the ObjectReader was unregistered, false otherwise
     */
    public boolean unregisterObjectReader(Type type, ObjectReader reader) {
        return unregisterObjectReader(type, reader, false);
    }

    /**
     * Unregisters the specified ObjectReader for the given type, but only if the currently
     * registered reader matches the specified reader.
     *
     * @param type the type for which to unregister the ObjectReader
     * @param reader the ObjectReader to unregister
     * @param fieldBased whether the ObjectReader is field-based
     * @return true if the ObjectReader was unregistered, false otherwise
     */
    public boolean unregisterObjectReader(Type type, ObjectReader reader, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type, reader);
    }

    /**
     * Registers an ObjectReaderModule. If the module is already registered, this method
     * does nothing and returns false.
     *
     * @param module the module to register
     * @return true if the module was registered, false if it was already registered
     */
    public boolean register(ObjectReaderModule module) {
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
     * Unregisters an ObjectReaderModule.
     *
     * @param module the module to unregister
     * @return true if the module was unregistered, false if it was not registered
     */
    public boolean unregister(ObjectReaderModule module) {
        return modules.remove(module);
    }

    /**
     * Cleans up cached ObjectReaders and mixin mappings associated with the specified class.
     *
     * @param objectClass the class for which to clean up cached ObjectReaders
     */
    public void cleanup(Class objectClass) {
        mixInCache.remove(objectClass);
        cache.remove(objectClass);
        cacheFieldBased.remove(objectClass);
        for (ConcurrentHashMap<Long, ObjectReader> tlc : tclHashCaches.values()) {
            for (Iterator<Map.Entry<Long, ObjectReader>> it = tlc.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Long, ObjectReader> entry = it.next();
                ObjectReader reader = entry.getValue();
                if (reader.getObjectClass() == objectClass) {
                    it.remove();
                }
            }
        }
        BeanUtils.cleanupCache(objectClass);
    }

    /**
     * Clears all cached ObjectReaders and mixin mappings.
     *
     * @since 2.0.53
     */
    public void clear() {
        mixInCache.clear();
        cache.clear();
        cacheFieldBased.clear();
    }

    static boolean match(Type objectType, ObjectReader objectReader, ClassLoader classLoader) {
        Class<?> objectClass = TypeUtils.getClass(objectType);
        if (objectClass != null && objectClass.getClassLoader() == classLoader) {
            return true;
        }

        if (objectType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) objectType;
            Type rawType = paramType.getRawType();
            if (match(rawType, objectReader, classLoader)) {
                return true;
            }

            for (Type argType : paramType.getActualTypeArguments()) {
                if (match(argType, objectReader, classLoader)) {
                    return true;
                }
            }
        }

        if (objectReader instanceof ObjectReaderImplMapTyped) {
            ObjectReaderImplMapTyped mapTyped = (ObjectReaderImplMapTyped) objectReader;
            Class valueClass = mapTyped.valueClass;
            if (valueClass != null && valueClass.getClassLoader() == classLoader) {
                return true;
            }
            Class keyClass = TypeUtils.getClass(mapTyped.keyType);
            return keyClass != null && keyClass.getClassLoader() == classLoader;
        } else if (objectReader instanceof ObjectReaderImplList) {
            ObjectReaderImplList list = (ObjectReaderImplList) objectReader;
            return list.itemClass != null && list.itemClass.getClassLoader() == classLoader;
        } else if (objectReader instanceof ObjectReaderImplOptional) {
            Class itemClass = ((ObjectReaderImplOptional) objectReader).itemClass;
            return itemClass != null && itemClass.getClassLoader() == classLoader;
        } else if (objectReader instanceof ObjectReaderAdapter) {
            FieldReader[] fieldReaders = ((ObjectReaderAdapter<?>) objectReader).fieldReaders;
            for (FieldReader fieldReader : fieldReaders) {
                if (fieldReader.fieldClass != null && fieldReader.fieldClass.getClassLoader() == classLoader) {
                    return true;
                }
                Type fieldType = fieldReader.fieldType;
                if (fieldType instanceof ParameterizedType) {
                    if (match(fieldType, null, classLoader)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Cleans up cached ObjectReaders associated with the specified ClassLoader.
     * This method removes all cached readers that are related to classes loaded
     * by the given ClassLoader.
     *
     * @param classLoader the ClassLoader for which to clean up cached ObjectReaders
     */
    public void cleanup(ClassLoader classLoader) {
        mixInCache.entrySet().removeIf(
                entry -> entry.getKey().getClassLoader() == classLoader
        );

        cache.entrySet().removeIf(
                entry -> match(entry.getKey(), entry.getValue(), classLoader)
        );

        cacheFieldBased.entrySet().removeIf(
                entry -> match(entry.getKey(), entry.getValue(), classLoader)
        );

        int tclHash = System.identityHashCode(classLoader);
        tclHashCaches.remove(tclHash);

        BeanUtils.cleanupCache(classLoader);
    }

    /**
     * Gets the ObjectReaderCreator used by this provider. If a context-specific creator
     * is available, it will be returned; otherwise, the default creator for this provider
     * will be returned.
     *
     * @return the ObjectReaderCreator
     */
    public ObjectReaderCreator getCreator() {
        ObjectReaderCreator contextCreator = JSONFactory.getContextReaderCreator();
        if (contextCreator != null) {
            return contextCreator;
        }
        return this.creator;
    }

    /**
     * Constructs an ObjectReaderProvider with the default ObjectReaderCreator based on
     * system configuration. The creator selection follows this priority:
     * 1. ASM creator (default, if not Android or GraalVM)
     * 2. Reflection/Lambda creator (fallback)
     *
     * <p>The provider is initialized with the base module and all registered modules.
     */
    public ObjectReaderProvider() {
        ObjectReaderCreator creator = null;
        switch (JSONFactory.CREATOR) {
            case "reflect":
            case "lambda":
                creator = ObjectReaderCreator.INSTANCE;
                break;
            case "asm":
            default:
                try {
                    if (!JDKUtils.ANDROID && !JDKUtils.GRAAL) {
                        creator = ObjectReaderCreatorASM.INSTANCE;
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
                if (creator == null) {
                    creator = ObjectReaderCreator.INSTANCE;
                }
                break;
        }
        this.creator = creator;

        modules.add(new ObjectReaderBaseModule(this));
        init();
    }

    /**
     * Constructs an ObjectReaderProvider with the specified ObjectReaderCreator.
     *
     * @param creator the ObjectReaderCreator to use for creating ObjectReader instances
     */
    public ObjectReaderProvider(ObjectReaderCreator creator) {
        this.creator = creator;
        modules.add(new ObjectReaderBaseModule(this));
        init();
    }

    void init() {
        for (ObjectReaderModule module : modules) {
            module.init(this);
        }
    }

    /**
     * Gets the type converter function that can convert values from one type to another.
     *
     * @param from the source type
     * @param to the target type
     * @return the converter function for the type pair, or null if no converter is registered
     */
    public Function getTypeConvert(Type from, Type to) {
        Map<Type, Function> map = typeConverts.get(from);
        if (map == null) {
            return null;
        }
        return map.get(to);
    }

    /**
     * Registers a type converter function that can convert values from one type to another.
     *
     * @param from the source type
     * @param to the target type
     * @param typeConvert the function to convert from source type to target type
     * @return the previous converter function for the type pair, or null if there was no previous converter
     */
    public Function registerTypeConvert(Type from, Type to, Function typeConvert) {
        Map<Type, Function> map = typeConverts.get(from);
        if (map == null) {
            typeConverts.putIfAbsent(from, new ConcurrentHashMap<>());
            map = typeConverts.get(from);
        }
        return map.put(to, typeConvert);
    }

    /**
     * Gets an ObjectReader by its hash code. This method first checks thread-local cache,
     * then global cache for performance optimization.
     *
     * @param hashCode the hash code of the ObjectReader to retrieve
     * @return the ObjectReader associated with the hash code, or null if not found
     */
    public ObjectReader getObjectReader(long hashCode) {
        ObjectReaderCachePair pair = readerCache;
        if (pair != null) {
            if (pair.hashCode == hashCode) {
                return pair.reader;
            } else {
                if (pair.missCount++ > 16) {
                    readerCache = null;
                }
            }
        }

        Long hashCodeObj = hashCode;
        ObjectReader objectReader = null;
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl != null && tcl != FASTJSON2_CLASS_LOADER) {
            int tclHash = System.identityHashCode(tcl);
            ConcurrentHashMap<Long, ObjectReader> tclHashCache = tclHashCaches.get(tclHash);
            if (tclHashCache != null) {
                objectReader = tclHashCache.get(hashCodeObj);
            }
        }

        if (objectReader == null) {
            objectReader = hashCache.get(hashCodeObj);
        }

        if (objectReader != null && readerCache == null) {
            readerCache = new ObjectReaderCachePair(hashCode, objectReader);
        }

        return objectReader;
    }

    /**
     * Gets an ObjectReader for the specified type name, expected class, and features.
     * This method handles auto-type resolution and ObjectReader caching.
     *
     * @param typeName the name of the type
     * @param expectClass the expected class type
     * @param features the JSON reader features
     * @return the ObjectReader for the specified type, or null if the type cannot be resolved
     */
    public ObjectReader getObjectReader(String typeName, Class<?> expectClass, long features) {
        Class<?> autoTypeClass = checkAutoType(typeName, expectClass, features);
        if (autoTypeClass == null) {
            return null;
        }
        boolean fieldBased = (features & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = getObjectReader(autoTypeClass, fieldBased);

        if (autoTypeClass != expectClass) {
            registerIfAbsent(Fnv.hashCode64(typeName), objectReader);
        }
        return objectReader;
    }

    final void afterAutoType(String typeName, Class type) {
        if (autoTypeHandler != null) {
            autoTypeHandler.accept(type);
        }

        synchronized (autoTypeList) {
            autoTypeList.putIfAbsent(typeName, new Date());
        }
    }

    /**
     * Checks and resolves the class for auto-type support. This method handles security
     * validation and class loading for auto-type deserialization.
     *
     * @param typeName the name of the type to check
     * @param expectClass the expected class type
     * @param features the JSON reader features
     * @return the resolved Class, or null if the type cannot be resolved or is not allowed
     * @throws JSONException if the type is not supported or security checks fail
     */
    public Class<?> checkAutoType(String typeName, Class<?> expectClass, long features) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }

        if (autoTypeBeforeHandler != null) {
            Class<?> resolvedClass = autoTypeBeforeHandler.apply(typeName, expectClass, features);
            if (resolvedClass != null) {
                afterAutoType(typeName, resolvedClass);
                return resolvedClass;
            }
        }

        if (SAFE_MODE) {
            return null;
        }

        int typeNameLength = typeName.length();
        if (typeNameLength >= 192) {
            throw new JSONException("autoType is not support. " + typeName);
        }

        if (typeName.charAt(0) == '[') {
            String componentTypeName = typeName.substring(1);
            checkAutoType(componentTypeName, null, features); // blacklist check for componentType
        }

        if (expectClass != null && expectClass.getName().equals(typeName)) {
            afterAutoType(typeName, expectClass);
            return expectClass;
        }

        boolean autoTypeSupport = (features & JSONReader.Feature.SupportAutoType.mask) != 0;
        Class<?> clazz;

        if (autoTypeSupport) {
            long hash = MAGIC_HASH_CODE;
            for (int i = 0; i < typeNameLength; ++i) {
                char ch = typeName.charAt(i);
                if (ch == '$') {
                    ch = '.';
                }
                hash ^= ch;
                hash *= MAGIC_PRIME;
                if (Arrays.binarySearch(acceptHashCodes, hash) >= 0) {
                    clazz = loadClass(typeName);
                    if (clazz != null) {
                        if (expectClass != null && !expectClass.isAssignableFrom(clazz)) {
                            throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
                        }

                        afterAutoType(typeName, clazz);
                        return clazz;
                    }
                }
            }
        }

        if (!autoTypeSupport) {
            long hash = MAGIC_HASH_CODE;
            for (int i = 0; i < typeNameLength; ++i) {
                char ch = typeName.charAt(i);
                if (ch == '$') {
                    ch = '.';
                }
                hash ^= ch;
                hash *= MAGIC_PRIME;

                // white list
                if (Arrays.binarySearch(acceptHashCodes, hash) >= 0) {
                    clazz = loadClass(typeName);

                    if (clazz != null && expectClass != null && !expectClass.isAssignableFrom(clazz)) {
                        throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
                    }

                    afterAutoType(typeName, clazz);
                    return clazz;
                }
            }
        }

        if (!autoTypeSupport) {
            return null;
        }

        clazz = TypeUtils.getMapping(typeName);

        if (clazz != null) {
            if (expectClass != null
                    && expectClass != Object.class
                    && clazz != java.util.HashMap.class
                    && !expectClass.isAssignableFrom(clazz)
            ) {
                throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
            }

            afterAutoType(typeName, clazz);
            return clazz;
        }

        clazz = loadClass(typeName);

        if (clazz != null) {
            if (ClassLoader.class.isAssignableFrom(clazz) || JDKUtils.isSQLDataSourceOrRowSet(clazz)) {
                throw new JSONException("autoType is not support. " + typeName);
            }

            if (expectClass != null) {
                if (expectClass.isAssignableFrom(clazz)) {
                    afterAutoType(typeName, clazz);
                    return clazz;
                } else {
                    if ((features & JSONReader.Feature.IgnoreAutoTypeNotMatch.mask) != 0) {
                        return expectClass;
                    }

                    throw new JSONException("type not match. " + typeName + " -> " + expectClass.getName());
                }
            }
        }

        afterAutoType(typeName, clazz);
        return clazz;
    }

    /**
     * Gets the list of registered ObjectReader modules.
     *
     * @return the list of modules
     */
    public List<ObjectReaderModule> getModules() {
        return modules;
    }

    /**
     * Gets bean information for the specified class by delegating to registered modules.
     *
     * @param beanInfo the BeanInfo object to populate with bean information
     * @param objectClass the class for which to get bean information
     */
    public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
        for (int i = 0; i < modules.size(); i++) {
            ObjectReaderModule module = modules.get(i);
            module.getBeanInfo(beanInfo, objectClass);
        }
    }

    /**
     * Gets field information for the specified field of a class.
     *
     * @param fieldInfo the FieldInfo object to populate with field information
     * @param objectClass the class containing the field
     * @param field the field for which to get information
     */
    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
        for (int i = 0; i < modules.size(); i++) {
            ObjectReaderModule module = modules.get(i);
            module.getFieldInfo(fieldInfo, objectClass, field);
        }
    }

    /**
     * Gets field information for the specified constructor parameter.
     *
     * @param fieldInfo the FieldInfo object to populate with field information
     * @param objectClass the class containing the constructor
     * @param constructor the constructor containing the parameter
     * @param paramIndex the index of the parameter in the constructor
     * @param parameter the parameter for which to get information
     */
    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Constructor constructor,
            int paramIndex,
            Parameter parameter
    ) {
        for (int i = 0; i < modules.size(); i++) {
            ObjectReaderAnnotationProcessor annotationProcessor = modules.get(i).getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getFieldInfo(fieldInfo, objectClass, constructor, paramIndex, parameter);
            }
        }
    }

    /**
     * Gets field information for the specified method parameter.
     *
     * @param fieldInfo the FieldInfo object to populate with field information
     * @param objectClass the class containing the method
     * @param method the method containing the parameter
     * @param paramIndex the index of the parameter in the method
     * @param parameter the parameter for which to get information
     */
    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Method method,
            int paramIndex,
            Parameter parameter) {
        for (int i = 0; i < modules.size(); i++) {
            ObjectReaderAnnotationProcessor annotationProcessor = modules.get(i).getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getFieldInfo(fieldInfo, objectClass, method, paramIndex, parameter);
            }
        }
    }

    /**
     * Gets an ObjectReader for the specified type. If an ObjectReader for the type
     * is already cached, it will be returned directly. Otherwise, a new ObjectReader
     * will be created and cached.
     *
     * @param objectType the type for which to get an ObjectReader
     * @return the ObjectReader for the specified type
     */
    public ObjectReader getObjectReader(Type objectType) {
        return getObjectReader(objectType, false);
    }

    /**
     * Creates a value consumer creator for byte array values.
     *
     * @param objectClass the class for which to create the value consumer creator
     * @param fieldReaderArray the field readers to use
     * @return a function that creates ByteArrayValueConsumer instances
     */
    public Function<Consumer, ByteArrayValueConsumer> createValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return creator.createByteArrayValueConsumerCreator(objectClass, fieldReaderArray);
    }

    /**
     * Creates a value consumer creator for char array values.
     *
     * @param objectClass the class for which to create the value consumer creator
     * @param fieldReaderArray the field readers to use
     * @return a function that creates CharArrayValueConsumer instances
     */
    public Function<Consumer, CharArrayValueConsumer> createCharArrayValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return creator.createCharArrayValueConsumerCreator(objectClass, fieldReaderArray);
    }

    /**
     * Gets an ObjectReader for the specified type with field-based option.
     * If an ObjectReader for the type is already cached, it will be returned directly.
     * Otherwise, a new ObjectReader will be created and cached.
     *
     * @param objectType the type for which to get an ObjectReader
     * @param fieldBased whether to use field-based reading (true) or method-based reading (false)
     * @return the ObjectReader for the specified type
     */
    public ObjectReader getObjectReader(Type objectType, boolean fieldBased) {
        if (objectType == null) {
            objectType = Object.class;
        }

        ObjectReader objectReader = fieldBased
                ? cacheFieldBased.get(objectType)
                : cache.get(objectType);

        if (objectReader == null && objectType instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) objectType).getUpperBounds();
            if (upperBounds.length == 1) {
                Type upperBoundType = upperBounds[0];
                objectReader = fieldBased ? cacheFieldBased.get(upperBoundType) : cache.get(upperBoundType);
            }
        }

        return objectReader != null
                ? objectReader
                : getObjectReaderInternal(objectType, fieldBased);
    }

    private ObjectReader getObjectReaderInternal(Type objectType, boolean fieldBased) {
        ObjectReader objectReader = null;

        for (ObjectReaderModule module : modules) {
            objectReader = module.getObjectReader(this, objectType);
            if (objectReader != null) {
                ObjectReader previous = fieldBased
                        ? cacheFieldBased.putIfAbsent(objectType, objectReader)
                        : cache.putIfAbsent(objectType, objectReader);

                if (previous != null) {
                    objectReader = previous;
                }
                return objectReader;
            }
        }

        if (objectType instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) objectType).getBounds();
            if (bounds.length > 0) {
                Type bound = bounds[0];
                if (bound instanceof Class) {
                    ObjectReader boundObjectReader = getObjectReader(bound, fieldBased);
                    if (boundObjectReader != null) {
                        ObjectReader previous = getPreviousObjectReader(fieldBased, objectType, boundObjectReader);
                        if (previous != null) {
                            boundObjectReader = previous;
                        }
                        return boundObjectReader;
                    }
                }
            }
        }

        if (objectType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) objectType;
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (rawType instanceof Class) {
                Class rawClass = (Class) rawType;

                boolean generic = false;
                for (Class clazz = rawClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                    if (clazz.getTypeParameters().length > 0) {
                        generic = true;
                        break;
                    }
                }
                if (typeArguments.length == 0 || !generic) {
                    ObjectReader rawClassReader = getObjectReader(rawClass, fieldBased);
                    if (rawClassReader != null) {
                        ObjectReader previous = getPreviousObjectReader(fieldBased, objectType, rawClassReader);
                        if (previous != null) {
                            rawClassReader = previous;
                        }
                        return rawClassReader;
                    }
                }
                if (typeArguments.length == 1 && ArrayList.class.isAssignableFrom(rawClass)) {
                    return ObjectReaderImplList.of(objectType, rawClass, 0);
                }

                if (typeArguments.length == 2 && Map.class.isAssignableFrom(rawClass)) {
                    return ObjectReaderImplMap.of(objectType, (Class) rawType, 0);
                }
            }
        }

        Class<?> objectClass = TypeUtils.getMapping(objectType);

        String className = objectClass.getName();
        if (!fieldBased) {
            if ("com.google.common.collect.ArrayListMultimap".equals(className)) {
                objectReader = ObjectReaderImplMap.of(null, objectClass, 0);
            }
        }

        if (objectReader == null) {
            ObjectReaderCreator creator = getCreator();
            objectReader = creator.createObjectReader(objectClass, objectType, fieldBased, this);
        }

        ObjectReader previous = getPreviousObjectReader(fieldBased, objectType, objectReader);
        if (previous != null) {
            objectReader = previous;
        }

        return objectReader;
    }

    private ObjectReader getPreviousObjectReader(boolean fieldBased, Type objectType, ObjectReader boundObjectReader) {
        return fieldBased
                ? cacheFieldBased.putIfAbsent(objectType, boundObjectReader)
                : cache.putIfAbsent(objectType, boundObjectReader);
    }

    /**
     * Gets the auto-type before handler that is invoked before type resolution.
     *
     * @return the auto-type before handler, or null if none is set
     */
    public AutoTypeBeforeHandler getAutoTypeBeforeHandler() {
        return autoTypeBeforeHandler;
    }

    /**
     * Sets the auto-type before handler that will be invoked before type resolution.
     *
     * @param autoTypeBeforeHandler the auto-type before handler to set
     */
    public void setAutoTypeBeforeHandler(AutoTypeBeforeHandler autoTypeBeforeHandler) {
        this.autoTypeBeforeHandler = autoTypeBeforeHandler;
    }

    public Map<String, Date> getAutoTypeList() {
        return autoTypeList;
    }

    private static final class LRUAutoTypeCache
            extends LinkedHashMap<String, Date> {
        private final int maxSize;

        public LRUAutoTypeCache(int maxSize) {
            super(16, 0.75f, false);
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Date> eldest) {
            return this.size() > this.maxSize;
        }
    }

    /**
     * Gets field information for the specified method of a class. This method also
     * handles setter methods by attempting to find corresponding fields.
     *
     * @param fieldInfo the FieldInfo object to populate with field information
     * @param objectClass the class containing the method
     * @param method the method for which to get information
     */
    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
        for (int i = 0; i < modules.size(); i++) {
            ObjectReaderAnnotationProcessor annotationProcessor = modules.get(i).getAnnotationProcessor();
            if (annotationProcessor == null) {
                continue;
            }
            annotationProcessor.getFieldInfo(fieldInfo, objectClass, method);
        }

        if (fieldInfo.fieldName == null && fieldInfo.alternateNames == null) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                String findName = methodName.substring(3);
                Field field = BeanUtils.getDeclaredField(objectClass, findName);
                if (field != null) {
                    fieldInfo.alternateNames = new String[]{findName};
                }
            }
        }
    }

    /**
     * Creates an object creator (supplier) for the specified class and reader features.
     *
     * @param objectClass the class for which to create an object creator
     * @param readerFeatures the reader features to use
     * @param <T> the type of the object
     * @return a supplier function that creates new instances of the object
     * @throws JSONException if no default constructor is found for the class
     */
    public <T> Supplier<T> createObjectCreator(Class<T> objectClass, long readerFeatures) {
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;
        ObjectReader objectReader = fieldBased
                ? cacheFieldBased.get(objectClass)
                : cache.get(objectClass);
        if (objectReader != null) {
            return () -> (T) objectReader.createInstance(0);
        }

        Constructor constructor = BeanUtils.getDefaultConstructor(objectClass, false);
        if (constructor == null) {
            throw new JSONException("default constructor not found : " + objectClass.getName());
        }

        return LambdaMiscCodec.createSupplier(constructor);
    }

    /**
     * Creates a FieldReader for the specified class, field name, and reader features.
     *
     * @param objectClass the class containing the field
     * @param fieldName the name of the field
     * @param readerFeatures the reader features to use
     * @return a FieldReader for the specified field, or null if the field is not found
     */
    public FieldReader createFieldReader(Class objectClass, String fieldName, long readerFeatures) {
        boolean fieldBased = (readerFeatures & JSONReader.Feature.FieldBased.mask) != 0;

        ObjectReader objectReader = fieldBased
                ? cacheFieldBased.get(objectClass)
                : cache.get(objectClass);

        if (objectReader != null) {
            return objectReader.getFieldReader(fieldName);
        }

        AtomicReference<Field> fieldRef = new AtomicReference<>();
        long nameHashLCase = Fnv.hashCode64LCase(fieldName);
        BeanUtils.fields(objectClass, field -> {
            if (nameHashLCase == Fnv.hashCode64LCase(field.getName())) {
                fieldRef.set(field);
            }
        });

        Field field = fieldRef.get();
        if (field != null) {
            return creator.createFieldReader(fieldName, null, field.getType(), field);
        }

        AtomicReference<Method> methodRef = new AtomicReference<>();
        BeanUtils.setters(objectClass, method -> {
            String setterName = BeanUtils.setterName(method.getName(), PropertyNamingStrategy.CamelCase.name());
            if (nameHashLCase == Fnv.hashCode64LCase(setterName)) {
                methodRef.set(method);
            }
        });

        Method method = methodRef.get();
        if (method != null) {
            Class<?>[] params = method.getParameterTypes();
            Class fieldClass = params[0];
            return creator.createFieldReaderMethod(objectClass, fieldName, null, fieldClass, fieldClass, method);
        }

        return null;
    }

    /**
     * Creates an ObjectReader for a custom object with specified field names, types, and consumer.
     *
     * @param names the field names
     * @param types the field types
     * @param supplier the supplier function to create new instances of the object
     * @param c the field consumer to set field values
     * @param <T> the type of the object
     * @return the created ObjectReader
     */
    public <T> ObjectReader<T> createObjectReader(
            String[] names,
            Type[] types,
            Supplier<T> supplier,
            FieldConsumer<T> c
    ) {
        return createObjectReader(names, types, null, supplier, c);
    }

    /**
     * Creates an ObjectReader for a custom object with specified field names, types, features, and consumer.
     *
     * @param names the field names
     * @param types the field types
     * @param features the field features (can be null)
     * @param supplier the supplier function to create new instances of the object
     * @param c the field consumer to set field values
     * @param <T> the type of the object
     * @return the created ObjectReader
     */
    public <T> ObjectReader<T> createObjectReader(
            String[] names,
            Type[] types,
            long[] features,
            Supplier<T> supplier,
            FieldConsumer<T> c
    ) {
        FieldReader[] fieldReaders = new FieldReader[names.length];
        for (int i = 0; i < names.length; i++) {
            Type fieldType = types[i];
            Class fieldClass = TypeUtils.getClass(fieldType);
            long feature = features != null && i < features.length ? features[i] : 0;
            fieldReaders[i] = creator.createFieldReader(
                    names[i],
                    fieldType,
                    fieldClass,
                    feature,
                    new FieldBiConsumer(i, c)
            );
        }

        return creator.createObjectReader(
                null,
                supplier,
                fieldReaders
        );
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
     * Checks if smart match is disabled.
     *
     * @return true if smart match is disabled, false otherwise
     */
    public boolean isDisableSmartMatch() {
        return disableSmartMatch;
    }

    /**
     * Sets whether smart match is disabled.
     *
     * @param disableSmartMatch true to disable smart match, false to enable it
     */
    public void setDisableSmartMatch(boolean disableSmartMatch) {
        this.disableSmartMatch = disableSmartMatch;
    }

    /**
     * Gets the property naming strategy used by this provider.
     *
     * @return the property naming strategy, or null if none is set
     * @since 2.0.52
     */
    public PropertyNamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    /**
     * Sets the property naming strategy used by this provider.
     *
     * @param namingStrategy the property naming strategy to set
     * @since 2.0.52
     */
    public void setNamingStrategy(PropertyNamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }
}
