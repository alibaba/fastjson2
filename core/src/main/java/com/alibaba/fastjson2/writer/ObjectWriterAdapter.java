package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.NameFilter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ObjectWriterAdapter<T>
        implements ObjectWriter<T> {
    PropertyPreFilter propertyPreFilter;
    PropertyFilter propertyFilter;
    NameFilter nameFilter;
    ValueFilter valueFilter;

    static final String TYPE = "@type";

    final Class objectType;
    final List<FieldWriter> fieldWriters;
    protected final FieldWriter[] fieldWriterArray;

    final String typeKey;
    byte[] typeKeyJSONB;
    protected final String typeName;
    protected final long typeNameHash;
    protected byte[] typeNameJSONB;

    byte[] nameWithColonUTF8;
    char[] nameWithColonUTF16;

    final long features;

    final long[] hashCodes;
    final short[] mapping;

    final boolean hasValueField;

    public ObjectWriterAdapter(Class<T> objectType, List<FieldWriter> fieldWriters) {
        this(objectType, null, null, 0, fieldWriters);
    }

    public ObjectWriterAdapter(
            Class<T> objectType,
            String typeKey,
            String typeName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        if (typeName == null && objectType != null) {
            if (Enum.class.isAssignableFrom(objectType) && !objectType.isEnum()) {
                typeName = objectType.getSuperclass().getName();
            } else {
                typeName = TypeUtils.getTypeName(objectType);
            }
        }

        this.objectType = objectType;
        this.typeKey = typeKey == null || typeKey.isEmpty() ? TYPE : typeKey;
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;
        this.features = features;
        this.fieldWriters = fieldWriters;

        this.fieldWriterArray = new FieldWriter[fieldWriters.size()];
        fieldWriters.toArray(fieldWriterArray);

        this.hasValueField = fieldWriterArray.length == 1 && fieldWriterArray[0].isValue();

        long[] hashCodes = new long[fieldWriterArray.length];
        for (int i = 0; i < fieldWriterArray.length; i++) {
            FieldWriter item = fieldWriterArray[i];
            long hashCode = Fnv.hashCode64(item.getFieldName());
            hashCodes[i] = hashCode;
        }

        this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
        Arrays.sort(this.hashCodes);

        mapping = new short[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = (short) i;
        }
    }

    @Override
    public long getFeatures() {
        return features;
    }

    public ObjectWriterAdapter(Class<T> objectType, long features, FieldWriter... fieldWriters) {
        this.objectType = objectType;
        this.typeKey = TYPE;
        this.fieldWriters = Arrays.asList(fieldWriters);
        this.fieldWriterArray = fieldWriters;
        this.features = features;
        this.hasValueField = fieldWriterArray.length == 1 && fieldWriterArray[0].isValue();

        String typeName = null;
        if (objectType != null) {
            if (Enum.class.isAssignableFrom(objectType) && !objectType.isEnum()) {
                typeName = objectType.getSuperclass().getName();
            } else {
                typeName = TypeUtils.getTypeName(objectType);
            }
        }
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;

        long[] hashCodes = new long[fieldWriterArray.length];
        for (int i = 0; i < fieldWriterArray.length; i++) {
            FieldWriter item = fieldWriterArray[i];
            long hashCode = Fnv.hashCode64(item.getFieldName());
            hashCodes[i] = hashCode;
        }

        this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
        Arrays.sort(this.hashCodes);

        mapping = new short[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = (short) i;
        }
    }

    @Override
    public FieldWriter getFieldWriter(long hashCode) {
        int m = Arrays.binarySearch(hashCodes, hashCode);
        if (m < 0) {
            return null;
        }

        int index = this.mapping[m];
        return fieldWriterArray[index];
    }

    @Override
    public boolean hasFilter(JSONWriter jsonWriter) {
        return propertyPreFilter != null
                || propertyFilter != null
                || nameFilter != null
                || valueFilter != null
                || jsonWriter.hasFilter();
    }

    public void setPropertyFilter(PropertyFilter propertyFilter) {
        this.propertyFilter = propertyFilter;
    }

    public void setValueFilter(ValueFilter valueFilter) {
        this.valueFilter = valueFilter;
    }

    public void setNameFilter(NameFilter nameFilter) {
        this.nameFilter = nameFilter;
    }

    public void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
        this.propertyPreFilter = propertyPreFilter;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        int size = fieldWriterArray.length;
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            writeClassInfo(jsonWriter);
        }
        jsonWriter.startObject();
        for (int i = 0; i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.write(jsonWriter, object);
        }
        jsonWriter.endObject();
    }

    protected void writeClassInfo(JSONWriter jsonWriter) {
        if (typeNameJSONB == null) {
            typeNameJSONB = JSONB.toBytes(typeName);
        }
        jsonWriter.writeTypeName(typeNameJSONB, typeNameHash);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (hasValueField) {
            FieldWriter fieldWriter = fieldWriterArray[0];
            fieldWriter.writeValue(jsonWriter, object);
            return;
        }

        if (jsonWriter.isJSONB()) {
            if (jsonWriter.isBeanToArray()) {
                writeArrayMappingJSONB(jsonWriter, object, fieldName, fieldType, features);
                return;
            }

            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (typeName != null) {
            switch (typeName) {
                case "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList":
                case "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet": {
                    Collection collection = (Collection) object;
                    ObjectWriterImplCollection.INSTANCE.write(jsonWriter, collection, fieldName, fieldType, features);
                    return;
                }
                default:
                    break;
            }
        }

        if (jsonWriter
                .isBeanToArray(
                        getFeatures())) {
            writeArrayMapping(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object);
            return;
        }

        jsonWriter.startObject();

        if (jsonWriter.isWriteTypeInfo(object, features)) {
            writeTypeInfo(jsonWriter);
        }

        for (int i = 0, size = fieldWriters.size(); i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.write(jsonWriter, object);
        }

        jsonWriter.endObject();
    }

    public Map<String, Object> toMap(Object object) {
        JSONObject map = new JSONObject(fieldWriters.size());
        for (int i = 0; i < fieldWriters.size(); i++) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            map.put(
                    fieldWriter.getFieldName(),
                    fieldWriter.getFieldValue(object)
            );
        }
        return map;
    }

    @Override
    public List<FieldWriter> getFieldWriters() {
        return fieldWriters;
    }

    byte[] jsonbClassInfo;

    @Override
    public boolean writeTypeInfo(JSONWriter jsonWriter) {
        if (jsonWriter.isUTF8()) {
            if (nameWithColonUTF8 == null) {
                byte[] chars = new byte[typeKey.length() + typeName.length() + 5];
                chars[0] = '"';
                typeKey.getBytes(0, typeKey.length(), chars, 1);
                chars[typeKey.length() + 1] = '"';
                chars[typeKey.length() + 2] = ':';
                chars[typeKey.length() + 3] = '"';
                typeName.getBytes(0, typeName.length(), chars, typeKey.length() + 4);
                chars[typeKey.length() + typeName.length() + 4] = '"';

                nameWithColonUTF8 = chars;
            }
            jsonWriter.writeNameRaw(nameWithColonUTF8);
            return true;
        } else if (jsonWriter.isUTF16()) {
            if (nameWithColonUTF16 == null) {
                char[] chars = new char[typeKey.length() + typeName.length() + 5];
                chars[0] = '"';
                typeKey.getChars(0, typeKey.length(), chars, 1);
                chars[typeKey.length() + 1] = '"';
                chars[typeKey.length() + 2] = ':';
                chars[typeKey.length() + 3] = '"';
                typeName.getChars(0, typeName.length(), chars, typeKey.length() + 4);
                chars[typeKey.length() + typeName.length() + 4] = '"';

                nameWithColonUTF16 = chars;
            }
            jsonWriter.writeNameRaw(nameWithColonUTF16);
            return true;
        } else if (jsonWriter.isJSONB()) {
            if (typeNameJSONB == null) {
                typeNameJSONB = JSONB.toBytes(typeName);
            }

            if (typeKeyJSONB == null) {
                typeKeyJSONB = JSONB.toBytes(typeKey);
            }
            jsonWriter.writeRaw(typeKeyJSONB);
            jsonWriter.writeRaw(typeNameJSONB);
            return true;
        }

        jsonWriter.writeString(typeKey);
        jsonWriter.writeColon();
        jsonWriter.writeString(typeName);
        return true;
    }

    @Override
    public void writeWithFilter(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.startObject();

        JSONWriter.Context ctx = jsonWriter.getContext();
        PropertyPreFilter propertyPreFilter = ctx.getPropertyPreFilter();
        NameFilter nameFilter = ctx.getNameFilter();
        ValueFilter valueFilter = ctx.getValueFilter();
        PropertyFilter propertyFilter = ctx.getPropertyFilter();

        List<FieldWriter> fieldWriters = getFieldWriters();
        for (int i = 0, size = fieldWriters.size(); i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);

            // pre property filter
            String fieldWriterFieldName = fieldWriter.getFieldName();
            if (propertyPreFilter != null
                    && !propertyPreFilter.process(jsonWriter, object, fieldWriterFieldName)) {
                continue;
            }

            // fast return
            if (nameFilter == null && propertyFilter == null && valueFilter == null) {
                fieldWriter.write(jsonWriter, object);
                continue;
            }

            Object fieldValue = fieldWriter.getFieldValue(object);
            if (fieldValue == null && !jsonWriter.isWriteNulls()) {
                continue;
            }

            // name filter
            String filteredName = null;
            if (nameFilter != null) {
                filteredName = nameFilter.process(object, fieldWriterFieldName, fieldValue);
            }

            // property filter
            if (propertyFilter != null
                    && !propertyFilter.apply(object, fieldWriterFieldName, fieldValue)) {
                continue;
            }

            boolean nameChanged = filteredName != fieldName;

            Object filteredValue;
            if (valueFilter != null
                    && (filteredValue = valueFilter.apply(object, fieldWriterFieldName, fieldValue)) != fieldValue) {
                if (nameChanged) {
                    jsonWriter.writeName(filteredName);
                    jsonWriter.writeColon();
                } else {
                    fieldWriter.writeFieldName(jsonWriter);
                }

                if (filteredValue == null) {
                    jsonWriter.writeNull();
                } else {
                    ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, filteredValue.getClass());
                    fieldValueWriter.write(jsonWriter, filteredValue, fieldName, fieldType, features);
                }
            } else {
                if (!nameChanged) {
                    fieldWriter.write(jsonWriter, object);
                } else {
                    if (nameChanged) {
                        jsonWriter.writeName(filteredName);
                        jsonWriter.writeColon();
                    } else {
                        fieldWriter.writeFieldName(jsonWriter);
                    }

                    if (fieldValue == null) {
                        ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, fieldWriter.getFieldClass());
                        fieldValueWriter.write(jsonWriter, null, fieldName, fieldType, features);
                    } else {
                        ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, fieldValue.getClass());
                        fieldValueWriter.write(jsonWriter, fieldValue, fieldName, fieldType, features);
                    }
                }
            }
        }

        jsonWriter.endObject();
    }

    public JSONObject toJSONObject(T object) {
        JSONObject jsonObject = new JSONObject();

        for (FieldWriter fieldWriter : fieldWriters) {
            Object fieldValue = fieldWriter.getFieldValue(object);
            jsonObject.put(fieldWriter.getFieldName(), fieldValue);
        }

        return jsonObject;
    }

    @Override
    public String toString() {
        return objectType.getName();
    }
}
