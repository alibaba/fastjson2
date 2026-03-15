package com.alibaba.fastjson3.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for configuring JSON serialization/deserialization at class level.
 *
 * <pre>
 * &#64;JSONType(naming = NamingStrategy.SnakeCase)
 * public class UserInfo {
 *     private String userName;  // serialized as "user_name"
 * }
 *
 * &#64;JSONType(orders = {"id", "name", "age"})
 * public class User { ... }
 *
 * &#64;JSONType(seeAlso = {Cat.class, Dog.class}, typeKey = "type")
 * public abstract class Animal { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JSONType {
    /**
     * Property naming strategy.
     */
    NamingStrategy naming() default NamingStrategy.NoneStrategy;

    /**
     * Properties to include (whitelist). Empty means all.
     */
    String[] includes() default {};

    /**
     * Properties to exclude (blacklist).
     */
    String[] ignores() default {};

    /**
     * Property ordering.
     */
    String[] orders() default {};

    /**
     * Sort properties alphabetically.
     */
    boolean alphabetic() default true;

    /**
     * Type name for polymorphic serialization.
     */
    String typeName() default "";

    /**
     * Type discriminator property name.
     */
    String typeKey() default "";

    /**
     * Known subtypes for polymorphic deserialization.
     */
    Class<?>[] seeAlso() default {};

    /**
     * Builder class for immutable object construction.
     */
    Class<?> builder() default void.class;

    /**
     * Property inclusion strategy for serialization.
     * Controls which property values are written (null handling, empty handling).
     */
    Inclusion inclusion() default Inclusion.DEFAULT;

    /**
     * JSON Schema for validating the entire object after deserialization.
     * The schema string is parsed as a JSON object conforming to JSON Schema specification.
     *
     * <pre>
     * &#64;JSONType(schema = "{\"required\": [\"name\", \"age\"], \"maxProperties\": 5}")
     * public class User {
     *     public String name;
     *     public int age;
     * }
     * </pre>
     */
    String schema() default "";
}
