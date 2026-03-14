package com.alibaba.fastjson3.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Optional Jackson annotation support via pure reflection.
 * Zero compile-time dependency on Jackson — annotations are detected by class name
 * and attributes are read via {@code Method.invoke()} on annotation proxies.
 *
 * <p>Enable via {@code ObjectMapper.builder().useJacksonAnnotation(true).build()}.
 * When Jackson annotations are not on the classpath, all methods return null/false
 * with zero overhead after the initial availability check.</p>
 */
public final class JacksonAnnotationSupport {
    private static final boolean AVAILABLE;

    static {
        boolean available;
        try {
            Class.forName("com.fasterxml.jackson.annotation.JsonProperty");
            available = true;
        } catch (ClassNotFoundException e) {
            available = false;
        }
        AVAILABLE = available;
    }

    private JacksonAnnotationSupport() {
    }

    public static boolean isAvailable() {
        return AVAILABLE;
    }

    // ==================== Field-level info ====================

    /**
     * Holds annotation metadata extracted from Jackson field/method annotations.
     * Null/empty/zero values indicate "not specified".
     */
    public record FieldInfo(
            String name,
            String[] alternateNames,
            int ordinal,
            boolean required,
            boolean noSerialize,
            boolean noDeserialize,
            Inclusion inclusion
    ) {
    }

    /**
     * Extract field-level Jackson annotation info from an annotation array.
     * Returns null if no relevant Jackson annotations are found.
     */
    public static FieldInfo getFieldInfo(Annotation[] annotations) {
        if (!AVAILABLE || annotations == null || annotations.length == 0) {
            return null;
        }

        String name = "";
        String[] alternateNames = new String[0];
        int ordinal = 0;
        boolean required = false;
        boolean noSerialize = false;
        boolean noDeserialize = false;
        Inclusion inclusion = null;
        boolean found = false;

        for (Annotation ann : annotations) {
            String typeName = ann.annotationType().getName();
            switch (typeName) {
                case "com.fasterxml.jackson.annotation.JsonProperty" -> {
                    found = true;
                    Object v = invoke(ann, "value");
                    if (v instanceof String s && !s.isEmpty()) {
                        name = s;
                    }
                    Object req = invoke(ann, "required");
                    if (req instanceof Boolean b && b) {
                        required = true;
                    }
                    // Note: Jackson's index is for schema generation, not serialization order.
                    // We map it to ordinal which controls serialization order — intentional
                    // behavioral difference to provide a useful migration path.
                    Object idx = invoke(ann, "index");
                    if (idx instanceof Integer i && i >= 0) {
                        ordinal = i;
                    }
                    // Jackson access semantics:
                    //   READ_ONLY  = only readable from POJO (serialize only, no deserialize)
                    //   WRITE_ONLY = only writable to POJO (deserialize only, no serialize)
                    Object access = invoke(ann, "access");
                    if (access instanceof Enum<?> e) {
                        switch (e.name()) {
                            case "READ_ONLY" -> noDeserialize = true;
                            case "WRITE_ONLY" -> noSerialize = true;
                        }
                    }
                }
                case "com.fasterxml.jackson.annotation.JsonIgnore" -> {
                    Object v = invoke(ann, "value");
                    if (!(v instanceof Boolean b) || b) {
                        found = true;
                        noSerialize = true;
                        noDeserialize = true;
                    }
                }
                case "com.fasterxml.jackson.annotation.JsonAlias" -> {
                    Object v = invoke(ann, "value");
                    if (v instanceof String[] aliases && aliases.length > 0) {
                        found = true;
                        alternateNames = aliases;
                    }
                }
                case "com.fasterxml.jackson.annotation.JsonInclude" -> {
                    Object v = invoke(ann, "value");
                    if (v instanceof Enum<?> e) {
                        Inclusion inc = mapInclusion(e.name());
                        if (inc != null) {
                            found = true;
                            inclusion = inc;
                        }
                    }
                }
                default -> {
                    // skip unknown annotations
                }
            }
        }

        return found ? new FieldInfo(name, alternateNames, ordinal, required,
                noSerialize, noDeserialize, inclusion) : null;
    }

    // ==================== Class-level info ====================

    /**
     * Holds annotation metadata extracted from Jackson class-level annotations.
     * Null values indicate "not specified".
     */
    public record BeanInfo(
            String[] ignoreProperties,
            String[] propertyOrder,
            Boolean alphabetic,
            NamingStrategy naming,
            Inclusion inclusion
    ) {
    }

    /**
     * Extract class-level Jackson annotation info.
     * Returns null if no relevant Jackson annotations are found.
     */
    public static BeanInfo getBeanInfo(Class<?> type) {
        if (!AVAILABLE) {
            return null;
        }

        Annotation[] annotations = type.getAnnotations();
        if (annotations.length == 0) {
            return null;
        }

        String[] ignoreProperties = null;
        String[] propertyOrder = null;
        Boolean alphabetic = null;
        NamingStrategy naming = null;
        Inclusion inclusion = null;
        boolean found = false;

        for (Annotation ann : annotations) {
            String annType = ann.annotationType().getName();
            switch (annType) {
                case "com.fasterxml.jackson.annotation.JsonIgnoreProperties" -> {
                    Object v = invoke(ann, "value");
                    if (v instanceof String[] props && props.length > 0) {
                        found = true;
                        ignoreProperties = props;
                    }
                }
                case "com.fasterxml.jackson.annotation.JsonPropertyOrder" -> {
                    found = true;
                    Object v = invoke(ann, "value");
                    if (v instanceof String[] order && order.length > 0) {
                        propertyOrder = order;
                    }
                    Object alpha = invoke(ann, "alphabetic");
                    if (alpha instanceof Boolean b) {
                        alphabetic = b;
                    }
                }
                case "com.fasterxml.jackson.annotation.JsonInclude" -> {
                    Object v = invoke(ann, "value");
                    if (v instanceof Enum<?> e) {
                        Inclusion inc = mapInclusion(e.name());
                        if (inc != null) {
                            found = true;
                            inclusion = inc;
                        }
                    }
                }
                case "com.fasterxml.jackson.databind.annotation.JsonNaming" -> {
                    Object v = invoke(ann, "value");
                    if (v instanceof Class<?> strategyClass) {
                        NamingStrategy ns = mapNaming(strategyClass.getSimpleName());
                        if (ns != null) {
                            found = true;
                            naming = ns;
                        }
                    }
                }
                default -> {
                    // skip unknown annotations
                }
            }
        }

        return found ? new BeanInfo(ignoreProperties, propertyOrder, alphabetic,
                naming, inclusion) : null;
    }

    // ==================== Constructor/Method checks ====================

    /**
     * Check if a constructor has @JsonCreator annotation.
     */
    public static boolean hasJsonCreator(Constructor<?> ctor) {
        if (!AVAILABLE) {
            return false;
        }
        for (Annotation ann : ctor.getAnnotations()) {
            if ("com.fasterxml.jackson.annotation.JsonCreator".equals(ann.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    // ==================== Internal helpers ====================

    private static Object invoke(Annotation ann, String methodName) {
        try {
            Method m = ann.annotationType().getMethod(methodName);
            return m.invoke(ann);
        } catch (Exception e) {
            return null;
        }
    }

    private static Inclusion mapInclusion(String name) {
        return switch (name) {
            case "ALWAYS" -> Inclusion.ALWAYS;
            case "NON_NULL", "NON_ABSENT" -> Inclusion.NON_NULL;
            case "NON_EMPTY" -> Inclusion.NON_EMPTY;
            default -> null;
        };
    }

    private static NamingStrategy mapNaming(String className) {
        return switch (className) {
            case "SnakeCaseStrategy" -> NamingStrategy.SnakeCase;
            case "UpperSnakeCaseStrategy" -> NamingStrategy.UpperSnakeCase;
            case "LowerCamelCaseStrategy" -> NamingStrategy.CamelCase;
            case "UpperCamelCaseStrategy" -> NamingStrategy.PascalCase;
            case "KebabCaseStrategy" -> NamingStrategy.KebabCase;
            default -> null;
        };
    }
}
