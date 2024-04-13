package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.JSONReader.AutoTypeBeforeHandler;
import com.alibaba.fastjson2.annotation.*;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.Consumer;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.function.impl.*;
import com.alibaba.fastjson2.time.ZoneId;
import com.alibaba.fastjson2.util.*;

import javax.sql.DataSource;
import javax.sql.RowSet;

import java.io.Closeable;
import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.regex.Pattern;

import static com.alibaba.fastjson2.util.BeanUtils.*;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static com.alibaba.fastjson2.util.TypeUtils.loadClass;

public class ObjectReaderProvider {
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

    final ConcurrentMap<Type, ObjectReader> cache = new ConcurrentHashMap<>();
    final ConcurrentMap<Type, ObjectReader> cacheFieldBased = new ConcurrentHashMap<>();
    final ConcurrentMap<Long, ObjectReader> hashCache = new ConcurrentHashMap<>();
    final ConcurrentMap<Class, Class> mixInCache = new ConcurrentHashMap<>();

    final LRUAutoTypeCache autoTypeList = new LRUAutoTypeCache(1024);

    private ConcurrentMap<Type, Map<Type, Function>> typeConverts;

    public final ObjectReaderCreator creator;

    private long[] denyHashCodes;
    private long[] acceptHashCodes;

    private AutoTypeBeforeHandler autoTypeBeforeHandler;
    private Consumer<Class> autoTypeHandler;

    {
        denyHashCodes = new long[]{
                -9164606388214699518L,
                -8754006975464705441L,
                -8720046426850100497L,
                -8649961213709896794L,
                -8614556368991373401L,
                -8382625455832334425L,
                -8165637398350707645L,
                -8109300701639721088L,
                -7966123100503199569L,
                -7921218830998286408L,
                -7775351613326101303L,
                -7768608037458185275L,
                -7766605818834748097L,
                -6835437086156813536L,
                -6316154655839304624L,
                -6179589609550493385L,
                -6149130139291498841L,
                -6149093380703242441L,
                -6088208984980396913L,
                -6025144546313590215L,
                -5939269048541779808L,
                -5885964883385605994L,
                -5767141746063564198L,
                -5764804792063216819L,
                -5472097725414717105L,
                -5194641081268104286L,
                -5076846148177416215L,
                -4837536971810737970L,
                -4836620931940850535L,
                -4733542790109620528L,
                -4703320437989596122L,
                -4608341446948126581L,
                -4537258998789938600L,
                -4438775680185074100L,
                -4314457471973557243L,
                -4150995715611818742L,
                -4082057040235125754L,
                -3975378478825053783L,
                -3967588558552655563L,
                -3935185854875733362L,
                -3319207949486691020L,
                -3077205613010077203L,
                -3053747177772160511L,
                -2995060141064716555L,
                -2825378362173150292L,
                -2533039401923731906L,
                -2439930098895578154L,
                -2378990704010641148L,
                -2364987994247679115L,
                -2262244760619952081L,
                -2192804397019347313L,
                -2095516571388852610L,
                -1872417015366588117L,
                -1650485814983027158L,
                -1589194880214235129L,
                -965955008570215305L,
                -905177026366752536L,
                -831789045734283466L,
                -803541446955902575L,
                -731978084025273882L,
                -666475508176557463L,
                -582813228520337988L,
                -254670111376247151L,
                -219577392946377768L,
                -190281065685395680L,
                -26639035867733124L,
                -9822483067882491L,
                4750336058574309L,
                33238344207745342L,
                156405680656087946L,
                218512992947536312L,
                313864100207897507L,
                386461436234701831L,
                744602970950881621L,
                823641066473609950L,
                1073634739308289776L,
                1153291637701043748L,
                1203232727967308606L,
                1214780596910349029L,
                1268707909007641340L,
                1459860845934817624L,
                1502845958873959152L,
                1534439610567445754L,
                1698504441317515818L,
                1818089308493370394L,
                2078113382421334967L,
                2164696723069287854L,
                2622551729063269307L,
                2653453629929770569L,
                2660670623866180977L,
                2731823439467737506L,
                2836431254737891113L,
                2930861374593775110L,
                3058452313624178956L,
                3085473968517218653L,
                3089451460101527857L,
                3114862868117605599L,
                3129395579983849527L,
                3256258368248066264L,
                3452379460455804429L,
                3547627781654598988L,
                3637939656440441093L,
                3688179072722109200L,
                3718352661124136681L,
                3730752432285826863L,
                3740226159580918099L,
                3794316665763266033L,
                3977090344859527316L,
                4000049462512838776L,
                4046190361520671643L,
                4147696707147271408L,
                4193204392725694463L,
                4215053018660518963L,
                4241163808635564644L,
                4254584350247334433L,
                4814658433570175913L,
                4841947709850912914L,
                4904007817188630457L,
                5100336081510080343L,
                5120543992130540564L,
                5274044858141538265L,
                5347909877633654828L,
                5450448828334921485L,
                5474268165959054640L,
                5545425291794704408L,
                5596129856135573697L,
                5688200883751798389L,
                5751393439502795295L,
                5916409771425455946L,
                5944107969236155580L,
                6007332606592876737L,
                6090377589998869205L,
                6280357960959217660L,
                6456855723474196908L,
                6511035576063254270L,
                6534946468240507089L,
                6584624952928234050L,
                6734240326434096246L,
                6742705432718011780L,
                6800727078373023163L,
                6854854816081053523L,
                7045245923763966215L,
                7123326897294507060L,
                7164889056054194741L,
                7179336928365889465L,
                7240293012336844478L,
                7347653049056829645L,
                7375862386996623731L,
                7442624256860549330L,
                7617522210483516279L,
                7658177784286215602L,
                8055461369741094911L,
                8064026652676081192L,
                8389032537095247355L,
                8488266005336625107L,
                8537233257283452655L,
                8735538376409180149L,
                8838294710098435315L,
                8861402923078831179L,
                9140390920032557669L,
                9140416208800006522L,
                9144212112462101475L
        };

        acceptHashCodes = new long[]{-6293031534589903644L};

        hashCache.put(ObjectArrayReader.TYPE_HASH_CODE, ObjectArrayReader.INSTANCE);
        final long STRING_CLASS_NAME_HASH = -4834614249632438472L; // Fnv.hashCode64(String.class.getName());
        hashCache.put(STRING_CLASS_NAME_HASH, ObjectReaderImplString.INSTANCE);
        final long HASH_MAP_CLASS_NAME_HASH = 77; // Fnv.hashCode64(TypeUtils.getTypeName(HashMap.class));
        hashCache.put(HASH_MAP_CLASS_NAME_HASH, ObjectReaderImplMap.INSTANCE);
    }

    public void registerIfAbsent(long hashCode, ObjectReader objectReader) {
        hashCache.put(hashCode, objectReader);
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

    public ObjectReader register(Type type, ObjectReader objectReader) {
        if (objectReader == null) {
            return cache.remove(type);
        }

        return cache.put(type, objectReader);
    }

    public ObjectReader registerIfAbsent(Type type, ObjectReader objectReader) {
        if (cache.containsKey(type)) {
            return cache.get(type);
        }
        return cache.put(type, objectReader);
    }

    public ObjectReader unregisterObjectReader(Type type) {
        return cache.remove(type);
    }

    public boolean unregisterObjectReader(Type type, ObjectReader reader) {
        return cache.remove(type, reader);
    }

    public void cleanup(Class objectClass) {
        mixInCache.remove(objectClass);
        cache.remove(objectClass);
        cacheFieldBased.remove(objectClass);
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

            Type[] actualTypeArguments = paramType.getActualTypeArguments();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type argType = actualTypeArguments[i];
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
        for (Iterator<Map.Entry<Class, Class>> it = mixInCache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Class, Class> entry = it.next();
            if (entry.getKey().getClassLoader() == classLoader) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<Type, ObjectReader>> it = cache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Type, ObjectReader> entry = it.next();
            if (match(entry.getKey(), entry.getValue(), classLoader)) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<Type, ObjectReader>> it = cacheFieldBased.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Type, ObjectReader> entry = it.next();
            if (match(entry.getKey(), entry.getValue(), classLoader)) {
                it.remove();
            }
        }

        BeanUtils.cleanupCache(classLoader);
    }

    public ObjectReaderCreator getCreator() {
        return this.creator;
    }

    public ObjectReaderProvider() {
        this.creator = ObjectReaderCreator.INSTANCE;
    }

    public ObjectReaderProvider(ObjectReaderCreator creator) {
        this.creator = creator;
    }

    public Function getTypeConvert(Type from, Type to) {
        ConcurrentMap<Type, Map<Type, Function>> typeConverts = this.typeConverts;
        if (typeConverts == null) {
            typeConverts = this.typeConverts = buildInitTypeConverts();
        }

        Map<Type, Function> map = typeConverts.get(from);
        if (map == null) {
            return null;
        }
        return map.get(to);
    }

    protected static ConcurrentMap<Type, Map<Type, Function>> buildInitTypeConverts() {
        ConcurrentMap<Type, Map<Type, Function>> typeConverts = new ConcurrentHashMap<>();

        registerTypeConvert(typeConverts, Character.class, char.class, o -> o);

        Class[] numberTypes = new Class[]{
                Boolean.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Number.class,
                Float.class,
                Double.class,
                BigInteger.class,
                BigDecimal.class,
                AtomicInteger.class,
                AtomicLong.class,
        };

        Function<Object, Boolean> TO_BOOLEAN = new ToAny(Boolean.class, null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Boolean.class, TO_BOOLEAN);
        }

        Function<Object, Boolean> TO_BOOLEAN_VALUE = new ToAny(Boolean.class, Boolean.FALSE);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, boolean.class, TO_BOOLEAN_VALUE);
        }

        Function<Object, String> TO_STRING = new ToAny(String.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, String.class, TO_STRING);
        }

        Function<Object, BigDecimal> TO_DECIMAL = new ToAny(BigDecimal.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, BigDecimal.class, TO_DECIMAL);
        }

        Function<Object, BigInteger> TO_BIGINT = new ToAny(BigInteger.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, BigInteger.class, TO_BIGINT);
        }

        Function<Object, Byte> TO_BYTE = new ToAny(Byte.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Byte.class, TO_BYTE);
        }

        Function<Object, Byte> TO_BYTE_VALUE = new ToAny(Byte.class, (byte) 0);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, byte.class, TO_BYTE_VALUE);
        }

        Function<Object, Short> TO_SHORT = new ToAny(Short.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Short.class, TO_SHORT);
        }

        Function<Object, Short> TO_SHORT_VALUE = new ToAny(Short.class, (short) 0);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, short.class, TO_SHORT_VALUE);
        }

        Function<Object, Integer> TO_INTEGER = new ToAny(Integer.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Integer.class, TO_INTEGER);
        }

        Function<Object, Integer> TO_INT = new ToAny(Integer.class, 0);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, int.class, TO_INT);
        }

        Function<Object, Long> TO_LONG = new ToAny(Long.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Long.class, TO_LONG);
        }

        Function<Object, Long> TO_LONG_VALUE = new ToAny(Long.class, 0L);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, long.class, TO_LONG_VALUE);
        }

        Function<Object, Float> TO_FLOAT = new ToAny(Float.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Float.class, TO_FLOAT);
        }

        Function<Object, Float> TO_FLOAT_VALUE = new ToAny(Float.class, 0F);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, float.class, TO_FLOAT_VALUE);
        }

        Function<Object, Double> TO_DOUBLE = new ToAny(Double.class);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Double.class, TO_DOUBLE);
        }

        Function<Object, Double> TO_DOUBLE_VALUE = new ToAny(Double.class, 0D);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, double.class, TO_DOUBLE_VALUE);
        }

        Function<Object, Number> TO_NUMBER = new ToAny(Number.class, 0D);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Number.class, TO_NUMBER);
        }

        {
            // String to Any
            registerTypeConvert(typeConverts, String.class, char.class, new StringToAny(char.class, '0'));
            registerTypeConvert(typeConverts, String.class, boolean.class, new StringToAny(boolean.class, false));
            registerTypeConvert(typeConverts, String.class, float.class, new StringToAny(float.class, (float) 0));
            registerTypeConvert(typeConverts, String.class, double.class, new StringToAny(double.class, (double) 0));
            registerTypeConvert(typeConverts, String.class, byte.class, new StringToAny(byte.class, (byte) 0));
            registerTypeConvert(typeConverts, String.class, short.class, new StringToAny(short.class, (short) 0));
            registerTypeConvert(typeConverts, String.class, int.class, new StringToAny(int.class, 0));
            registerTypeConvert(typeConverts, String.class, long.class, new StringToAny(long.class, 0L));

            registerTypeConvert(typeConverts, String.class, Character.class, new StringToAny(Character.class, null));
            registerTypeConvert(typeConverts, String.class, Boolean.class, new StringToAny(Boolean.class, null));
            registerTypeConvert(typeConverts, String.class, Double.class, new StringToAny(Double.class, null));
            registerTypeConvert(typeConverts, String.class, Float.class, new StringToAny(Float.class, null));
            registerTypeConvert(typeConverts, String.class, Byte.class, new StringToAny(Byte.class, null));
            registerTypeConvert(typeConverts, String.class, Short.class, new StringToAny(Short.class, null));
            registerTypeConvert(typeConverts, String.class, Integer.class, new StringToAny(Integer.class, null));
            registerTypeConvert(typeConverts, String.class, Long.class, new StringToAny(Long.class, null));
            registerTypeConvert(typeConverts, String.class, BigDecimal.class, new StringToAny(BigDecimal.class, null));
            registerTypeConvert(typeConverts, String.class, BigInteger.class, new StringToAny(BigInteger.class, null));
            registerTypeConvert(typeConverts, String.class, Number.class, new StringToAny(BigDecimal.class, null));
            registerTypeConvert(typeConverts, String.class, Collection.class, new StringToAny(Collection.class, null));
            registerTypeConvert(typeConverts, String.class, List.class, new StringToAny(List.class, null));
            registerTypeConvert(typeConverts, String.class, JSONArray.class, new StringToAny(JSONArray.class, null));
        }

        {
            registerTypeConvert(typeConverts, Boolean.class, boolean.class, o -> o);
        }
        {
            Function function = o -> o == null || "null".equals(o) || "".equals(o)
                    ? null
                    : UUID.fromString((String) o);
            registerTypeConvert(typeConverts, String.class, UUID.class, function);
        }
        return typeConverts;
    }

    static Function registerTypeConvert(
            ConcurrentMap<Type, Map<Type, Function>> typeConverts,
            Type from, Type to,
            Function typeConvert
    ) {
        Map<Type, Function> map = typeConverts.get(from);
        if (map == null) {
            typeConverts.put(from, new ConcurrentHashMap<>());
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
        ObjectReader objectReader = hashCache.get(hashCodeObj);

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
            autoTypeList.put(typeName, new Date());
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
                if (Arrays.binarySearch(denyHashCodes, hash) >= 0 && TypeUtils.getMapping(typeName) == null) {
                    throw new JSONException("autoType is not support. " + typeName);
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

                if (Arrays.binarySearch(denyHashCodes, hash) >= 0) {
                    throw new JSONException("autoType is not support. " + typeName);
                }

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
            if (ClassLoader.class.isAssignableFrom(clazz) || DataSource.class.isAssignableFrom(clazz) || RowSet.class.isAssignableFrom(clazz)) {
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

    public ObjectReader getObjectReader(Type objectType) {
        return getObjectReader(objectType, false);
    }

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

        if (objectReader != null) {
            return objectReader;
        }

        objectReader = getObjectReaderModule(objectType);
        if (objectReader != null) {
            ObjectReader previous = fieldBased
                    ? cacheFieldBased.put(objectType, objectReader)
                    : cache.put(objectType, objectReader);

            if (previous != null) {
                objectReader = previous;
            }
            return objectReader;
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
            }
        }

        ObjectReaderCreator creator = getCreator();
        objectReader = creator.createObjectReader(
                TypeUtils.getMapping(objectType),
                objectType,
                fieldBased,
                this);

        ObjectReader previous = getPreviousObjectReader(fieldBased, objectType, objectReader);
        if (previous != null) {
            objectReader = previous;
        }

        return objectReader;
    }

    private ObjectReader getPreviousObjectReader(boolean fieldBased, Type objectType, ObjectReader boundObjectReader) {
        return fieldBased
                ? cacheFieldBased.put(objectType, boundObjectReader)
                : cache.put(objectType, boundObjectReader);
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

    static final class LRUAutoTypeCache
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

    public void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
        Class mixInSource = mixInCache.get(objectClass);
        if (mixInSource != null && mixInSource != objectClass) {
            beanInfo.mixIn = true;

            for (Annotation annotation : mixInSource.getDeclaredAnnotations()) {
                if (annotation.annotationType() == JSONType.class) {
                    getBeanInfo1x(beanInfo, annotation);
                }
            }

            BeanUtils.staticMethod(mixInSource,
                    method -> getCreator(beanInfo, objectClass, method)
            );

            BeanUtils.constructor(mixInSource, constructor ->
                    getCreator(beanInfo, objectClass, constructor)
            );
        }

        Class seeAlsoClass = null;
        for (Class superClass = objectClass.getSuperclass(); ; superClass = superClass.getSuperclass()) {
            if (superClass == null || superClass == Object.class || superClass == Enum.class) {
                break;
            }

            BeanInfo superBeanInfo = new BeanInfo();
            getBeanInfo(superBeanInfo, superClass);
            if (superBeanInfo.seeAlso != null) {
                boolean inSeeAlso = false;
                for (Class seeAlsoItem : superBeanInfo.seeAlso) {
                    if (seeAlsoItem == objectClass) {
                        inSeeAlso = true;
                        break;
                    }
                }
                if (!inSeeAlso) {
                    seeAlsoClass = superClass;
                }
            }
        }

        if (seeAlsoClass != null) {
            getBeanInfo(beanInfo, seeAlsoClass);
        }

        Annotation[] annotations = objectClass.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == JSONType.class) {
                getBeanInfo1x(beanInfo, annotation);
            }
        }

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            String annotationTypeName = annotationType.getName();
            switch (annotationTypeName) {
                case "com.alibaba.fastjson.annotation.JSONType":
                    getBeanInfo1x(beanInfo, annotation);
                    break;
                case "kotlin.Metadata":
                    beanInfo.kotlin = true;
                    break;
                default:
                    break;
            }
        }

        BeanUtils.staticMethod(objectClass,
                method -> getCreator(beanInfo, objectClass, method)
        );

        BeanUtils.constructor(objectClass, constructor ->
                getCreator(beanInfo, objectClass, constructor)
        );

        if (beanInfo.creatorConstructor == null
                && (beanInfo.readerFeatures & JSONReader.Feature.FieldBased.mask) == 0
                && beanInfo.kotlin) {
            BeanUtils.getKotlinConstructor(objectClass, beanInfo);
            beanInfo.createParameterNames = BeanUtils.getKotlinConstructorParameters(objectClass);
        }
    }

    void getBeanInfo1x(BeanInfo beanInfo, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);

                switch (name) {
                    case "seeAlso": {
                        Class<?>[] classes = (Class<?>[]) result;
                        if (classes.length != 0) {
                            beanInfo.seeAlso = classes;
                            beanInfo.seeAlsoNames = new String[classes.length];
                            for (int i = 0; i < classes.length; i++) {
                                Class<?> item = classes[i];

                                BeanInfo itemBeanInfo = new BeanInfo();
                                processSeeAlsoAnnotation(itemBeanInfo, item);
                                String typeName = itemBeanInfo.typeName;
                                if (typeName == null || typeName.isEmpty()) {
                                    typeName = item.getSimpleName();
                                }
                                beanInfo.seeAlsoNames[i] = typeName;
                            }
                            beanInfo.readerFeatures |= JSONReader.Feature.SupportAutoType.mask;
                        }
                        break;
                    }
                    case "seeAlsoDefault": {
                        Class<?> seeAlsoDefault = (Class<?>) result;
                        if (seeAlsoDefault != Void.class) {
                            beanInfo.seeAlsoDefault = seeAlsoDefault;
                        }
                    }
                    case "typeKey": {
                        String jsonTypeKey = (String) result;
                        if (!jsonTypeKey.isEmpty()) {
                            beanInfo.typeKey = jsonTypeKey;
                        }
                        break;
                    }
                    case "typeName": {
                        String typeName = (String) result;
                        if (!typeName.isEmpty()) {
                            beanInfo.typeName = typeName;
                        }
                        break;
                    }
                    case "naming": {
                        Enum naming = (Enum) result;
                        beanInfo.namingStrategy = naming.name();
                        break;
                    }
                    case "ignores": {
                        String[] ignores = (String[]) result;
                        if (ignores.length > 0) {
                            beanInfo.ignores = ignores;
                        }
                        break;
                    }
                    case "orders": {
                        String[] fields = (String[]) result;
                        if (fields.length != 0) {
                            beanInfo.orders = fields;
                        }
                        break;
                    }
                    case "deserializer": {
                        Class<?> deserializer = (Class) result;
                        if (ObjectReader.class.isAssignableFrom(deserializer)) {
                            beanInfo.deserializer = deserializer;
                        }
                        break;
                    }
                    case "parseFeatures": {
                        Enum[] features = (Enum[]) result;
                        for (int i = 0; i < features.length; i++) {
                            Enum feature = features[i];
                            switch (feature.name()) {
                                case "SupportAutoType":
                                    beanInfo.readerFeatures |= JSONReader.Feature.SupportAutoType.mask;
                                    break;
                                case "SupportArrayToBean":
                                    beanInfo.readerFeatures |= JSONReader.Feature.SupportArrayToBean.mask;
                                    break;
                                case "InitStringFieldAsEmpty":
                                    beanInfo.readerFeatures |= JSONReader.Feature.InitStringFieldAsEmpty.mask;
                                    break;
                                case "TrimStringFieldValue":
//                                        beanInfo.readerFeatures |= JSONReader.Feature.TrimStringFieldValue.mask;
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    }
                    case "deserializeFeatures": {
                        JSONReader.Feature[] features = (JSONReader.Feature[]) result;
                        for (int i = 0; i < features.length; i++) {
                            beanInfo.readerFeatures |= features[i].mask;
                        }
                        break;
                    }
                    case "builder": {
                        Class<?> builderClass = (Class) result;
                        if (builderClass != void.class && builderClass != Void.class) {
                            beanInfo.builder = builderClass;

                            for (Annotation builderAnnotation : builderClass.getDeclaredAnnotations()) {
                                Class<? extends Annotation> builderAnnotationClass = builderAnnotation.annotationType();
                                String builderAnnotationName = builderAnnotationClass.getName();

                                if (builderAnnotationName.equals("com.alibaba.fastjson.annotation.JSONPOJOBuilder")) {
                                    getBeanInfo1xJSONPOJOBuilder(beanInfo, builderClass, builderAnnotation, builderAnnotationClass);
                                } else {
                                    JSONBuilder jsonBuilder = findAnnotation(builderClass, JSONBuilder.class);
                                    if (jsonBuilder != null) {
                                        String buildMethodName = jsonBuilder.buildMethod();
                                        beanInfo.buildMethod = buildMethod(builderClass, buildMethodName);
                                        String withPrefix = jsonBuilder.withPrefix();
                                        if (!withPrefix.isEmpty()) {
                                            beanInfo.builderWithPrefix = withPrefix;
                                        }
                                    }
                                }
                            }

                            if (beanInfo.buildMethod == null) {
                                beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "build");
                            }

                            if (beanInfo.buildMethod == null) {
                                beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, "create");
                            }
                        }
                        break;
                    }
                    case "deserializeUsing": {
                        Class<?> deserializeUsing = (Class) result;
                        if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
                            beanInfo.deserializer = deserializeUsing;
                        }
                        break;
                    }
                    case "autoTypeBeforeHandler":
                    case "autoTypeCheckHandler": {
                        Class<?> autoTypeCheckHandler = (Class) result;
                        if (JSONReader.AutoTypeBeforeHandler.class.isAssignableFrom(autoTypeCheckHandler)) {
                            beanInfo.autoTypeBeforeHandler = (Class<JSONReader.AutoTypeBeforeHandler>) autoTypeCheckHandler;
                        }
                        break;
                    }
                    default:
                        break;
                }
            } catch (Throwable ignored) {
            }
        });
    }

    private void processSeeAlsoAnnotation(BeanInfo beanInfo, Class<?> objectClass) {
        Class mixInSource = mixInCache.get(objectClass);
        if (mixInSource != null && mixInSource != objectClass) {
            beanInfo.mixIn = true;
            processSeeAlsoAnnotation(beanInfo, mixInSource.getDeclaredAnnotations());
        }

        processSeeAlsoAnnotation(beanInfo, objectClass.getDeclaredAnnotations());
    }

    private void processSeeAlsoAnnotation(BeanInfo beanInfo, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> itemAnnotationType = annotation.annotationType();
            BeanUtils.annotationMethods(itemAnnotationType, m -> {
                String name = m.getName();
                try {
                    Object result = m.invoke(annotation);
                    if (name.equals("typeName")) {
                        String typeName = (String) result;
                        if (!typeName.isEmpty()) {
                            beanInfo.typeName = typeName;
                        }
                    }
                } catch (Throwable ignored) {
                    // ignored
                }
            });
        }
    }

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Constructor constructor,
            int paramIndex,
            Annotation[][] parameterAnnotations
    ) {
        if (objectClass != null) {
            Class mixInSource = mixInCache.get(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Constructor mixInConstructor = null;
                try {
                    mixInConstructor = mixInSource.getDeclaredConstructor(constructor.getParameterTypes());
                } catch (NoSuchMethodException ignored) {
                }
                if (mixInConstructor != null) {
                    Annotation[] mixInParamAnnotations = mixInConstructor.getParameterAnnotations()[paramIndex];
                    processAnnotation(fieldInfo, mixInParamAnnotations);
                }
            }
        }

        Annotation[] annotations = null;
        if (parameterAnnotations == null) {
            parameterAnnotations = constructor.getParameterAnnotations();
        }

        int paIndex;
        if (parameterAnnotations.length == constructor.getParameterTypes().length) {
            paIndex = paramIndex;
        } else {
            paIndex = paramIndex - 1;
        }
        if (paIndex >= 0 && paIndex < parameterAnnotations.length) {
            annotations = parameterAnnotations[paIndex];
        }

        if (annotations != null && annotations.length > 0) {
            processAnnotation(fieldInfo, annotations);
        }
    }

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Method method,
            int paramIndex
    ) {
        if (objectClass != null) {
            Class mixInSource = mixInCache.get(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Method mixInMethod = null;
                try {
                    mixInMethod = mixInSource.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ignored) {
                }
                if (mixInMethod != null) {
                    Annotation[] mixInParamAnnotations = mixInMethod.getParameterAnnotations()[paramIndex];
                    processAnnotation(fieldInfo, mixInParamAnnotations);
                }
            }
        }

        Annotation[] parameterAnnotations = method.getParameterAnnotations()[paramIndex];
        processAnnotation(fieldInfo, parameterAnnotations);
    }

    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
        if (objectClass != null) {
            Class mixInSource = mixInCache.get(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Field mixInField = null;
                try {
                    mixInField = mixInSource.getDeclaredField(field.getName());
                } catch (Exception ignored) {
                }

                if (mixInField != null) {
                    getFieldInfo(fieldInfo, mixInSource, mixInField);
                }
            }
        }

        Annotation[] annotations = field.getDeclaredAnnotations();
        if (annotations.length > 0) {
            processAnnotation(fieldInfo, annotations);
        }
    }

    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
        String methodName = method.getName();

        if (objectClass != null) {
            Class superclass = objectClass.getSuperclass();
            if (superclass != Object.class && superclass != null) {
                Method supperMethod = BeanUtils.getMethod(superclass, method);
                if (supperMethod != null) {
                    getFieldInfo(fieldInfo, superclass, supperMethod);
                }
            }

            Class[] interfaces = objectClass.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Class item = interfaces[i];
                if (item == Serializable.class) {
                    continue;
                }

                Method interfaceMethod = BeanUtils.getMethod(item, method);
                if (interfaceMethod != null && superclass != null) {
                    getFieldInfo(fieldInfo, superclass, interfaceMethod);
                }
            }

            Class mixInSource = mixInCache.get(objectClass);
            if (mixInSource != null && mixInSource != objectClass) {
                Method mixInMethod = null;
                try {
                    mixInMethod = mixInSource.getDeclaredMethod(methodName, method.getParameterTypes());
                } catch (Exception ignored) {
                }

                if (mixInMethod != null) {
                    getFieldInfo(fieldInfo, mixInSource, mixInMethod);
                }
            }
        }

        String jsonFieldName = null;

        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            JSONField jsonField = findAnnotation(annotation, JSONField.class);
            if (jsonField != null) {
                getFieldInfo(fieldInfo, jsonField);
                jsonFieldName = jsonField.name();
                if (jsonField == annotation) {
                    continue;
                }
            }

            String annotationTypeName = annotationType.getName();
            if (annotationTypeName.equals("com.alibaba.fastjson.annotation.JSONField")) {
                processJSONField1x(fieldInfo, annotation);
            }
        }

        String fieldName;
        if (methodName.startsWith("set", 0)) {
            fieldName = BeanUtils.setterName(methodName, null);
        } else {
            fieldName = BeanUtils.getterName(methodName, null); // readOnlyProperty
        }

        String fieldName1, fieldName2;
        char c0, c1;
        if (fieldName.length() > 1
                && (c0 = fieldName.charAt(0)) >= 'A' && c0 <= 'Z'
                && (c1 = fieldName.charAt(1)) >= 'A' && c1 <= 'Z'
                && (jsonFieldName == null || jsonFieldName.isEmpty())) {
            char[] chars = fieldName.toCharArray();
            chars[0] = (char) (chars[0] + 32);
            fieldName1 = new String(chars);

            chars[1] = (char) (chars[1] + 32);
            fieldName2 = new String(chars);
        } else {
            fieldName1 = null;
            fieldName2 = null;
        }

        BeanUtils.getFieldInfo(objectClass, fieldInfo, this, fieldName, fieldName1, fieldName2);
        if (fieldName1 != null && fieldInfo.fieldName == null && fieldInfo.alternateNames == null) {
            fieldInfo.alternateNames = new String[]{fieldName1, fieldName2};
        }
    }

    private void processAnnotation(FieldInfo fieldInfo, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            JSONField jsonField = findAnnotation(annotation, JSONField.class);
            if (jsonField != null) {
                getFieldInfo(fieldInfo, jsonField);
                if (jsonField == annotation) {
                    continue;
                }
            }

            String annotationTypeName = annotationType.getName();
            if (annotationTypeName.equals("com.alibaba.fastjson.annotation.JSONField")) {
                processJSONField1x(fieldInfo, annotation);
            }
        }
    }

    private void processJSONField1x(FieldInfo fieldInfo, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                switch (name) {
                    case "name": {
                        String value = (String) result;
                        if (!value.isEmpty()) {
                            fieldInfo.fieldName = value;
                        }
                        break;
                    }
                    case "format": {
                        String format = (String) result;
                        if (!format.isEmpty()) {
                            format = format.trim();

                            if (format.indexOf('T') != -1 && !format.contains("'T'")) {
                                format = format.replaceAll("T", "'T'");
                            }

                            fieldInfo.format = format;
                        }
                        break;
                    }
                    case "label": {
                        String label = (String) result;
                        if (!label.isEmpty()) {
                            fieldInfo.label = label;
                        }
                        break;
                    }
                    case "defaultValue": {
                        String value = (String) result;
                        if (!value.isEmpty()) {
                            fieldInfo.defaultValue = value;
                        }
                        break;
                    }
                    case "alternateNames": {
                        String[] alternateNames = (String[]) result;
                        if (alternateNames.length != 0) {
                            if (fieldInfo.alternateNames == null) {
                                fieldInfo.alternateNames = alternateNames;
                            } else {
                                Set<String> nameSet = new LinkedHashSet<>();
                                nameSet.addAll(Arrays.asList(alternateNames));
                                nameSet.addAll(Arrays.asList(fieldInfo.alternateNames));
                                fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
                            }
                        }
                        break;
                    }
                    case "ordinal": {
                        Integer ordinal = (Integer) result;
                        if (ordinal.intValue() != 0) {
                            fieldInfo.ordinal = ordinal;
                        }
                        break;
                    }
                    case "deserialize": {
                        Boolean serialize = (Boolean) result;
                        if (!serialize.booleanValue()) {
                            fieldInfo.ignore = true;
                        }
                        break;
                    }
                    case "parseFeatures": {
                        Enum[] features = (Enum[]) result;
                        for (Enum feature : features) {
                            switch (feature.name()) {
                                case "SupportAutoType":
                                    fieldInfo.features |= JSONReader.Feature.SupportAutoType.mask;
                                    break;
                                case "SupportArrayToBean":
                                    fieldInfo.features |= JSONReader.Feature.SupportArrayToBean.mask;
                                    break;
                                case "InitStringFieldAsEmpty":
                                    fieldInfo.features |= JSONReader.Feature.InitStringFieldAsEmpty.mask;
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    }
                    case "deserializeUsing": {
                        Class<?> deserializeUsing = (Class) result;
                        if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
                            fieldInfo.readUsing = deserializeUsing;
                        }
                        break;
                    }
                    default:
                        break;
                }
            } catch (Throwable ignored) {
                // ignored
            }
        });
    }

    private void getFieldInfo(FieldInfo fieldInfo, JSONField jsonField) {
        if (jsonField == null) {
            return;
        }

        String jsonFieldName = jsonField.name();
        if (!jsonFieldName.isEmpty()) {
            fieldInfo.fieldName = jsonFieldName;
        }

        String jsonFieldFormat = jsonField.format();
        if (!jsonFieldFormat.isEmpty()) {
            jsonFieldFormat = jsonFieldFormat.trim();
            if (jsonFieldFormat.indexOf('T') != -1 && !jsonFieldFormat.contains("'T'")) {
                jsonFieldFormat = jsonFieldFormat.replaceAll("T", "'T'");
            }

            fieldInfo.format = jsonFieldFormat;
        }

        String label = jsonField.label();
        if (!label.isEmpty()) {
            label = label.trim();
            fieldInfo.label = label;
        }

        String defaultValue = jsonField.defaultValue();
        if (!defaultValue.isEmpty()) {
            fieldInfo.defaultValue = defaultValue;
        }

        String locale = jsonField.locale();
        if (!locale.isEmpty()) {
            String[] parts = locale.split("_");
            if (parts.length == 2) {
                fieldInfo.locale = new Locale(parts[0], parts[1]);
            }
        }

        String[] alternateNames = jsonField.alternateNames();
        if (alternateNames.length != 0) {
            if (fieldInfo.alternateNames == null) {
                fieldInfo.alternateNames = alternateNames;
            } else {
                Set<String> nameSet = new LinkedHashSet<>();
                Collections.addAll(nameSet, alternateNames);
                nameSet.addAll(Arrays.asList(fieldInfo.alternateNames));
                fieldInfo.alternateNames = nameSet.toArray(new String[nameSet.size()]);
            }
        }

        if (!fieldInfo.ignore) {
            fieldInfo.ignore = !jsonField.deserialize();
        }

        for (JSONReader.Feature feature : jsonField.deserializeFeatures()) {
            fieldInfo.features |= feature.mask;
            if (fieldInfo.ignore && feature == JSONReader.Feature.FieldBased) {
                fieldInfo.ignore = false;
            }
        }

        int ordinal = jsonField.ordinal();
        if (ordinal != 0) {
            fieldInfo.ordinal = ordinal;
        }

        boolean value = jsonField.value();
        if (value) {
            fieldInfo.features |= FieldInfo.VALUE_MASK;
        }

        if (jsonField.unwrapped()) {
            fieldInfo.features |= FieldInfo.UNWRAPPED_MASK;
        }

        if (jsonField.required()) {
            fieldInfo.required = true;
        }

        Class deserializeUsing = jsonField.deserializeUsing();
        if (ObjectReader.class.isAssignableFrom(deserializeUsing)) {
            fieldInfo.readUsing = deserializeUsing;
        }
    }

    private void getBeanInfo1xJSONPOJOBuilder(
            BeanInfo beanInfo,
            Class<?> builderClass,
            Annotation builderAnnatation,
            Class<? extends Annotation> builderAnnatationClass
    ) {
        BeanUtils.annotationMethods(builderAnnatationClass, method -> {
            try {
                String methodName = method.getName();
                switch (methodName) {
                    case "buildMethod": {
                        String buildMethodName = (String) method.invoke(builderAnnatation);
                        beanInfo.buildMethod = BeanUtils.buildMethod(builderClass, buildMethodName);
                        break;
                    }
                    case "withPrefix": {
                        String withPrefix = (String) method.invoke(builderAnnatation);
                        if (!withPrefix.isEmpty()) {
                            beanInfo.builderWithPrefix = withPrefix;
                        }
                        break;
                    }
                    default:
                        break;
                }
            } catch (Throwable ignored) {
            }
        });
    }

    private void getCreator(BeanInfo beanInfo, Class<?> objectClass, Constructor constructor) {
        if (objectClass.isEnum()) {
            return;
        }

        Annotation[] annotations = constructor.getDeclaredAnnotations();

        boolean creatorMethod = false;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();

            JSONCreator jsonCreator = findAnnotation(annotation, JSONCreator.class);
            if (jsonCreator != null) {
                String[] createParameterNames = jsonCreator.parameterNames();
                if (createParameterNames.length != 0) {
                    beanInfo.createParameterNames = createParameterNames;
                }

                creatorMethod = true;
                if (jsonCreator == annotation) {
                    continue;
                }
            }

            String annotationTypeName = annotationType.getName();
            if (annotationTypeName.equals("com.alibaba.fastjson.annotation.JSONCreator")
                    || annotationTypeName.equals("com.alibaba.fastjson2.annotation.JSONCreator")
            ) {
                creatorMethod = true;
                annotationMethods(annotationType, m1 -> {
                    try {
                        switch (m1.getName()) {
                            case "parameterNames":
                                String[] createParameterNames = (String[]) m1.invoke(annotation);
                                if (createParameterNames.length != 0) {
                                    beanInfo.createParameterNames = createParameterNames;
                                }
                                break;
                            default:
                                break;
                        }
                    } catch (Throwable ignored) {
                    }
                });
            }
        }

        if (!creatorMethod) {
            return;
        }

        Constructor<?> targetConstructor = null;
        try {
            targetConstructor = objectClass.getDeclaredConstructor(constructor.getParameterTypes());
        } catch (NoSuchMethodException ignored) {
        }
        if (targetConstructor != null) {
            beanInfo.creatorConstructor = targetConstructor;
        }
    }

    private void getCreator(BeanInfo beanInfo, Class<?> objectClass, Method method) {
        if (method.getDeclaringClass() == Enum.class) {
            return;
        }

        String methodName = method.getName();
        if (objectClass.isEnum()) {
            if (methodName.equals("values")) {
                return;
            }
        }

        Annotation[] annotations = method.getDeclaredAnnotations();

        boolean creatorMethod = false;
        JSONCreator jsonCreator = null;
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            jsonCreator = findAnnotation(annotation, JSONCreator.class);
            if (jsonCreator == annotation) {
                continue;
            }

            if (annotationType.getName().equals("com.alibaba.fastjson.annotation.JSONCreator")) {
                creatorMethod = true;
                annotationMethods(annotationType, m1 -> {
                    try {
                        switch (m1.getName()) {
                            case "parameterNames":
                                String[] createParameterNames = (String[]) m1.invoke(annotation);
                                if (createParameterNames.length != 0) {
                                    beanInfo.createParameterNames = createParameterNames;
                                }
                                break;
                            default:
                                break;
                        }
                    } catch (Throwable ignored) {
                    }
                });
            }
        }

        if (jsonCreator != null) {
            String[] createParameterNames = jsonCreator.parameterNames();
            if (createParameterNames.length != 0) {
                beanInfo.createParameterNames = createParameterNames;
            }

            creatorMethod = true;
        }

        if (!creatorMethod) {
            return;
        }

        Method targetMethod = null;
        try {
            targetMethod = objectClass.getDeclaredMethod(methodName, method.getParameterTypes());
        } catch (NoSuchMethodException ignored) {
        }

        if (targetMethod != null) {
            beanInfo.createMethod = targetMethod;
        }
    }

    private ObjectReader getObjectReaderModule(Type type) {
        if (type == String.class || type == CharSequence.class) {
            return ObjectReaderImplString.INSTANCE;
        }

        if (type == char.class || type == Character.class) {
            return ObjectReaderImplCharacter.INSTANCE;
        }

        if (type == boolean.class || type == Boolean.class) {
            return ObjectReaderImplBoolean.INSTANCE;
        }

        if (type == byte.class || type == Byte.class) {
            return ObjectReaderImplByte.INSTANCE;
        }

        if (type == short.class || type == Short.class) {
            return ObjectReaderImplShort.INSTANCE;
        }

        if (type == int.class || type == Integer.class) {
            return ObjectReaderImplInteger.INSTANCE;
        }

        if (type == long.class || type == Long.class) {
            return ObjectReaderImplInt64.INSTANCE;
        }

        if (type == float.class || type == Float.class) {
            return ObjectReaderImplFloat.INSTANCE;
        }

        if (type == double.class || type == Double.class) {
            return ObjectReaderImplDouble.INSTANCE;
        }

        if (type == BigInteger.class) {
            return ObjectReaderImplBigInteger.INSTANCE;
        }

        if (type == BigDecimal.class) {
            return ObjectReaderImplBigDecimal.INSTANCE;
        }

        if (type == Number.class) {
            return ObjectReaderImplNumber.INSTANCE;
        }

        if (type == UUID.class) {
            return ObjectReaderImplUUID.INSTANCE;
        }

        if (type == AtomicBoolean.class) {
            return new ObjectReaderImplFromBoolean(
                    AtomicBoolean.class,
                    (Function<Boolean, AtomicBoolean>) AtomicBoolean::new
            );
        }

        if (type == URI.class) {
            return new ObjectReaderImplFromString<URI>(
                    URI.class,
                    URI::create
            );
        }

        if (type == Charset.class) {
            return new ObjectReaderImplFromString<Charset>(Charset.class, e -> Charset.forName(e));
        }

        if (type == File.class) {
            return new ObjectReaderImplFromString<File>(File.class, e -> new File(e));
        }

        if (type == URL.class) {
            return new ObjectReaderImplFromString<URL>(
                    URL.class,
                    e -> {
                        try {
                            return new URL(e);
                        } catch (MalformedURLException ex) {
                            throw new JSONException("read URL error", ex);
                        }
                    });
        }

        if (type == Pattern.class) {
            return new ObjectReaderImplFromString<Pattern>(Pattern.class, Pattern::compile);
        }

        if (type == SimpleDateFormat.class) {
            return new ObjectReaderImplFromString<SimpleDateFormat>(SimpleDateFormat.class, SimpleDateFormat::new);
        }

        if (type == Class.class) {
            return ObjectReaderImplClass.INSTANCE;
        }

        if (type == Method.class) {
            return new ObjectReaderImplMethod();
        }

        if (type == Field.class) {
            return new ObjectReaderImplField();
        }

        if (type == Type.class) {
            return ObjectReaderImplClass.INSTANCE;
        }

        final String typeName;
        if (type instanceof Class) {
            typeName = ((Class<?>) type).getName();
        } else {
            typeName = "";
        }

        if (type == Map.class || type == AbstractMap.class) {
            return ObjectReaderImplMap.of(null, (Class) type, 0);
        }

        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return typedMap((Class) type, ConcurrentHashMap.class, null, Object.class);
        }

        if (type == ConcurrentNavigableMap.class
                || type == ConcurrentSkipListMap.class
        ) {
            return typedMap((Class) type, ConcurrentSkipListMap.class, null, Object.class);
        }

        if (type == SortedMap.class
                || type == NavigableMap.class
                || type == TreeMap.class
        ) {
            return typedMap((Class) type, TreeMap.class, null, Object.class);
        }

        if (type == Calendar.class) {
            return ObjectReaderImplCalendar.INSTANCE;
        }

        if (type == Date.class) {
            return ObjectReaderImplDate.INSTANCE;
        }

        if (type == Locale.class) {
            return ObjectReaderImplLocale.INSTANCE;
        }

        if (type == Currency.class) {
            return ObjectReaderImplCurrency.INSTANCE;
        }

        if (type == ZoneId.class) {
//            return ZoneIdImpl.INSTANCE;
            // ZoneId.of(strVal)
            return new ObjectReaderImplFromString<ZoneId>(ZoneId.class, e -> ZoneId.of(e));
        }

        if (type == TimeZone.class) {
            return new ObjectReaderImplFromString<TimeZone>(TimeZone.class, e -> TimeZone.getTimeZone(e));
        }

        if (type == char[].class) {
            return ObjectReaderImplCharValueArray.INSTANCE;
        }

        if (type == float[].class) {
            return ObjectReaderImplFloatValueArray.INSTANCE;
        }

        if (type == double[].class) {
            return ObjectReaderImplDoubleValueArray.INSTANCE;
        }

        if (type == boolean[].class) {
            return ObjectReaderImplBoolValueArray.INSTANCE;
        }

        if (type == byte[].class) {
            return ObjectReaderImplInt8ValueArray.INSTANCE;
        }

        if (type == short[].class) {
            return ObjectReaderImplInt16ValueArray.INSTANCE;
        }

        if (type == int[].class) {
            return ObjectReaderImplInt32ValueArray.INSTANCE;
        }

        if (type == long[].class) {
            return ObjectReaderImplInt64ValueArray.INSTANCE;
        }

        if (type == Byte[].class) {
            return ObjectReaderImplInt8Array.INSTANCE;
        }

        if (type == Short[].class) {
            return ObjectReaderImplInt16Array.INSTANCE;
        }

        if (type == Integer[].class) {
            return ObjectReaderImplInt32Array.INSTANCE;
        }

        if (type == Long[].class) {
            return ObjectReaderImplInt64Array.INSTANCE;
        }

        if (type == Float[].class) {
            return ObjectReaderImplFloatArray.INSTANCE;
        }

        if (type == Double[].class) {
            return ObjectReaderImplDoubleArray.INSTANCE;
        }

        if (type == Number[].class) {
            return ObjectReaderImplNumberArray.INSTANCE;
        }

        if (type == String[].class) {
            return ObjectReaderImplStringArray.INSTANCE;
        }

        if (type == AtomicInteger.class) {
            return new ObjectReaderImplFromInt(AtomicInteger.class, AtomicInteger::new);
        }

        if (type == AtomicLong.class) {
            return new ObjectReaderImplFromLong(AtomicLong.class, AtomicLong::new);
        }

        if (type == AtomicIntegerArray.class) {
            return new ObjectReaderImplInt32ValueArray(AtomicIntegerArray.class, AtomicIntegerArray::new);
            //return ObjectReaderImplAtomicIntegerArray.INSTANCE;
        }

        if (type == AtomicLongArray.class) {
            return new ObjectReaderImplInt64ValueArray(AtomicLongArray.class, AtomicLongArray::new);
//            return ObjectReaderImplAtomicLongArray.INSTANCE;
        }

        if (type == AtomicReference.class) {
            return ObjectReaderImplAtomicReference.INSTANCE;
        }

        if (type instanceof MultiType) {
            return new ObjectArrayReaderMultiType((MultiType) type);
        }

        if (type instanceof MapMultiValueType) {
            return new ObjectReaderImplMapMultiValueType((MapMultiValueType) type);
        }

        if (type == StringBuffer.class || type == StringBuilder.class) {
            try {
                Class objectClass = (Class) type;
                return new ObjectReaderImplValue(
                        objectClass,
                        String.class,
                        String.class,
                        0,
                        null,
                        null,
                        objectClass.getConstructor(String.class),
                        null,
                        null
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        if (type == Iterable.class
                || type == Collection.class
                || type == List.class
                || type == AbstractCollection.class
                || type == AbstractList.class
                || type == ArrayList.class
        ) {
            return ObjectReaderImplList.of(type, null, 0);
            // return new ObjectReaderImplList(type, (Class) type, ArrayList.class, Object.class, null);
        }

        if (type == Queue.class
                || type == Deque.class
                || type == AbstractSequentialList.class
                || type == LinkedList.class) {
//            return new ObjectReaderImplList(type, (Class) type, LinkedList.class, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == Set.class || type == AbstractSet.class || type == EnumSet.class) {
//            return new ObjectReaderImplList(type, (Class) type, HashSet.class, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == NavigableSet.class || type == SortedSet.class) {
//            return new ObjectReaderImplList(type, (Class) type, TreeSet.class, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == ConcurrentLinkedQueue.class
                || type == ConcurrentSkipListSet.class
                || type == LinkedHashSet.class
                || type == HashSet.class
                || type == TreeSet.class
                || type == CopyOnWriteArrayList.class
        ) {
//            return new ObjectReaderImplList(type, (Class) type, (Class) type, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == ObjectReaderImplList.CLASS_EMPTY_SET
                || type == ObjectReaderImplList.CLASS_EMPTY_LIST
                || type == ObjectReaderImplList.CLASS_SINGLETON
                || type == ObjectReaderImplList.CLASS_SINGLETON_LIST
                || type == ObjectReaderImplList.CLASS_ARRAYS_LIST
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_COLLECTION
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_LIST
                || type == ObjectReaderImplList.CLASS_UNMODIFIABLE_SET
        ) {
//            return new ObjectReaderImplList(type, (Class) type, (Class) type, Object.class, null);
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == TypeUtils.CLASS_SINGLE_SET) {
//            return SingletonSetImpl.INSTANCE;
            return ObjectReaderImplList.of(type, null, 0);
        }

        if (type == Object.class
                || type == Cloneable.class
                || type == Closeable.class
                || type == Serializable.class
                || type == Comparable.class
        ) {
            return ObjectReaderImplObject.INSTANCE;
        }

        if (type == Map.Entry.class) {
            return new ObjectReaderImplMapEntry(null, null);
        }

        if (type instanceof Class) {
            Class objectClass = (Class) type;

            if (Map.class.isAssignableFrom(objectClass)) {
                return ObjectReaderImplMap.of(null, objectClass, 0);
            }

            if (Collection.class.isAssignableFrom(objectClass)) {
                return ObjectReaderImplList.of(objectClass, objectClass, 0);
            }

            if (objectClass.isArray()) {
                Class componentType = objectClass.getComponentType();
                if (componentType == Object.class) {
                    return ObjectArrayReader.INSTANCE;
                }
                return new ObjectArrayTypedReader(objectClass);
            }

            ObjectReaderCreator creator = JSONFactory
                    .defaultObjectReaderProvider
                    .creator;

            if (objectClass == StackTraceElement.class) {
                try {
                    Constructor constructor = objectClass.getConstructor(
                            String.class,
                            String.class,
                            String.class,
                            int.class);

                    return creator
                            .createObjectReaderNoneDefaultConstructor(
                                    constructor,
                                    "className",
                                    "methodName",
                                    "fileName",
                                    "lineNumber");
                } catch (Throwable ignored) {
                    //
                }
            }
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 2) {
                Type actualTypeParam0 = actualTypeArguments[0];
                Type actualTypeParam1 = actualTypeArguments[1];

                if (rawType == Map.class
                        || rawType == AbstractMap.class
                        || rawType == HashMap.class
                ) {
                    return typedMap((Class) rawType, HashMap.class, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == ConcurrentMap.class
                        || rawType == ConcurrentHashMap.class
                ) {
                    return typedMap((Class) rawType, ConcurrentHashMap.class, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == ConcurrentNavigableMap.class
                        || rawType == ConcurrentSkipListMap.class
                ) {
                    return typedMap((Class) rawType, ConcurrentSkipListMap.class, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == LinkedHashMap.class || rawType == TreeMap.class || rawType == EnumMap.class) {
                    return typedMap((Class) rawType, (Class) rawType, actualTypeParam0, actualTypeParam1);
                }

                if (rawType == Map.Entry.class) {
                    return new ObjectReaderImplMapEntry(actualTypeArguments[0], actualTypeArguments[1]);
                }
            }

            if (actualTypeArguments.length == 1) {
                Type itemType = actualTypeArguments[0];
                Class itemClass = TypeUtils.getMapping(itemType);

                if (rawType == Iterable.class
                        || rawType == Collection.class
                        || rawType == List.class
                        || rawType == AbstractCollection.class
                        || rawType == AbstractList.class
                        || rawType == ArrayList.class) {
                    if (itemClass == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, ArrayList.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, ArrayList.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == Queue.class
                        || rawType == Deque.class
                        || rawType == AbstractSequentialList.class
                        || rawType == LinkedList.class) {
                    if (itemClass == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, LinkedList.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, LinkedList.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == Set.class || rawType == AbstractSet.class || rawType == EnumSet.class) {
                    if (itemClass == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, HashSet.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, HashSet.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == NavigableSet.class || rawType == SortedSet.class) {
                    if (itemType == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, TreeSet.class);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, TreeSet.class);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == ConcurrentLinkedQueue.class
                        || rawType == ConcurrentSkipListSet.class
                        || rawType == LinkedHashSet.class
                        || rawType == HashSet.class
                        || rawType == TreeSet.class
                        || rawType == CopyOnWriteArrayList.class
                ) {
                    if (itemType == String.class) {
                        return new ObjectReaderImplListStr((Class) rawType, (Class) rawType);
                    } else if (itemClass == Long.class) {
                        return new ObjectReaderImplListInt64((Class) rawType, (Class) rawType);
                    } else {
                        return ObjectReaderImplList.of(type, null, 0);
                    }
                }

                if (rawType == AtomicReference.class) {
                    return new ObjectReaderImplAtomicReference(itemType);
                }

                if (itemType instanceof WildcardType) {
                    return getObjectReaderModule(rawType);
                }
            }

            return null;
        }

        if (type instanceof GenericArrayType) {
            return new ObjectReaderImplGenericArray((GenericArrayType) type);
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getObjectReaderModule(upperBounds[0]);
            }
        }

        if (type == ParameterizedType.class) {
            return ObjectReaders.ofReflect(ParameterizedTypeImpl.class);
        }

        switch (typeName) {
            case "java.sql.Time":
                return new JdbcSupport.TimeReader(null, null);
            case "java.sql.Timestamp":
                return new JdbcSupport.TimestampReader(null, null);
            case "java.sql.Date":
                return new JdbcSupport.DateReader(null, null);
            case "java.util.RegularEnumSet":
            case "java.util.JumboEnumSet":
                return ObjectReaderImplList.of(type, TypeUtils.getClass(type), 0);
            case "java.net.InetSocketAddress":
                return new ObjectReaderMisc((Class) type);
            case "java.net.InetAddress":
                return ObjectReaderImplValue.of((Class<InetAddress>) type, String.class, address -> {
                    try {
                        return InetAddress.getByName(address);
                    } catch (UnknownHostException e) {
                        throw new JSONException("create address error", e);
                    }
                });
            case "java.text.SimpleDateFormat":
                return ObjectReaderImplValue.of((Class<SimpleDateFormat>) type, String.class, SimpleDateFormat::new);
            default:
                break;
        }

        return null;
    }

    public static ObjectReader typedMap(Class mapType, Class instanceType, Type keyType, Type valueType) {
        if ((keyType == null || keyType == String.class) && valueType == String.class) {
            return new ObjectReaderImplMapString(mapType, instanceType, 0);
        }
        return new ObjectReaderImplMapTyped(mapType, instanceType, keyType, valueType, 0, null);
    }
}
