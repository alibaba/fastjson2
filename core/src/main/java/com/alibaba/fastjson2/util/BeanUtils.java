package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.modules.ObjectCodecProvider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.*;
import java.util.function.Consumer;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

/**
 * @author Bob Lee
 * @author Jesse Wilson
 * @author Shaojin Wen
 */
public abstract class BeanUtils {
    static final Type[] EMPTY_TYPE_ARRAY = new Type[]{};

    static final ConcurrentMap<Class, Field[]> fieldCache = new ConcurrentHashMap<>();
    static final ConcurrentMap<Class, Map<String, Field>> fieldMapCache = new ConcurrentHashMap<>();
    static final ConcurrentMap<Class, Field[]> declaredFieldCache = new ConcurrentHashMap<>();
    static final ConcurrentMap<Class, Method[]> methodCache = new ConcurrentHashMap<>();
    static final ConcurrentMap<Class, Constructor[]> constructorCache = new ConcurrentHashMap<>();

    private static volatile Class RECORD_CLASS;
    private static volatile Method RECORD_GET_RECORD_COMPONENTS;
    private static volatile Method RECORD_COMPONENT_GET_NAME;

    public static final String SUPER = "$super$";

    static final long[] IGNORE_CLASS_HASH_CODES = {
            -9214723784238596577L,
            -9030616758866828325L,
            -8335274122997354104L,
            -6963030519018899258L,
            -4863137578837233966L,
            -3653547262287832698L,
            -2819277587813726773L,
            -2291619803571459675L,
            -1811306045128064037L,
            -864440709753525476L,
            8731803887940231L,
            1616814008855344660L,
            2164749833121980361L,
            3724195282986200606L,
            4882459834864833642L,
            7981148566008458638L,
            8344106065386396833L
    };

    public static String[] getRecordFieldNames(Class<?> recordType) {
        if (JVM_VERSION < 14) {
            return new String[0];
        }

        try {
            if (RECORD_GET_RECORD_COMPONENTS == null) {
                RECORD_GET_RECORD_COMPONENTS = Class.class.getMethod("getRecordComponents");
            }

            if (RECORD_COMPONENT_GET_NAME == null) {
                Class<?> c = Class.forName("java.lang.reflect.RecordComponent");
                RECORD_COMPONENT_GET_NAME = c.getMethod("getName");
            }

            final Object[] components = (Object[]) RECORD_GET_RECORD_COMPONENTS.invoke(recordType);
            final String[] names = new String[components.length];
            for (int i = 0; i < components.length; i++) {
                names[i] = (String) RECORD_COMPONENT_GET_NAME.invoke(components[i]);
            }

            return names;
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                    "Failed to access Methods needed to support `java.lang.Record`: (%s) %s",
                    e.getClass().getName(), e.getMessage()), e);
        }
    }

    public static void getKotlinConstructor(Class<?> objectClass, BeanInfo beanInfo) {
        Constructor<?>[] constructors = constructorCache.get(objectClass);
        if (constructors == null) {
            constructors = objectClass.getDeclaredConstructors();
            constructorCache.putIfAbsent(objectClass, constructors);
        }

        Constructor<?> creatorConstructor = null;
        String[] paramNames = beanInfo.createParameterNames;

        for (Constructor<?> constructor : constructors) {
            int parameterCount = constructor.getParameterCount();
            if (paramNames != null && parameterCount != paramNames.length) {
                continue;
            }

            if (parameterCount > 2) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes[parameterCount - 2] == int.class &&
                        "kotlin.jvm.internal.DefaultConstructorMarker".equals(parameterTypes[parameterCount - 1].getName())
                ) {
                    beanInfo.markerConstructor = constructor;
                    continue;
                }
            }

            if (creatorConstructor != null && creatorConstructor.getParameterCount() >= parameterCount) {
                continue;
            }

            creatorConstructor = constructor;
        }

        beanInfo.creatorConstructor = creatorConstructor;
    }

    private static volatile boolean kotlinClassKlassError;
    private static volatile Constructor<?> kotlinKClassConstructor;
    private static volatile Method kotlinKClassGetConstructors;
    private static volatile Method kotlinKFunctionGetParameters;
    private static volatile Method kotlinKParameterGetName;
    private static volatile boolean kotlinError;

    public static String[] getKotlinConstructorParameters(Class<?> clazz) {
        if (kotlinKClassConstructor == null && !kotlinClassKlassError) {
            try {
                Class<?> classKotlinKClass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlinKClassConstructor = classKotlinKClass.getConstructor(Class.class);
            } catch (Throwable e) {
                kotlinClassKlassError = true;
            }
        }
        if (kotlinKClassConstructor == null) {
            return null;
        }

        if (kotlinKClassGetConstructors == null && !kotlinClassKlassError) {
            try {
                Class<?> classKotlinKClass = Class.forName("kotlin.reflect.jvm.internal.KClassImpl");
                kotlinKClassGetConstructors = classKotlinKClass.getMethod("getConstructors");
            } catch (Throwable e) {
                kotlinClassKlassError = true;
            }
        }

        if (kotlinKFunctionGetParameters == null && !kotlinClassKlassError) {
            try {
                Class<?> classKotlinKFunction = Class.forName("kotlin.reflect.KFunction");
                kotlinKFunctionGetParameters = classKotlinKFunction.getMethod("getParameters");
            } catch (Throwable e) {
                kotlinClassKlassError = true;
            }
        }

        if (kotlinKParameterGetName == null && !kotlinClassKlassError) {
            try {
                Class<?> classKotlinKParameter = Class.forName("kotlin.reflect.KParameter");
                kotlinKParameterGetName = classKotlinKParameter.getMethod("getName");
            } catch (Throwable e) {
                kotlinClassKlassError = true;
            }
        }

        if (kotlinError) {
            return null;
        }

        try {
            Object constructor = null;
            Object classImpl = kotlinKClassConstructor.newInstance(clazz);
            Iterable it = (Iterable) kotlinKClassGetConstructors.invoke(classImpl);
            for (Iterator iterator = it.iterator(); iterator.hasNext(); iterator.hasNext()) {
                Object item = iterator.next();
                List parameters = (List) kotlinKFunctionGetParameters.invoke(item);
                if (constructor != null && parameters.size() == 0) {
                    continue;
                }
                constructor = item;
            }

            if (constructor == null) {
                return null;
            }

            List parameters = (List) kotlinKFunctionGetParameters.invoke(constructor);
            String[] names = new String[parameters.size()];
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                names[i] = (String) kotlinKParameterGetName.invoke(param);
            }
            return names;
        } catch (Throwable ignored) {
            kotlinError = true;
        }
        return null;
    }

    public static void fields(Class objectClass, Consumer<Field> fieldReaders) {
        if (TypeUtils.isProxy(objectClass)) {
            Class superclass = objectClass.getSuperclass();
            fields(superclass, fieldReaders);
            return;
        }

        Field[] fields = fieldCache.get(objectClass);
        if (fields == null) {
            fields = objectClass.getFields();
            fieldCache.putIfAbsent(objectClass, fields);
        }

        boolean enumClass = Enum.class.isAssignableFrom(objectClass);
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && !enumClass) {
                continue;
            }

            fieldReaders.accept(field);
        }
    }

    public static Method getMethod(Class objectClass, String methodName) {
        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        return null;
    }

    public static Method getMethod(Class objectClass, Method signature) {
        if (objectClass == null
                || objectClass == Object.class
                || objectClass == Serializable.class
        ) {
            return null;
        }

        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        for (Method method : methods) {
            if (!method.getName().equals(signature.getName())) {
                continue;
            }
            if (method.getParameterCount() != signature.getParameterCount()) {
                continue;
            }
            Class<?>[] parameterTypes0 = method.getParameterTypes();
            Class<?>[] parameterTypes1 = signature.getParameterTypes();
            boolean paramMatch = true;
            for (int i = 0; i < parameterTypes0.length; i++) {
                if (!parameterTypes0[i].equals(parameterTypes1[i])) {
                    paramMatch = false;
                    break;
                }
            }
            if (paramMatch) {
                return method;
            }
        }

        return null;
    }

    public static Field getDeclaredField(Class objectClass, String fieldName) {
        Map<String, Field> fieldMap = fieldMapCache.get(objectClass);
        if (fieldMap == null) {
            Map<String, Field> map = new HashMap<>();
            declaredFields(objectClass, field -> map.put(field.getName(), field));

            fieldMapCache.putIfAbsent(objectClass, map);
            fieldMap = fieldMapCache.get(objectClass);
        }

        return fieldMap.get(fieldName);
    }

    public static Method getSetter(Class objectClass, String methodName) {
        Method[] methods = new Method[1];
        setters(objectClass, e -> {
            if (!methodName.equals(e.getName())) {
                return;
            }

            methods[0] = e;
        });
        return null;
    }

    /**
     * ignore static fields
     */
    public static void declaredFields(Class objectClass, Consumer<Field> fieldConsumer) {
        if (objectClass == null || fieldConsumer == null) {
            return;
        }

        if (ignore(objectClass)) {
            return;
        }

        if (TypeUtils.isProxy(objectClass)) {
            Class superclass = objectClass.getSuperclass();
            declaredFields(superclass, fieldConsumer);
            return;
        }

        Class superClass = objectClass.getSuperclass();

        boolean protobufMessageV3 = false;
        if (superClass != null
                && superClass != Object.class
        ) {
            protobufMessageV3 = superClass.getName().equals("com.google.protobuf.GeneratedMessageV3");
            if (!protobufMessageV3) {
                declaredFields(superClass, fieldConsumer);
            }
        }

        Field[] fields = declaredFieldCache.get(objectClass);
        if (fields == null) {
            Field[] declaredFields = null;
            try {
                declaredFields = objectClass.getDeclaredFields();
                declaredFieldCache.put(objectClass, declaredFields);
            } catch (Throwable ignored) {
                declaredFields = new Field[0];
            }

            boolean allMatch = true;
            for (Field field : declaredFields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                fields = declaredFields;
            } else {
                List<Field> list = new ArrayList<>(declaredFields.length);
                for (Field field : declaredFields) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers)) {
                        continue;
                    }
                    list.add(field);
                }
                fields = list.toArray(new Field[list.size()]);
            }

            fieldCache.putIfAbsent(objectClass, fields);
        }

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((modifiers & Modifier.STATIC) != 0) {
                continue;
            }

            if (protobufMessageV3) {
                String fieldName = field.getName();
                Class<?> fieldClass = field.getType();
                if ("cardsmap_".equals(fieldName)
                        && fieldClass.getName().equals("com.google.protobuf.MapField")) {
                    return;
                }
            }

            Class<?> declaringClass = field.getDeclaringClass();
            if (declaringClass == AbstractMap.class
                    || declaringClass == HashMap.class
                    || declaringClass == LinkedHashMap.class
                    || declaringClass == TreeMap.class
                    || declaringClass == ConcurrentHashMap.class
            ) {
                continue;
            }

            fieldConsumer.accept(field);
        }
    }

    public static void staticMethod(Class objectClass, Consumer<Method> methodConsumer) {
        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                continue;
            }

            methodConsumer.accept(method);
        }
    }

    public static Method buildMethod(Class objectClass, String methodName) {
        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }

            if (method.getParameterCount() != 0) {
                continue;
            }

            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        return null;
    }

    public static void constructor(Class objectClass, Consumer<Constructor> constructorConsumer) {
        Constructor[] constructors = constructorCache.get(objectClass);
        if (constructors == null) {
            constructors = objectClass.getDeclaredConstructors();
            constructorCache.putIfAbsent(objectClass, constructors);
        }

        for (Constructor constructor : constructors) {
            constructorConsumer.accept(constructor);
        }
    }

    public static Constructor[] getConstructor(Class objectClass) {
        Constructor[] constructors = constructorCache.get(objectClass);
        if (constructors == null) {
            constructors = objectClass.getDeclaredConstructors();
            constructorCache.putIfAbsent(objectClass, constructors);
        }

        return constructors;
    }

    public static Constructor getDefaultConstructor(Class objectClass, boolean includeNoneStaticMember) {
        if (objectClass == StackTraceElement.class && JVM_VERSION >= 9) {
            return null;
        }

        Constructor[] constructors = constructorCache.get(objectClass);
        if (constructors == null) {
            constructors = objectClass.getDeclaredConstructors();
            constructorCache.putIfAbsent(objectClass, constructors);
        }

        for (Constructor constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }

        if (!includeNoneStaticMember) {
            return null;
        }

        Class declaringClass = objectClass.getDeclaringClass();
        if (declaringClass != null) {
            for (Constructor constructor : constructors) {
                if (constructor.getParameterCount() == 1) {
                    Class firstParamType = constructor.getParameterTypes()[0];
                    if (declaringClass.equals(firstParamType)) {
                        return constructor;
                    }
                }
            }
        }

        return null;
    }

    public static void setters(Class objectClass, Consumer<Method> methodConsumer) {
        setters(objectClass, null, methodConsumer);
    }

    public static void setters(Class objectClass, Class mixin, Consumer<Method> methodConsumer) {
        if (ignore(objectClass)) {
            return;
        }

        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        for (Method method : methods) {
            int mods = method.getModifiers();
            if (Modifier.isStatic(mods)) {
                continue;
            }

            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            String methodName = method.getName();

            boolean methodSkip = false;
            switch (methodName) {
                case "equals":
                case "hashCode":
                case "toString":
                    methodSkip = true;
                    break;
                default:
                    break;
            }

            if (methodSkip) {
                continue;
            }

            int paramCount = method.getParameterCount();

            // read only getter
            if (paramCount == 0) {
                if (methodName.length() <= 3 || !methodName.startsWith("get")) {
                    continue;
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == AtomicInteger.class
                        || returnType == AtomicLong.class
                        || returnType == AtomicBoolean.class
                        || returnType == AtomicIntegerArray.class
                        || returnType == AtomicLongArray.class
                        || returnType == AtomicReference.class
                        || Collection.class.isAssignableFrom(returnType)
                        || Map.class.isAssignableFrom(returnType)
                ) {
                    methodConsumer.accept(method);
                    continue;
                }
            }

            if (paramCount == 2
                    && method.getReturnType() == Void.TYPE
                    && method.getParameterTypes()[0] == String.class
            ) {
                Annotation[] annotations = getAnnotations(method);

                boolean unwrapped = false;
                for (Annotation annotation : annotations) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    JSONField jsonField = findAnnotation(annotation, JSONField.class);
                    if (jsonField != null) {
                        if (jsonField.unwrapped()) {
                            unwrapped = true;
                            break;
                        }
                        continue;
                    }

                    switch (annotationType.getName()) {
                        case "com.fasterxml.jackson.annotation.JsonAnySetter":
                        case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonAnySetter":
                            if (JSONFactory.isUseJacksonAnnotation()) {
                                unwrapped = true;
                            }
                            break;
                        default:
                            break;
                    }
                }

                if (unwrapped) {
                    methodConsumer.accept(method);
                }
                continue;
            }

            if (paramCount != 1) {
                continue;
            }

            final int methodNameLength = methodName.length();
            boolean nameMatch = methodNameLength > 3 && methodName.startsWith("set");
            if (!nameMatch) {
                if (mixin != null) {
                    Method mixinMethod = getMethod(mixin, method);
                    if (mixinMethod != null) {
                        Annotation[] annotations = getAnnotations(mixinMethod);
                        for (Annotation annotation : annotations) {
                            if (annotation.annotationType() == JSONField.class) {
                                JSONField jsonField = (JSONField) annotation;
                                if (!jsonField.unwrapped()) {
                                    nameMatch = true;
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (!nameMatch) {
                Annotation[] annotations = getAnnotations(method);
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() == JSONField.class) {
                        JSONField jsonField = (JSONField) annotation;
                        if (!jsonField.unwrapped()) {
                            nameMatch = true;
                        }
                        break;
                    }
                }
            }

            if (!nameMatch) {
                continue;
            }

            methodConsumer.accept(method);
        }
    }

    public static void setters(Class objectClass, boolean checkPrefix, Consumer<Method> methodConsumer) {
        if (ignore(objectClass)) {
            return;
        }

        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        for (Method method : methods) {
            int paramType = method.getParameterCount();

            // read only getter
            if (paramType == 0) {
                String methodName = method.getName();
                if (checkPrefix && (methodName.length() <= 3 || !methodName.startsWith("get"))) {
                    continue;
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == AtomicInteger.class
                        || returnType == AtomicLong.class
                        || returnType == AtomicBoolean.class
                        || returnType == AtomicIntegerArray.class
                        || returnType == AtomicLongArray.class
                        || Collection.class.isAssignableFrom(returnType)
                ) {
                    methodConsumer.accept(method);
                    continue;
                }
            }

            if (paramType != 1) {
                continue;
            }

            int mods = method.getModifiers();
            if (Modifier.isStatic(mods)) {
                continue;
            }

            String methodName = method.getName();
            final int methodNameLength = methodName.length();
            if (checkPrefix && (methodNameLength <= 3 || !methodName.startsWith("set"))) {
                continue;
            }

            methodConsumer.accept(method);
        }
    }

    public static void annotationMethods(Class objectClass, Consumer<Method> methodConsumer) {
        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        for (Method method : methods) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Object.class) {
                continue;
            }

            switch (method.getName()) {
                case "toString":
                case "hashCode":
                case "annotationType":
                    continue;
                default:
                    break;
            }

            methodConsumer.accept(method);
        }
    }

    public static boolean isWriteEnumAsJavaBean(Class clazz) {
        Annotation[] annotations = getAnnotations(clazz);
        for (Annotation annotation : annotations) {
            JSONType jsonType = findAnnotation(annotation, JSONType.class);
            if (jsonType != null) {
                return jsonType.writeEnumAsJavaBean();
            }

            Class<? extends Annotation> annotationType = annotation.annotationType();
            String name = annotationType.getName();
            if ("com.alibaba.fastjson.annotation.JSONType".equals(name)) {
                BeanInfo beanInfo = new BeanInfo();
                BeanUtils.annotationMethods(annotationType, method -> BeanUtils.processJSONType1x(beanInfo, annotation, method));
                if (beanInfo.writeEnumAsJavaBean) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String[] getEnumAnnotationNames(Class enumClass) {
        Enum[] enumConstants = (Enum[]) enumClass.getEnumConstants();
        String[] annotationNames = new String[enumConstants.length];
        BeanUtils.fields(enumClass, field -> {
            String fieldName = field.getName();
            for (int i = 0; i < enumConstants.length; i++) {
                Enum e = enumConstants[i];
                String enumName = e.name();
                if (fieldName.equals(enumName)) {
                    JSONField annotation = field.getAnnotation(JSONField.class);
                    if (annotation != null) {
                        String annotationName = annotation.name();
                        if (annotationName.length() != 0 && !annotationName.equals(enumName)) {
                            annotationNames[i] = annotationName;
                        }
                    }
                    break;
                }
            }
        });

        int nulls = 0;
        for (String annotationName : annotationNames) {
            if (annotationName == null) {
                nulls++;
            }
        }

        if (nulls == annotationNames.length) {
            return null;
        }

        return annotationNames;
    }

    public static Member getEnumValueField(Class enumClass, ObjectCodecProvider mixinProvider) {
        if (enumClass == null) {
            return null;
        }

        Class[] interfaces = enumClass.getInterfaces();

        Method[] methods = methodCache.get(enumClass);
        if (methods == null) {
            methods = enumClass.getMethods();
            methodCache.putIfAbsent(enumClass, methods);
        }

        for (Method method : methods) {
            if (method.getReturnType() == Void.class) {
                continue;
            }

            if (method.getParameterCount() != 0) {
                continue;
            }

            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Enum.class || declaringClass == Object.class) {
                continue;
            }

            String methodName = method.getName();
            if (methodName.equals("values")) {
                continue;
            }

            if (isJSONField(method)) {
                return method;
            }

            if (methodName.startsWith("get")) {
                String fieldName = BeanUtils.getterName(methodName, null);
                Field field = BeanUtils.getDeclaredField(enumClass, fieldName);
                if (field != null && isJSONField(field)) {
                    return method;
                }
            }

            AtomicReference<Member> memberRef = new AtomicReference<>();
            for (Class enumInterface : interfaces) {
                getters(enumInterface, e -> {
                    if (e.getName().equals(methodName)) {
                        if (isJSONField(e)) {
                            memberRef.set(method);
                        }
                    }
                });

                Class mixIn;
                if (mixinProvider != null) {
                    mixIn = mixinProvider.getMixIn(enumInterface);
                } else {
                    mixIn = JSONFactory.getDefaultObjectWriterProvider().getMixIn(enumInterface);
                }

                if (mixIn != null) {
                    getters(mixIn, e -> {
                        if (e.getName().equals(methodName)) {
                            if (isJSONField(e)) {
                                memberRef.set(method);
                            }
                        }
                    });
                }
            }
            Member refMember = memberRef.get();
            if (refMember != null) {
                return refMember;
            }
        }

        Field[] fields = fieldCache.get(enumClass);
        if (fields == null) {
            fields = enumClass.getFields();
            fieldCache.putIfAbsent(enumClass, fields);
        }

        Member member = null;
        Enum[] enumConstants = (Enum[]) enumClass.getEnumConstants();
        for (Field field : fields) {
            boolean found = false;
            if (enumConstants != null) {
                String fieldName = field.getName();
                for (Enum e : enumConstants) {
                    if (fieldName.equals(e.name())) {
                        found = true;
                        break;
                    }
                }
            }

            if (isJSONField(field)) {
                if (!found) {
                    member = field;
                    break;
                }
            }
        }

        return member;
    }

    public static void getters(Class objectClass, Consumer<Method> methodConsumer) {
        getters(objectClass, null, methodConsumer);
    }

    public static void getters(Class objectClass, Class mixinSource, Consumer<Method> methodConsumer) {
        if (objectClass == null) {
            return;
        }

        if (Proxy.isProxyClass(objectClass)) {
            Class[] interfaces = objectClass.getInterfaces();
            if (interfaces.length == 1) {
                getters(interfaces[0], methodConsumer);
                return;
            }
        }

        if (ignore(objectClass)) {
            return;
        }

        Class superClass = objectClass.getSuperclass();
        if (TypeUtils.isProxy(objectClass)) {
            getters(superClass, methodConsumer);
            return;
        }

        boolean record = isRecord(objectClass);

        String[] recordFieldNames = null;
        if (record) {
            recordFieldNames = getRecordFieldNames(objectClass);
        }

        Method[] methods = methodCache.get(objectClass);
        if (methods == null) {
            methods = getMethods(objectClass);
            methodCache.putIfAbsent(objectClass, methods);
        }

        boolean protobufMessageV3 = superClass != null && superClass.getName().equals("com.google.protobuf.GeneratedMessageV3");

        for (Method method : methods) {
            int paramType = method.getParameterCount();
            if (paramType != 0) {
                continue;
            }

            int mods = method.getModifiers();
            if (Modifier.isStatic(mods)) {
                continue;
            }

            Class<?> returnClass = method.getReturnType();
            if (returnClass == Void.class) {
                continue;
            }

            Class<?> declaringClass = method.getDeclaringClass();
            if (declaringClass == Enum.class || declaringClass == Object.class) {
                continue;
            }

            String methodName = method.getName();

            boolean methodSkip = false;
            switch (methodName) {
                case "isInitialized":
                case "getInitializationErrorString":
                case "getSerializedSize":
                    if (protobufMessageV3) {
                        methodSkip = true;
                    }
                    break;
                case "equals":
                case "hashCode":
                case "toString":
                    methodSkip = true;
                    break;
                default:
                    break;
            }

            if (methodSkip) {
                continue;
            }

            if (protobufMessageV3) {
                if ((methodName.endsWith("Type") || methodName.endsWith("Bytes"))
                        && returnClass.getName().equals("com.google.protobuf.ByteString")) {
                    continue;
                }
            }

            // skip thrift isSetXXX
            if (methodName.startsWith("isSet") && returnClass == boolean.class) {
                boolean setterFound = false, unsetFound = false, getterFound = false;
                String setterName = BeanUtils.getterName(methodName, null);
                String getterName = "g" + setterName.substring(1);

                String unsetName = "un" + setterName;
                for (Method m : methods) {
                    if (m.getName().equals(setterName)
                            && m.getParameterCount() == 1
                            && m.getReturnType() == void.class) {
                        setterFound = true;
                    } else if (m.getName().equals(getterName)
                            && m.getParameterCount() == 0) {
                        getterFound = true;
                    } else if (m.getName().equals(unsetName)
                            && m.getParameterCount() == 0
                            && m.getReturnType() == void.class) {
                        unsetFound = true;
                    }
                }

                if (setterFound && unsetFound && getterFound
                        && findAnnotation(method, JSONField.class) == null) {
                    continue;
                }
            }

            if (record) {
                boolean match = false;
                for (String recordFieldName : recordFieldNames) {
                    if (methodName.equals(recordFieldName)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    methodConsumer.accept(method);
                    continue;
                }
            }

            final int methodNameLength = methodName.length();
            boolean nameMatch = methodNameLength > 3 && methodName.startsWith("get");
            if (nameMatch) {
                char firstChar = methodName.charAt(3);
                if (firstChar >= 'a' && firstChar <= 'z' && methodNameLength == 4) {
                    nameMatch = false;
                }
            } else if (returnClass == boolean.class || returnClass == Boolean.class) {
                nameMatch = methodNameLength > 2 && methodName.startsWith("is");
                if (nameMatch) {
                    char firstChar = methodName.charAt(2);
                    if (firstChar >= 'a' && firstChar <= 'z' && methodNameLength == 3) {
                        nameMatch = false;
                    }
                }
            }

            if (!nameMatch) {
                if (isJSONField(method)) {
                    nameMatch = true;
                }
            }

            if (!nameMatch && mixinSource != null) {
                Method mixinMethod = getMethod(mixinSource, method);
                if (mixinMethod != null) {
                    if (isJSONField(mixinMethod)) {
                        nameMatch = true;
                    }
                }
            }

            if (!nameMatch) {
                continue;
            }

            if (protobufMessageV3) {
                if (method.getDeclaringClass() == superClass) {
                    continue;
                }
                Class<?> returnType = method.getReturnType();
                boolean ignore = false;
                switch (methodName) {
                    case "getUnknownFields":
                    case "getSerializedSize":
                    case "getParserForType":
                    case "getMessageBytes":
                    case "getDefaultInstanceForType":
                        ignore = returnType.getName().startsWith("com.google.protobuf.") || returnType == objectClass;
                        break;
                    default:
                        break;
                }

                if (ignore) {
                    continue;
                }
            }

            methodConsumer.accept(method);
        }
    }

    private static Method[] getMethods(Class objectClass) {
        Method[] methods;
        try {
            methods = objectClass.getMethods();
        } catch (NoClassDefFoundError ignored) {
            methods = new Method[0];
        }
        return methods;
    }

    private static boolean isJSONField(AnnotatedElement element) {
        Annotation[] annotations = element.getAnnotations();
        for (Annotation annotation : annotations) {
            String annotationTypeName = annotation.annotationType().getName();
            switch (annotationTypeName) {
                case "com.alibaba.fastjson.annotation.JSONField":
                case "com.alibaba.fastjson2.annotation.JSONField":
                    return true;
                case "com.fasterxml.jackson.annotation.JsonValue":
                case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonValue":
                case "com.fasterxml.jackson.annotation.JsonRawValue":
                case "com.fasterxml.jackson.annotation.JsonProperty":
                case "com.alibaba.fastjson2.adapter.jackson.annotation.JsonProperty":
                    if (JSONFactory.isUseJacksonAnnotation()) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    static boolean ignore(Class objectClass) {
        if (objectClass == null) {
            return true;
        }

        return Arrays.binarySearch(
                IGNORE_CLASS_HASH_CODES,
                Fnv.hashCode64(
                        objectClass.getName()
                )
        ) >= 0;
    }

    public static boolean isRecord(Class objectClass) {
        Class superclass = objectClass.getSuperclass();
        if (superclass == null) {
            return false;
        }

        if (RECORD_CLASS == null) {
            String superclassName = superclass.getName();
            if ("java.lang.Record".equals(superclassName)) {
                RECORD_CLASS = superclass;
                return true;
            } else {
                return false;
            }
        }

        return superclass == RECORD_CLASS;
    }

    public static String setterName(String methodName, String namingStrategy) {
        if (namingStrategy == null) {
            namingStrategy = "CamelCase";
        }

        int methodNameLength = methodName.length();
        if (methodNameLength <= 3) {
            return methodName;
        }

        int prefixLength = methodName.startsWith("set") ? 3 : 0;

        switch (namingStrategy) {
            case "NeverUseThisValueExceptDefaultValue":
            case "CamelCase": {
                char[] chars = new char[methodNameLength - prefixLength];
                methodName.getChars(prefixLength, methodNameLength, chars, 0);
                char c0 = chars[0];
                boolean c1UCase = chars.length > 1 && chars[1] >= 'A' && chars[1] <= 'Z';
                if (c0 >= 'A' && c0 <= 'Z' && !c1UCase) {
                    chars[0] = (char) (c0 + 32);
                }
                return new String(chars);
            }
            case "PascalCase":
                return pascal(methodName, methodNameLength, prefixLength);
            case "SnakeCase": {
                return snakeCase(methodName, prefixLength);
            }
            case "UpperCaseWithUnderScores": {
                return underScores(methodName, prefixLength, true);
            }
            case "UpperCase": {
                char[] chars = new char[methodNameLength - prefixLength];
                methodName.getChars(prefixLength, methodNameLength, chars, 0);
                char c0 = chars[0];
                for (int i = 0; i < chars.length; i++) {
                    char ch = chars[i];
                    if (ch >= 'a' && c0 <= 'z') {
                        chars[i] = (char) (ch - 32);
                    }
                }
                return new String(chars);
            }
            default:
                throw new JSONException("TODO : " + namingStrategy);
        }
    }

    public static String setterName(String methodName, int prefixLength) {
        int methodNameLength = methodName.length();
        char[] chars = new char[methodNameLength - prefixLength];
        methodName.getChars(prefixLength, methodNameLength, chars, 0);
        char c0 = chars[0];
        boolean c1UCase = chars.length > 1 && chars[1] >= 'A' && chars[1] <= 'Z';
        if (c0 >= 'A' && c0 <= 'Z' && !c1UCase) {
            chars[0] = (char) (c0 + 32);
        }
        return new String(chars);
    }

    public static String getterName(Method method, String namingStrategy) {
        String fieldName = getterName(method.getName(), namingStrategy);

        if (fieldName.length() > 2
                && fieldName.charAt(0) >= 'A' && fieldName.charAt(0) <= 'Z'
                && fieldName.charAt(1) >= 'A' && fieldName.charAt(1) <= 'Z'
        ) {
            char[] chars = fieldName.toCharArray();
            chars[0] = (char) (chars[0] + 32);
            String fieldName1 = new String(chars);
            Field field = BeanUtils.getDeclaredField(method.getDeclaringClass(), fieldName1);
            if (field != null && Modifier.isPublic(field.getModifiers())) {
                fieldName = field.getName();
            }
        }

        return fieldName;
    }

    public static Field getField(Class objectClass, Method method) {
        String methodName = method.getName();
        final int len = methodName.length();
        Class<?> returnType = method.getReturnType();

        boolean is = false, get = false, set = false;
        if (len > 2) {
            char c0 = methodName.charAt(0);
            char c1 = methodName.charAt(1);
            char c2 = methodName.charAt(2);

            if (c0 == 'i' && c1 == 's') {
                is = returnType == Boolean.class || returnType == boolean.class;
            } else if (c0 == 'g' && c1 == 'e' && c2 == 't') {
                get = len > 3;
            } else if (c0 == 's' && c1 == 'e' && c2 == 't') {
                set = len > 3 && method.getParameterCount() == 1;
            }
        }

        Field[] fields = new Field[2];
        if (is || get || set) {
            Class type = (is || get) ? returnType : method.getParameterTypes()[0];
            int prefix = is ? 2 : 3;
            char[] chars = new char[len - prefix];
            methodName.getChars(prefix, len, chars, 0);
            char c0 = chars[0];
            declaredFields(objectClass, field -> {
                if (field.getDeclaringClass() != method.getDeclaringClass()) {
                    return;
                }

                String fieldName = field.getName();
                int fieldNameLength = fieldName.length();
                if (fieldNameLength == len - prefix
                        && (field.getType() == type || type.isAssignableFrom(field.getType()))) {
                    if (c0 >= 'A' && c0 <= 'Z' && (c0 + 32) == fieldName.charAt(0)
                            && fieldName.regionMatches(1, methodName, prefix + 1, fieldNameLength - 1)
                    ) {
                        fields[0] = field;
                    } else if (fieldName.regionMatches(0, methodName, prefix, fieldNameLength)) {
                        fields[1] = field;
                    }
                }
            });
        }

        return fields[0] != null ? fields[0] : fields[1];
    }

    public static String getterName(String methodName, String namingStrategy) {
        if (namingStrategy == null) {
            namingStrategy = "CamelCase";
        }

        final int methodNameLength = methodName.length();
        boolean is = methodName.startsWith("is");
        boolean get = methodName.startsWith("get");

        final int prefixLength;
        if (is) {
            prefixLength = 2;
        } else if (get) {
            prefixLength = 3;
        } else {
            prefixLength = 0;
        }

        if (methodNameLength == prefixLength) {
            return methodName;
        }

        switch (namingStrategy) {
            case "NeverUseThisValueExceptDefaultValue":
            case "CamelCase": {
                char[] chars = new char[methodNameLength - prefixLength];
                methodName.getChars(prefixLength, methodNameLength, chars, 0);
                char c0 = chars[0];
                boolean c1UCase = chars.length > 1 && chars[1] >= 'A' && chars[1] <= 'Z';
                if (c0 >= 'A' && c0 <= 'Z' && !c1UCase) {
                    chars[0] = (char) (c0 + 32);
                }
                return new String(chars);
            }
            case "CamelCase1x": {
                char[] chars = new char[methodNameLength - prefixLength];
                methodName.getChars(prefixLength, methodNameLength, chars, 0);
                char c0 = chars[0];
                if (c0 >= 'A' && c0 <= 'Z') {
                    chars[0] = (char) (c0 + 32);
                }
                return new String(chars);
            }
            case "PascalCase": {
                return pascal(methodName, methodNameLength, prefixLength);
            }
            case "SnakeCase": {
                return snakeCase(methodName, prefixLength);
            }
            case "UpperCaseWithUnderScores": {
                return underScores(methodName, prefixLength, true);
            }
            case "UpperCamelCaseWithSpaces":
                return upperCamelWith(methodName, prefixLength, ' ');
            case "UpperCase":
                return methodName.substring(prefixLength).toUpperCase();
            case "UpperCaseWithDashes":
                return dashes(methodName, prefixLength, true);
            case "UpperCaseWithDots":
                return dots(methodName, prefixLength, true);
            case "KebabCase": {
                StringBuilder buf = new StringBuilder();
                final int firstIndex;
                if (is) {
                    firstIndex = 2;
                } else if (get) {
                    firstIndex = 3;
                } else {
                    firstIndex = 0;
                }

                for (int i = firstIndex; i < methodName.length(); ++i) {
                    char ch = methodName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        char u = (char) (ch + 32);
                        if (i > firstIndex) {
                            buf.append('-');
                        }
                        buf.append(u);
                    } else {
                        buf.append(ch);
                    }
                }
                return buf.toString();
            }
            default:
                throw new JSONException("TODO : " + namingStrategy);
        }
    }

    private static String pascal(String methodName, int methodNameLength, int prefixLength) {
        char[] chars = new char[methodNameLength - prefixLength];
        methodName.getChars(prefixLength, methodNameLength, chars, 0);
        char c0 = chars[0];
        if (c0 >= 'a' && c0 <= 'z' && chars.length > 1) {
            chars[0] = (char) (c0 - 32);
        } else if (c0 == '_' && chars.length > 2) {
            char c1 = chars[1];
            if (c1 >= 'a' && c1 <= 'z' && chars[2] >= 'a' && chars[2] <= 'z') {
                chars[1] = (char) (c1 - 32);
            }
        }
        return new String(chars);
    }

    public static String fieldName(String methodName, String namingStrategy) {
        if (namingStrategy == null) {
            namingStrategy = "CamelCase";
        }
        if (methodName == null || methodName.isEmpty()) {
            return methodName;
        }

        switch (namingStrategy) {
            case "NoChange":
            case "NeverUseThisValueExceptDefaultValue":
            case "CamelCase": {
                char c0 = methodName.charAt(0);
                char c1 = methodName.length() > 1 ? methodName.charAt(1) : '\0';
                if (c0 >= 'A' && c0 <= 'Z'
                        && methodName.length() > 1
                        && (c1 < 'A' || c1 > 'Z')) {
                    char[] chars = methodName.toCharArray();
                    chars[0] = (char) (c0 + 32);
                    return new String(chars);
                }
                return methodName;
            }
            case "CamelCase1x": {
                char c0 = methodName.charAt(0);
                if (c0 >= 'A' && c0 <= 'Z' && methodName.length() > 1) {
                    char[] chars = methodName.toCharArray();
                    chars[0] = (char) (c0 + 32);
                    return new String(chars);
                }
                return methodName;
            }
            case "PascalCase": {
                char c0 = methodName.charAt(0);
                char c1;
                if (c0 >= 'a' && c0 <= 'z'
                        && methodName.length() > 1
                        && (c1 = methodName.charAt(1)) >= 'a'
                        && c1 <= 'z') {
                    char[] chars = methodName.toCharArray();
                    chars[0] = (char) (c0 - 32);
                    return new String(chars);
                } else if (c0 == '_'
                        && methodName.length() > 1
                        && (c1 = methodName.charAt(1)) >= 'a'
                        && c1 <= 'z') {
                    char[] chars = methodName.toCharArray();
                    chars[1] = (char) (c1 - 32);
                    return new String(chars);
                }
                return methodName;
            }
            case "SnakeCase":
                return snakeCase(methodName, 0);
            case "UpperCaseWithUnderScores":
                return underScores(methodName, 0, true);
            case "LowerCaseWithUnderScores":
                return underScores(methodName, 0, false);
            case "UpperCaseWithDashes":
                return dashes(methodName, 0, true);
            case "LowerCaseWithDashes":
                return dashes(methodName, 0, false);
            case "UpperCaseWithDots":
                return dots(methodName, 0, true);
            case "LowerCaseWithDots":
                return dots(methodName, 0, false);
            case "UpperCase":
                return methodName.toUpperCase();
            case "LowerCase":
                return methodName.toLowerCase();
            case "UpperCamelCaseWithSpaces":
                return upperCamelWith(methodName, 0, ' ');
            case "UpperCamelCaseWithUnderScores":
                return upperCamelWith(methodName, 0, '_');
            case "UpperCamelCaseWithDashes":
                return upperCamelWith(methodName, 0, '-');
            case "UpperCamelCaseWithDots":
                return upperCamelWith(methodName, 0, '.');
            case "KebabCase": {
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < methodName.length(); ++i) {
                    char ch = methodName.charAt(i);
                    if (ch >= 'A' && ch <= 'Z') {
                        char u = (char) (ch + 32);
                        if (i > 0) {
                            buf.append('-');
                        }
                        buf.append(u);
                    } else {
                        buf.append(ch);
                    }
                }
                return buf.toString();
            }
            default:
                throw new JSONException("TODO : " + namingStrategy);
        }
    }

    static String snakeCase(String methodName, int prefixLength) {
        final int methodNameLength = methodName.length();

        char[] buf = TypeUtils.CHARS_UPDATER.getAndSet(TypeUtils.CACHE, null);
        if (buf == null) {
            buf = new char[128];
        }
        try {
            int off = 0;
            for (int i = prefixLength; i < methodNameLength; ++i) {
                char ch = methodName.charAt(i);
                if (ch >= 'A' && ch <= 'Z') {
                    char u = (char) (ch + 32);
                    if (i > prefixLength) {
                        buf[off++] = '_';
                    }
                    buf[off++] = u;
                } else {
                    buf[off++] = ch;
                }
            }
            return new String(buf, 0, off);
        } finally {
            TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, buf);
        }
    }

    static String upperCamelWith(String methodName, int prefixLength, char separator) {
        final int methodNameLength = methodName.length();

        char[] buf = TypeUtils.CHARS_UPDATER.getAndSet(TypeUtils.CACHE, null);
        if (buf == null) {
            buf = new char[128];
        }
        try {
            int off = 0;
            for (int i = prefixLength; i < methodNameLength; ++i) {
                char ch = methodName.charAt(i);
                char c1;
                if (i == prefixLength) {
                    if (ch >= 'a' && ch <= 'z'
                            && i + 1 < methodNameLength
                            && (c1 = methodName.charAt(i + 1)) >= 'a'
                            && c1 <= 'z') {
                        buf[off++] = (char) (ch - 32);
                    } else if (ch == '_' && i + 1 < methodNameLength
                            && (c1 = methodName.charAt(i + 1)) >= 'a'
                            && c1 <= 'z') {
                        buf[off] = ch;
                        buf[off + 1] = (char) (c1 - 32);
                        off += 2;
                        ++i;
                    } else {
                        buf[off++] = ch;
                    }
                } else if (ch >= 'A' && ch <= 'Z'
                        && i + 1 < methodNameLength
                        && ((c1 = methodName.charAt(i + 1)) < 'A' || c1 > 'Z')) {
                    if (i > prefixLength) {
                        buf[off++] = separator;
                    }
                    buf[off++] = ch;
                } else if (ch >= 'A' && ch <= 'Z'
                        && i > prefixLength
                        && i + 1 < methodNameLength
                        && (c1 = methodName.charAt(i + 1)) >= 'A'
                        && c1 <= 'Z'
                        && (c1 = methodName.charAt(i - 1)) >= 'a'
                        && c1 <= 'z') {
                    buf[off++] = separator;
                    buf[off++] = ch;
                } else {
                    buf[off++] = ch;
                }
            }
            return new String(buf, 0, off);
        } finally {
            TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, buf);
        }
    }

    static String underScores(String methodName, int prefixLength, boolean upper) {
        final int methodNameLength = methodName.length();

        char[] buf = TypeUtils.CHARS_UPDATER.getAndSet(TypeUtils.CACHE, null);
        if (buf == null) {
            buf = new char[128];
        }
        try {
            int off = 0;
            for (int i = prefixLength; i < methodNameLength; ++i) {
                char ch = methodName.charAt(i);
                if (upper) {
                    if (ch < 'A' || ch > 'Z') {
                        if (ch >= 'a' && ch <= 'z') {
                            ch -= 32;
                        }
                    } else {
                        if (i > prefixLength) {
                            buf[off++] = '_';
                        }
                    }
                    buf[off++] = ch;
                } else {
                    if (ch >= 'A' && ch <= 'Z') {
                        if (i > prefixLength) {
                            buf[off++] = '_';
                        }
                        buf[off++] = (char) (ch + 32);
                    } else {
                        buf[off++] = ch;
                    }
                }
            }
            return new String(buf, 0, off);
        } finally {
            TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, buf);
        }
    }

    static String dashes(String methodName, int prefixLength, boolean upper) {
        final int methodNameLength = methodName.length();

        char[] buf = TypeUtils.CHARS_UPDATER.getAndSet(TypeUtils.CACHE, null);
        if (buf == null) {
            buf = new char[128];
        }
        try {
            int off = 0;
            for (int i = prefixLength; i < methodNameLength; ++i) {
                char ch = methodName.charAt(i);
                if (upper) {
                    if (ch >= 'A' && ch <= 'Z') {
                        if (i > prefixLength) {
                            buf[off++] = '-';
                        }
                    } else {
                        if (ch >= 'a' && ch <= 'z') {
                            ch -= 32;
                        }
                    }
                    buf[off++] = ch;
                } else {
                    if (ch >= 'A' && ch <= 'Z') {
                        if (i > prefixLength) {
                            buf[off++] = '-';
                        }
                        buf[off++] = (char) (ch + 32);
                    } else {
                        buf[off++] = ch;
                    }
                }
            }
            return new String(buf, 0, off);
        } finally {
            TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, buf);
        }
    }

    static String dots(String methodName, int prefixLength, boolean upper) {
        final int methodNameLength = methodName.length();

        char[] buf = TypeUtils.CHARS_UPDATER.getAndSet(TypeUtils.CACHE, null);
        if (buf == null) {
            buf = new char[128];
        }
        try {
            int off = 0;
            for (int i = prefixLength; i < methodNameLength; ++i) {
                char ch = methodName.charAt(i);
                if (upper) {
                    if (ch >= 'A' && ch <= 'Z') {
                        if (i > prefixLength) {
                            buf[off++] = '.';
                        }
                    } else {
                        if (ch >= 'a' && ch <= 'z') {
                            ch -= 32;
                        }
                    }
                    buf[off++] = ch;
                } else {
                    if (ch >= 'A' && ch <= 'Z') {
                        if (i > prefixLength) {
                            buf[off++] = '.';
                        }
                        buf[off++] = (char) (ch + 32);
                    } else {
                        buf[off++] = ch;
                    }
                }
            }
            return new String(buf, 0, off);
        } finally {
            TypeUtils.CHARS_UPDATER.set(TypeUtils.CACHE, buf);
        }
    }

    public static Type getFieldType(TypeReference typeReference, Class<?> raw, Member field, Type fieldType) {
        Class<?> declaringClass;
        if (field == null) {
            declaringClass = null;
        } else {
            declaringClass = field.getDeclaringClass();
        }

        while (raw != Object.class) {
            Type type = typeReference == null ? null : typeReference.getType();
            if (declaringClass == raw) {
                return resolve(type, declaringClass, fieldType);
            }
            Type superType = raw.getGenericSuperclass();
            // interface has no generic super class
            if (superType == null) {
                break;
            }
            typeReference = TypeReference.get(resolve(type, raw, superType));
            raw = typeReference.getRawType();
        }
        return null;
    }

    public static Type getParamType(TypeReference type,
                                    Class<?> raw,
                                    Class declaringClass,
                                    Parameter field,
                                    Type fieldType) {
        while (raw != Object.class) {
            if (declaringClass == raw) {
                return resolve(type.getType(), declaringClass, fieldType);
            }
            type = TypeReference.get(resolve(type.getType(), raw, raw.getGenericSuperclass()));
            raw = type.getRawType();
        }
        return null;
    }

    /**
     * Returns a new parameterized type, applying {@code typeArguments} to
     * {@code rawType} and enclosed by {@code ownerType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType newParameterizedTypeWithOwner(
            Type ownerType, Type rawType, Type... typeArguments) {
        return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
    }

    /**
     * Returns an array type whose elements are all instances of
     * {@code componentType}.
     *
     * @return a {@link java.io.Serializable serializable} generic array type.
     */
    public static GenericArrayType arrayOf(Type componentType) {
        return new GenericArrayTypeImpl(componentType);
    }

    /**
     * Returns a type that represents an unknown type that extends {@code bound}.
     * For example, if {@code bound} is {@code CharSequence.class}, this returns
     * {@code ? extends CharSequence}. If {@code bound} is {@code Object.class},
     * this returns {@code ?}, which is shorthand for {@code ? extends Object}.
     */
    public static WildcardType subtypeOf(Type bound) {
        Type[] upperBounds;
        if (bound instanceof WildcardType) {
            upperBounds = ((WildcardType) bound).getUpperBounds();
        } else {
            upperBounds = new Type[]{bound};
        }
        return new WildcardTypeImpl(upperBounds, EMPTY_TYPE_ARRAY);
    }

    /**
     * Returns a type that represents an unknown supertype of {@code bound}. For
     * example, if {@code bound} is {@code String.class}, this returns {@code ?
     * super String}.
     */
    public static WildcardType supertypeOf(Type bound) {
        Type[] lowerBounds;
        if (bound instanceof WildcardType) {
            lowerBounds = ((WildcardType) bound).getLowerBounds();
        } else {
            lowerBounds = new Type[]{bound};
        }
        return new WildcardTypeImpl(new Type[]{Object.class}, lowerBounds);
    }

    /**
     * Returns a type that is functionally equal but not necessarily equal
     * according to {@link Object#equals(Object) Object.equals()}. The returned
     * type is {@link java.io.Serializable}.
     */
    public static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(),
                    p.getRawType(), p.getActualTypeArguments());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            return new GenericArrayTypeImpl(g.getGenericComponentType());
        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
        } else {
            // type is either serializable as-is or unsupported
            return type;
        }
    }

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // type is a normal class.
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class.
            // Neal isn't either but suspects some pathological case related
            // to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            checkArgument(rawType instanceof Class);
            return (Class<?>) rawType;
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        } else if (type instanceof TypeVariable) {
            // we could use the variable's bounds, but that won't work if there are multiple.
            // having a raw type that's more general than necessary is okay
            return Object.class;
        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        } else {
            String className = type == null ? "null" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                    + "GenericArrayType, but <" + type + "> is of type " + className);
        }
    }

    static boolean equal(Object a, Object b) {
        return Objects.equals(a, b);
    }

    /**
     * Returns true if {@code a} and {@code b} are equal.
     */
    public static boolean equals(Type a, Type b) {
        if (a == b) {
            // also handles (a == null && b == null)
            return true;
        } else if (a instanceof Class) {
            // Class already specifies equals().
            return a.equals(b);
        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            // TODO: save a .clone() call
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equal(pa.getOwnerType(), pb.getOwnerType())
                    && pa.getRawType().equals(pb.getRawType())
                    && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());
        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) {
                return false;
            }

            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                    && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());
        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) {
                return false;
            }
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration()
                    && va.getName().equals(vb.getName());
        } else {
            // This isn't a type we support. Could be a generic array type, wildcard type, etc.
            return false;
        }
    }

    static int hashCodeOrZero(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    public static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }

    /**
     * Returns the generic supertype for {@code supertype}. For example, given a class {@code
     * IntegerSet}, the result for when supertype is {@code Set.class} is {@code Set<Integer>} and the
     * result when the supertype is {@code Collection.class} is {@code Collection<Integer>}.
     */
    static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) {
            return context;
        }

        // we skip searching through interfaces if unknown is an interface
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // check our supertypes
        if (rawType != null && !rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // we can't resolve this further
        return toResolve;
    }

    public static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
        return resolve(context, contextRawType, toResolve, new HashMap<>());
    }

    private static Type resolve(Type context, Class<?> contextRawType, Type toResolve,
                                Map<TypeVariable<?>, Type> visitedTypeVariables) {
        // this implementation is made a little more complicated in an attempt to avoid object-creation
        TypeVariable<?> resolving = null;
        while (true) {
            if (toResolve instanceof TypeVariable) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
                Type previouslyResolved = visitedTypeVariables.get(typeVariable);
                if (previouslyResolved != null) {
                    // cannot reduce due to infinite recursion
                    return (previouslyResolved == Void.TYPE) ? toResolve : previouslyResolved;
                }

                // Insert a placeholder to mark the fact that we are in the process of resolving this type
                visitedTypeVariables.put(typeVariable, Void.TYPE);
                if (resolving == null) {
                    resolving = typeVariable;
                }

                toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
                if (toResolve == typeVariable) {
                    break;
                }
            } else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
                Class<?> original = (Class<?>) toResolve;
                Type componentType = original.getComponentType();
                Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
                toResolve = equal(componentType, newComponentType)
                        ? original
                        : arrayOf(newComponentType);
                break;
            } else if (toResolve instanceof GenericArrayType) {
                GenericArrayType original = (GenericArrayType) toResolve;
                Type componentType = original.getGenericComponentType();
                Type newComponentType = resolve(context, contextRawType, componentType, visitedTypeVariables);
                toResolve = equal(componentType, newComponentType)
                        ? original
                        : arrayOf(newComponentType);
                break;
            } else if (toResolve instanceof ParameterizedType) {
                ParameterizedType original = (ParameterizedType) toResolve;
                Type ownerType = original.getOwnerType();
                Type newOwnerType = resolve(context, contextRawType, ownerType, visitedTypeVariables);
                boolean changed = !equal(newOwnerType, ownerType);

                Type[] args = original.getActualTypeArguments();
                for (int t = 0, length = args.length; t < length; t++) {
                    Type resolvedTypeArgument = resolve(context, contextRawType, args[t], visitedTypeVariables);
                    if (!equal(resolvedTypeArgument, args[t])) {
                        if (!changed) {
                            args = args.clone();
                            changed = true;
                        }
                        args[t] = resolvedTypeArgument;
                    }
                }

                toResolve = changed
                        ? newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
                        : original;
                break;
            } else if (toResolve instanceof WildcardType) {
                WildcardType original = (WildcardType) toResolve;
                Type[] originalLowerBound = original.getLowerBounds();
                Type[] originalUpperBound = original.getUpperBounds();

                if (originalLowerBound.length == 1) {
                    Type lowerBound = resolve(context, contextRawType, originalLowerBound[0], visitedTypeVariables);
                    if (lowerBound != originalLowerBound[0]) {
                        toResolve = supertypeOf(lowerBound);
                        break;
                    }
                } else if (originalUpperBound.length == 1) {
                    Type upperBound = resolve(context, contextRawType, originalUpperBound[0], visitedTypeVariables);
                    if (upperBound != originalUpperBound[0]) {
                        toResolve = subtypeOf(upperBound);
                        break;
                    }
                }
                break;
            } else {
                break;
            }
        }
        // ensure that any in-process resolution gets updated with the final result
        if (resolving != null) {
            visitedTypeVariables.put(resolving, toResolve);
        }
        return toResolve;
    }

    static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        // we can't reduce this further
        if (declaredByRaw == null) {
            return unknown;
        }

        Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        return unknown;
    }

    private static int indexOf(Object[] array, Object toFind) {
        for (int i = 0, length = array.length; i < length; i++) {
            if (toFind.equals(array[i])) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by
     * a class.
     */
    private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class
                ? (Class<?>) genericDeclaration
                : null;
    }

    static void checkNotPrimitive(Type type) {
        checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());
    }

    /**
     * Find the first annotation of {@code annotationType} that is either
     * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
     * present</em> on the supplied {@code element}.
     *
     * <p>If the element is a class and the annotation is neither <em>directly
     * present</em> nor <em>meta-present</em> on the class, this method will additionally search on
     * interfaces implemented by the class before finding an annotation that is <em>indirectly
     * present</em> on the class.
     *
     * @param element the element on which to search for the annotation
     * @param annotationType the annotation type of need to search
     * @param <A> the annotation
     * @return the searched annotation type
     */
    public static <A extends Annotation> A findAnnotation(AnnotatedElement element, Class<A> annotationType) {
        if (annotationType == null) {
            throw new NullPointerException("annotationType must not be null");
        }

        boolean inherited = annotationType.isAnnotationPresent(Inherited.class);
        return findAnnotation(element, annotationType, inherited, new HashSet<>());
    }

    /**
     * If the {@code annotation}'s annotationType is not {@code annotationType}, then to find the
     * first annotation of {@code annotationType} that is either
     * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
     * present</em> on the supplied {@code element}.
     *
     * @param annotation annotation
     * @param annotationType the annotation type of need to search
     * @param <A> the searched annotation type
     * @return the searched annotation
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A findAnnotation(Annotation annotation, Class<A> annotationType) {
        if (annotation == null) {
            throw new NullPointerException("annotation must not be null");
        }
        if (annotationType == null) {
            throw new NullPointerException("annotationType must not be null");
        }

        Class<? extends Annotation> annotationTypeClass = annotation.annotationType();
        if (annotationTypeClass == annotationType) {
            return (A) annotation;
        }

        boolean inherited = annotationType.isAnnotationPresent(Inherited.class);
        return findAnnotation(annotationTypeClass, annotationType, inherited, new HashSet<>());
    }

    /**
     * Find the first annotation of {@code annotationType} that is either
     * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
     * present</em> on the supplied {@code element}.
     *
     * <p>If the element is a class and the annotation is neither <em>directly
     * present</em> nor <em>meta-present</em> on the class, this method will additionally search on
     * interfaces implemented by the class before finding an annotation that is <em>indirectly
     * present</em> on the class.
     *
     * @param element the element on which to search for the annotation
     * @param annotationType the annotation type of need to search
     * @param inherited whether has {@link Inherited}
     * @param visited this annotation whether visited
     * @param <A> the annotation type
     * @return the searched annotation
     */
    private static <A extends Annotation> A findAnnotation(
            AnnotatedElement element,
            Class<A> annotationType,
            boolean inherited,
            Set<Annotation> visited
    ) {
        if (element == null || annotationType == null) {
            return null;
        }

        A annotation = element.getDeclaredAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }

        Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
        A directMetaAnnotation = findMetaAnnotation(annotationType, declaredAnnotations, inherited, visited);
        if (directMetaAnnotation != null) {
            return directMetaAnnotation;
        }

        if (element instanceof Class) {
            Class<?> clazz = (Class<?>) element;

            for (Class<?> ifc : clazz.getInterfaces()) {
                if (ifc != Annotation.class) {
                    A annotationOnInterface = findAnnotation(ifc, annotationType, inherited, visited);
                    if (annotationOnInterface != null) {
                        return annotationOnInterface;
                    }
                }
            }

            if (inherited) {
                Class<?> superclass = clazz.getSuperclass();
                if (superclass != null && superclass != Object.class) {
                    A annotationOnSuperclass = findAnnotation(superclass, annotationType, true, visited);
                    if (annotationOnSuperclass != null) {
                        return annotationOnSuperclass;
                    }
                }
            }
        }

        return findMetaAnnotation(annotationType, getAnnotations(element), inherited, visited);
    }

    /**
     * Find meta-present on indirectly present annotations.
     *
     * @param annotationType the annotation type of need to search
     * @param candidates annotations for candidates
     * @param inherited whether has {@link Inherited}
     * @param visited this annotation whether visited
     * @param <A> the annotation type
     * @return the searched annotation
     */
    private static <A extends Annotation> A findMetaAnnotation(
            Class<A> annotationType,
            Annotation[] candidates,
            boolean inherited,
            Set<Annotation> visited
    ) {
        for (Annotation candidateAnnotation : candidates) {
            Class<? extends Annotation> candidateAnnotationType = candidateAnnotation.annotationType();
            String name = candidateAnnotationType.getName();
            boolean isInJavaLangAnnotationPackage = name.startsWith("java.lang.annotation") || name.startsWith("kotlin.");
            if (!isInJavaLangAnnotationPackage && visited.add(candidateAnnotation)) {
                A metaAnnotation = findAnnotation(candidateAnnotationType, annotationType, inherited, visited);
                if (metaAnnotation != null) {
                    return metaAnnotation;
                }
            }
        }
        return null;
    }

    public static Annotation[] getAnnotations(AnnotatedElement element) {
        try {
            return element.getDeclaredAnnotations();
        } catch (Throwable ignored) {
            return new Annotation[0];
        }
    }

    static final class ParameterizedTypeImpl
            implements ParameterizedType, Serializable {
        private final Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
            // require an owner type if the raw type needs it
            if (rawType instanceof Class<?>) {
                Class<?> rawTypeAsClass = (Class<?>) rawType;
                boolean isStaticOrTopLevelClass = Modifier.isStatic(rawTypeAsClass.getModifiers())
                        || rawTypeAsClass.getEnclosingClass() == null;
                checkArgument(ownerType != null || isStaticOrTopLevelClass);
            }

            this.ownerType = ownerType == null ? null : canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = typeArguments.clone();
            for (int t = 0, length = this.typeArguments.length; t < length; t++) {
                checkNotPrimitive(this.typeArguments[t]);
                this.typeArguments[t] = canonicalize(this.typeArguments[t]);
            }
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType
                    && BeanUtils.equals(this, (ParameterizedType) other);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments)
                    ^ rawType.hashCode()
                    ^ hashCodeOrZero(ownerType);
        }

        @Override
        public String toString() {
            int length = typeArguments.length;
            if (length == 0) {
                return typeToString(rawType);
            }

            StringBuilder stringBuilder = new StringBuilder(30 * (length + 1));
            stringBuilder.append(typeToString(rawType)).append("<").append(typeToString(typeArguments[0]));
            for (int i = 1; i < length; i++) {
                stringBuilder.append(", ").append(typeToString(typeArguments[i]));
            }
            return stringBuilder.append(">").toString();
        }

        private static final long serialVersionUID = 0;
    }

    public static final class GenericArrayTypeImpl
            implements GenericArrayType, Serializable {
        private final Type componentType;

        public GenericArrayTypeImpl(Type componentType) {
            this.componentType = canonicalize(componentType);
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType
                    && BeanUtils.equals(this, (GenericArrayType) o);
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

        @Override
        public String toString() {
            return typeToString(componentType) + "[]";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * The WildcardType interface supports multiple upper bounds and multiple
     * lower bounds. We only support what the Java 6 language needs - at most one
     * bound. If a lower bound is set, the upper bound must be Object.class.
     */
    static final class WildcardTypeImpl
            implements WildcardType, Serializable {
        private final Type upperBound;
        private final Type lowerBound;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            checkArgument(lowerBounds.length <= 1);
            checkArgument(upperBounds.length == 1);

            if (lowerBounds.length == 1) {
                checkNotPrimitive(lowerBounds[0]);
                checkArgument(upperBounds[0] == Object.class);
                this.lowerBound = canonicalize(lowerBounds[0]);
                this.upperBound = Object.class;
            } else {
                checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = canonicalize(upperBounds[0]);
            }
        }

        @Override
        public Type[] getUpperBounds() {
            return new Type[]{upperBound};
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBound != null ? new Type[]{lowerBound} : EMPTY_TYPE_ARRAY;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType
                    && BeanUtils.equals(this, (WildcardType) other);
        }

        @Override
        public int hashCode() {
            // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1)
                    ^ (31 + upperBound.hashCode());
        }

        @Override
        public String toString() {
            if (lowerBound != null) {
                return "? super " + typeToString(lowerBound);
            } else if (upperBound == Object.class) {
                return "?";
            } else {
                return "? extends " + typeToString(upperBound);
            }
        }

        private static final long serialVersionUID = 0;
    }

    static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    public static void processJacksonJsonIgnore(FieldInfo fieldInfo, Annotation annotation) {
        fieldInfo.ignore = true;
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                if (name.equals("value")) {
                    fieldInfo.ignore = (boolean) (Boolean) result;
                }
            } catch (Throwable ignored) {
                // ignored
            }
        });
    }

    public static boolean isNoneStaticMemberClass(Class objectClass, Class memberClass) {
        if (memberClass == null
                || memberClass.isPrimitive()
                || memberClass == String.class
                || memberClass == List.class
        ) {
            return false;
        }

        Class enclosingClass = memberClass.getEnclosingClass();
        if (enclosingClass == null) {
            return false;
        }

        if (objectClass != null && !objectClass.equals(enclosingClass)) {
            return false;
        }

        Constructor[] constructors = constructorCache.get(memberClass);
        if (constructors == null) {
            constructors = memberClass.getDeclaredConstructors();
            constructorCache.putIfAbsent(memberClass, constructors);
        }

        if (constructors.length == 0) {
            return false;
        }

        Constructor firstConstructor = constructors[0];
        if (firstConstructor.getParameterCount() == 0) {
            return false;
        }

        Class[] parameterTypes = firstConstructor.getParameterTypes();
        return enclosingClass.equals(parameterTypes[0]);
    }

    public static void setNoneStaticMemberClassParent(Object object, Object parent) {
        Class objectClass = object.getClass();
        Field[] fields = declaredFieldCache.get(objectClass);
        if (fields == null) {
            Field[] declaredFields = objectClass.getDeclaredFields();

            boolean allMatch = true;
            for (Field field : declaredFields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) {
                fields = declaredFields;
            } else {
                List<Field> list = new ArrayList<>(declaredFields.length);
                for (Field field : declaredFields) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers)) {
                        continue;
                    }
                    list.add(field);
                }
                fields = list.toArray(new Field[list.size()]);
            }

            fieldCache.putIfAbsent(objectClass, fields);
        }

        Field this0 = null;
        for (Field field : fields) {
            if ("this$0".equals(field.getName())) {
                this0 = field;
            }
        }

        if (this0 != null) {
            this0.setAccessible(true);
            try {
                this0.set(object, parent);
            } catch (IllegalAccessException e) {
                throw new JSONException("setNoneStaticMemberClassParent error, class " + objectClass);
            }
        }
    }

    public static void cleanupCache(Class objectClass) {
        if (objectClass == null) {
            return;
        }

        fieldCache.remove(objectClass);
        fieldMapCache.remove(objectClass);
        declaredFieldCache.remove(objectClass);
        methodCache.remove(objectClass);
        constructorCache.remove(objectClass);
    }

    public static void cleanupCache(ClassLoader classLoader) {
        for (Iterator<Map.Entry<Class, Field[]>> it = fieldCache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Class, Field[]> entry = it.next();
            Class entryKey = entry.getKey();
            if (entryKey.getClassLoader() == classLoader) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<Class, Map<String, Field>>> it = fieldMapCache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Class, Map<String, Field>> entry = it.next();
            Class entryKey = entry.getKey();
            if (entryKey.getClassLoader() == classLoader) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<Class, Field[]>> it = declaredFieldCache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Class, Field[]> entry = it.next();
            Class entryKey = entry.getKey();
            if (entryKey.getClassLoader() == classLoader) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<Class, Method[]>> it = methodCache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Class, Method[]> entry = it.next();
            Class entryKey = entry.getKey();
            if (entryKey.getClassLoader() == classLoader) {
                it.remove();
            }
        }

        for (Iterator<Map.Entry<Class, Constructor[]>> it = constructorCache.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Class, Constructor[]> entry = it.next();
            Class entryKey = entry.getKey();
            if (entryKey.getClassLoader() == classLoader) {
                it.remove();
            }
        }
    }

    public static void processJSONType1x(BeanInfo beanInfo, Annotation jsonType1x, Method method) {
        try {
            Object result = method.invoke(jsonType1x);
            switch (method.getName()) {
                case "seeAlso": {
                    Class<?>[] classes = (Class[]) result;
                    if (classes.length != 0) {
                        beanInfo.seeAlso = classes;
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
                case "typeKey": {
                    String typeKey = (String) result;
                    if (!typeKey.isEmpty()) {
                        beanInfo.typeKey = typeKey;
                    }
                    break;
                }
                case "alphabetic": {
                    Boolean alphabetic = (Boolean) result;
                    if (!alphabetic) {
                        beanInfo.alphabetic = false;
                    }
                    break;
                }
                case "serializeFeatures":
                case "serialzeFeatures": {
                    Enum[] serializeFeatures = (Enum[]) result;
                    for (Enum feature : serializeFeatures) {
                        switch (feature.name()) {
                            case "WriteMapNullValue":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNulls.mask;
                                break;
                            case "WriteNullListAsEmpty":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNullListAsEmpty.mask;
                                break;
                            case "WriteNullStringAsEmpty":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNullStringAsEmpty.mask;
                                break;
                            case "WriteNullNumberAsZero":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNullNumberAsZero.mask;
                                break;
                            case "WriteNullBooleanAsFalse":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNullBooleanAsFalse.mask;
                                break;
                            case "BrowserCompatible":
                                beanInfo.writerFeatures |= JSONWriter.Feature.BrowserCompatible.mask;
                                break;
                            case "WriteClassName":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteClassName.mask;
                                break;
                            case "WriteNonStringValueAsString":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
                                break;
                            case "WriteEnumUsingToString":
                                beanInfo.writerFeatures |= JSONWriter.Feature.WriteEnumUsingToString.mask;
                                break;
                            case "NotWriteRootClassName":
                                beanInfo.writerFeatures |= JSONWriter.Feature.NotWriteRootClassName.mask;
                                break;
                            case "IgnoreErrorGetter":
                                beanInfo.writerFeatures |= JSONWriter.Feature.IgnoreErrorGetter.mask;
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                }
                case "serializeEnumAsJavaBean": {
                    boolean serializeEnumAsJavaBean = (Boolean) result;
                    if (serializeEnumAsJavaBean) {
                        beanInfo.writeEnumAsJavaBean = true;
                    }
                    break;
                }
                case "naming": {
                    Enum naming = (Enum) result;
                    beanInfo.namingStrategy = naming.name();
                    break;
                }
                case "ignores": {
                    String[] fields = (String[]) result;
                    if (fields.length != 0) {
                        if (beanInfo.ignores == null) {
                            beanInfo.ignores = fields;
                        } else {
                            LinkedHashSet<String> ignoresSet = new LinkedHashSet<>();
                            ignoresSet.addAll(Arrays.asList(beanInfo.ignores));
                            ignoresSet.addAll(Arrays.asList(fields));
                            beanInfo.ignores = ignoresSet.toArray(new String[ignoresSet.size()]);
                        }
                    }
                    break;
                }
                case "includes": {
                    String[] fields = (String[]) result;
                    if (fields.length != 0) {
                        beanInfo.includes = fields;
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
                default:
                    break;
            }
        } catch (Throwable ignored) {
            // ignored
        }
    }

    public static void processJacksonJsonFormat(FieldInfo fieldInfo, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                switch (name) {
                    case "pattern": {
                        String pattern = (String) result;
                        if (pattern.length() != 0) {
                            fieldInfo.format = pattern;
                        }
                        break;
                    }
                    case "shape": {
                        String shape = ((Enum) result).name();
                        if (shape.equals("STRING")) {
                            fieldInfo.features |= JSONWriter.Feature.WriteNonStringValueAsString.mask;
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

    public static void processJacksonJsonFormat(BeanInfo beanInfo, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                if (name.equals("pattern")) {
                    String pattern = (String) result;
                    if (pattern.length() != 0) {
                        beanInfo.format = pattern;
                    }
                }
            } catch (Throwable ignored) {
                // ignored
            }
        });
    }

    public static void processJacksonJsonInclude(BeanInfo beanInfo, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                if (name.equals("value")) {
                    String include = ((Enum) result).name();
                    switch (include) {
                        case "ALWAYS":
                            beanInfo.writerFeatures |= JSONWriter.Feature.WriteNulls.mask;
                            break;
                        case "NON_DEFAULT":
                            beanInfo.writerFeatures |= JSONWriter.Feature.NotWriteDefaultValue.mask;
                            break;
                        default:
                            break;
                    }
                }
            } catch (Throwable ignored) {
                // ignored
            }
        });
    }

    public static void processJacksonJsonTypeName(BeanInfo beanInfo, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                if (name.equals("value")) {
                    String value = (String) result;
                    if (!value.isEmpty()) {
                        beanInfo.typeName = value;
                    }
                }
            } catch (Throwable ignored) {
                // ignored
            }
        });
    }

    public static void processJacksonJsonSubTypesType(BeanInfo beanInfo, int index, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                switch (name) {
                    case "value": {
                        Class value = (Class) result;
                        beanInfo.seeAlso[index] = value;
                        break;
                    }
                    case "name": {
                        String value = (String) result;
                        beanInfo.seeAlsoNames[index] = value;
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

    public static void processGsonSerializedName(FieldInfo fieldInfo, Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.getClass();
        BeanUtils.annotationMethods(annotationClass, m -> {
            String name = m.getName();
            try {
                Object result = m.invoke(annotation);
                switch (name) {
                    case "value":
                        String value = (String) result;
                        if (!value.isEmpty()) {
                            fieldInfo.fieldName = value;
                        }
                        break;
                    case "alternate":
                        String[] alternate = (String[]) result;
                        if (alternate.length != 0) {
                            fieldInfo.alternateNames = alternate;
                        }
                        break;
                    default:
                        break;
                }
            } catch (Throwable ignored) {
                // ignored
            }
        });
    }

    public static boolean isExtendedMap(Class objectClass) {
        if (objectClass == HashMap.class
                || objectClass == LinkedHashMap.class
                || objectClass == TreeMap.class
                || "".equals(objectClass.getSimpleName())
        ) {
            return false;
        }

        Class superclass = objectClass.getSuperclass();
        if (superclass != HashMap.class
                && superclass != LinkedHashMap.class
                && superclass != TreeMap.class
        ) {
            return false;
        }

        Constructor defaultConstructor = getDefaultConstructor(objectClass, false);
        if (defaultConstructor != null) {
            return false;
        }

        List<Field> fields = new ArrayList<>();
        BeanUtils.declaredFields(objectClass, field -> {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)
                    || Modifier.isTransient(modifiers)
                    || field.getDeclaringClass().isAssignableFrom(superclass)
                    || field.getName().equals("this$0")
            ) {
                return;
            }

            fields.add(field);
        });

        return !fields.isEmpty();
    }
}
