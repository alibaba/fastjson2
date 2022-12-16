package com.alibaba.fastjson2.adapter.jackson.databind.annotation;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonSerializer;
import com.alibaba.fastjson2.adapter.jackson.databind.util.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerialize {
    Class<? extends JsonSerializer> using() default JsonSerializer.None.class;

    Class<? extends JsonSerializer> keyUsing() default JsonSerializer.None.class;

    Class<? extends JsonSerializer> contentUsing() default JsonSerializer.None.class;

    Class<? extends Converter> converter() default Converter.None.class;

    Class<? extends Converter> contentConverter() default Converter.None.class;
}
