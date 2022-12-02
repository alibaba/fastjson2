package com.alibaba.fastjson2.adapter.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE,
        ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIgnoreProperties {
    String[] value() default { };

    boolean ignoreUnknown() default false;

    boolean allowGetters() default false;

    boolean allowSetters() default false;
}
