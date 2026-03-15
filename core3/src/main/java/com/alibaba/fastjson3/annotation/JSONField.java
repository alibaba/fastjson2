package com.alibaba.fastjson3.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for configuring JSON serialization/deserialization at field or method level.
 *
 * <pre>
 * public class User {
 *     &#64;JSONField(name = "user_name")
 *     private String userName;
 *
 *     &#64;JSONField(format = "yyyy-MM-dd")
 *     private LocalDate birthday;
 *
 *     &#64;JSONField(serialize = false)
 *     private String password;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface JSONField {
    /**
     * Custom JSON property name. Empty means use field name.
     */
    String name() default "";

    /**
     * Alternate names accepted during deserialization.
     */
    String[] alternateNames() default {};

    /**
     * Date/time format pattern.
     */
    String format() default "";

    /**
     * Field ordering (lower values come first).
     */
    int ordinal() default 0;

    /**
     * Whether to include in serialization output.
     */
    boolean serialize() default true;

    /**
     * Whether to include in deserialization.
     */
    boolean deserialize() default true;

    /**
     * Default value (as string) when field is null/missing.
     */
    String defaultValue() default "";

    /**
     * Whether the field is required during deserialization.
     */
    boolean required() default false;

    /**
     * Unwrap nested object properties to parent level.
     */
    boolean unwrapped() default false;

    /**
     * Property inclusion strategy for this field (overrides class-level).
     */
    Inclusion inclusion() default Inclusion.DEFAULT;

    /**
     * Mark this method/field as the single value representation of the entire object.
     * When true, the return value of this method becomes the JSON output for the whole object.
     *
     * <pre>
     * public enum Status {
     *     ACTIVE("active");
     *     &#64;JSONField(value = true)
     *     public String getCode() { return code; }
     * }
     * // Serializes as: "active"
     * </pre>
     */
    boolean value() default false;

    /**
     * Custom ObjectWriter class for serialization of this field.
     * The class must implement {@code ObjectWriter<T>} and have a no-arg constructor.
     */
    Class<?> serializeUsing() default Void.class;

    /**
     * Custom ObjectReader class for deserialization of this field.
     * The class must implement {@code ObjectReader<T>} and have a no-arg constructor.
     */
    Class<?> deserializeUsing() default Void.class;

    /**
     * Label for view-based filtering. Fields with a label are only serialized
     * when a matching {@code LabelFilter} is configured on the ObjectMapper.
     *
     * <pre>
     * &#64;JSONField(label = "admin")
     * private String internalNote;
     * </pre>
     */
    String label() default "";

    /**
     * Mark a Map-returning method as the source of dynamic extra properties.
     * The Map entries are appended after regular fields during serialization.
     * Only one anyGetter per class is allowed.
     */
    boolean anyGetter() default false;

    /**
     * Mark a two-parameter method as the sink for unknown JSON properties.
     * Method signature must be {@code void xxx(String key, Object value)}.
     * Only one anySetter per class is allowed.
     */
    boolean anySetter() default false;
}
