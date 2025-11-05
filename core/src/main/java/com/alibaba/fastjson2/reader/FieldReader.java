package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.JdbcSupport;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.alibaba.fastjson2.util.JDKUtils.*;

/**
 * FieldReader is responsible for reading and setting field values during JSON deserialization.
 * It provides an abstraction over various field access methods including direct field access,
 * setter methods, and constructor parameters.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Multiple field access strategies (direct field, method, constructor parameter)</li>
 *   <li>Type conversion and validation</li>
 *   <li>Default value support</li>
 *   <li>Schema validation</li>
 *   <li>Format and locale support for date/time and number fields</li>
 *   <li>Optimized field access using UNSAFE when available</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Typically used internally by ObjectReader implementations
 * FieldReader fieldReader = objectReader.getFieldReader("name");
 * if (fieldReader != null) {
 *     fieldReader.accept(object, "John Doe");
 * }
 * }</pre>
 *
 * @param <T> the type of the object containing the field
 * @since 2.0.0
 */
public abstract class FieldReader<T>
        implements Comparable<FieldReader> {
    public final int ordinal;
    public final String fieldName;
    public final Class fieldClass;
    public final Type fieldType;
    public final long features;
    public final String format;
    public final Method method;
    public final Field field;
    protected final long fieldOffset;
    public final Object defaultValue;
    public final Locale locale;
    public final JSONSchema schema;

    final boolean fieldClassSerializable;
    final long fieldNameHash;
    final long fieldNameHashLCase;

    volatile ObjectReader reader;

    volatile JSONPath referenceCache;
    final boolean noneStaticMemberClass;
    final boolean readOnly;

    Type itemType;
    Class itemClass;
    volatile ObjectReader itemReader;

    public FieldReader(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field
    ) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.fieldClassSerializable = fieldClass != null
                && (Serializable.class.isAssignableFrom(fieldClass)
                || Modifier.isInterface(fieldClass.getModifiers())
                || BeanUtils.isRecord(fieldClass) || fieldClass.isPrimitive());
        this.features = features;
        this.fieldNameHash = Fnv.hashCode64(fieldName);
        this.fieldNameHashLCase = Fnv.hashCode64LCase(fieldName);
        this.ordinal = ordinal;
        this.format = format;
        this.locale = locale;
        this.defaultValue = defaultValue;
        this.schema = schema;
        this.method = method;
        this.field = field;

        boolean readOnly = false;
        if (method != null && method.getParameterCount() == 0) {
            readOnly = true;
        } else if (field != null && Modifier.isFinal(field.getModifiers())) {
            readOnly = true;
        }
        this.readOnly = readOnly;

        long fieldOffset = -1L;
        if (field != null && (features & FieldInfo.DISABLE_UNSAFE) == 0) {
            fieldOffset = UNSAFE.objectFieldOffset(field);
        }
        this.fieldOffset = fieldOffset;

        if (fieldOffset == -1 && field != null && method == null) {
            try {
                field.setAccessible(true);
            } catch (Throwable e) {
                JDKUtils.setReflectErrorLast(e);
            }
        }

        Class declaringClass = null;
        if (method != null) {
            declaringClass = method.getDeclaringClass();
        } else if (field != null) {
            declaringClass = field.getDeclaringClass();
        }

        this.noneStaticMemberClass = BeanUtils.isNoneStaticMemberClass(declaringClass, fieldClass);
    }

    /**
     * Accepts and sets the default value for this field on the specified object,
     * if a default value is configured.
     *
     * @param object the object on which to set the default value
     */
    public void acceptDefaultValue(T object) {
        if (defaultValue != null) {
            accept(object, defaultValue);
        }
    }

    /**
     * Gets or creates an ObjectReader for this field's type using the specified JSONReader.
     * The result is cached for subsequent calls.
     *
     * @param jsonReader the JSONReader to use for obtaining the ObjectReader
     * @return the ObjectReader for this field's type
     */
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (reader != null) {
            return reader;
        }
        return reader = jsonReader.getObjectReader(fieldType);
    }

    /**
     * Gets or creates an ObjectReader for this field's type using the specified context.
     * The result is cached for subsequent calls.
     *
     * @param context the JSONReader context to use for obtaining the ObjectReader
     * @return the ObjectReader for this field's type
     */
    public ObjectReader getObjectReader(JSONReader.Context context) {
        if (reader != null) {
            return reader;
        }
        return reader = context.getObjectReader(fieldType);
    }

    /**
     * Gets or creates an ObjectReader for this field's type using the specified provider.
     * The result is cached for subsequent calls. This method respects the FieldBased feature.
     *
     * @param provider the ObjectReaderProvider to use for obtaining the ObjectReader
     * @return the ObjectReader for this field's type
     */
    public ObjectReader getObjectReader(ObjectReaderProvider provider) {
        if (reader != null) {
            return reader;
        }

        boolean fieldBased = (this.features & JSONReader.Feature.FieldBased.mask) != 0;
        return reader = provider.getObjectReader(fieldType, fieldBased);
    }

    /**
     * Gets the type of items in this field if it represents a collection or array.
     *
     * @return the item type, or null if this field is not a collection or array
     */
    public Type getItemType() {
        return itemType;
    }

    /**
     * Gets the class of items in this field if it represents a collection or array.
     * The result is cached for subsequent calls.
     *
     * @return the item class, or null if this field is not a collection or array
     */
    public Class getItemClass() {
        if (itemType == null) {
            return null;
        }

        if (itemClass == null) {
            itemClass = TypeUtils.getClass(itemType);
        }

        return itemClass;
    }

    /**
     * Gets the hash code of the item class name for this field.
     *
     * @return the hash code of the item class name, or 0 if no item class exists
     */
    public long getItemClassHash() {
        Class itemClass = getItemClass();
        if (itemClass == null) {
            return 0;
        }
        return Fnv.hashCode64(itemClass.getName());
    }

    @Override
    public String toString() {
        Member member = this.method != null ? this.method : this.field;
        if (member != null) {
            return member.getName();
        }
        return fieldName;
    }

    /**
     * Adds a reference resolution task for this field. This is used when deserializing
     * JSON with circular references or forward references.
     *
     * @param jsonReader the JSONReader managing the deserialization
     * @param object the object containing this field
     * @param reference the reference path string to be resolved
     */
    public void addResolveTask(JSONReader jsonReader, Object object, String reference) {
        JSONPath path;
        if (referenceCache != null && referenceCache.toString().equals(reference)) {
            path = referenceCache;
        } else {
            path = referenceCache = JSONPath.of(reference);
        }
        jsonReader.addResolveTask(this, object, path);
    }

    @Override
    public int compareTo(FieldReader o) {
        int nameCompare = this.fieldName.compareTo(o.fieldName);
        if (nameCompare != 0) {
            if (this.ordinal < o.ordinal) {
                return -1;
            }
            if (this.ordinal > o.ordinal) {
                return 1;
            }

            return nameCompare;
        }

        int cmp = (isReadOnly() == o.isReadOnly()) ? 0 : (isReadOnly() ? 1 : -1);
        if (cmp != 0) {
            return cmp;
        }

        Member thisMember = this.field != null ? this.field : this.method;
        Member otherMember = o.field != null ? o.field : o.method;
        if (thisMember != null && otherMember != null && thisMember.getClass() != otherMember.getClass()) {
            Class otherDeclaringClass = otherMember.getDeclaringClass();
            Class thisDeclaringClass = thisMember.getDeclaringClass();
            if (thisDeclaringClass != otherDeclaringClass
            ) {
                if (thisDeclaringClass.isAssignableFrom(otherDeclaringClass)) {
                    return 1;
                } else if (otherDeclaringClass.isAssignableFrom(thisDeclaringClass)) {
                    return -1;
                }
            }
        }

        if (this.field != null && o.field != null) {
            Class<?> thisDeclaringClass = this.field.getDeclaringClass();
            Class<?> otherDeclaringClass = o.field.getDeclaringClass();

            for (Class s = thisDeclaringClass.getSuperclass(); s != null && s != Object.class; s = s.getSuperclass()) {
                if (s == otherDeclaringClass) {
                    return 1;
                }
            }

            for (Class s = otherDeclaringClass.getSuperclass(); s != null && s != Object.class; s = s.getSuperclass()) {
                if (s == thisDeclaringClass) {
                    return -1;
                }
            }
        }

        if (this.method != null && o.method != null) {
            Class<?> thisDeclaringClass = this.method.getDeclaringClass();
            Class<?> otherDeclaringClass = o.method.getDeclaringClass();
            //declaring class compare
            if (thisDeclaringClass != otherDeclaringClass) {
                for (Class s = thisDeclaringClass.getSuperclass(); s != null && s != Object.class; s = s.getSuperclass()) {
                    if (s == otherDeclaringClass) {
                        return -1;
                    }
                }
                for (Class s = otherDeclaringClass.getSuperclass(); s != null && s != Object.class; s = s.getSuperclass()) {
                    if (s == thisDeclaringClass) {
                        return 1;
                    }
                }
            }

            if (this.method.getParameterCount() == 1 && o.method.getParameterCount() == 1) {
                Class<?> thisParamType = this.method.getParameterTypes()[0];
                Class<?> otherParamType = o.method.getParameterTypes()[0];

                if (thisParamType != otherParamType) {
                    if (thisParamType.isAssignableFrom(otherParamType)) {
                        return 1;
                    }

                    if (otherParamType.isAssignableFrom(thisParamType)) {
                        return -1;
                    }

                    // Collection first
                    if (Collection.class.isAssignableFrom(otherParamType) && !Collection.class.isAssignableFrom(thisParamType)) {
                        return 1;
                    }

                    if (Collection.class.isAssignableFrom(thisParamType) && !Collection.class.isAssignableFrom(otherParamType)) {
                        return -1;
                    }

                    // field class compare
                    if (needCompareToActualFieldClass(thisParamType) || needCompareToActualFieldClass(otherParamType)) {
                        Class actualFieldClass = null;
                        try {
                            actualFieldClass = thisDeclaringClass.getDeclaredField(this.fieldName).getType();
                            if (actualFieldClass == null) {
                                actualFieldClass = otherDeclaringClass.getDeclaredField(this.fieldName).getType();
                            }
                        } catch (NoSuchFieldException ignored) {
                            // ignored
                        }
                        if (actualFieldClass != null) {
                            for (Class s = thisParamType; s != null && s != Object.class; s = s.getSuperclass()) {
                                if (s == actualFieldClass) {
                                    return -1;
                                }
                            }
                            for (Class s = otherParamType; s != null && s != Object.class; s = s.getSuperclass()) {
                                if (s == actualFieldClass) {
                                    return 1;
                                }
                            }
                        }
                    }
                    //JSONField annotation priority over non JSONField annotation
                    JSONField thisAnnotation = BeanUtils.findAnnotation(this.method, JSONField.class);
                    JSONField otherAnnotation = BeanUtils.findAnnotation(o.method, JSONField.class);
                    boolean thisAnnotatedWithJsonFiled = thisAnnotation != null;
                    if (thisAnnotatedWithJsonFiled == (otherAnnotation == null)) {
                        return thisAnnotatedWithJsonFiled ? -1 : 1;
                    }
                }
            }

            String thisMethodName = this.method.getName();
            String otherMethodName = o.method.getName();
            if (!thisMethodName.equals(otherMethodName)) {
                //setter priority over non setter
                boolean thisMethodNameSetStart = thisMethodName.startsWith("set");
                if (thisMethodNameSetStart != otherMethodName.startsWith("set")) {
                    return thisMethodNameSetStart ? -1 : 1;
                }
                //different field name priority over same field name
                String thisName = BeanUtils.setterName(thisMethodName, null);
                String otherName = BeanUtils.setterName(otherMethodName, null);
                boolean thisFieldNameEquals = this.fieldName.equals(thisName);
                if (thisFieldNameEquals != o.fieldName.equals(otherName)) {
                    return thisFieldNameEquals ? 1 : -1;
                }
            }
        }

        ObjectReader thisInitReader = this.getInitReader();
        ObjectReader otherInitReader = o.getInitReader();
        if (thisInitReader != null && otherInitReader == null) {
            return -1;
        }

        if (thisInitReader == null && otherInitReader != null) {
            return 1;
        }

        Class thisFieldClass = this.fieldClass;
        Class otherClass = o.fieldClass;

        boolean thisClassPrimitive = thisFieldClass.isPrimitive();
        boolean otherClassPrimitive = otherClass.isPrimitive();
        if (thisClassPrimitive && !otherClassPrimitive) {
            return -1;
        }

        if (!thisClassPrimitive && otherClassPrimitive) {
            return 1;
        }

        boolean thisClassStartsWithJava = thisFieldClass.getName().startsWith("java.");
        boolean otherClassStartsWithJava = otherClass.getName().startsWith("java.");
        if (thisClassStartsWithJava && !otherClassStartsWithJava) {
            return -1;
        }

        if (!thisClassStartsWithJava && otherClassStartsWithJava) {
            return 1;
        }

        return cmp;
    }

    /**
     * Checks if this field has the unwrapped feature enabled. Unwrapped fields
     * have their properties flattened into the parent object during serialization/deserialization.
     *
     * @return true if this field is unwrapped, false otherwise
     */
    public boolean isUnwrapped() {
        return (features & FieldInfo.UNWRAPPED_MASK) != 0;
    }

    /**
     * Adds a reference resolution task for an element in a list field.
     *
     * @param jsonReader the JSONReader managing the deserialization
     * @param object the list object containing the reference
     * @param i the index of the element in the list
     * @param reference the reference path string to be resolved
     */
    public void addResolveTask(JSONReader jsonReader, List object, int i, String reference) {
        jsonReader.addResolveTask(object, i, JSONPath.of(reference));
    }

    /**
     * Reads and sets the field value from JSONB format. By default, delegates to {@link #readFieldValue(JSONReader, Object)}.
     *
     * @param jsonReader the JSONReader to read from
     * @param object the object on which to set the field value
     */
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        readFieldValue(jsonReader, object);
    }

    /**
     * Reads the field value from the JSONReader without setting it on an object.
     * This method must be implemented by concrete subclasses.
     *
     * @param jsonReader the JSONReader to read from
     * @return the field value read from the JSONReader
     */
    public abstract Object readFieldValue(JSONReader jsonReader);

    /**
     * Accepts and sets a boolean value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the boolean value to set
     */
    public void accept(T object, boolean value) {
        accept(object, Boolean.valueOf(value));
    }

    /**
     * Checks if this field can directly accept values of the specified class type
     * without conversion.
     *
     * @param valueClass the class of the value to check
     * @return true if the value class matches the field class, false otherwise
     */
    public boolean supportAcceptType(Class valueClass) {
        return fieldClass == valueClass;
    }

    /**
     * Accepts and sets a byte value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the byte value to set
     */
    public void accept(T object, byte value) {
        accept(object, Byte.valueOf(value));
    }

    /**
     * Accepts and sets a short value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the short value to set
     */
    public void accept(T object, short value) {
        accept(object, Short.valueOf(value));
    }

    /**
     * Accepts and sets an int value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the int value to set
     */
    public void accept(T object, int value) {
        accept(object, Integer.valueOf(value));
    }

    /**
     * Accepts and sets a long value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the long value to set
     */
    public void accept(T object, long value) {
        accept(object, Long.valueOf(value));
    }

    /**
     * Accepts and sets a char value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the char value to set
     */
    public void accept(T object, char value) {
        accept(object, Character.valueOf(value));
    }

    /**
     * Accepts and sets a float value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the float value to set
     */
    public void accept(T object, float value) {
        accept(object, Float.valueOf(value));
    }

    /**
     * Accepts and sets a double value on the specified object for this field.
     *
     * @param object the object on which to set the field value
     * @param value the double value to set
     */
    public void accept(T object, double value) {
        accept(object, Double.valueOf(value));
    }

    /**
     * Accepts and sets an Object value on the specified object for this field.
     * This method must be implemented by concrete subclasses to handle the actual field setting.
     *
     * @param object the object on which to set the field value
     * @param value the value to set
     */
    public abstract void accept(T object, Object value);

    protected void acceptAny(T object, Object fieldValue, long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        boolean autoCast = true;

        if (fieldValue != null) {
            Class<?> valueClass = fieldValue.getClass();
            if (!supportAcceptType(valueClass)) {
                if (valueClass == String.class) {
                    if (fieldClass == Date.class) {
                        autoCast = false;
                    }
                } else if (valueClass == Integer.class
                        && (fieldClass == boolean.class || fieldClass == Boolean.class)
                        && (features & JSONReader.Feature.NonZeroNumberCastToBooleanAsTrue.mask) != 0
                ) {
                    int intValue = ((Integer) fieldValue);
                    fieldValue = intValue != 0;
                }

                if (valueClass != fieldClass && autoCast) {
                    Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);

                    if (typeConvert != null) {
                        fieldValue = typeConvert.apply(fieldValue);
                    }
                }
            }
        }

        Object typedFieldValue;
        if (fieldValue == null || fieldType == fieldValue.getClass() || fieldType == Object.class) {
            typedFieldValue = fieldValue;
        } else {
            if (fieldValue instanceof JSONObject) {
                JSONReader.Feature[] toFeatures = (features & JSONReader.Feature.SupportSmartMatch.mask) != 0
                        ? new JSONReader.Feature[] {JSONReader.Feature.SupportSmartMatch}
                        : new JSONReader.Feature[0];
                typedFieldValue = ((JSONObject) fieldValue).to(fieldType, toFeatures);
            } else if (fieldValue instanceof JSONArray) {
                typedFieldValue = ((JSONArray) fieldValue).to(fieldType, features);
            } else if ((features == 0 || features == JSONReader.Feature.SupportSmartMatch.mask) // default or fastjson 1.x default
                    && !fieldClass.isInstance(fieldValue) && format == null
            ) {
                ObjectReader initReader = getInitReader();
                if (initReader != null) {
                    String fieldValueJson = JSON.toJSONString(fieldValue);
                    typedFieldValue = initReader.readObject(JSONReader.of(fieldValueJson), fieldType, fieldName, features);
                } else {
                    typedFieldValue = TypeUtils.cast(fieldValue, fieldType, provider);
                }
            } else {
                if (autoCast) {
                    String fieldValueJSONString = JSON.toJSONString(fieldValue);
                    JSONReader.Context readContext = JSONFactory.createReadContext(features);
                    try (JSONReader jsonReader = JSONReader.of(fieldValueJSONString, readContext)) {
                        ObjectReader fieldObjectReader = getObjectReader(jsonReader);
                        typedFieldValue = fieldObjectReader.readObject(jsonReader, null, fieldName, features);
                    }
                } else {
                    typedFieldValue = fieldValue;
                }
            }
        }
        accept(object, typedFieldValue);
    }

    /**
     * Reads and sets the field value from the JSONReader on the specified object.
     * This method must be implemented by concrete subclasses.
     *
     * @param jsonReader the JSONReader to read from
     * @param object the object on which to set the field value
     */
    public abstract void readFieldValue(JSONReader jsonReader, T object);

    /**
     * Checks and returns an auto-type ObjectReader for this field if applicable.
     * This is used for polymorphic deserialization.
     *
     * @param jsonReader the JSONReader to use for type detection
     * @return the auto-type ObjectReader, or null if not applicable
     */
    public ObjectReader checkObjectAutoType(JSONReader jsonReader) {
        return null;
    }

    /**
     * Checks if this field is read-only. Read-only fields can be read but not written,
     * typically getter methods without corresponding setters or final fields.
     *
     * @return true if this field is read-only, false otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Gets the initialization ObjectReader for this field, if one is configured.
     * This is used for fields that require special initialization logic.
     *
     * @return the initialization ObjectReader, or null if none is configured
     */
    public ObjectReader getInitReader() {
        return null;
    }

    /**
     * Processes extra JSON data that doesn't match any known field.
     * By default, this method skips the value.
     *
     * @param jsonReader the JSONReader containing the extra data
     * @param object the object being deserialized
     */
    public void processExtra(JSONReader jsonReader, Object object) {
        jsonReader.skipValue();
    }

    /**
     * Accepts extra field data that doesn't match any known field.
     * By default, this method does nothing. Subclasses can override to handle extra fields.
     *
     * @param object the object being deserialized
     * @param name the name of the extra field
     * @param value the value of the extra field
     */
    public void acceptExtra(Object object, String name, Object value) {
    }

    /**
     * Gets or creates an ObjectReader for the item type of this field (for collections/arrays).
     * The result is cached for subsequent calls.
     *
     * @param ctx the JSONReader context to use for obtaining the ObjectReader
     * @return the ObjectReader for the item type
     */
    public ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        if (itemReader != null) {
            return itemReader;
        }
        return itemReader = ctx.getObjectReader(itemType);
    }

    /**
     * Gets or creates an ObjectReader for the item type of this field (for collections/arrays).
     *
     * @param jsonReader the JSONReader to use for obtaining the ObjectReader
     * @return the ObjectReader for the item type
     */
    public ObjectReader getItemObjectReader(JSONReader jsonReader) {
        return getItemObjectReader(jsonReader.getContext());
    }

    static ObjectReader createFormattedObjectReader(Type fieldType, Class fieldClass, String format, Locale locale) {
        if (format != null && !format.isEmpty()) {
            String typeName = fieldType.getTypeName();
            switch (typeName) {
                case "java.sql.Time":
                    return JdbcSupport.createTimeReader((Class) fieldType, format, locale);
                case "java.sql.Timestamp":
                    return JdbcSupport.createTimestampReader((Class) fieldType, format, locale);
                case "java.sql.Date":
                    return JdbcSupport.createDateReader((Class) fieldType, format, locale);
                case "byte[]":
                case "[B":
                    return new ObjectReaderImplInt8Array(format);
                default:
                    if (Calendar.class.isAssignableFrom(fieldClass)) {
                        return ObjectReaderImplCalendar.of(format, locale);
                    }

                    if (fieldClass == ZonedDateTime.class) {
                        return ObjectReaderImplZonedDateTime.of(format, locale);
                    }

                    if (fieldClass == LocalDateTime.class) {
                        return new ObjectReaderImplLocalDateTime(format, locale);
                    }

                    if (fieldClass == LocalDate.class) {
                        return ObjectReaderImplLocalDate.of(format, locale);
                    }

                    if (fieldClass == LocalTime.class) {
                        return new ObjectReaderImplLocalTime(format, locale);
                    }

                    if (fieldClass == Instant.class) {
                        return ObjectReaderImplInstant.of(format, locale);
                    }

                    if (fieldClass == OffsetTime.class) {
                        return ObjectReaderImplOffsetTime.of(format, locale);
                    }

                    if (fieldClass == OffsetDateTime.class) {
                        return ObjectReaderImplOffsetDateTime.of(format, locale);
                    }

                    if (fieldClass == Optional.class) {
                        return ObjectReaderImplOptional.of(fieldType, format, locale);
                    }

                    if (fieldClass == Date.class) {
                        return ObjectReaderImplDate.of(format, locale);
                    }

                    break;
            }
        }
        return null;
    }

    /**
     * Gets the BiConsumer function for this field, if one is configured.
     * This is used for alternative field setting strategies.
     *
     * @return the BiConsumer function, or null if none is configured
     */
    public BiConsumer getFunction() {
        return null;
    }

    /**
     * Checks if this FieldReader represents the same field as another FieldReader.
     * This compares the underlying field or method names.
     *
     * @param other the other FieldReader to compare with
     * @return true if both represent the same field, false otherwise
     */
    public boolean sameTo(FieldReader other) {
        if (this.field != null) {
            String thisName = this.field.getName();
            if (other.field != null) {
                String otherName = other.field.getName();
                if (thisName.equals(otherName)) {
                    return true;
                }
            }
            if (other.method != null) {
                String otherName = getActualFieldName(other);
                if (thisName.equals(otherName)) {
                    return true;
                }
            }
        }

        if (this.method != null) {
            String thisName = getActualFieldName(this);
            if (other.method != null) {
                String otherName = getActualFieldName(other);
                if (thisName != null && thisName.equals(otherName)) {
                    return true;
                }
            }
            if (other.field != null) {
                return thisName != null && thisName.equals(other.field.getName());
            }
        }

        return false;
    }

    /**
     * Checks if this FieldReader belongs to the specified class.
     *
     * @param clazz the class to check
     * @return true if this field belongs to the specified class, false otherwise
     */
    public boolean belongTo(Class clazz) {
        return (this.field != null && this.field.getDeclaringClass() == clazz)
                || (this.method != null && this.method.getDeclaringClass().isAssignableFrom(clazz));
    }

    private String getActualFieldName(FieldReader fieldReader) {
        String name = fieldReader.method.getName();
        return fieldReader.isReadOnly() ? BeanUtils.getterName(name, PropertyNamingStrategy.CamelCase.name()) : BeanUtils.setterName(name, PropertyNamingStrategy.CamelCase.name());
    }

    private boolean needCompareToActualFieldClass(Class clazz) {
        return clazz.isEnum() || clazz.isInterface();
    }
}
