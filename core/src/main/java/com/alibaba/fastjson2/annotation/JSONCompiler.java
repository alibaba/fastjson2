package com.alibaba.fastjson2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures the compilation strategy for JSON serialization and deserialization.
 * This annotation controls how fastjson2 generates code for reading and writing JSON,
 * allowing optimization of performance characteristics.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * @JSONCompiler(JSONCompiler.CompilerOption.LAMBDA)
 * public class Product {
 *     private String name;
 *     private double price;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE })
public @interface JSONCompiler {
    /**
     * Specifies the compiler option to use for code generation.
     *
     * @return the compiler option (DEFAULT or LAMBDA)
     */
    CompilerOption value() default CompilerOption.DEFAULT;

    /**
     * Defines the available compiler options for code generation.
     */
    enum CompilerOption {
        /**
         * Use the default compilation strategy.
         */
        DEFAULT,

        /**
         * Use lambda-based compilation for potentially better performance.
         */
        LAMBDA
    }
}
