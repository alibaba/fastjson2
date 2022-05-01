package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface JSONType {
    Class<?> builder() default void.class;

    String typeKey() default "";
    String typeName() default "";

    Class<?>[] seeAlso() default{};

    /**
     * Specify {@link JSONReader.Feature}s to use features when deserializing
     */
    JSONReader.Feature[] deserializeFeatures() default {};

    /**
     * Specify {@link JSONWriter.Feature}s to use features when serializing output
     */
    JSONWriter.Feature[] serializeFeatures1() default {};

    NamingStrategy naming() default NamingStrategy.CamelCase;

    boolean writeEnumAsJavaBean() default false;

    String[] ignores() default {};

    String[] includes() default {};

    String[] orders() default {};
}
