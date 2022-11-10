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
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author wenshao[szujobs@hotmail.com]
 */
public class TypeUtils {
    public static final long fnv1a_64_magic_hashcode = 0xcbf29ce484222325L;
    public static final long fnv1a_64_magic_prime = 0x100000001b3L;

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

    public static String castToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static long fnv1a_64_lower(String key) {
        long hashCode = fnv1a_64_magic_hashcode;
        for (int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char) (ch + 32);
            }
            hashCode ^= ch;
            hashCode *= fnv1a_64_magic_prime;
        }
        return hashCode;
    }

    public static long fnv1a_64(String key) {
        long hashCode = fnv1a_64_magic_hashcode;
        for (int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            hashCode ^= ch;
            hashCode *= fnv1a_64_magic_prime;
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
}
