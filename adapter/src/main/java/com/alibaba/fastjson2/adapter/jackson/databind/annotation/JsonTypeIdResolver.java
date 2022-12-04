package com.alibaba.fastjson2.adapter.jackson.databind.annotation;

import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeIdResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonTypeIdResolver {
    Class<? extends TypeIdResolver> value();
}
