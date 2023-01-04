package com.alibaba.fastjson2.adapter.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE,
        ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonTypeInfo {
    JsonTypeInfo.Id use();

    As include() default As.PROPERTY;

    String property() default "";

    boolean visible() default false;

    enum Id {
        NONE(null),
        NAME("@type"),
        MINIMAL_CLASS("@c"),
        CLASS("@class");

        private final String defaultPropertyName;

        Id(String defProp) {
            defaultPropertyName = defProp;
        }

        public String getDefaultPropertyName() { return defaultPropertyName; }
    }

    enum As {
        PROPERTY,
        WRAPPER_OBJECT,
        WRAPPER_ARRAY,
        EXTERNAL_PROPERTY,
        EXISTING_PROPERTY
    }
}
