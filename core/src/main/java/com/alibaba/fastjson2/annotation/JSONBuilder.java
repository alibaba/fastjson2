package com.alibaba.fastjson2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures builder pattern support for JSON deserialization.
 * Use this annotation on a class to specify the build method and setter prefix
 * for builder-based object construction during JSON deserialization.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * @JSONBuilder(buildMethod = "create", withPrefix = "set")
 * public static class Person {
 *     public static PersonBuilder builder() {
 *         return new PersonBuilder();
 *     }
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JSONBuilder {
    /**
     * Specifies the name of the method that builds the final object.
     * The default value is "build".
     *
     * @return the build method name
     */
    String buildMethod() default "build";

    /**
     * Specifies the prefix for setter methods in the builder.
     * The default value is "with" (e.g., withName, withAge).
     *
     * @return the setter method prefix
     */
    String withPrefix() default "with";
}
