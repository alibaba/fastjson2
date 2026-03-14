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
}
