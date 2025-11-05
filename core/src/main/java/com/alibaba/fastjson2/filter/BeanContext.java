package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Provides context information about a bean property during JSON processing.
 * Contains metadata about the property including its name, type, annotations,
 * features, and accessor methods.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * public class MyContextFilter implements ContextValueFilter {
 *     @Override
 *     public Object process(BeanContext context, Object object, String name, Object value) {
 *         if (context.getFieldClass() == Date.class) {
 *             // Custom date handling
 *             return formatDate((Date) value);
 *         }
 *         return value;
 *     }
 * }
 * }</pre>
 */
public class BeanContext {
    private final Class beanClass;

    private final Method method;

    private final Field field;

    private final String name;

    private final String label;

    private final Class fieldClass;
    private final Type fieldType;

    private final long features;

    private final String format;

    public BeanContext(
            Class beanClass,
            Method method,
            Field field,
            String name,
            String label,
            Class fieldClass,
            Type fieldType,
            long features,
            String format) {
        this.beanClass = beanClass;
        this.method = method;
        this.field = field;
        this.name = name;
        this.label = label;
        this.fieldClass = fieldClass;
        this.fieldType = fieldType;
        this.features = features;
        this.format = format;
    }

    /**
     * Returns the class containing this property.
     *
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Returns the getter/setter method for this property.
     *
     * @return the method, or null if the property is a field
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the field for this property.
     *
     * @return the field, or null if the property is accessed via methods
     */
    public Field getField() {
        return field;
    }

    /**
     * Returns the JSON name of this property.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the label of this property for filtering purposes.
     *
     * @return the label, or null if no label is defined
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the class type of this property.
     *
     * @return the field class
     */
    public Class<?> getFieldClass() {
        return fieldClass;
    }

    /**
     * Returns the generic type of this property.
     *
     * @return the field type
     */
    public Type getFieldType() {
        return fieldType;
    }

    /**
     * Returns the feature flags configured for this property.
     *
     * @return the feature flags
     */
    public long getFeatures() {
        return features;
    }

    /**
     * Returns whether this property should be serialized as raw JSON.
     *
     * @return true if the field value should be treated as raw JSON
     */
    public boolean isJsonDirect() {
        return (features & FieldInfo.RAW_VALUE_MASK) != 0;
    }

    /**
     * Returns the specified annotation if present on this property.
     * Checks both the method and field for the annotation.
     *
     * @param <T> the annotation type
     * @param annotationClass the annotation class to look for
     * @return the annotation if present, null otherwise
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = null;
        if (method != null) {
            annotation = method.getAnnotation(annotationClass);
        }

        if (annotation == null && field != null) {
            annotation = field.getAnnotation(annotationClass);
        }

        return annotation;
    }

    /**
     * Returns the date/time format pattern for this property.
     *
     * @return the format pattern, or null if not specified
     */
    public String getFormat() {
        return format;
    }
}
