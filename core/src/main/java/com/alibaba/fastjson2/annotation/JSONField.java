package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When the Key in the JSON does not match the property in the {@code Bean},
 * the conversion process may not meet expectations. {@link JSONField} can not only solve this problem but also implement custom requirements.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface JSONField {
    /**
     * The order of the fields during
     * serialization and output in ascending order
     */
    int ordinal() default 0;

    /**
     * Specify the {@link String} as the key name in JSON
     */
    String name() default "";

    /**
     * If the field is {@link java.util.Date},
     * then please define the date format
     */
    String format() default "";

    String label() default "";

    /**
     * Whether the field is serialized
     * during serialization, default {@code ture}
     */
    boolean serialize() default true;

    /**
     * Whether the field is deserialized
     * during deserialization, default {@code ture}
     */
    boolean deserialize() default true;

    /**
     * If true, serialize and deserialize the field's internal properties
     */
    boolean unwrapped() default false;

    /**
     * Using multiple different field names when deserializing
     */
    String[] alternateNames() default {};

    /**
     * Specifies that {@link com.alibaba.fastjson2.writer.ObjectWriter} is used when serializing
     */
    @SuppressWarnings("rawtypes")
    @Deprecated
    Class writeUsing() default Void.class;

    /**
     * Specifies that {@link com.alibaba.fastjson2.writer.ObjectWriter} is used when serializing
     */
    @SuppressWarnings("rawtypes")
    Class serializeUsing() default Void.class;

    /**
     * Specifies that {@link com.alibaba.fastjson2.writer.ObjectWriter} is used when serializing
     */
    @SuppressWarnings("rawtypes")
    Class deserializeUsing() default Void.class;

    /**
     * Specify {@link JSONReader.Feature}s to use features when deserializing
     */
    JSONReader.Feature[] deserializeFeatures() default {};

    /**
     * Specify {@link JSONWriter.Feature}s to use features when serializing output
     */
    JSONWriter.Feature[] serializeFeatures() default {};

    boolean value() default false;

    /**
     *
     * @since 1.2.61
     */
    String defaultValue() default "";

    String locale() default "";

    String schema() default "";

    boolean jsonDirect() default false;

    /**
     * Property that indicates whether a value (which may be explicit
     * null) is expected for property during deserialization or not.
     * If expected, <code>ObjectReader</code> should indicate
     * this as a validity problem (usually by throwing an exception,
     * but this may be sent via problem handlers that can try to
     * rectify the problem, for example, by supplying a default
     * value).
     *
     * @since 2.0.20
     */
    boolean required() default false;
}
