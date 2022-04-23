package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface JSONField {
    int ordinal() default 0;
    String name() default "";
    String format() default "";

    boolean serialize() default true;
    boolean deserialize() default true;

    JSONWriter.Feature[] writeFeatures() default {};
    JSONReader.Feature[] readeFeatures() default {};

    Class writeUsing() default Void.class;

    String[] alternateNames() default {};
    boolean unwrapped() default false;
}
