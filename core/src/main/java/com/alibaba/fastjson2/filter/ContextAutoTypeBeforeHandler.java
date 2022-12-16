package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static com.alibaba.fastjson2.util.Fnv.MAGIC_HASH_CODE;
import static com.alibaba.fastjson2.util.Fnv.MAGIC_PRIME;
import static com.alibaba.fastjson2.util.TypeUtils.*;

public class ContextAutoTypeBeforeHandler
        implements JSONReader.AutoTypeBeforeHandler {
    static final Class[] BASIC_TYPES = {
            Object.class,
            byte.class,
            Byte.class,
            short.class,
            Short.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            float.class,
            Float.class,
            double.class,
            Double.class,

            Number.class,
            BigInteger.class,
            BigDecimal.class,

            AtomicInteger.class,
            AtomicLong.class,
            AtomicBoolean.class,
            AtomicIntegerArray.class,
            AtomicLongArray.class,
            AtomicReference.class,

            boolean.class,
            Boolean.class,
            char.class,
            Character.class,

            String.class,
            UUID.class,
            Currency.class,
            BitSet.class,
            EnumSet.class,

            Date.class,
            Calendar.class,
            LocalTime.class,
            LocalDate.class,
            LocalDateTime.class,
            Instant.class,
            SimpleDateFormat.class,
            DateTimeFormatter.class,
            TimeUnit.class,

            Set.class,
            HashSet.class,
            LinkedHashSet.class,
            TreeSet.class,
            List.class,
            ArrayList.class,
            LinkedList.class,
            ConcurrentLinkedQueue.class,
            ConcurrentSkipListSet.class,
            CopyOnWriteArrayList.class,

            Collections.emptyList().getClass(),
            Collections.emptyMap().getClass(),
            CLASS_SINGLE_SET,
            CLASS_UNMODIFIABLE_COLLECTION,
            CLASS_UNMODIFIABLE_LIST,
            CLASS_UNMODIFIABLE_SET,
            CLASS_UNMODIFIABLE_SORTED_SET,
            CLASS_UNMODIFIABLE_NAVIGABLE_SET,
            Collections.unmodifiableMap(new HashMap<>()).getClass(),
            Collections.unmodifiableNavigableMap(new TreeMap<>()).getClass(),
            Collections.unmodifiableSortedMap(new TreeMap<>()).getClass(),

            Map.class,
            HashMap.class,
            Hashtable.class,
            TreeMap.class,
            LinkedHashMap.class,
            WeakHashMap.class,
            IdentityHashMap.class,
            ConcurrentMap.class,
            ConcurrentHashMap.class,
            ConcurrentSkipListMap.class,

            Exception.class,
            IllegalAccessError.class,
            IllegalAccessException.class,
            IllegalArgumentException.class,
            IllegalMonitorStateException.class,
            IllegalStateException.class,
            IllegalThreadStateException.class,
            IndexOutOfBoundsException.class,
            InstantiationError.class,
            InstantiationException.class,
            InternalError.class,
            InterruptedException.class,
            LinkageError.class,
            NegativeArraySizeException.class,
            NoClassDefFoundError.class,
            NoSuchFieldError.class,
            NoSuchFieldException.class,
            NoSuchMethodError.class,
            NoSuchMethodException.class,
            NullPointerException.class,
            NumberFormatException.class,
            OutOfMemoryError.class,
            RuntimeException.class,
            SecurityException.class,
            StackOverflowError.class,
            StringIndexOutOfBoundsException.class,
            TypeNotPresentException.class,
            VerifyError.class,
            StackTraceElement.class,
    };

    final long[] acceptHashCodes;
    final ConcurrentMap<Integer, ConcurrentHashMap<Long, Class>> tclHashCaches = new ConcurrentHashMap<>();
    final Map<Long, Class> classCache = new ConcurrentHashMap<>(16, 0.75f, 1);

    public ContextAutoTypeBeforeHandler(Class... types) {
        this(false, types);
    }

    public ContextAutoTypeBeforeHandler(boolean includeBasic, Class... types) {
        this(
                includeBasic,
                names(
                        Arrays.asList(types)
                )
        );
    }

    public ContextAutoTypeBeforeHandler(String... acceptNames) {
        this(false, acceptNames);
    }

    public ContextAutoTypeBeforeHandler(boolean includeBasic) {
        this(includeBasic, new String[0]);
    }

    static String[] names(Collection<Class> types) {
        Set<String> nameSet = new HashSet<>();
        for (Class type : types) {
            if (type == null) {
                continue;
            }

            String name = TypeUtils.getTypeName(type);
            nameSet.add(name);
        }
        return nameSet.toArray(new String[nameSet.size()]);
    }

    public ContextAutoTypeBeforeHandler(boolean includeBasic, String... acceptNames) {
        Set<String> nameSet = new HashSet<>();
        if (includeBasic) {
            for (Class basicType : BASIC_TYPES) {
                String name = TypeUtils.getTypeName(basicType);
                nameSet.add(name);
            }
        }

        for (String name : acceptNames) {
            if (name == null || name.isEmpty()) {
                continue;
            }

            Class mapping = TypeUtils.getMapping(name);
            if (mapping != null) {
                name = TypeUtils.getTypeName(mapping);
            }
            nameSet.add(name);
        }

        long[] array = new long[nameSet.size()];

        int index = 0;
        for (String name : nameSet) {
            long hashCode = MAGIC_HASH_CODE;
            for (int j = 0; j < name.length(); ++j) {
                char ch = name.charAt(j);
                if (ch == '$') {
                    ch = '.';
                }
                hashCode ^= ch;
                hashCode *= MAGIC_PRIME;
            }

            array[index++] = hashCode;
        }

        if (index != array.length) {
            array = Arrays.copyOf(array, index);
        }
        Arrays.sort(array);
        this.acceptHashCodes = array;
    }

    public Class<?> apply(long typeNameHash, Class<?> expectClass, long features) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl != null && tcl != JSON.class.getClassLoader()) {
            int tclHash = System.identityHashCode(tcl);
            ConcurrentHashMap<Long, Class> tclHashCache = tclHashCaches.get(tclHash);
            if (tclHashCache != null) {
                return tclHashCache.get(typeNameHash);
            }
        }

        return classCache.get(typeNameHash);
    }

    @Override
    public Class<?> apply(String typeName, Class<?> expectClass, long features) {
        if ("O".equals(typeName)) {
            typeName = "Object";
        }

        long hash = MAGIC_HASH_CODE;
        for (int i = 0, typeNameLength = typeName.length(); i < typeNameLength; ++i) {
            char ch = typeName.charAt(i);
            if (ch == '$') {
                ch = '.';
            }
            hash ^= ch;
            hash *= MAGIC_PRIME;

            if (Arrays.binarySearch(acceptHashCodes, hash) >= 0) {
                long typeNameHash = Fnv.hashCode64(typeName);
                Class clazz = apply(typeNameHash, expectClass, features);

                if (clazz == null) {
                    clazz = loadClass(typeName);
                    if (clazz != null) {
                        Class origin = putCacheIfAbsent(typeNameHash, clazz);
                        if (origin != null) {
                            clazz = origin;
                        }
                    }
                }

                if (clazz != null) {
                    return clazz;
                }
            }
        }

        long typeNameHash = Fnv.hashCode64(typeName);

        if (typeName.length() > 0
                && typeName.charAt(0) == '[') {
            Class clazz = apply(typeNameHash, expectClass, features);
            if (clazz != null) {
                return clazz;
            }

            String itemTypeName = typeName.substring(1);
            Class itemExpectClass = null;
            if (expectClass != null) {
                itemExpectClass = expectClass.getComponentType();
            }
            Class itemType = apply(itemTypeName, itemExpectClass, features);
            if (itemType != null) {
                Class arrayType;
                if (itemType == itemExpectClass) {
                    arrayType = expectClass;
                } else {
                    arrayType = TypeUtils.getArrayClass(itemType);
                }
                Class origin = putCacheIfAbsent(typeNameHash, arrayType);
                if (origin != null) {
                    arrayType = origin;
                }
                return arrayType;
            }
        }

        Class mapping = TypeUtils.getMapping(typeName);
        if (mapping != null) {
            String mappingTypeName = TypeUtils.getTypeName(mapping);
            if (!typeName.equals(mappingTypeName)) {
                Class<?> mappingClass = apply(mappingTypeName, expectClass, features);
                if (mappingClass != null) {
                    putCacheIfAbsent(typeNameHash, mappingClass);
                }
                return mappingClass;
            }
        }

        return null;
    }

    private Class putCacheIfAbsent(long typeNameHash, Class type) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl != null && tcl != JSON.class.getClassLoader()) {
            int tclHash = System.identityHashCode(tcl);
            ConcurrentHashMap<Long, Class> tclHashCache = tclHashCaches.get(tclHash);
            if (tclHashCache == null) {
                tclHashCaches.putIfAbsent(tclHash, new ConcurrentHashMap<>());
                tclHashCache = tclHashCaches.get(tclHash);
            }

            return tclHashCache.putIfAbsent(typeNameHash, type);
        }
        return classCache.putIfAbsent(typeNameHash, type);
    }
}
