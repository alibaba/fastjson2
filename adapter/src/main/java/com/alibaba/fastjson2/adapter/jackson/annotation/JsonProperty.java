package com.alibaba.fastjson2.adapter.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonProperty {
    String value() default "";

    boolean required() default false;

    Access access() default Access.AUTO;

    int index() default -1;

    enum Access {
        /**
         * Access setting which means that visibility rules are to be used
         * to automatically determine read- and/or write-access of this property.
         */
        AUTO,

        /**
         * Access setting that means that the property may only be read for serialization
         * (value accessed via "getter" Method, or read from Field)
         * but not written (set) during deserialization.
         * Put another way, this would reflect "read-only POJO", in which value contained
         * may be read but not written/set.
         */
        READ_ONLY,

        /**
         * Access setting that means that the property may only be written (set)
         * as part of deserialization (using "setter" method, or assigning to Field,
         * or passed as Creator argument)
         * but will not be read (get) for serialization, that is, the value of the property
         * is not included in serialization.
         */
        WRITE_ONLY,

        /**
         * Access setting that means that the property will be accessed for both
         * serialization (writing out values as external representation)
         * and deserialization (reading values from external representation),
         * regardless of visibility rules.
         */
        READ_WRITE;
    }
}
