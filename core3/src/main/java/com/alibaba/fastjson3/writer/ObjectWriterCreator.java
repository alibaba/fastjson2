package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.annotation.JSONField;
import com.alibaba.fastjson3.annotation.JSONType;
import com.alibaba.fastjson3.annotation.NamingStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Creates {@link ObjectWriter} instances for arbitrary POJO classes via reflection.
 * Inspects getter methods, public fields, and annotations ({@link JSONField}, {@link JSONType})
 * to build a sorted list of {@link FieldWriter}s that drive serialization.
 */
public final class ObjectWriterCreator {
    private ObjectWriterCreator() {
    }

    /**
     * Create an ObjectWriter for the given type by introspecting its getters, fields, and annotations.
     *
     * @param type the class to create a writer for
     * @return a new ObjectWriter that serializes instances of the given type
     */
    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type) {
        return createObjectWriter(type, null);
    }

    public static <T> ObjectWriter<T> createObjectWriter(Class<T> type, Class<?> mixIn) {
        if (com.alibaba.fastjson3.util.JDKUtils.isRecord(type)) {
            return createRecordWriter(type, mixIn);
        }

        return createPojoWriter(type, mixIn);
    }

    private static <T> ObjectWriter<T> createRecordWriter(Class<T> type, Class<?> mixIn) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null ? Set.of(jsonType.ignores()) : Set.of();
        boolean alphabetic = jsonType == null || jsonType.alphabetic();

        String[] componentNames = com.alibaba.fastjson3.util.JDKUtils.getRecordComponentNames(type);
        java.lang.reflect.Method[] accessors = com.alibaba.fastjson3.util.JDKUtils.getRecordComponentAccessors(type);

        Map<String, FieldWriter> writerMap = new LinkedHashMap<>();
        for (int i = 0; i < componentNames.length; i++) {
            String propertyName = componentNames[i];
            java.lang.reflect.Method accessor = accessors[i];

            if (!includes.isEmpty() && !includes.contains(propertyName)) {
                continue;
            }
            if (ignores.contains(propertyName)) {
                continue;
            }

            JSONField jsonField = accessor.getAnnotation(JSONField.class);
            // Check mixin for annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInAnnotation(mixIn, accessor, propertyName, JSONField.class);
            }
            // Also check field-level annotation
            if (jsonField == null) {
                try {
                    java.lang.reflect.Field f = type.getDeclaredField(propertyName);
                    jsonField = f.getAnnotation(JSONField.class);
                } catch (NoSuchFieldException ignored) {
                }
            }
            // Check mixin field annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInFieldAnnotation(mixIn, propertyName, JSONField.class);
            }
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            String jsonName;
            int ordinal = 0;
            if (jsonField != null && !jsonField.name().isEmpty()) {
                jsonName = jsonField.name();
            } else {
                jsonName = applyNamingStrategy(propertyName, naming);
            }
            if (jsonField != null) {
                ordinal = jsonField.ordinal();
            }

            // Prefer backing field for Unsafe direct access
            java.lang.reflect.Field backingField = null;
            try {
                backingField = type.getDeclaredField(propertyName);
                backingField.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
            }

            if (backingField != null) {
                writerMap.put(propertyName, createFieldWriterForField(
                        jsonName, ordinal, accessor.getGenericReturnType(), accessor.getReturnType(), backingField
                ));
            } else {
                accessor.setAccessible(true);
                writerMap.put(propertyName, createFieldWriterForGetter(
                        jsonName, ordinal, accessor.getGenericReturnType(), accessor.getReturnType(), accessor
                ));
            }
        }

        List<FieldWriter> fieldWriters = new ArrayList<>(writerMap.values());
        if (alphabetic) {
            Collections.sort(fieldWriters);
        }
        FieldWriter[] writers = fieldWriters.toArray(new FieldWriter[0]);

        return buildObjectWriter(writers);
    }

    private static <T> ObjectWriter<T> createPojoWriter(Class<T> type, Class<?> mixIn) {
        JSONType jsonType = type.getAnnotation(JSONType.class);
        NamingStrategy naming = jsonType != null ? jsonType.naming() : NamingStrategy.NoneStrategy;
        Set<String> includes = jsonType != null ? Set.of(jsonType.includes()) : Set.of();
        Set<String> ignores = jsonType != null ? Set.of(jsonType.ignores()) : Set.of();
        String[] orders = jsonType != null ? jsonType.orders() : new String[0];
        boolean alphabetic = jsonType == null || jsonType.alphabetic();

        // Collect field writers keyed by property name to deduplicate getter vs field
        Map<String, FieldWriter> writerMap = new LinkedHashMap<>();

        // 1. Inspect getter methods
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            String methodName = method.getName();
            String propertyName = extractPropertyName(methodName, method.getReturnType());
            if (propertyName == null) {
                continue;
            }

            JSONField jsonField = method.getAnnotation(JSONField.class);
            // Check mixin for getter annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInAnnotation(mixIn, method, propertyName, JSONField.class);
            }
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            String jsonName;
            int ordinal = 0;

            if (jsonField != null && !jsonField.name().isEmpty()) {
                jsonName = jsonField.name();
            } else {
                jsonName = applyNamingStrategy(propertyName, naming);
            }

            if (jsonField != null) {
                ordinal = jsonField.ordinal();
            }

            // Check field-level annotation as well (for the corresponding field)
            if (jsonField == null) {
                try {
                    Field f = findDeclaredField(type, propertyName);
                    if (f != null) {
                        JSONField fieldAnnotation = f.getAnnotation(JSONField.class);
                        // Also check mixin field
                        if (fieldAnnotation == null && mixIn != null) {
                            fieldAnnotation = findMixInFieldAnnotation(mixIn, propertyName, JSONField.class);
                        }
                        if (fieldAnnotation != null) {
                            if (!fieldAnnotation.serialize()) {
                                continue;
                            }
                            if (!fieldAnnotation.name().isEmpty()) {
                                jsonName = fieldAnnotation.name();
                            }
                            ordinal = fieldAnnotation.ordinal();
                        }
                    }
                } catch (Exception ignored) {
                    // field lookup failure is not fatal
                }
            }

            // Prefer backing field for Unsafe direct access (avoids Method.invoke overhead)
            Field backingField = findDeclaredField(type, propertyName);
            if (backingField != null) {
                backingField.setAccessible(true);
                writerMap.put(propertyName, createFieldWriterForField(
                        jsonName, ordinal, method.getGenericReturnType(), method.getReturnType(), backingField
                ));
            } else {
                method.setAccessible(true);
                writerMap.put(propertyName, createFieldWriterForGetter(
                        jsonName, ordinal, method.getGenericReturnType(), method.getReturnType(), method
                ));
            }
        }

        // 2. Inspect public fields as fallback (only if no getter already found)
        for (Field field : type.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            String propertyName = field.getName();
            if (writerMap.containsKey(propertyName)) {
                continue;
            }

            JSONField jsonField = field.getAnnotation(JSONField.class);
            // Check mixin for field annotation
            if (jsonField == null && mixIn != null) {
                jsonField = findMixInFieldAnnotation(mixIn, propertyName, JSONField.class);
            }
            if (jsonField != null && !jsonField.serialize()) {
                continue;
            }

            String jsonName;
            int ordinal = 0;

            if (jsonField != null && !jsonField.name().isEmpty()) {
                jsonName = jsonField.name();
            } else {
                jsonName = applyNamingStrategy(propertyName, naming);
            }

            if (jsonField != null) {
                ordinal = jsonField.ordinal();
            }

            field.setAccessible(true);
            writerMap.put(propertyName, createFieldWriterForField(
                    jsonName, ordinal, field.getGenericType(), field.getType(), field
            ));
        }

        // 3. Apply includes/ignores filters
        if (!includes.isEmpty()) {
            writerMap.keySet().retainAll(includes);
        }
        if (!ignores.isEmpty()) {
            writerMap.keySet().removeAll(ignores);
        }

        // 4. Sort: apply order hints, then ordinal, then alphabetic
        List<FieldWriter> fieldWriters = new ArrayList<>(writerMap.values());

        if (orders.length > 0) {
            Map<String, Integer> orderMap = new HashMap<>();
            for (int i = 0; i < orders.length; i++) {
                orderMap.put(orders[i], i);
            }
            fieldWriters.sort((a, b) -> {
                int oa = orderMap.getOrDefault(a.getFieldName(), Integer.MAX_VALUE);
                int ob = orderMap.getOrDefault(b.getFieldName(), Integer.MAX_VALUE);
                int cmp = Integer.compare(oa, ob);
                if (cmp != 0) {
                    return cmp;
                }
                return a.compareTo(b);
            });
        } else if (alphabetic) {
            Collections.sort(fieldWriters);
        }

        FieldWriter[] writers = fieldWriters.toArray(new FieldWriter[0]);

        return buildObjectWriter(writers);
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectWriter<T> buildObjectWriter(FieldWriter[] writers) {
        return (generator, object, fieldName, fieldType, features) -> {
            generator.startObject();
            if (generator.hasFilters()) {
                for (FieldWriter fw : writers) {
                    fw.writeFieldFiltered(generator, object, features,
                            generator.propertyFilters, generator.valueFilters, generator.nameFilters);
                }
            } else {
                for (FieldWriter fw : writers) {
                    fw.writeField(generator, object, features);
                }
            }
            generator.endObject();
        };
    }

    /**
     * Extract the property name from a getter method name.
     * Supports getXxx and isXxx (for boolean) patterns.
     *
     * @return the property name with first letter lowercased, or null if not a getter
     */
    private static String extractPropertyName(String methodName, Class<?> returnType) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2
                && (returnType == boolean.class || returnType == Boolean.class)) {
            return decapitalize(methodName.substring(2));
        }
        return null;
    }

    /**
     * Decapitalize a string following JavaBeans conventions.
     * If the first two characters are both uppercase, return as-is (e.g., "URL" stays "URL").
     */
    private static String decapitalize(String name) {
        if (name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Apply a naming strategy to convert a Java property name to a JSON field name.
     */
    static String applyNamingStrategy(String name, NamingStrategy strategy) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (strategy == NamingStrategy.NoneStrategy || strategy == NamingStrategy.CamelCase) {
            return name;
        } else if (strategy == NamingStrategy.PascalCase) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else if (strategy == NamingStrategy.SnakeCase) {
            return camelToSeparated(name, '_', false);
        } else if (strategy == NamingStrategy.UpperSnakeCase) {
            return camelToSeparated(name, '_', true);
        } else if (strategy == NamingStrategy.KebabCase) {
            return camelToSeparated(name, '-', false);
        } else if (strategy == NamingStrategy.UpperKebabCase) {
            return camelToSeparated(name, '-', true);
        }
        return name;
    }

    /**
     * Convert camelCase to a separated format (snake_case, kebab-case, etc.).
     *
     * @param name      the camelCase name
     * @param separator the separator character ('_' or '-')
     * @param upper     whether to uppercase the result
     */
    private static String camelToSeparated(String name, char separator, boolean upper) {
        var sb = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) {
                    sb.append(separator);
                }
                sb.append(upper ? ch : Character.toLowerCase(ch));
            } else {
                sb.append(upper ? Character.toUpperCase(ch) : ch);
            }
        }
        return sb.toString();
    }

    private static FieldWriter createFieldWriterForGetter(
            String jsonName, int ordinal, Type fieldType, Class<?> fieldClass, Method method
    ) {
        if (List.class.isAssignableFrom(fieldClass)) {
            Class<?> elemClass = extractListElementClass(fieldType);
            if (elemClass != null) {
                return FieldWriter.ofList(jsonName, ordinal, fieldType, fieldClass, elemClass, method);
            }
        }
        return FieldWriter.ofGetter(jsonName, ordinal, fieldType, fieldClass, method);
    }

    private static FieldWriter createFieldWriterForField(
            String jsonName, int ordinal, Type fieldType, Class<?> fieldClass, Field field
    ) {
        if (List.class.isAssignableFrom(fieldClass)) {
            Class<?> elemClass = extractListElementClass(fieldType);
            if (elemClass != null) {
                return FieldWriter.ofList(jsonName, ordinal, fieldType, fieldClass, elemClass, field);
            }
        }
        return FieldWriter.ofField(jsonName, ordinal, fieldType, fieldClass, field);
    }

    private static Class<?> extractListElementClass(Type fieldType) {
        if (fieldType instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> elemClass) {
                return elemClass;
            }
        }
        return null;
    }

    /**
     * Find a declared field by name, searching up the class hierarchy.
     */
    private static Field findDeclaredField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    // ==================== Mixin support ====================

    /**
     * Find an annotation on the corresponding method in the mixin class.
     * Matches by method name and parameter types.
     */
    static <A extends java.lang.annotation.Annotation> A findMixInAnnotation(
            Class<?> mixIn, Method targetMethod, String propertyName, Class<A> annotationType) {
        // Try matching method by name and signature
        for (Method m : mixIn.getMethods()) {
            if (m.getName().equals(targetMethod.getName())
                    && m.getParameterCount() == targetMethod.getParameterCount()
                    && java.util.Arrays.equals(m.getParameterTypes(), targetMethod.getParameterTypes())) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        // Try matching by getter/setter naming convention for the property
        String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        String isName = "is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        for (Method m : mixIn.getMethods()) {
            String mn = m.getName();
            if ((mn.equals(getterName) || mn.equals(isName)) && m.getParameterCount() == 0) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        // Also check declared methods (including non-public in interfaces/abstract classes)
        for (Method m : mixIn.getDeclaredMethods()) {
            String mn = m.getName();
            if ((mn.equals(getterName) || mn.equals(isName)) && m.getParameterCount() == 0) {
                A ann = m.getAnnotation(annotationType);
                if (ann != null) {
                    return ann;
                }
            }
        }
        return null;
    }

    /**
     * Find an annotation on the corresponding field in the mixin class.
     */
    static <A extends java.lang.annotation.Annotation> A findMixInFieldAnnotation(
            Class<?> mixIn, String fieldName, Class<A> annotationType) {
        try {
            Field f = mixIn.getDeclaredField(fieldName);
            return f.getAnnotation(annotationType);
        } catch (NoSuchFieldException ignored) {
        }
        return null;
    }
}
