package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JdbcSupport;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.*;
import java.time.*;
import java.util.*;

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
    public final Object defaultValue;
    public final Locale locale;
    public final JSONSchema schema;

    final boolean fieldClassSerializable;
    final long fieldNameHash;
    final long fieldNameHashLCase;

    volatile ObjectReader reader;

    volatile JSONPath referenceCache;
    final boolean noneStaticMemberClass;

    Type itemType;
    Class itemClass;
    volatile ObjectReader itemReader;

    public FieldReader(String fieldName, Type fieldType) {
        this (fieldName, fieldType, TypeUtils.getClass(fieldType), 0, 0L, null, null, null, null, null, null);
    }

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
        this.fieldClassSerializable = fieldClass != null && (Serializable.class.isAssignableFrom(fieldClass)
                || Modifier.isInterface(fieldClass.getModifiers()));
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
            if (thisDeclaringClass != otherDeclaringClass && thisDeclaringClass != null && otherDeclaringClass != null) {
                if (thisDeclaringClass.isAssignableFrom(otherDeclaringClass)) {
                    return 1;
                } else if (otherDeclaringClass.isAssignableFrom(thisDeclaringClass)) {
                    return -1;
                }
            }
        }

        if (this.field != null && o.field != null) {
            Class<?> thisFieldDeclaringClass = this.field.getDeclaringClass();
            Class<?> otherFieldDeclaringClass = o.field.getDeclaringClass();

            for (Class superClass = thisFieldDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == otherFieldDeclaringClass) {
                    return 1;
                }
            }

            for (Class superClass = otherFieldDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == thisFieldDeclaringClass) {
                    return -1;
                }
            }
        }

        if (this.method != null && o.method != null) {
            Class<?> thisMethodDeclaringClass = this.method.getDeclaringClass();
            Class<?> otherMethodDeclaringClass = o.method.getDeclaringClass();

            for (Class superClass = thisMethodDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == otherMethodDeclaringClass) {
                    return -1;
                }
            }

            for (Class superClass = otherMethodDeclaringClass.getSuperclass(); superClass != null && superClass != Object.class; superClass = superClass.getSuperclass()) {
                if (superClass == thisMethodDeclaringClass) {
                    return 1;
                }
            }

            if (this.method.getParameterCount() == 1 && o.method.getParameterCount() == 1) {
                Class<?> thisParamType = this.method.getParameterTypes()[0];
                Class<?> otherParamType = o.method.getParameterTypes()[0];

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

    public abstract void readFieldValue(JSONReader jsonReader, T object);

    public ObjectReader checkObjectAutoType(JSONReader jsonReader) {
        return null;
    }

    public boolean isReadOnly() {
        return false;
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
                        if (format == null) {
                            return ObjectReaderImplLocalDateTime.INSTANCE;
                        }

                        return new ObjectReaderImplLocalDateTime(format, locale);
                    }

                    if (fieldClass == LocalDate.class) {
                        if (format == null) {
                            return ObjectReaderImplLocalDate.INSTANCE;
                        }

                        return new ObjectReaderImplLocalDate(format, locale);
                    }

                    if (fieldClass == LocalTime.class) {
                        if (format == null) {
                            return ObjectReaderImplLocalTime.INSTANCE;
                        }

                        return new ObjectReaderImplLocalTime(format, locale);
                    }

                    if (fieldClass == Instant.class) {
                        return ObjectReaderImplInstant.of(format, locale);
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
}
