/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public class TypeUtils {
    static final long FNV1A_64_MAGIC_HASHCODE = 0xcbf29ce484222325L;
    static final long FNV1A_64_MAGIC_PRIME = 0x100000001b3L;

    private static boolean setAccessibleEnable = true;
    public static boolean compatibleWithJavaBean;
    public static boolean compatibleWithFieldName;

    private static volatile Class kotlin_metadata;
    private static volatile boolean kotlin_metadata_error;
    private static volatile boolean kotlin_class_klass_error;
    private static volatile Constructor kotlin_kclass_constructor;
    private static volatile Method kotlin_kclass_getConstructors;
    private static volatile Method kotlin_kfunction_getParameters;
    private static volatile Method kotlin_kparameter_getName;
    private static volatile boolean kotlin_error;
    private static volatile Map<Class, String[]> kotlinIgnores;
    private static volatile boolean kotlinIgnores_error;
    private static ConcurrentMap<String, Class<?>> mappings = new ConcurrentHashMap<String, Class<?>>(256, 0.75f, 1);
    private static Class<?> pathClass;
    private static boolean PATH_CLASS_ERROR;

    private static boolean transientClassInited;
    private static Class<? extends Annotation> transientClass;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T cast(Object obj, Class<T> clazz, ParserConfig config) {
        return com.alibaba.fastjson2.util.TypeUtils.cast(obj, clazz, config.getProvider());
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Type type, ParserConfig mapping) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof String) {
            String strVal = (String) obj;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
        }

        if (type instanceof Class) {
            return cast(obj, (Class<T>) type, mapping);
        }

        if (type instanceof ParameterizedType) {
            return cast(obj, (ParameterizedType) type, mapping);
        }

        if (type instanceof TypeVariable) {
            return (T) obj;
        }
        throw new JSONException("can not cast to : " + type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T cast(Object obj, ParameterizedType type, ParserConfig mapping) {
        Type rawTye = type.getRawType();

        if (rawTye == List.class || rawTye == ArrayList.class) {
            Type itemType = type.getActualTypeArguments()[0];
            if (obj instanceof List) {
                List listObj = (List) obj;
                List arrayList = new ArrayList(listObj.size());

                for (int i = 0; i < listObj.size(); i++) {
                    Object item = listObj.get(i);

                    Object itemValue;
                    if (itemType instanceof Class) {
                        if (item != null && item.getClass() == JSONObject.class) {
                            itemValue = ((JSONObject) item).toJavaObject((Class<T>) itemType, mapping, 0);
                        } else {
                            itemValue = cast(item, (Class<T>) itemType, mapping);
                        }
                    } else {
                        itemValue = cast(item, itemType, mapping);
                    }

                    arrayList.add(itemValue);
                }
                return (T) arrayList;
            }
        }

        if (rawTye == Set.class || rawTye == HashSet.class //
                || rawTye == TreeSet.class //
                || rawTye == Collection.class //
                || rawTye == List.class //
                || rawTye == ArrayList.class) {
            Type itemType = type.getActualTypeArguments()[0];
            if (obj instanceof Iterable) {
                Collection collection;
                if (rawTye == Set.class || rawTye == HashSet.class) {
                    collection = new HashSet();
                } else if (rawTye == TreeSet.class) {
                    collection = new TreeSet();
                } else {
                    collection = new ArrayList();
                }
                for (Iterator it = ((Iterable) obj).iterator(); it.hasNext(); ) {
                    Object item = it.next();

                    Object itemValue;
                    if (itemType instanceof Class) {
                        if (item != null && item.getClass() == JSONObject.class) {
                            itemValue = ((JSONObject) item).toJavaObject((Class<T>) itemType, mapping, 0);
                        } else {
                            itemValue = cast(item, (Class<T>) itemType, mapping);
                        }
                    } else {
                        itemValue = cast(item, itemType, mapping);
                    }

                    collection.add(itemValue);
                }
                return (T) collection;
            }
        }

        if (rawTye == Map.class || rawTye == HashMap.class) {
            Type keyType = type.getActualTypeArguments()[0];
            Type valueType = type.getActualTypeArguments()[1];
            if (obj instanceof Map) {
                Map map = new HashMap();
                for (Map.Entry entry : ((Map<?, ?>) obj).entrySet()) {
                    Object key = cast(entry.getKey(), keyType, mapping);
                    Object value = cast(entry.getValue(), valueType, mapping);
                    map.put(key, value);
                }
                return (T) map;
            }
        }
        if (obj instanceof String) {
            String strVal = (String) obj;
            if (strVal.length() == 0) {
                return null;
            }
        }
        if (type.getActualTypeArguments().length == 1) {
            Type argType = type.getActualTypeArguments()[0];
            if (argType instanceof WildcardType) {
                return cast(obj, rawTye, mapping);
            }
        }

        if (rawTye == Map.Entry.class && obj instanceof Map && ((Map) obj).size() == 1) {
            Map.Entry entry = (Map.Entry) ((Map) obj).entrySet().iterator().next();
            return (T) entry;
        }

        if (rawTye instanceof Class) {
            if (mapping == null) {
                mapping = ParserConfig.global;
            }
//            ObjectDeserializer deserializer = mapping.getDeserializer(rawTye);
//            if (deserializer != null) {
//                String str = JSON.toJSONString(obj);
//                DefaultJSONParser parser = new DefaultJSONParser(str, mapping);
//                return (T) deserializer.deserialze(parser, type, null);
//            }

            throw new JSONException("TODO : " + type); // TOD: cast
        }

        throw new JSONException("can not cast to : " + type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T castToJavaBean(Map<String, Object> map, Class<T> clazz, ParserConfig config) {
        try {
            if (clazz == StackTraceElement.class) {
                String declaringClass = (String) map.get("className");
                String methodName = (String) map.get("methodName");
                String fileName = (String) map.get("fileName");
                int lineNumber;
                {
                    Number value = (Number) map.get("lineNumber");
                    if (value == null) {
                        lineNumber = 0;
                    } else if (value instanceof BigDecimal) {
                        lineNumber = ((BigDecimal) value).intValueExact();
                    } else {
                        lineNumber = value.intValue();
                    }
                }
                return (T) new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
            }

            {
                Object iClassObject = map.get(JSON.DEFAULT_TYPE_KEY);
                if (iClassObject instanceof String) {
                    String className = (String) iClassObject;
                    Class<?> loadClazz;
                    if (config == null) {
                        config = ParserConfig.global;
                    }
//                    loadClazz = config.checkAutoType(className, null);
//                    if(loadClazz == null){
//                        throw new ClassNotFoundException(className + " not found");
//                    }
//                    if(!loadClazz.equals(clazz)){
//                        return (T) castToJavaBean(map, loadClazz, config);
//                    }
                    throw new JSONException("TODO"); // TODO : castToJavaBean
                }
            }

            if (clazz.isInterface()) {
                JSONObject object;
                if (map instanceof JSONObject) {
                    object = (JSONObject) map;
                } else {
                    object = new JSONObject(map);
                }
                if (config == null) {
                    config = ParserConfig.getGlobalInstance();
                }
//                ObjectDeserializer deserializer = config.getDeserializers().get(clazz);
//                if(deserializer != null){
//                    String json = JSON.toJSONString(object);
//                    return (T) JSON.parseObject(json, clazz);
//                }
//                return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
//                        new Class<?>[]{clazz}, object);
                throw new JSONException("TODO"); // TODO : castToJavaBean
            }

            if (clazz == Locale.class) {
                Object arg0 = map.get("language");
                Object arg1 = map.get("country");
                if (arg0 instanceof String) {
                    String language = (String) arg0;
                    if (arg1 instanceof String) {
                        String country = (String) arg1;
                        return (T) new Locale(language, country);
                    } else if (arg1 == null) {
                        return (T) new Locale(language);
                    }
                }
            }

            if (clazz == String.class && map instanceof JSONObject) {
                return (T) map.toString();
            }

            if (clazz == LinkedHashMap.class && map instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) map;
                Map innerMap = jsonObject.getInnerMap();
                if (innerMap instanceof LinkedHashMap) {
                    return (T) innerMap;
                } else {
                    LinkedHashMap linkedHashMap = new LinkedHashMap();
                    linkedHashMap.putAll(innerMap);
                }
            }

            ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(clazz);
            return (T) objectReader.createInstance(map, 0L);
        } catch (Exception e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public static Type checkPrimitiveArray(GenericArrayType genericArrayType) {
        Type clz = genericArrayType;
        Type genericComponentType = genericArrayType.getGenericComponentType();

        String prefix = "[";
        while (genericComponentType instanceof GenericArrayType) {
            genericComponentType = ((GenericArrayType) genericComponentType)
                    .getGenericComponentType();
            prefix += prefix;
        }

        if (genericComponentType instanceof Class<?>) {
            Class<?> ck = (Class<?>) genericComponentType;
            if (ck.isPrimitive()) {
                try {
                    if (ck == boolean.class) {
                        clz = Class.forName(prefix + "Z");
                    } else if (ck == char.class) {
                        clz = Class.forName(prefix + "C");
                    } else if (ck == byte.class) {
                        clz = Class.forName(prefix + "B");
                    } else if (ck == short.class) {
                        clz = Class.forName(prefix + "S");
                    } else if (ck == int.class) {
                        clz = Class.forName(prefix + "I");
                    } else if (ck == long.class) {
                        clz = Class.forName(prefix + "J");
                    } else if (ck == float.class) {
                        clz = Class.forName(prefix + "F");
                    } else if (ck == double.class) {
                        clz = Class.forName(prefix + "D");
                    }
                } catch (ClassNotFoundException e) {
                }
            }
        }

        return clz;
    }

    public static boolean isProxy(Class<?> clazz) {
        return com.alibaba.fastjson2.util.TypeUtils.isProxy(clazz);
    }

    public static boolean isGenericParamType(Type type) {
        if (type instanceof ParameterizedType) {
            return true;
        }
        if (type instanceof Class) {
            Type superType = ((Class<?>) type).getGenericSuperclass();
            return superType != Object.class && isGenericParamType(superType);
        }
        return false;
    }

    public static Type getGenericParamType(Type type) {
        if (type instanceof ParameterizedType) {
            return type;
        }
        if (type instanceof Class) {
            return getGenericParamType(((Class<?>) type).getGenericSuperclass());
        }
        return type;
    }

    public static boolean isTransient(Method method) {
        if (method == null) {
            return false;
        }
        if (!transientClassInited) {
            try {
                transientClass = (Class<? extends Annotation>) Class.forName("java.beans.Transient");
            } catch (Exception e) {
                // skip
            } finally {
                transientClassInited = true;
            }
        }
        if (transientClass != null) {
            Annotation annotation = TypeUtils.getAnnotation(method, transientClass);
            return annotation != null;
        }
        return false;
    }

    public static String castToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static long fnv1a_64_lower(String key) {
        long hashCode = FNV1A_64_MAGIC_HASHCODE;
        for (int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + 32);
            }
            hashCode ^= ch;
            hashCode *= FNV1A_64_MAGIC_PRIME;
        }
        return hashCode;
    }

    public static long fnv1a_64(String key) {
        long hashCode = FNV1A_64_MAGIC_HASHCODE;
        for (int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            hashCode ^= ch;
            hashCode *= FNV1A_64_MAGIC_PRIME;
        }
        return hashCode;
    }

    public static long fnv1a_64_extract(String key) {
        long hashCode = FNV1A_64_MAGIC_HASHCODE;
        for (int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            if (ch == '_' || ch == '-') {
                continue;
            }
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + 32);
            }
            hashCode ^= ch;
            hashCode *= FNV1A_64_MAGIC_PRIME;
        }
        return hashCode;
    }

    public static Long castToLong(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toLong(value);
    }

    public static Integer castToInt(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toInteger(value);
    }

    public static Boolean castToBoolean(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toBoolean(value);
    }

    public static long longExtractValue(Number number) {
        if (number == null) {
            return 0;
        }

        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).longValueExact();
        }

        return number.longValue();
    }

    public static <A extends Annotation> A getAnnotation(Class<?> targetClass, Class<A> annotationClass) {
        A targetAnnotation = targetClass.getAnnotation(annotationClass);

        Class<?> mixInClass = null;
        Type type = JSON.getMixInAnnotations(targetClass);
        if (type instanceof Class<?>) {
            mixInClass = (Class<?>) type;
        }

        if (mixInClass != null) {
            A mixInAnnotation = mixInClass.getAnnotation(annotationClass);
            Annotation[] annotations = mixInClass.getAnnotations();
            if (mixInAnnotation == null && annotations.length > 0) {
                for (Annotation annotation : annotations) {
                    mixInAnnotation = annotation.annotationType().getAnnotation(annotationClass);
                    if (mixInAnnotation != null) {
                        break;
                    }
                }
            }
            if (mixInAnnotation != null) {
                return mixInAnnotation;
            }
        }

        Annotation[] targetClassAnnotations = targetClass.getAnnotations();
        if (targetAnnotation == null && targetClassAnnotations.length > 0) {
            for (Annotation annotation : targetClassAnnotations) {
                targetAnnotation = annotation.annotationType().getAnnotation(annotationClass);
                if (targetAnnotation != null) {
                    break;
                }
            }
        }
        return targetAnnotation;
    }

    public static <A extends Annotation> A getAnnotation(Field field, Class<A> annotationClass) {
        A targetAnnotation = field.getAnnotation(annotationClass);

        Class<?> clazz = field.getDeclaringClass();
        A mixInAnnotation;
        Class<?> mixInClass = null;
        Type type = JSON.getMixInAnnotations(clazz);
        if (type instanceof Class<?>) {
            mixInClass = (Class<?>) type;
        }

        if (mixInClass != null) {
            Field mixInField = null;
            String fieldName = field.getName();
            // 递归从MixIn类的父类中查找注解（如果有父类的话）
            for (Class<?> currClass = mixInClass; currClass != null && currClass != Object.class; currClass = currClass.getSuperclass()) {
                try {
                    mixInField = currClass.getDeclaredField(fieldName);
                    break;
                } catch (NoSuchFieldException e) {
                    // skip
                }
            }
            if (mixInField == null) {
                return targetAnnotation;
            }
            mixInAnnotation = mixInField.getAnnotation(annotationClass);
            if (mixInAnnotation != null) {
                return mixInAnnotation;
            }
        }
        return targetAnnotation;
    }

    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationClass) {
        A targetAnnotation = method.getAnnotation(annotationClass);

        Class<?> clazz = method.getDeclaringClass();
        A mixInAnnotation;
        Class<?> mixInClass = null;
        Type type = JSON.getMixInAnnotations(clazz);
        if (type instanceof Class<?>) {
            mixInClass = (Class<?>) type;
        }

        if (mixInClass != null) {
            Method mixInMethod = null;
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            // 递归从MixIn类的父类中查找注解（如果有父类的话）
            for (Class<?> currClass = mixInClass; currClass != null && currClass != Object.class; currClass = currClass.getSuperclass()) {
                try {
                    mixInMethod = currClass.getDeclaredMethod(methodName, parameterTypes);
                    break;
                } catch (NoSuchMethodException e) {
                    // skip
                }
            }
            if (mixInMethod == null) {
                return targetAnnotation;
            }
            mixInAnnotation = mixInMethod.getAnnotation(annotationClass);
            if (mixInAnnotation != null) {
                return mixInAnnotation;
            }
        }
        return targetAnnotation;
    }

    public static Double castToDouble(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toDouble(value);
    }

    public static <T> T castToJavaBean(Object obj, Class<T> clazz) {
        return com.alibaba.fastjson2.util.TypeUtils.cast(obj, clazz);
    }

    public static Class<?> getClass(Type type) {
        return com.alibaba.fastjson2.util.TypeUtils.getClass(type);
    }

    public static BigDecimal castToBigDecimal(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toBigDecimal(value);
    }

    public static Timestamp castToTimestamp(final Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.cast(value, Timestamp.class);
    }

    public static java.sql.Date castToSqlDate(final Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.cast(value, java.sql.Date.class);
    }

    public static byte byteValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.byteValue();
        }

        return decimal.byteValueExact();
    }

    public static short shortValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.shortValue();
        }

        return decimal.shortValueExact();
    }

    public static int intValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.intValue();
        }

        return decimal.intValueExact();
    }

    public static long longValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.longValue();
        }

        return decimal.longValueExact();
    }

    public static Character castToChar(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Character) {
            return (Character) value;
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0) {
                return null;
            }
            if (strVal.length() != 1) {
                throw new JSONException("can not cast to char, value : " + value);
            }
            return strVal.charAt(0);
        }
        throw new JSONException("can not cast to char, value : " + value);
    }

    public static Short castToShort(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toShort(value);
    }

    public static Byte castToByte(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toByte(value);
    }

    public static Float castToFloat(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toFloat(value);
    }

    public static Date castToDate(Object value) {
        return com.alibaba.fastjson2.util.TypeUtils.toDate(value);
    }

    public static byte[] castToBytes(Object value) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        if (value instanceof String) {
            return IOUtils.decodeBase64((String) value);
        }
        throw new JSONException("can not cast to byte[], value : " + value);
    }

    public static List<FieldInfo> computeGetters(Class<?> clazz, Map<String, String> aliasMap) {
        return computeGetters(clazz, aliasMap, true);
    }

    public static List<FieldInfo> computeGetters(Class<?> clazz, Map<String, String> aliasMap, boolean sorted) {
        JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
        Map<String, Field> fieldCacheMap = new HashMap<String, Field>();
        ParserConfig.parserAllFieldToCache(clazz, fieldCacheMap);
        return computeGetters(clazz, jsonType, aliasMap, fieldCacheMap, sorted, PropertyNamingStrategy.CamelCase);
    }

    public static List<FieldInfo> computeGetters(Class<?> clazz, //
                                                 JSONType jsonType, //
                                                 Map<String, String> aliasMap, //
                                                 Map<String, Field> fieldCacheMap, //
                                                 boolean sorted, //
                                                 PropertyNamingStrategy propertyNamingStrategy //
    ) {
        Map<String, FieldInfo> fieldInfoMap = new LinkedHashMap<String, FieldInfo>();
        boolean kotlin = TypeUtils.isKotlin(clazz);
        // for kotlin
        Constructor[] constructors = null;
        Annotation[][] paramAnnotationArrays = null;
        String[] paramNames = null;
        short[] paramNameMapping = null;
        Method[] methods = clazz.getMethods();
        try {
            Arrays.sort(methods, new MethodInheritanceComparator());
        } catch (Throwable ignored) {
            // ignored
        }

        for (Method method : methods) {
            String methodName = method.getName();
            int ordinal = 0, serialzeFeatures = 0, parserFeatures = 0;
            String label = null;
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            Class<?> returnType = method.getReturnType();
            if (returnType.equals(Void.TYPE)) {
                continue;
            }

            if (method.getParameterTypes().length != 0) {
                continue;
            }

            if (returnType == ClassLoader.class
                    || returnType == InputStream.class
                    || returnType == Reader.class) {
                continue;
            }

            if (methodName.equals("getMetaClass")
                    && returnType.getName().equals("groovy.lang.MetaClass")) {
                continue;
            }
            if (methodName.equals("getSuppressed")
                    && method.getDeclaringClass() == Throwable.class) {
                continue;
            }

            if (kotlin && isKotlinIgnore(clazz, methodName)) {
                continue;
            }
            /**
             *  如果在属性或者方法上存在JSONField注解，并且定制了name属性，不以类上的propertyNamingStrategy设置为准，以此字段的JSONField的name定制为准。
             */
            Boolean fieldAnnotationAndNameExists = false;
            JSONField annotation = TypeUtils.getAnnotation(method, JSONField.class);
            if (annotation == null) {
                annotation = getSuperMethodAnnotation(clazz, method);
            }
            if (annotation == null && kotlin) {
                if (constructors == null) {
                    constructors = clazz.getDeclaredConstructors();
                    Constructor creatorConstructor = TypeUtils.getKotlinConstructor(constructors);
                    if (creatorConstructor != null) {
                        paramAnnotationArrays = TypeUtils.getParameterAnnotations(creatorConstructor);
                        paramNames = TypeUtils.getKoltinConstructorParameters(clazz);
                        if (paramNames != null) {
                            String[] paramNames_sorted = new String[paramNames.length];
                            System.arraycopy(paramNames, 0, paramNames_sorted, 0, paramNames.length);

                            Arrays.sort(paramNames_sorted);
                            paramNameMapping = new short[paramNames.length];
                            for (short p = 0; p < paramNames.length; p++) {
                                int index = Arrays.binarySearch(paramNames_sorted, paramNames[p]);
                                paramNameMapping[index] = p;
                            }
                            paramNames = paramNames_sorted;
                        }
                    }
                }
                if (paramNames != null && paramNameMapping != null && methodName.startsWith("get")) {
                    String propertyName = decapitalize(methodName.substring(3));
                    int p = Arrays.binarySearch(paramNames, propertyName);
                    if (p < 0) {
                        for (int i = 0; i < paramNames.length; i++) {
                            if (propertyName.equalsIgnoreCase(paramNames[i])) {
                                p = i;
                                break;
                            }
                        }
                    }
                    if (p >= 0) {
                        short index = paramNameMapping[p];
                        Annotation[] paramAnnotations = paramAnnotationArrays[index];
                        if (paramAnnotations != null) {
                            for (Annotation paramAnnotation : paramAnnotations) {
                                if (paramAnnotation instanceof JSONField) {
                                    annotation = (JSONField) paramAnnotation;
                                    break;
                                }
                            }
                        }
                        if (annotation == null) {
                            Field field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                            if (field != null) {
                                annotation = TypeUtils.getAnnotation(field, JSONField.class);
                            }
                        }
                    }
                }
            }
            if (annotation != null) {
                if (!annotation.serialize()) {
                    continue;
                }
                ordinal = annotation.ordinal();
                serialzeFeatures = SerializerFeature.of(annotation.serialzeFeatures());
                parserFeatures = Feature.of(annotation.parseFeatures());
                if (annotation.name().length() != 0) {
                    String propertyName = annotation.name();
                    if (aliasMap != null) {
                        propertyName = aliasMap.get(propertyName);
                        if (propertyName == null) {
                            continue;
                        }
                    }
                    FieldInfo fieldInfo = new FieldInfo(propertyName, method, null, clazz, null, ordinal,
                            serialzeFeatures, parserFeatures, annotation, null, label);
                    fieldInfoMap.put(propertyName, fieldInfo);
                    continue;
                }
                if (annotation.label().length() != 0) {
                    label = annotation.label();
                }
            }
            if (methodName.startsWith("get")) {
                if (methodName.length() < 4) {
                    continue;
                }
                if (methodName.equals("getClass")) {
                    continue;
                }
                if (methodName.equals("getDeclaringClass") && clazz.isEnum()) {
                    continue;
                }
                char c3 = methodName.charAt(3);
                String propertyName;
                Field field = null;
                if (Character.isUpperCase(c3) //
                        || c3 > 512 // for unicode method name
                ) {
                    if (compatibleWithJavaBean) {
                        propertyName = decapitalize(methodName.substring(3));
                    } else {
                        propertyName = TypeUtils.getPropertyNameByMethodName(methodName);
                    }
                    propertyName = getPropertyNameByCompatibleFieldName(fieldCacheMap, methodName, propertyName, 3);
                } else if (c3 == '_') {
                    propertyName = methodName.substring(3);
                    field = fieldCacheMap.get(propertyName);
                    if (field == null) {
                        String temp = propertyName;
                        propertyName = methodName.substring(4);
                        field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                        if (field == null) {
                            propertyName = temp; //减少修改代码带来的影响
                        }
                    }
                } else if (c3 == 'f') {
                    propertyName = methodName.substring(3);
                } else if (methodName.length() >= 5 && Character.isUpperCase(methodName.charAt(4))) {
                    propertyName = decapitalize(methodName.substring(3));
                } else {
                    propertyName = methodName.substring(3);
                    field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                    if (field == null) {
                        continue;
                    }
                }
                boolean ignore = isJSONTypeIgnore(clazz, propertyName);
                if (ignore) {
                    continue;
                }

                if (field == null) {
                    // 假如bean的field很多的情况一下，轮询时将大大降低效率
                    field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                }

                if (field == null && propertyName.length() > 1) {
                    char ch = propertyName.charAt(1);
                    if (ch >= 'A' && ch <= 'Z') {
                        String javaBeanCompatiblePropertyName = decapitalize(methodName.substring(3));
                        field = ParserConfig.getFieldFromCache(javaBeanCompatiblePropertyName, fieldCacheMap);
                    }
                }
                JSONField fieldAnnotation = null;
                if (field != null) {
                    fieldAnnotation = TypeUtils.getAnnotation(field, JSONField.class);
                    if (fieldAnnotation != null) {
                        if (!fieldAnnotation.serialize()) {
                            continue;
                        }
                        ordinal = fieldAnnotation.ordinal();
                        serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                        parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                        if (fieldAnnotation.name().length() != 0) {
                            fieldAnnotationAndNameExists = true;
                            propertyName = fieldAnnotation.name();
                            if (aliasMap != null) {
                                propertyName = aliasMap.get(propertyName);
                                if (propertyName == null) {
                                    continue;
                                }
                            }
                        }
                        if (fieldAnnotation.label().length() != 0) {
                            label = fieldAnnotation.label();
                        }
                    }
                }
                if (aliasMap != null) {
                    propertyName = aliasMap.get(propertyName);
                    if (propertyName == null) {
                        continue;
                    }
                }
                if (propertyNamingStrategy != null && !fieldAnnotationAndNameExists) {
                    propertyName = propertyNamingStrategy.translate(propertyName);
                }
                FieldInfo fieldInfo = new FieldInfo(propertyName, method, field, clazz, null, ordinal, serialzeFeatures, parserFeatures,
                        annotation, fieldAnnotation, label);
                fieldInfoMap.put(propertyName, fieldInfo);
            }
            if (methodName.startsWith("is")) {
                if (methodName.length() < 3) {
                    continue;
                }
                if (returnType != Boolean.TYPE
                        && returnType != Boolean.class) {
                    continue;
                }
                char c2 = methodName.charAt(2);
                String propertyName;
                Field field = null;
                if (Character.isUpperCase(c2)) {
                    if (compatibleWithJavaBean) {
                        propertyName = decapitalize(methodName.substring(2));
                    } else {
                        propertyName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                    }
                    propertyName = getPropertyNameByCompatibleFieldName(fieldCacheMap, methodName, propertyName, 2);
                } else if (c2 == '_') {
                    propertyName = methodName.substring(3);
                    field = fieldCacheMap.get(propertyName);
                    if (field == null) {
                        String temp = propertyName;
                        propertyName = methodName.substring(2);
                        field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                        if (field == null) {
                            propertyName = temp;
                        }
                    }
                } else if (c2 == 'f') {
                    propertyName = methodName.substring(2);
                } else {
                    propertyName = methodName.substring(2);
                    field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                    if (field == null) {
                        continue;
                    }
                }
                boolean ignore = isJSONTypeIgnore(clazz, propertyName);
                if (ignore) {
                    continue;
                }

                if (field == null) {
                    field = ParserConfig.getFieldFromCache(propertyName, fieldCacheMap);
                }

                if (field == null) {
                    field = ParserConfig.getFieldFromCache(methodName, fieldCacheMap);
                }
                JSONField fieldAnnotation = null;
                if (field != null) {
                    fieldAnnotation = TypeUtils.getAnnotation(field, JSONField.class);
                    if (fieldAnnotation != null) {
                        if (!fieldAnnotation.serialize()) {
                            continue;
                        }
                        ordinal = fieldAnnotation.ordinal();
                        serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                        parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                        if (fieldAnnotation.name().length() != 0) {
                            propertyName = fieldAnnotation.name();
                            if (aliasMap != null) {
                                propertyName = aliasMap.get(propertyName);
                                if (propertyName == null) {
                                    continue;
                                }
                            }
                        }
                        if (fieldAnnotation.label().length() != 0) {
                            label = fieldAnnotation.label();
                        }
                    }
                }
                if (aliasMap != null) {
                    propertyName = aliasMap.get(propertyName);
                    if (propertyName == null) {
                        continue;
                    }
                }
                if (propertyNamingStrategy != null) {
                    propertyName = propertyNamingStrategy.translate(propertyName);
                }
                //优先选择get
                if (fieldInfoMap.containsKey(propertyName)) {
                    continue;
                }
                FieldInfo fieldInfo = new FieldInfo(
                        propertyName,
                        method,
                        field,
                        clazz,
                        null,
                        ordinal,
                        serialzeFeatures,
                        parserFeatures,
                        annotation,
                        fieldAnnotation,
                        label
                );
                fieldInfoMap.put(propertyName, fieldInfo);
            }
        }
        Field[] fields = clazz.getFields();
        computeFields(clazz, aliasMap, propertyNamingStrategy, fieldInfoMap, fields);
        return getFieldInfos(clazz, sorted, fieldInfoMap);
    }

    private static void computeFields(
            Class<?> clazz,
            Map<String, String> aliasMap,
            PropertyNamingStrategy propertyNamingStrategy,
            Map<String, FieldInfo> fieldInfoMap,
            Field[] fields
    ) {
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            JSONField fieldAnnotation = TypeUtils.getAnnotation(field, JSONField.class);
            int ordinal = 0, serialzeFeatures = 0, parserFeatures = 0;
            String propertyName = field.getName();
            String label = null;
            if (fieldAnnotation != null) {
                if (!fieldAnnotation.serialize()) {
                    continue;
                }
                ordinal = fieldAnnotation.ordinal();
                serialzeFeatures = SerializerFeature.of(fieldAnnotation.serialzeFeatures());
                parserFeatures = Feature.of(fieldAnnotation.parseFeatures());
                if (fieldAnnotation.name().length() != 0) {
                    propertyName = fieldAnnotation.name();
                }
                if (fieldAnnotation.label().length() != 0) {
                    label = fieldAnnotation.label();
                }
            }
            if (aliasMap != null) {
                propertyName = aliasMap.get(propertyName);
                if (propertyName == null) {
                    continue;
                }
            }
            if (propertyNamingStrategy != null) {
                propertyName = propertyNamingStrategy.translate(propertyName);
            }
            if (!fieldInfoMap.containsKey(propertyName)) {
                FieldInfo fieldInfo = new FieldInfo(propertyName, null, field, clazz, null, ordinal, serialzeFeatures, parserFeatures,
                        null, fieldAnnotation, label);
                fieldInfoMap.put(propertyName, fieldInfo);
            }
        }
    }

    private static List<FieldInfo> getFieldInfos(Class<?> clazz, boolean sorted, Map<String, FieldInfo> fieldInfoMap) {
        List<FieldInfo> fieldInfoList = new ArrayList<FieldInfo>();
        String[] orders = null;
        JSONType annotation = TypeUtils.getAnnotation(clazz, JSONType.class);
        if (annotation != null) {
            orders = annotation.orders();
        }
        if (orders != null && orders.length > 0) {
            LinkedHashMap<String, FieldInfo> map = new LinkedHashMap<String, FieldInfo>(fieldInfoMap.size());
            for (FieldInfo field : fieldInfoMap.values()) {
                map.put(field.name, field);
            }
            for (String item : orders) {
                FieldInfo field = map.get(item);
                if (field != null) {
                    fieldInfoList.add(field);
                    map.remove(item);
                }
            }
            fieldInfoList.addAll(map.values());
        } else {
            fieldInfoList.addAll(fieldInfoMap.values());
            if (sorted) {
                Collections.sort(fieldInfoList);
            }
        }
        return fieldInfoList;
    }

    static void setAccessible(AccessibleObject obj) {
        if (!setAccessibleEnable) {
            return;
        }
        if (obj.isAccessible()) {
            return;
        }
        try {
            obj.setAccessible(true);
        } catch (Throwable error) {
            setAccessibleEnable = false;
        }
    }

    public static boolean isKotlin(Class clazz) {
        if (kotlin_metadata == null && !kotlin_metadata_error) {
            try {
                kotlin_metadata = Class.forName("kotlin.Metadata");
            } catch (Throwable e) {
                kotlin_metadata_error = true;
            }
        }
        return kotlin_metadata != null && clazz.isAnnotationPresent(kotlin_metadata);
    }

    public static Constructor getKotlinConstructor(Constructor[] constructors) {
        return getKotlinConstructor(constructors, null);
    }

    public static Constructor getKotlinConstructor(Constructor[] constructors, String[] paramNames) {
        Constructor creatorConstructor = null;
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            if (paramNames != null && parameterTypes.length != paramNames.length) {
                continue;
            }

            if (parameterTypes.length > 0 && parameterTypes[parameterTypes.length - 1].getName().equals("kotlin.jvm.internal.DefaultConstructorMarker")) {
                continue;
            }
            if (creatorConstructor != null && creatorConstructor.getParameterTypes().length >= parameterTypes.length) {
                continue;
            }
            creatorConstructor = constructor;
        }
        return creatorConstructor;
    }

    public static String[] getKoltinConstructorParameters(Class clazz) {
        if (kotlin_kclass_constructor == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_constructor = class_kotlin_kclass.getConstructor(Class.class);
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }
        if (kotlin_kclass_constructor == null) {
            return null;
        }

        if (kotlin_kclass_getConstructors == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlin_kclass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlin_kclass_getConstructors = class_kotlin_kclass.getMethod("getConstructors");
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_kfunction_getParameters == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlin_kfunction = Class.forName("kotlin.reflect.KFunction");
                kotlin_kfunction_getParameters = class_kotlin_kfunction.getMethod("getParameters");
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_kparameter_getName == null && !kotlin_class_klass_error) {
            try {
                Class class_kotlinn_kparameter = Class.forName("kotlin.reflect.KParameter");
                kotlin_kparameter_getName = class_kotlinn_kparameter.getMethod("getName");
            } catch (Throwable e) {
                kotlin_class_klass_error = true;
            }
        }

        if (kotlin_error) {
            return null;
        }

        try {
            Object constructor = null;
            Object kclassImpl = kotlin_kclass_constructor.newInstance(clazz);
            Iterable it = (Iterable) kotlin_kclass_getConstructors.invoke(kclassImpl);
            for (Iterator iterator = it.iterator(); iterator.hasNext(); iterator.hasNext()) {
                Object item = iterator.next();
                List parameters = (List) kotlin_kfunction_getParameters.invoke(item);
                if (constructor != null && parameters.size() == 0) {
                    continue;
                }
                constructor = item;
            }

            if (constructor == null) {
                return null;
            }

            List parameters = (List) kotlin_kfunction_getParameters.invoke(constructor);
            String[] names = new String[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                names[i] = (String) kotlin_kparameter_getName.invoke(param);
            }
            return names;
        } catch (Throwable e) {
            e.printStackTrace();
            kotlin_error = true;
        }
        return null;
    }

    static boolean isKotlinIgnore(Class clazz, String methodName) {
        if (kotlinIgnores == null && !kotlinIgnores_error) {
            try {
                Map<Class, String[]> map = new HashMap<Class, String[]>();
                Class charRangeClass = Class.forName("kotlin.ranges.CharRange");
                map.put(charRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class intRangeClass = Class.forName("kotlin.ranges.IntRange");
                map.put(intRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class longRangeClass = Class.forName("kotlin.ranges.LongRange");
                map.put(longRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class floatRangeClass = Class.forName("kotlin.ranges.ClosedFloatRange");
                map.put(floatRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                Class doubleRangeClass = Class.forName("kotlin.ranges.ClosedDoubleRange");
                map.put(doubleRangeClass, new String[]{"getEndInclusive", "isEmpty"});
                kotlinIgnores = map;
            } catch (Throwable error) {
                kotlinIgnores_error = true;
            }
        }
        if (kotlinIgnores == null) {
            return false;
        }
        String[] ignores = kotlinIgnores.get(clazz);
        return ignores != null && Arrays.binarySearch(ignores, methodName) >= 0;
    }

    private static boolean isJSONTypeIgnore(Class<?> clazz, String propertyName) {
        JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
        if (jsonType != null) {
            // 1、新增 includes 支持，如果 JSONType 同时设置了includes 和 ignores 属性，则以includes为准。
            // 2、个人认为对于大小写敏感的Java和JS而言，使用 equals() 比 equalsIgnoreCase() 更好，改动的唯一风险就是向后兼容性的问题
            // 不过，相信开发者应该都是严格按照大小写敏感的方式进行属性设置的
            String[] fields = jsonType.includes();
            if (fields.length > 0) {
                for (String field : fields) {
                    if (propertyName.equals(field)) {
                        return false;
                    }
                }
                return true;
            } else {
                fields = jsonType.ignores();
                for (String field : fields) {
                    if (propertyName.equals(field)) {
                        return true;
                    }
                }
            }
        }
        if (clazz.getSuperclass() != Object.class && clazz.getSuperclass() != null) {
            return isJSONTypeIgnore(clazz.getSuperclass(), propertyName);
        }
        return false;
    }

    public static JSONField getSuperMethodAnnotation(final Class<?> clazz, final Method method) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            Class<?>[] types = method.getParameterTypes();
            for (Class<?> interfaceClass : interfaces) {
                for (Method interfaceMethod : interfaceClass.getMethods()) {
                    Class<?>[] interfaceTypes = interfaceMethod.getParameterTypes();
                    if (interfaceTypes.length != types.length) {
                        continue;
                    }
                    if (!interfaceMethod.getName().equals(method.getName())) {
                        continue;
                    }
                    boolean match = true;
                    for (int i = 0; i < types.length; ++i) {
                        if (!interfaceTypes[i].equals(types[i])) {
                            match = false;
                            break;
                        }
                    }
                    if (!match) {
                        continue;
                    }
                    JSONField annotation = TypeUtils.getAnnotation(interfaceMethod, JSONField.class);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null) {
            return null;
        }
        if (Modifier.isAbstract(superClass.getModifiers())) {
            Class<?>[] types = method.getParameterTypes();
            for (Method interfaceMethod : superClass.getMethods()) {
                Class<?>[] interfaceTypes = interfaceMethod.getParameterTypes();
                if (interfaceTypes.length != types.length) {
                    continue;
                }
                if (!interfaceMethod.getName().equals(method.getName())) {
                    continue;
                }
                boolean match = true;
                for (int i = 0; i < types.length; ++i) {
                    if (!interfaceTypes[i].equals(types[i])) {
                        match = false;
                        break;
                    }
                }
                if (!match) {
                    continue;
                }
                JSONField annotation = TypeUtils.getAnnotation(interfaceMethod, JSONField.class);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return null;
    }

    private static String getPropertyNameByCompatibleFieldName(Map<String, Field> fieldCacheMap, String methodName,
                                                               String propertyName, int fromIdx) {
        if (compatibleWithFieldName) {
            if (!fieldCacheMap.containsKey(propertyName)) {
                String tempPropertyName = methodName.substring(fromIdx);
                return fieldCacheMap.containsKey(tempPropertyName) ? tempPropertyName : propertyName;
            }
        }
        return propertyName;
    }

    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * resolve property name from get/set method name
     *
     * @param methodName get/set method name
     * @return property name
     */
    public static String getPropertyNameByMethodName(String methodName) {
        return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
    }

    public static Annotation[][] getParameterAnnotations(Constructor constructor) {
        Annotation[][] targetAnnotations = constructor.getParameterAnnotations();

        Class<?> clazz = constructor.getDeclaringClass();
        Annotation[][] mixInAnnotations;
        Class<?> mixInClass = null;
        Type type = JSON.getMixInAnnotations(clazz);
        if (type instanceof Class<?>) {
            mixInClass = (Class<?>) type;
        }

        if (mixInClass != null) {
            Constructor mixInConstructor = null;
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            // 构建参数列表，因为内部类的构造函数需要传入外部类的引用
            List<Class<?>> enclosingClasses = new ArrayList<Class<?>>(2);
            for (Class<?> enclosingClass = mixInClass.getEnclosingClass(); enclosingClass != null; enclosingClass = enclosingClass.getEnclosingClass()) {
                enclosingClasses.add(enclosingClass);
            }
            int level = enclosingClasses.size();
            // 递归从MixIn类的父类中查找注解（如果有父类的话）
            for (Class<?> currClass = mixInClass; currClass != null && currClass != Object.class; currClass = currClass.getSuperclass()) {
                try {
                    if (level != 0) {
                        Class<?>[] outerClassAndParameterTypes = new Class[level + parameterTypes.length];
                        System.arraycopy(parameterTypes, 0, outerClassAndParameterTypes, level, parameterTypes.length);
                        for (int i = level; i > 0; i--) {
                            outerClassAndParameterTypes[i - 1] = enclosingClasses.get(i - 1);
                        }
                        mixInConstructor = mixInClass.getDeclaredConstructor(outerClassAndParameterTypes);
                    } else {
                        mixInConstructor = mixInClass.getDeclaredConstructor(parameterTypes);
                    }
                    break;
                } catch (NoSuchMethodException e) {
                    level--;
                }
            }
            if (mixInConstructor == null) {
                return targetAnnotations;
            }
            mixInAnnotations = mixInConstructor.getParameterAnnotations();
            if (mixInAnnotations != null) {
                return mixInAnnotations;
            }
        }
        return targetAnnotations;
    }

    public static class MethodInheritanceComparator
            implements Comparator<Method> {
        public int compare(Method m1, Method m2) {
            int cmp = m1.getName().compareTo(m2.getName());
            if (cmp != 0) {
                return cmp;
            }

            Class<?> class1 = m1.getReturnType();
            Class<?> class2 = m2.getReturnType();

            if (class1.equals(class2)) {
                return 0;
            }

            if (class1.isAssignableFrom(class2)) {
                return -1;
            }

            if (class2.isAssignableFrom(class1)) {
                return 1;
            }
            return 0;
        }
    }
}
