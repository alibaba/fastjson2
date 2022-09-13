package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplEnum;
import com.alibaba.fastjson2.reader.ObjectReaderImplInstant;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

public class TypeUtils {
    public static final Class CLASS_SINGLE_SET = Collections.singleton(1).getClass();
    public static final Class CLASS_UNMODIFIABLE_COLLECTION = Collections.unmodifiableCollection(new ArrayList<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_LIST = Collections.unmodifiableList(new ArrayList<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_SET = Collections.unmodifiableSet(new HashSet<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet<>()).getClass();
    public static final Class CLASS_UNMODIFIABLE_NAVIGABLE_SET = Collections.unmodifiableNavigableSet(new TreeSet<>()).getClass();

    static class Cache {
        volatile char[] chars;
    }

    static final Cache CACHE = new Cache();
    static final AtomicReferenceFieldUpdater<Cache, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(Cache.class, char[].class, "chars");

    public static Class<?> getMapping(Type type) {
        if (type == null) {
            return null;
        }

        if (type.getClass() == Class.class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getMapping(((ParameterizedType) type).getRawType());
        }

        if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable<?>) type).getBounds()[0];
            if (boundType instanceof Class) {
                return (Class) boundType;
            }
            return getMapping(boundType);
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getMapping(upperBounds[0]);
            }
        }

        return Object.class;
    }

    public static Date toDate(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Date) {
            return (Date) obj;
        }

        if (obj instanceof Instant) {
            Instant instant = (Instant) obj;
            return new Date(
                    instant.toEpochMilli());
        }

        if (obj instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) obj;
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof LocalDate) {
            LocalDate localDate = (LocalDate) obj;
            ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime) obj;
            ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
            return new Date(
                    zdt.toInstant().toEpochMilli());
        }

        if (obj instanceof String) {
            String str = (String) obj;

            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }

            JSONReader jsonReader;
            if (str.charAt(0) != '"') {
                jsonReader = JSONReader.of('"' + str + '"');
            } else {
                jsonReader = JSONReader.of(str);
            }
            return jsonReader.read(Date.class);
        }

        if (obj instanceof Long) {
            return new Date(((Long) obj).longValue());
        }

        throw new JSONException("can not cast to Date from " + obj.getClass());
    }

    public static Instant toInstant(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Instant) {
            return (Instant) obj;
        }

        if (obj instanceof Date) {
            return ((Date) obj).toInstant();
        }

        if (obj instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime) obj;
            return zdt.toInstant();
        }

        if (obj instanceof String) {
            String str = (String) obj;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }

            JSONReader jsonReader;
            if (str.charAt(0) != '"') {
                jsonReader = JSONReader.of('"' + str + '"');
            } else {
                jsonReader = JSONReader.of(str);
            }
            return jsonReader.read(Instant.class);
        }

        if (obj instanceof Map) {
            return (Instant) ObjectReaderImplInstant.INSTANCE.createInstance((Map) obj, 0L);
        }

        throw new JSONException("can not cast to Date from " + obj.getClass());
    }

    public static <T> T cast(Object obj, Class<T> targetClass) {
        if (obj == null) {
            return null;
        }

        if (targetClass.isInstance(obj)) {
            return (T) obj;
        }

        if (targetClass == Date.class) {
            return (T) toDate(obj);
        }

        if (targetClass == Instant.class) {
            return (T) toInstant(obj);
        }

        if (targetClass == String.class) {
            if (obj instanceof Character) {
                return (T) obj.toString();
            }

            return (T) JSON.toJSONString(obj);
        }

        if (targetClass == AtomicInteger.class) {
            return (T) new AtomicInteger(toIntValue(obj));
        }

        if (targetClass == AtomicLong.class) {
            return (T) new AtomicLong(toLongValue(obj));
        }

        if (targetClass == AtomicBoolean.class) {
            return (T) new AtomicBoolean((Boolean) obj);
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        if (obj instanceof Map) {
            ObjectReader objectReader = provider.getObjectReader(targetClass);
            return (T) objectReader.createInstance((Map) obj, 0L);
        }

        Function typeConvert = provider.getTypeConvert(obj.getClass(), targetClass);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (obj instanceof String) {
            String json = (String) obj;
            if (json.isEmpty() || "null".equals(json)) {
                return null;
            }

            JSONReader jsonReader;
            char first = json.trim().charAt(0);
            if (first == '"' || first == '{' || first == '[') {
                jsonReader = JSONReader.of(json);
            } else {
                jsonReader = JSONReader.of(
                        JSON.toJSONString(json));
            }

            ObjectReader objectReader = JSONFactory
                    .getDefaultObjectReaderProvider()
                    .getObjectReader(targetClass);
            return (T) objectReader.readObject(jsonReader, null, null, 0);
        }

        if (targetClass.isEnum()) {
            if (obj instanceof Integer) {
                int ordinal = ((Integer) obj).intValue();
                ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(targetClass);
                if (objectReader instanceof ObjectReaderImplEnum) {
                    Enum e = ((ObjectReaderImplEnum) objectReader).getEnumByOrdinal(ordinal);
                    return (T) e;
                }
            }
        }

        if (obj instanceof Collection) {
            ObjectReader objectReader = provider.getObjectReader(targetClass);
            return (T) objectReader.createInstance((Collection) obj);
        }

        throw new JSONException("can not cast to " + targetClass.getName() + ", from " + obj.getClass());
    }

    static final Map<Class, String> NAME_MAPPINGS = new IdentityHashMap<>();
    static final Map<String, Class> TYPE_MAPPINGS = new ConcurrentHashMap<>();

    static {
        NAME_MAPPINGS.put(byte.class, "B");
        NAME_MAPPINGS.put(short.class, "S");
        NAME_MAPPINGS.put(int.class, "I");
        NAME_MAPPINGS.put(long.class, "J");
        NAME_MAPPINGS.put(float.class, "F");
        NAME_MAPPINGS.put(double.class, "D");
        NAME_MAPPINGS.put(char.class, "C");
        NAME_MAPPINGS.put(boolean.class, "Z");

        NAME_MAPPINGS.put(Object[].class, "[O");
        NAME_MAPPINGS.put(Object[][].class, "[[O");
        NAME_MAPPINGS.put(byte[].class, "[B");
        NAME_MAPPINGS.put(byte[][].class, "[[B");
        NAME_MAPPINGS.put(short[].class, "[S");
        NAME_MAPPINGS.put(short[][].class, "[[S");
        NAME_MAPPINGS.put(int[].class, "[I");
        NAME_MAPPINGS.put(int[][].class, "[[I");
        NAME_MAPPINGS.put(long[].class, "[J");
        NAME_MAPPINGS.put(long[][].class, "[[J");
        NAME_MAPPINGS.put(float[].class, "[F");
        NAME_MAPPINGS.put(float[][].class, "[[F");
        NAME_MAPPINGS.put(double[].class, "[D");
        NAME_MAPPINGS.put(double[][].class, "[[D");
        NAME_MAPPINGS.put(char[].class, "[C");
        NAME_MAPPINGS.put(char[][].class, "[[C");
        NAME_MAPPINGS.put(boolean[].class, "[Z");
        NAME_MAPPINGS.put(boolean[][].class, "[[Z");

        NAME_MAPPINGS.put(Byte[].class, "[Byte");
        NAME_MAPPINGS.put(Byte[][].class, "[[Byte");
        NAME_MAPPINGS.put(Short[].class, "[Short");
        NAME_MAPPINGS.put(Short[][].class, "[[Short");
        NAME_MAPPINGS.put(Integer[].class, "[Integer");
        NAME_MAPPINGS.put(Integer[][].class, "[[Integer");
        NAME_MAPPINGS.put(Long[].class, "[Long");
        NAME_MAPPINGS.put(Long[][].class, "[[Long");
        NAME_MAPPINGS.put(Float[].class, "[Float");
        NAME_MAPPINGS.put(Float[][].class, "[[Float");
        NAME_MAPPINGS.put(Double[].class, "[Double");
        NAME_MAPPINGS.put(Double[][].class, "[[Double");
        NAME_MAPPINGS.put(Character[].class, "[Character");
        NAME_MAPPINGS.put(Character[][].class, "[[Character");
        NAME_MAPPINGS.put(Boolean[].class, "[Boolean");
        NAME_MAPPINGS.put(Boolean[][].class, "[[Boolean");

        NAME_MAPPINGS.put(String[].class, "[String");
        NAME_MAPPINGS.put(String[][].class, "[[String");

        NAME_MAPPINGS.put(BigDecimal[].class, "[BigDecimal");
        NAME_MAPPINGS.put(BigDecimal[][].class, "[[BigDecimal");

        NAME_MAPPINGS.put(BigInteger[].class, "[BigInteger");
        NAME_MAPPINGS.put(BigInteger[][].class, "[[BigInteger");

        NAME_MAPPINGS.put(UUID[].class, "[UUID");
        NAME_MAPPINGS.put(UUID[][].class, "[[UUID");

        NAME_MAPPINGS.put(Object.class, "Object");
        NAME_MAPPINGS.put(Object[].class, "[O");

        NAME_MAPPINGS.put(HashMap.class, "M");
        TYPE_MAPPINGS.put("HashMap", HashMap.class);

        NAME_MAPPINGS.put(LinkedHashMap.class, "LM");
        TYPE_MAPPINGS.put("LinkedHashMap", LinkedHashMap.class);

        NAME_MAPPINGS.put(TreeMap.class, "TM");
        TYPE_MAPPINGS.put("TreeMap", TreeMap.class);

        NAME_MAPPINGS.put(ArrayList.class, "A");
        TYPE_MAPPINGS.put("ArrayList", ArrayList.class);

        NAME_MAPPINGS.put(LinkedList.class, "LA");
        TYPE_MAPPINGS.put("LinkedList", LinkedList.class);

        //java.util.LinkedHashMap.class,

        NAME_MAPPINGS.put(HashSet.class, "HashSet");
        NAME_MAPPINGS.put(TreeSet.class, "TreeSet");
        NAME_MAPPINGS.put(LinkedHashSet.class, "LinkedHashSet");
        NAME_MAPPINGS.put(ConcurrentHashMap.class, "ConcurrentHashMap");
        NAME_MAPPINGS.put(ConcurrentLinkedQueue.class, "ConcurrentLinkedQueue");
        NAME_MAPPINGS.put(JSONObject.class, "JSONObject");
        NAME_MAPPINGS.put(JSONArray.class, "JSONArray");
        NAME_MAPPINGS.put(Currency.class, "Currency");
        NAME_MAPPINGS.put(TimeUnit.class, "TimeUnit");

        Class<?>[] classes = new Class[]{
                Object.class,
                Cloneable.class,
                AutoCloseable.class,
                java.lang.Exception.class,
                java.lang.RuntimeException.class,
                java.lang.IllegalAccessError.class,
                java.lang.IllegalAccessException.class,
                java.lang.IllegalArgumentException.class,
                java.lang.IllegalMonitorStateException.class,
                java.lang.IllegalStateException.class,
                java.lang.IllegalThreadStateException.class,
                java.lang.IndexOutOfBoundsException.class,
                java.lang.InstantiationError.class,
                java.lang.InstantiationException.class,
                java.lang.InternalError.class,
                java.lang.InterruptedException.class,
                java.lang.LinkageError.class,
                java.lang.NegativeArraySizeException.class,
                java.lang.NoClassDefFoundError.class,
                java.lang.NoSuchFieldError.class,
                java.lang.NoSuchFieldException.class,
                java.lang.NoSuchMethodError.class,
                java.lang.NoSuchMethodException.class,
                java.lang.NullPointerException.class,
                java.lang.NumberFormatException.class,
                java.lang.OutOfMemoryError.class,
                java.lang.SecurityException.class,
                java.lang.StackOverflowError.class,
                java.lang.StringIndexOutOfBoundsException.class,
                java.lang.TypeNotPresentException.class,
                java.lang.VerifyError.class,
                java.lang.StackTraceElement.class,
                java.util.Hashtable.class,
                java.util.TreeMap.class,
                java.util.IdentityHashMap.class,
                java.util.WeakHashMap.class,
                java.util.HashSet.class,
                java.util.LinkedHashSet.class,
                java.util.TreeSet.class,
                java.util.LinkedList.class,
                java.util.concurrent.TimeUnit.class,
                java.util.concurrent.ConcurrentHashMap.class,
                java.util.concurrent.atomic.AtomicInteger.class,
                java.util.concurrent.atomic.AtomicLong.class,
                java.util.Collections.EMPTY_MAP.getClass(),
                java.lang.Boolean.class,
                java.lang.Character.class,
                java.lang.Byte.class,
                java.lang.Short.class,
                java.lang.Integer.class,
                java.lang.Long.class,
                java.lang.Float.class,
                java.lang.Double.class,
                java.lang.Number.class,
                java.lang.String.class,
                java.math.BigDecimal.class,
                java.math.BigInteger.class,
                java.util.BitSet.class,
                java.util.Calendar.class,
                java.util.Date.class,
                java.util.Locale.class,
                java.util.UUID.class,
                java.util.Currency.class,
                java.text.SimpleDateFormat.class,
                JSONObject.class,
                JSONArray.class,
                java.util.concurrent.ConcurrentSkipListMap.class,
                java.util.concurrent.ConcurrentSkipListSet.class,
        };
        for (Class clazz : classes) {
            TYPE_MAPPINGS.put(clazz.getSimpleName(), clazz);
            TYPE_MAPPINGS.put(clazz.getName(), clazz);
            NAME_MAPPINGS.put(clazz, clazz.getSimpleName());
        }

        TYPE_MAPPINGS.put("JO10", JSONObject1O.class);
        TYPE_MAPPINGS.put("[O", Object[].class);
        TYPE_MAPPINGS.put("[Ljava.lang.Object;", Object[].class);
        TYPE_MAPPINGS.put("[java.lang.Object", Object[].class);
        TYPE_MAPPINGS.put("[Object", Object[].class);
        TYPE_MAPPINGS.put("StackTraceElement", StackTraceElement.class);
        TYPE_MAPPINGS.put("[StackTraceElement", StackTraceElement[].class);

        String[] items = new String[]{
                "java.util.Collections$UnmodifiableMap",
                "java.util.Collections$UnmodifiableCollection",
        };

        for (String className : items) {
            Class<?> clazz = loadClass(className);
            TYPE_MAPPINGS.put(clazz.getName(), clazz);
        }

        {
            Class<?> objectClass = loadClass("com.alibaba.fastjson.JSONObject");
            if (objectClass != null) {
                TYPE_MAPPINGS.putIfAbsent("JO1", objectClass);
                TYPE_MAPPINGS.putIfAbsent(objectClass.getName(), objectClass);
            }
            Class<?> arrayClass = loadClass("com.alibaba.fastjson.JSONArray");
            if (arrayClass != null) {
                TYPE_MAPPINGS.putIfAbsent("JA1", arrayClass);
                TYPE_MAPPINGS.putIfAbsent(arrayClass.getName(), arrayClass);
            }
        }

        NAME_MAPPINGS.put(new HashMap().keySet().getClass(), "Set");
        NAME_MAPPINGS.put(new LinkedHashMap().keySet().getClass(), "Set");
        NAME_MAPPINGS.put(new TreeMap<>().keySet().getClass(), "Set");
        NAME_MAPPINGS.put(((Map) new ConcurrentHashMap()).keySet().getClass(), "Set"); // bug fix for android9
        NAME_MAPPINGS.put(((Map) new ConcurrentSkipListMap()).keySet().getClass(), "Set"); // bug fix for android9
        TYPE_MAPPINGS.put("Set", HashSet.class);

        NAME_MAPPINGS.put(new HashMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new LinkedHashMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new TreeMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new ConcurrentHashMap().values().getClass(), "List");
        NAME_MAPPINGS.put(new ConcurrentSkipListMap().values().getClass(), "List");
        TYPE_MAPPINGS.put("List", ArrayList.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$Map1", HashMap.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$MapN", LinkedHashMap.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$Set12", LinkedHashSet.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$SetN", LinkedHashSet.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$List12", ArrayList.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$ListN", ArrayList.class);
        TYPE_MAPPINGS.put("java.util.ImmutableCollections$SubList", ArrayList.class);

        for (Map.Entry<Class, String> entry : NAME_MAPPINGS.entrySet()) {
            TYPE_MAPPINGS.putIfAbsent(entry.getValue(), entry.getKey());
        }
    }

    public static String getTypeName(Class type) {
        String mapTypeName = NAME_MAPPINGS.get(type);
        if (mapTypeName != null) {
            return mapTypeName;
        }

        if (Proxy.isProxyClass(type)) {
            Class[] interfaces = type.getInterfaces();
            if (interfaces.length > 0) {
                type = interfaces[0];
            }
        }

        String typeName = type.getTypeName();
        switch (typeName) {
            case "com.alibaba.fastjson.JSONObject":
                NAME_MAPPINGS.putIfAbsent(type, "JO1");
                return NAME_MAPPINGS.get(type);
            case "com.alibaba.fastjson.JSONArray":
                NAME_MAPPINGS.putIfAbsent(type, "JA1");
                return NAME_MAPPINGS.get(type);
//            case "org.apache.commons.lang3.tuple.ImmutablePair":
//                NAME_MAPPINGS.putIfAbsent(type, "org.apache.commons.lang3.tuple.Pair");
//                return NAME_MAPPINGS.get(type);
            default:
                break;
        }

        return typeName;
    }

    public static Class getMapping(String typeName) {
        return TYPE_MAPPINGS.get(typeName);
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null || value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return new BigDecimal(str);
        }

        throw new JSONException("can not cast to decimal from " + value.getClass());
    }

    public static BigInteger toBigInteger(Object value) {
        if (value == null || value instanceof BigInteger) {
            return (BigInteger) value;
        }

        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return BigInteger.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return new BigInteger(str);
        }

        throw new JSONException("can not cast to bigint");
    }

    public static Long toLong(Object value) {
        if (value == null || value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return Long.valueOf(((Number) value).longValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Long.parseLong(str);
        }

        throw new JSONException("can not cast to long, class " + value.getClass());
    }

    public static long toLongValue(Object value) {
        if (value == null) {
            return 0L;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Long.parseLong(str);
        }

        throw new JSONException("can not cast to long");
    }

    public static Integer toInteger(Object value) {
        if (value == null || value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Integer.parseInt(str);
        }

        if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
            return null;
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? 1 : 0;
        }

        throw new JSONException("can not cast to integer");
    }

    public static Byte toByte(Object value) {
        if (value == null || value instanceof Byte) {
            return (Byte) value;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Byte.parseByte(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static byte toByteValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Byte) {
            return (Byte) value;
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Byte.parseByte(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static Short toShort(Object value) {
        if (value == null || value instanceof Short) {
            return (Short) value;
        }

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Short.parseShort(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static short toShortValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Short) {
            return (Short) value;
        }

        if (value instanceof Number) {
            return (byte) ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Short.parseShort(str);
        }

        throw new JSONException("can not cast to byte");
    }

    public static int toIntValue(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }

            if (str.indexOf('.') != -1) {
                return new BigDecimal(str).intValueExact();
            }

            return Integer.parseInt(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static boolean toBooleanValue(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return false;
            }
            return Boolean.parseBoolean(str);
        }

        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (intValue == 1) {
                return true;
            }
            if (intValue == 0) {
                return false;
            }
        }

        throw new JSONException("can not cast to boolean");
    }

    public static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Boolean.parseBoolean(str);
        }

        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (intValue == 1) {
                return true;
            }
            if (intValue == 0) {
                return false;
            }
        }

        throw new JSONException("can not cast to boolean");
    }

    public static float toFloatValue(Object value) {
        if (value == null) {
            return 0F;
        }

        if (value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0;
            }
            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static Float toFloat(Object value) {
        if (value == null || value instanceof Float) {
            return (Float) value;
        }

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Float.parseFloat(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static double toDoubleValue(Object value) {
        if (value == null) {
            return 0D;
        }

        if (value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return 0D;
            }
            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static Double toDouble(Object value) {
        if (value == null || value instanceof Double) {
            return (Double) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty() || "null".equals(str)) {
                return null;
            }
            return Double.parseDouble(str);
        }

        throw new JSONException("can not cast to decimal");
    }

    public static int compare(Object a, Object b) {
        if (a.getClass() == b.getClass()) {
            return ((Comparable) a).compareTo(b);
        }

        Class typeA = a.getClass();
        Class typeB = b.getClass();

        if (typeA == BigDecimal.class) {
            if (typeB == Integer.class) {
                b = new BigDecimal((Integer) b);
            } else if (typeB == Long.class) {
                b = new BigDecimal((Long) b);
            } else if (typeB == Float.class) {
                b = new BigDecimal((Float) b);
            } else if (typeB == Double.class) {
                b = new BigDecimal((Double) b);
            } else if (typeB == BigInteger.class) {
                b = new BigDecimal((BigInteger) b);
            }
        } else if (typeA == BigInteger.class) {
            if (typeB == Integer.class) {
                b = BigInteger.valueOf((Integer) b);
            } else if (typeB == Long.class) {
                b = BigInteger.valueOf((Long) b);
            } else if (typeB == Float.class) {
                b = new BigDecimal((Float) b);
                a = new BigDecimal((BigInteger) a);
            } else if (typeB == Double.class) {
                b = new BigDecimal((Double) b);
                a = new BigDecimal((BigInteger) a);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal((BigInteger) a);
            }
        } else if (typeA == Long.class) {
            if (typeB == Integer.class) {
                b = new Long((Integer) b);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal((Long) a);
            } else if (typeB == Float.class) {
                a = new Float((Long) a);
            } else if (typeB == Double.class) {
                a = new Double((Long) a);
            } else if (typeB == BigInteger.class) {
                a = BigInteger.valueOf((Long) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Long) a);
                b = new BigDecimal((String) b);
            }
        } else if (typeA == Integer.class) {
            if (typeB == Long.class) {
                a = new Long((Integer) a);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal((Integer) a);
            } else if (typeB == BigInteger.class) {
                a = BigInteger.valueOf((Integer) a);
            } else if (typeB == Float.class) {
                a = new Float((Integer) a);
            } else if (typeB == Double.class) {
                a = new Double((Integer) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Integer) a);
                b = new BigDecimal((String) b);
            }
        } else if (typeA == Double.class) {
            if (typeB == Integer.class) {
                b = new Double((Integer) b);
            } else if (typeB == Long.class) {
                b = new Double((Long) b);
            } else if (typeB == Float.class) {
                b = new Double((Float) b);
            } else if (typeB == BigDecimal.class) {
                a = BigDecimal.valueOf((Double) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Double) a);
                b = new BigDecimal((String) b);
            } else if (typeB == BigInteger.class) {
                a = BigDecimal.valueOf((Double) a);
                b = new BigDecimal((BigInteger) b);
            }
        } else if (typeA == Float.class) {
            if (typeB == Integer.class) {
                b = new Float((Integer) b);
            } else if (typeB == Long.class) {
                b = new Float((Long) b);
            } else if (typeB == Double.class) {
                a = new Double((Float) a);
            } else if (typeB == BigDecimal.class) {
                a = BigDecimal.valueOf((Float) a);
            } else if (typeB == String.class) {
                a = BigDecimal.valueOf((Float) a);
                b = new BigDecimal((String) b);
            } else if (typeB == BigInteger.class) {
                a = BigDecimal.valueOf((Float) a);
                b = new BigDecimal((BigInteger) b);
            }
        } else if (typeA == String.class) {
            String strA = (String) a;
            if (typeB == Integer.class) {
                NumberFormatException error = null;
                try {
                    a = Integer.parseInt(strA);
                } catch (NumberFormatException ex) {
                    error = ex;
                }
                if (error != null) {
                    try {
                        a = Long.parseLong(strA);
                        b = Long.valueOf((Integer) b);
                        error = null;
                    } catch (NumberFormatException ex) {
                        error = ex;
                    }
                }
                if (error != null) {
                    a = new BigDecimal(strA);
                    b = BigDecimal.valueOf((Integer) b);
                }
            } else if (typeB == Long.class) {
                a = new BigDecimal(strA);
                b = BigDecimal.valueOf((Long) b);
            } else if (typeB == Float.class) {
                a = Float.parseFloat(strA);
            } else if (typeB == Double.class) {
                a = Double.parseDouble(strA);
            } else if (typeB == BigInteger.class) {
                a = new BigInteger(strA);
            } else if (typeB == BigDecimal.class) {
                a = new BigDecimal(strA);
            }
        }

        return ((Comparable) a).compareTo(b);
    }

    public static Object getDefaultValue(Type paramType) {
        if (paramType == int.class) {
            return 0;
        }

        if (paramType == long.class) {
            return 0L;
        }

        if (paramType == float.class) {
            return 0F;
        }

        if (paramType == double.class) {
            return 0D;
        }

        if (paramType == boolean.class) {
            return Boolean.FALSE;
        }

        if (paramType == short.class) {
            return (short) 0;
        }

        if (paramType == byte.class) {
            return (byte) 0;
        }

        if (paramType == char.class) {
            return (char) 0;
        }

        if (paramType == Optional.class) {
            return Optional.empty();
        }

        if (paramType == OptionalInt.class) {
            return OptionalInt.empty();
        }

        if (paramType == OptionalLong.class) {
            return OptionalLong.empty();
        }

        if (paramType == OptionalDouble.class) {
            return OptionalDouble.empty();
        }

        return null;
    }

    public static Class loadClass(String className) {
        if (className.length() >= 192) {
            return null;
        }

        switch (className) {
            case "O":
            case "Object":
            case "java.lang.Object":
                return Object.class;
            case "class java.util.Collections$EmptyMap":
                return Collections.EMPTY_MAP.getClass();
            case "java.util.Collections$EmptyList":
                return Collections.EMPTY_LIST.getClass();
            case "java.util.Collections$EmptySet":
                return Collections.EMPTY_SET.getClass();
            case "java.util.Optional":
                return Optional.class;
            case "java.util.OptionalInt":
                return OptionalInt.class;
            case "java.util.OptionalLong":
                return OptionalLong.class;
            case "List":
            case "java.util.List":
                return List.class;
            case "java.util.ArrayList":
                return ArrayList.class;
            case "Map":
            case "java.util.Map":
                return Map.class;
            case "HashMap":
            case "java.util.HashMap":
                return HashMap.class;
            case "Set":
            case "java.util.Set":
                return Set.class;
            case "HashSet":
            case "java.util.HashSet":
                return HashSet.class;
            case "java.lang.Class":
                return Class.class;
            case "java.lang.Integer":
                return Integer.class;
            case "java.lang.Long":
                return Long.class;
            case "String":
            case "java.lang.String":
                return String.class;
            case "[String":
                return String[].class;
            case "I":
            case "int":
                return int.class;
            case "S":
            case "short":
                return short.class;
            case "J":
            case "long":
                return long.class;
            case "Z":
            case "boolean":
                return boolean.class;
            case "B":
            case "byte":
                return byte.class;
            case "F":
            case "float":
                return float.class;
            case "D":
            case "double":
                return double.class;
            case "C":
            case "char":
                return char.class;
            case "[B":
            case "byte[]":
                return byte[].class;
            case "[S":
            case "short[]":
                return short[].class;
            case "[I":
            case "int[]":
                return int[].class;
            case "[J":
            case "long[]":
                return long[].class;
            case "[F":
            case "float[]":
                return float[].class;
            case "[D":
            case "double[]":
                return double[].class;
            case "[C":
            case "char[]":
                return char[].class;
            case "[Z":
            case "boolean[]":
                return boolean[].class;
            case "[O":
                return Object[].class;
            case "java.io.IOException":
                return java.io.IOException.class;
            case "java.util.Collections$UnmodifiableRandomAccessList":
                return CLASS_UNMODIFIABLE_LIST;
            case "java.util.Collections$SingletonSet":
                return CLASS_SINGLE_SET;
            default:
                break;
        }

        if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
            className = className.substring(1, className.length() - 1);
        }

        if (className.charAt(0) == '[' || className.endsWith("[]")) {
            String itemClassName = className.charAt(0) == '[' ? className.substring(1) : className.substring(0, className.length() - 2);
            Class itemClass = loadClass(itemClassName);
            if (itemClass == null) {
                throw new JSONException("load class error " + className);
            }
            return Array.newInstance(itemClass, 0).getClass();
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            try {
                return contextClassLoader.loadClass(className);
            } catch (ClassNotFoundException ignored) {
            }
        }

        try {
            return JSON.class.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException ignored) {
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
        }

        return null;
    }

    public static Class<?> getArrayClass(Class componentClass) {
        return Array.newInstance(componentClass, 1).getClass();
    }

    public static Class<?> getClass(Type type) {
        if (type == null) {
            return null;
        }

        if (type.getClass() == Class.class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        }

        if (type instanceof TypeVariable) {
            Type boundType = ((TypeVariable<?>) type).getBounds()[0];
            if (boundType instanceof Class) {
                return (Class) boundType;
            }
            return getClass(boundType);
        }

        if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClass(upperBounds[0]);
            }
        }

        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type componentType = genericArrayType.getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            return getArrayClass(componentClass);
        }

        return Object.class;
    }

    public static boolean isProxy(Class<?> clazz) {
        for (Class<?> item : clazz.getInterfaces()) {
            String interfaceName = item.getName();
            switch (interfaceName) {
                case "org.springframework.cglib.proxy.Factory":
                case "javassist.util.proxy.ProxyObject":
                case "org.apache.ibatis.javassist.util.proxy.ProxyObject":
                case "org.hibernate.proxy.HibernateProxy":
                case "org.springframework.context.annotation.ConfigurationClassEnhancer$EnhancedConfiguration":
                case "org.mockito.cglib.proxy.Factory":
                    return true;
                default:
                    break;
            }
        }
        return false;
    }
}
