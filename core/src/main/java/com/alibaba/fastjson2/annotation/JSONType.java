package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.Filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JSONType {
    Class<?> builder() default void.class;

    String typeKey() default "";
    String typeName() default "";

    Class<?>[] seeAlso() default{};

    /**
     * @since 2.0.24
     */
    Class<?> seeAlsoDefault() default Void.class;

    /**
     * Property that defines what to do regarding ordering of properties not explicitly included in annotation instance.
     * If set to true, they will be alphabetically ordered (default setting); if false, order is undefined
     * @return
     */
    boolean alphabetic() default true;

    /**
     * Specify {@link JSONReader.Feature}s to use features when deserializing
     */
    JSONReader.Feature[] deserializeFeatures() default {};

    /**
     * Specify {@link JSONWriter.Feature}s to use features when serializing output
     */
    JSONWriter.Feature[] serializeFeatures() default {};

    PropertyNamingStrategy naming() default PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue;

    boolean writeEnumAsJavaBean() default false;

    String[] ignores() default {};

    String[] includes() default {};

    /**
     * Order in which properties of annotated object are to be serialized in.
     */
    String[] orders() default {};

    Class<?> serializer() default Void.class;

    Class<?> deserializer() default Void.class;

    Class<? extends Filter>[] serializeFilters() default {};

    String schema() default "";

    /**
     * @since 2.0.8
     */
    String format() default "";

    /**
     * @since 2.0.8
     */
    String locale() default "";

    /**
     * @since 2.0.25
     */
    Class<? extends JSONReader.AutoTypeBeforeHandler> autoTypeBeforeHandler() default JSONReader.AutoTypeBeforeHandler.class;

    /**
     * Reduce code branches during code generation to improve performance,
     * If it is true, there will be no code related to reference detection.
     * @since 2.0.50
     */
    boolean disableReferenceDetect() default false;

    /**
     * Similar to {@code javax.xml.bind.annotation.XmlRootElement},
     * used to indicate name to use for root-level wrapping, if wrapping is
     * enabled. Annotation itself does not indicate that wrapping should
     * be used; but if it is, the name used for serialization should be the
     * name specified here, and deserializer will expect the name as well.
     * @since 2.0.52
     */
    String rootName() default "";

    /**
     * @since 2.0.58
     */
    boolean skipTransient() default true;
}
