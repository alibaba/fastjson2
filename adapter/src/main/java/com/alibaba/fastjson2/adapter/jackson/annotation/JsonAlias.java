package com.alibaba.fastjson2.adapter.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, // for combo-annotations
        ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER// for properties (field, setter, ctor param)
})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonAlias {
    public String[] value() default { };
}
