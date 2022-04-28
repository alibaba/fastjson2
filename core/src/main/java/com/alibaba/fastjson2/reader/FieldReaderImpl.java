package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JdbcSupport;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;

abstract class FieldReaderImpl<T>
        implements FieldReader<T> {
    final int ordinal;
    final String fieldName;
    final Class fieldClass;
    final boolean fieldClassSerializable;
    final Type fieldType;
    final long fieldNameHash;
    final long features;
    final String format;
    volatile ObjectReader reader;

    volatile JSONPath referenceCache;

    public FieldReaderImpl(String fieldName, Type fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldClass = TypeUtils.getMapping(fieldType);
        this.fieldClassSerializable = fieldClass != null && Serializable.class.isAssignableFrom(fieldClass);
        this.fieldNameHash = Fnv.hashCode64(fieldName);
        this.features = 0;
        this.ordinal = 0;
        this.format = null;
    }

    public FieldReaderImpl(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.fieldClassSerializable = fieldClass != null && Serializable.class.isAssignableFrom(fieldClass);
        this.features = features;
        this.fieldNameHash = Fnv.hashCode64(fieldName);
        this.ordinal = ordinal;
        this.format = format;
    }

    @Override
    public boolean isFieldClassSerializable() {
        return fieldClassSerializable;
    }

    @Override
    public ObjectReader getObjectReader(JSONReader jsonReader) {
        if (reader != null) {
            return reader;
        }

        if (format != null && !format.isEmpty()) {
            String typeName = fieldType.getTypeName();
            switch (typeName) {
                case "java.sql.Time":
                    return reader = JdbcSupport.createTimeReader(format);
                case "java.sql.Timestamp":
                    return reader = JdbcSupport.createTimestampReader(format);
                case "java.sql.Date":
                    return JdbcSupport.createDateReader(format);
                default:
                    break;
            }
        }

        return reader = jsonReader.getObjectReader(fieldType);
    }

    @Override
    public int ordinal() {
        return ordinal;
    }

    @Override
    public long getFeatures() {
        return features;
    }

    @Override
    public long getFieldNameHash() {
        return fieldNameHash;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public Type getFieldType() {
        return fieldType;
    }

    @Override
    public Class getFieldClass() {
        return fieldClass;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }

    @Override
    public void addResolveTask(JSONReader jsonReader, Object object, String reference) {
        JSONPath path;
        if (referenceCache != null && referenceCache.toString().equals(reference)) {
            path = referenceCache;
        } else {
            path = referenceCache = JSONPath.of(reference);
        }
        jsonReader.addResolveTask(this, object, path);
    }

    public void addResolveTask(JSONReader jsonReader, Collection object, int i, String reference) {
        JSONPath path;
        if (referenceCache != null && referenceCache.toString().equals(reference)) {
            path = referenceCache;
        } else {
            path = referenceCache = JSONPath.of(reference);
        }
        jsonReader.addResolveTask(object, i, path);
    }
}
