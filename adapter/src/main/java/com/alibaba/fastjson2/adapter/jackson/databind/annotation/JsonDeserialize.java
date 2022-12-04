package com.alibaba.fastjson2.adapter.jackson.databind.annotation;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonDeserializer;
import com.alibaba.fastjson2.adapter.jackson.databind.KeyDeserializer;
import com.alibaba.fastjson2.adapter.jackson.databind.util.Converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonDeserialize {
    Class<? extends JsonDeserializer> using()
            default JsonDeserializer.None.class;

    Class<? extends KeyDeserializer> keyUsing()
            default KeyDeserializer.None.class;

    Class<? extends JsonDeserializer> contentUsing()
            default JsonDeserializer.None.class;

    Class<? extends Converter> converter() default Converter.None.class;
}
