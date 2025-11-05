package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.filter.Filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures JSON serialization and deserialization behavior for a class.
 * This annotation provides comprehensive control over type handling, naming conventions,
 * feature settings, and custom serializers/deserializers.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * @JSONType(naming = PropertyNamingStrategy.SnakeCase,
 *           ignores = {"internalField"},
 *           orders = {"id", "name", "createdAt"})
 * public class User {
 *     private Long id;
 *     private String name;
 *     private String internalField;
 *     private Date createdAt;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JSONType {
    /**
     * Specifies the builder class to use for constructing objects during deserialization.
     *
     * @return the builder class, or void.class if not using a builder
     */
    Class<?> builder() default void.class;

    /**
     * Specifies the key name for type information in polymorphic serialization.
     * Common values include "@type", "type", or custom keys.
     *
     * @return the type key name
     */
    String typeKey() default "";

    /**
     * Specifies the type name to use for this class in polymorphic serialization.
     *
     * @return the type name
     */
    String typeName() default "";

    /**
     * Specifies subclasses for polymorphic deserialization.
     * Used in conjunction with typeKey and typeName for handling inheritance hierarchies.
     *
     * @return array of subclasses
     */
    Class<?>[] seeAlso() default{};

    /**
     * Specifies the default class to use when deserializing polymorphic types
     * if no type information is found or the type is unrecognized.
     *
     * @return the default class for polymorphic deserialization
     * @since 2.0.24
     */
    Class<?> seeAlsoDefault() default Void.class;

    /**
     * Property that defines what to do regarding ordering of properties not explicitly included in annotation instance.
     * If set to true, they will be alphabetically ordered (default setting); if false, order is undefined
     * @return true if properties should be alphabetically ordered, false otherwise
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

    /**
     * Specifies the property naming strategy for this type.
     * Common strategies include CamelCase, SnakeCase, PascalCase, etc.
     *
     * @return the naming strategy to apply
     */
    PropertyNamingStrategy naming() default PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue;

    /**
     * When true, enum values are serialized as JavaBeans with their properties,
     * rather than as simple string values.
     *
     * @return true to serialize enums as JavaBeans
     */
    boolean writeEnumAsJavaBean() default false;

    /**
     * Specifies field names to ignore during serialization and deserialization.
     *
     * @return array of field names to ignore
     */
    String[] ignores() default {};

    /**
     * Specifies field names to include during serialization and deserialization.
     * When specified, only these fields will be processed.
     *
     * @return array of field names to include
     */
    String[] includes() default {};

    /**
     * Order in which properties of annotated object are to be serialized in.
     */
    /**
     * Specifies the order in which properties should be serialized.
     * Properties not listed will be serialized after the listed ones.
     *
     * @return array of property names in the desired order
     */
    String[] orders() default {};

    /**
     * Specifies a custom ObjectWriter class for serialization.
     *
     * @return the custom serializer class, or Void.class for default serialization
     */
    Class<?> serializer() default Void.class;

    /**
     * Specifies a custom ObjectReader class for deserialization.
     *
     * @return the custom deserializer class, or Void.class for default deserialization
     */
    Class<?> deserializer() default Void.class;

    /**
     * Specifies filters to apply during serialization for customizing output.
     *
     * @return array of filter classes
     */
    Class<? extends Filter>[] serializeFilters() default {};

    /**
     * JSON Schema definition for this type for validation purposes.
     *
     * @return the JSON schema definition
     */
    String schema() default "";

    /**
     * Date/time format pattern to use for all date/time fields in this type.
     *
     * @return the format pattern
     * @since 2.0.8
     */
    String format() default "";

    /**
     * Locale to use for formatting all date/time fields in this type.
     *
     * @return the locale string
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
