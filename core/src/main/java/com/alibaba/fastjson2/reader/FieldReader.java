package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.util.*;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

import static com.alibaba.fastjson2.codec.FieldInfo.READ_ONLY;

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
            Method method,
            Field field
    ) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.fieldClassSerializable = fieldClass != null
                && (fieldClass.isPrimitive()
                || fieldClass == String.class
                || fieldClass == List.class
                || Serializable.class.isAssignableFrom(fieldClass)
                || Modifier.isInterface(fieldClass.getModifiers()));
        this.features = features;
        this.fieldNameHash = Fnv.hashCode64(fieldName);
        this.fieldNameHashLCase = Fnv.hashCode64LCase(fieldName);
        this.ordinal = ordinal;
        this.format = format;
        this.locale = locale;
        this.defaultValue = defaultValue;
        this.method = method;
        this.field = field;

        boolean readOnly = field != null && Modifier.isFinal(field.getModifiers())
                || (features & READ_ONLY) != 0;
        this.readOnly = readOnly;

        long fieldOffset = -1L;
        if (field != null && (features & FieldInfo.DISABLE_UNSAFE) == 0) {
            fieldOffset = JDKUtils.UNSAFE.objectFieldOffset(field);
        }
        this.fieldOffset = fieldOffset;

        if (fieldOffset == -1 && field != null && method == null) {
            try {
                field.setAccessible(true);
            } catch (Throwable ignored) {
                // ignored
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

    public void acceptDefaultValue(T object) {
        if (defaultValue != null) {
            accept(object, defaultValue);
        }
    }

    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (reader != null) {
            return reader;
        }
        return reader = jsonReader.getObjectReader(fieldType);
    }

    public ObjectReader getObjectReader(JSONReader.Context context) {
        if (reader != null) {
            return reader;
        }
        return reader = context.getObjectReader(fieldType);
    }

    public Type getItemType() {
        return itemType;
    }

    public Class getItemClass() {
        if (itemType == null) {
            return null;
        }

        if (itemClass == null) {
            itemClass = TypeUtils.getClass(itemType);
        }

        return itemClass;
    }

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
            if (thisDeclaringClass != otherDeclaringClass) {
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

            Class<?>[] thisParameterTypes = this.method.getParameterTypes();
            Class<?>[] otherParameterTypes = o.method.getParameterTypes();

            if (thisParameterTypes.length == 1 && otherParameterTypes.length == 1) {
                Class<?> thisParamType = thisParameterTypes[0];
                Class<?> otherParamType = otherParameterTypes[0];

                if (thisParamType != otherParamType) {
                    if (thisParamType.isAssignableFrom(otherParamType)) {
                        return 1;
                    }

                    if (otherParamType.isAssignableFrom(thisParamType)) {
                        return -1;
                    }

                    if (thisParamType.isEnum() && (otherParamType == Integer.class || otherParamType == int.class)) {
                        return 1;
                    }

                    if (otherParamType.isEnum() && (thisParamType == Integer.class || thisParamType == int.class)) {
                        return -1;
                    }

                    JSONField thisAnnotation = BeanUtils.findAnnotation(this.method, JSONField.class);
                    JSONField otherAnnotation = BeanUtils.findAnnotation(o.method, JSONField.class);

                    if (thisAnnotation != null && otherAnnotation == null) {
                        return -1;
                    }

                    if (thisAnnotation == null && otherAnnotation != null) {
                        return 1;
                    }
                }
            }

            String thisMethodName = this.method.getName();
            String otherMethodName = o.method.getName();
            if (!thisMethodName.equals(otherMethodName)) {
                String thisName = BeanUtils.setterName(thisMethodName, null);
                String otherName = BeanUtils.setterName(otherMethodName, null);
                if (this.fieldName.equals(thisName) && !o.fieldName.equals(otherName)) {
                    return 1;
                }
                if (o.fieldName.equals(otherName) && !this.fieldName.equals(thisName)) {
                    return -1;
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

        boolean thisClassStartsWithJava = thisFieldClass.getName().startsWith("java.", 0);
        boolean otherClassStartsWithJava = otherClass.getName().startsWith("java.", 0);
        if (thisClassStartsWithJava && !otherClassStartsWithJava) {
            return -1;
        }

        if (!thisClassStartsWithJava && otherClassStartsWithJava) {
            return 1;
        }

        return cmp;
    }

    public boolean isUnwrapped() {
        return (features & FieldInfo.UNWRAPPED_MASK) != 0;
    }

    public void addResolveTask(JSONReader jsonReader, List object, int i, String reference) {
        jsonReader.addResolveTask(object, i, JSONPath.of(reference));
    }

    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        readFieldValue(jsonReader, object);
    }

    public abstract Object readFieldValue(JSONReader jsonReader);

    public void accept(T object, boolean value) {
        accept(object, Boolean.valueOf(value));
    }

    public boolean supportAcceptType(Class valueClass) {
        return fieldClass == valueClass;
    }

    public void accept(T object, byte value) {
        accept(object, Byte.valueOf(value));
    }

    public void accept(T object, short value) {
        accept(object, Short.valueOf(value));
    }

    public void accept(T object, int value) {
        accept(object, Integer.valueOf(value));
    }

    public void accept(T object, long value) {
        accept(object, Long.valueOf(value));
    }

    public void accept(T object, char value) {
        accept(object, Character.valueOf(value));
    }

    public void accept(T object, float value) {
        accept(object, Float.valueOf(value));
    }

    public void accept(T object, double value) {
        accept(object, Double.valueOf(value));
    }

    public abstract void accept(T object, Object value);

    protected void acceptAny(T object, Object fieldValue, long features) {
        ObjectReaderProvider provider = JSONFactory.defaultObjectReaderProvider;
        boolean autoCast = true;

        if (fieldValue != null) {
            Class<?> valueClass = fieldValue.getClass();
            if (!supportAcceptType(valueClass)) {
                if (valueClass == String.class) {
                    if (fieldClass == java.util.Date.class) {
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
        if (fieldValue == null || fieldType == fieldValue.getClass()) {
            typedFieldValue = fieldValue;
        } else {
            if (fieldValue instanceof JSONObject) {
                JSONReader.Feature[] toFeatures = (features & JSONReader.Feature.SupportSmartMatch.mask) != 0
                        ? new JSONReader.Feature[] {JSONReader.Feature.SupportSmartMatch}
                        : new JSONReader.Feature[0];
                typedFieldValue = ((JSONObject) fieldValue).to(fieldType, toFeatures);
            } else if (fieldValue instanceof JSONArray) {
                typedFieldValue = ((JSONArray) fieldValue).to(fieldType);
            } else if (features == 0 && !fieldClass.isInstance(fieldValue) && format == null) {
                ObjectReader initReader = getInitReader();
                if (initReader != null) {
                    String fieldValueJson = JSON.toJSONString(fieldValue);
                    typedFieldValue = initReader.readObject(JSONReader.of(fieldValueJson), null, null, features);
                } else {
                    typedFieldValue = TypeUtils.cast(fieldValue, fieldClass, provider);
                }
            } else {
                if (autoCast) {
                    String fieldValueJSONString = JSON.toJSONString(fieldValue);
                    JSONReader.Context readContext = JSONFactory.createReadContext();
                    if ((features & JSONReader.Feature.SupportSmartMatch.mask) != 0) {
                        readContext.config(JSONReader.Feature.SupportSmartMatch);
                    }
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

    public abstract void readFieldValue(JSONReader jsonReader, T object);

    public ObjectReader checkObjectAutoType(JSONReader jsonReader) {
        return null;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public ObjectReader getInitReader() {
        return null;
    }

    public void processExtra(JSONReader jsonReader, Object object) {
        jsonReader.skipValue();
    }

    public void acceptExtra(Object object, String name, Object value) {
    }

    public ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        if (itemReader != null) {
            return itemReader;
        }
        return itemReader = ctx.getObjectReader(itemType);
    }

    public ObjectReader getObjectReader(ObjectReaderProvider provider) {
        if (reader != null) {
            return reader;
        }

        boolean fieldBased = (this.features & JSONReader.Feature.FieldBased.mask) != 0;
        return reader = provider.getObjectReader(fieldType, fieldBased);
    }

    public ObjectReader getItemObjectReader(JSONReader jsonReader) {
        return getItemObjectReader(jsonReader.context);
    }

    static ObjectReader createFormattedObjectReader(Type fieldType, Class fieldClass, String format, Locale locale) {
        if (format != null && !format.isEmpty()) {
            String typeName = TypeUtils.getTypeName(fieldType);
            switch (typeName) {
                case "java.sql.Time":
                    return new JdbcSupport.TimeReader(format, locale);
                case "java.sql.Timestamp":
                    return new JdbcSupport.TimestampReader(format, locale);
                case "java.sql.Date":
                    return new JdbcSupport.DateReader(format, locale);
                case "byte[]":
                case "[B":
                    return new ObjectReaderImplInt8Array(format);
                default:
                    if (Calendar.class.isAssignableFrom(fieldClass)) {
                        return ObjectReaderImplCalendar.of(format, locale);
                    }

                    if (fieldClass == Date.class) {
                        return ObjectReaderImplDate.of(format, locale);
                    }

                    break;
            }
        }
        return null;
    }
}
