package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.JSONReader.AutoTypeBeforeHandler;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.Consumer;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.function.impl.*;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.fastjson2.JSONFactory.*;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static com.alibaba.fastjson2.util.TypeUtils.loadClass;

public class ObjectReaderProvider {
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
    final ConcurrentMap<Long, ObjectReader> hashCache = new ConcurrentHashMap<>();
    final ConcurrentMap<Class, Class> mixInCache = new ConcurrentHashMap<>();

    final LRUAutoTypeCache autoTypeList = new LRUAutoTypeCache(1024);

    private ConcurrentMap<Type, Map<Type, Function>> typeConverts;

    public final ObjectReaderCreator creator;
    final ObjectReaderModule module = new ObjectReaderModule(this);

    private long[] denyHashCodes;
    private long[] acceptHashCodes;

    private AutoTypeBeforeHandler autoTypeBeforeHandler = DEFAULT_AUTO_TYPE_BEFORE_HANDLER;
    private Consumer<Class> autoTypeHandler = DEFAULT_AUTO_TYPE_HANDLER;

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

    public void addAutoTypeDeny(String name) {
        if (name != null && name.length() != 0) {
            long hash = Fnv.hashCode64(name);
            if (Arrays.binarySearch(this.denyHashCodes, hash) < 0) {
                long[] hashCodes = new long[this.denyHashCodes.length + 1];
                hashCodes[hashCodes.length - 1] = hash;
                System.arraycopy(this.denyHashCodes, 0, hashCodes, 0, this.denyHashCodes.length);
                Arrays.sort(hashCodes);
                this.denyHashCodes = hashCodes;
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
        init();
    }

    public ObjectReaderProvider(ObjectReaderCreator creator) {
        this.creator = creator;
        init();
    }

    void init() {
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

        Function<Object, Boolean> TO_BOOLEAN = new ToBoolean(null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Boolean.class, TO_BOOLEAN);
        }

        Function<Object, Boolean> TO_BOOLEAN_VALUE = new ToBoolean(Boolean.FALSE);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, boolean.class, TO_BOOLEAN_VALUE);
        }

        Function<Object, String> TO_STRING = new ToString();
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, String.class, TO_STRING);
        }

        Function<Object, BigDecimal> TO_DECIMAL = new ToBigDecimal();
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, BigDecimal.class, TO_DECIMAL);
        }

        Function<Object, BigInteger> TO_BIGINT = new ToBigInteger();
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, BigInteger.class, TO_BIGINT);
        }

        Function<Object, Byte> TO_BYTE = new ToByte(null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Byte.class, TO_BYTE);
        }

        Function<Object, Byte> TO_BYTE_VALUE = new ToByte((byte) 0);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, byte.class, TO_BYTE_VALUE);
        }

        Function<Object, Short> TO_SHORT = new ToShort(null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Short.class, TO_SHORT);
        }

        Function<Object, Short> TO_SHORT_VALUE = new ToShort((short) 0);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, short.class, TO_SHORT_VALUE);
        }

        Function<Object, Integer> TO_INTEGER = new ToInteger(null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Integer.class, TO_INTEGER);
        }

        Function<Object, Integer> TO_INT = new ToInteger(0);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, int.class, TO_INT);
        }

        Function<Object, Long> TO_LONG = new ToLong(null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Long.class, TO_LONG);
        }

        Function<Object, Long> TO_LONG_VALUE = new ToLong(0L);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, long.class, TO_LONG_VALUE);
        }

        Function<Object, Float> TO_FLOAT = new ToFloat(null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Float.class, TO_FLOAT);
        }

        Function<Object, Float> TO_FLOAT_VALUE = new ToFloat(0F);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, float.class, TO_FLOAT_VALUE);
        }

        Function<Object, Double> TO_DOUBLE = new ToDouble(null);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, Double.class, TO_DOUBLE);
        }

        Function<Object, Double> TO_DOUBLE_VALUE = new ToDouble(0D);
        for (Class type : numberTypes) {
            registerTypeConvert(typeConverts, type, double.class, TO_DOUBLE_VALUE);
        }

        Function<Object, Number> TO_NUMBER = new ToNumber(0D);
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

    public void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
        module.getBeanInfo(beanInfo, objectClass);
    }

    public void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
        module.getFieldInfo(fieldInfo, objectClass, field);
    }

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Constructor constructor,
            int paramIndex,
            Annotation[][] parameterAnnotations
    ) {
        module.getFieldInfo(fieldInfo, objectClass, constructor, paramIndex, parameterAnnotations);
    }

    public void getFieldInfo(
            FieldInfo fieldInfo,
            Class objectClass,
            Method method,
            int paramIndex) {
        module.getFieldInfo(fieldInfo, objectClass, method, paramIndex);
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

        if (objectReader != null) {
            return objectReader;
        }

        objectReader = module.getObjectReader(this, objectType);
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

        Class<?> objectClass = TypeUtils.getMapping(objectType);
        if (objectClass.isAnnotationPresent(JSONCompiled.class)) {
            String codeGenClassName = objectClass.getName() + "_FASTJOSNReader";
            ClassLoader classLoader = objectClass.getClassLoader();

            try {
                Class<?> loadedClass = classLoader.loadClass(codeGenClassName);
                if (ObjectReader.class.isAssignableFrom(loadedClass)) {
                    objectReader = (ObjectReader) loadedClass.newInstance();
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
                // ignored
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
}
