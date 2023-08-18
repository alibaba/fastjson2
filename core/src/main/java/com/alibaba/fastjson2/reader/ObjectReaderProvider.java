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

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static com.alibaba.fastjson2.util.TypeUtils.loadClass;

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

    static class ObjectReaderCachePair {
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
                property = JSONFactory.getProperty(PROPERTY_DENY_PROPERTY);
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
                property = JSONFactory.getProperty(PROPERTY_AUTO_TYPE_ACCEPT);
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
                property = JSONFactory.getProperty(PROPERTY_AUTO_TYPE_BEFORE_HANDLER);
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
                property = JSONFactory.getProperty(PROPERTY_AUTO_TYPE_HANDLER);
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
                property = JSONFactory.getProperty("fastjson.parser.safeMode");
            }

            if (property == null || property.isEmpty()) {
                property = System.getProperty("fastjson2.parser.safeMode");
            }
            if (property == null || property.isEmpty()) {
                property = JSONFactory.getProperty("fastjson2.parser.safeMode");
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
    final List<ObjectReaderModule> modules = new ArrayList<>();

    private long[] acceptHashCodes;

    private AutoTypeBeforeHandler autoTypeBeforeHandler = DEFAULT_AUTO_TYPE_BEFORE_HANDLER;
    private Consumer<Class> autoTypeHandler = DEFAULT_AUTO_TYPE_HANDLER;

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

    public Consumer<Class> getAutoTypeHandler() {
        return autoTypeHandler;
    }

    public void setAutoTypeHandler(Consumer<Class> autoTypeHandler) {
        this.autoTypeHandler = autoTypeHandler;
    }

    public Class getMixIn(Class target) {
        return mixInCache.get(target);
    }

    public void cleanupMixIn() {
        mixInCache.clear();
    }

    public void mixIn(Class target, Class mixinSource) {
        if (mixinSource == null) {
            mixInCache.remove(target);
        } else {
            mixInCache.put(target, mixinSource);
        }
        cache.remove(target);
        cacheFieldBased.remove(target);
    }

    public void registerSeeAlsoSubType(Class subTypeClass) {
        registerSeeAlsoSubType(subTypeClass, null);
    }

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

    public ObjectReader register(Type type, ObjectReader objectReader, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        if (objectReader == null) {
            return cache.remove(type);
        }

        return cache.put(type, objectReader);
    }

    public ObjectReader register(Type type, ObjectReader objectReader) {
        return register(type, objectReader, false);
    }

    public ObjectReader registerIfAbsent(Type type, ObjectReader objectReader) {
        return registerIfAbsent(type, objectReader, false);
    }

    public ObjectReader registerIfAbsent(Type type, ObjectReader objectReader, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.putIfAbsent(type, objectReader);
    }

    public ObjectReader unregisterObjectReader(Type type) {
        return unregisterObjectReader(type, false);
    }

    public ObjectReader unregisterObjectReader(Type type, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type);
    }

    public boolean unregisterObjectReader(Type type, ObjectReader reader) {
        return unregisterObjectReader(type, reader, false);
    }

    public boolean unregisterObjectReader(Type type, ObjectReader reader, boolean fieldBased) {
        ConcurrentMap<Type, ObjectReader> cache = fieldBased ? this.cacheFieldBased : this.cache;
        return cache.remove(type, reader);
    }

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

    public boolean unregister(ObjectReaderModule module) {
        return modules.remove(module);
    }

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

    public ObjectReaderCreator getCreator() {
        ObjectReaderCreator contextCreator = JSONFactory.getContextReaderCreator();
        if (contextCreator != null) {
            return contextCreator;
        }
        return this.creator;
    }

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

    public Function getTypeConvert(Type from, Type to) {
        Map<Type, Function> map = typeConverts.get(from);
        if (map == null) {
            return null;
        }
        return map.get(to);
    }

    public Function registerTypeConvert(Type from, Type to, Function typeConvert) {
        Map<Type, Function> map = typeConverts.get(from);
        if (map == null) {
            typeConverts.putIfAbsent(from, new ConcurrentHashMap<>());
            map = typeConverts.get(from);
        }
        return map.put(to, typeConvert);
    }

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

        Long hashCodeObj = new Long(hashCode);
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

    public List<ObjectReaderModule> getModules() {
        return modules;
    }

    public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
        for (ObjectReaderModule module : modules) {
            module.getBeanInfo(beanInfo, objectClass);
        }
    }

    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
        for (ObjectReaderModule module : modules) {
            module.getFieldInfo(fieldInfo, objectClass, field);
        }
    }

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Constructor constructor,
            int paramIndex,
            Parameter parameter
    ) {
        for (ObjectReaderModule module : modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getFieldInfo(fieldInfo, objectClass, constructor, paramIndex, parameter);
            }
        }
    }

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Method method,
            int paramIndex,
            Parameter parameter) {
        for (ObjectReaderModule module : modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
            if (annotationProcessor != null) {
                annotationProcessor.getFieldInfo(fieldInfo, objectClass, method, paramIndex, parameter);
            }
        }
    }

    public ObjectReader getObjectReader(Type objectType) {
        return getObjectReader(objectType, false);
    }

    public Function<Consumer, ByteArrayValueConsumer> createValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return creator.createByteArrayValueConsumerCreator(objectClass, fieldReaderArray);
    }

    public Function<Consumer, CharArrayValueConsumer> createCharArrayValueConsumerCreator(
            Class objectClass,
            FieldReader[] fieldReaderArray
    ) {
        return creator.createCharArrayValueConsumerCreator(objectClass, fieldReaderArray);
    }

    public ObjectReader getObjectReader(Type objectType, boolean fieldBased) {
        if (objectType == null) {
            objectType = Object.class;
        }

        ObjectReader objectReader = fieldBased
                ? cacheFieldBased.get(objectType)
                : cache.get(objectType);

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
            }
        }

        Class<?> objectClass = TypeUtils.getMapping(objectType);

        String className = objectClass.getName();
        if (!fieldBased) {
            if (className.equals("com.google.common.collect.ArrayListMultimap")) {
                objectReader = ObjectReaderImplMap.of(null, objectClass, 0);
            }
        }

        if (objectReader == null) {
            boolean jsonCompiled = false;
            Annotation[] annotations = objectClass.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                jsonCompiled = annotationType.getName().equals("com.alibaba.fastjson2.annotation.JSONCompiled");
            }
            if (jsonCompiled) {
                String codeGenClassName = objectClass.getName() + "_FASTJOSNReader";
                ClassLoader classLoader = objectClass.getClassLoader();
                if (classLoader == null) {
                    classLoader = Thread.currentThread().getContextClassLoader();
                }
                if (classLoader == null) {
                    classLoader = this.getClass().getClassLoader();
                }

                try {
                    Class<?> loadedClass = classLoader.loadClass(codeGenClassName);
                    if (ObjectReader.class.isAssignableFrom(loadedClass)) {
                        objectReader = (ObjectReader) loadedClass.newInstance();
                    }
                } catch (Exception ignored) {
                    // ignored
                }
            }

            // objectClass.getResource(objectClass.)
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

    public AutoTypeBeforeHandler getAutoTypeBeforeHandler() {
        return autoTypeBeforeHandler;
    }

    public Map<String, Date> getAutoTypeList() {
        return autoTypeList;
    }

    public void setAutoTypeBeforeHandler(AutoTypeBeforeHandler autoTypeBeforeHandler) {
        this.autoTypeBeforeHandler = autoTypeBeforeHandler;
    }

    static class LRUAutoTypeCache
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

    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
        for (ObjectReaderModule module : modules) {
            ObjectReaderAnnotationProcessor annotationProcessor = module.getAnnotationProcessor();
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

    public <T> ObjectReader<T> createObjectReader(
            String[] names,
            Type[] types,
            Supplier<T> supplier,
            FieldConsumer<T> c
    ) {
        return createObjectReader(names, types, null, supplier, c);
    }

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
}
