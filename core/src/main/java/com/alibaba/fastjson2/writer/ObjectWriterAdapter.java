package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
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
    final boolean serializable;

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
        this.serializable = java.io.Serializable.class.isAssignableFrom(objectType);

        this.fieldWriterArray = new FieldWriter[fieldWriters.size()];
        fieldWriters.toArray(fieldWriterArray);

        this.hasValueField = fieldWriterArray.length == 1 && (fieldWriterArray[0].getFeatures() & FieldInfo.VALUE_MASK) != 0;

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
        this.hasValueField = fieldWriterArray.length == 1
                && (fieldWriterArray[0].getFeatures() & FieldInfo.VALUE_MASK) != 0;
        this.serializable = objectType == null || java.io.Serializable.class.isAssignableFrom(objectType);

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
    public void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            writeClassInfo(jsonWriter);
        }

        List<FieldWriter> fieldWriters = getFieldWriters();
        int size = fieldWriters.size();
        jsonWriter.startArray(size);
        for (int i = 0; i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);
            fieldWriter.writeValue(jsonWriter, object);
        }
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        long featuresAll = features | this.features | jsonWriter.getFeatures();

        if (!serializable) {
            if ((featuresAll & JSONWriter.Feature.ErrorOnNoneSerializable.mask) != 0) {
                errorOnNoneSerializable();
                return;
            }

            if ((featuresAll & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
                jsonWriter.writeNull();
                return;
            }
        }

        if ((featuresAll & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
            writeWithFilter(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

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

        long featuresAll = features | this.features | jsonWriter.getFeatures();
        if ((featuresAll & JSONWriter.Feature.BeanToArray.mask) != 0) {
            writeArrayMapping(jsonWriter, object, fieldName, fieldType, features | this.features);
            return;
        }

        if (!serializable) {
            if ((featuresAll & JSONWriter.Feature.ErrorOnNoneSerializable.mask) != 0) {
                errorOnNoneSerializable();
                return;
            }

            if ((featuresAll & JSONWriter.Feature.IgnoreNoneSerializable.mask) != 0) {
                jsonWriter.writeNull();
                return;
            }
        }

        if (hasFilter(jsonWriter)) {
            writeWithFilter(jsonWriter, object, fieldName, fieldType, 0);
            return;
        }

        jsonWriter.startObject();

        if (((features | this.features) & JSONWriter.Feature.WriteClassName.mask) != 0 || jsonWriter.isWriteTypeInfo(object, features)) {
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
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            if (jsonWriter.isJSONB()) {
                writeClassInfo(jsonWriter);
                jsonWriter.startObject();
            } else {
                jsonWriter.startObject();
                writeTypeInfo(jsonWriter);
            }
        } else {
            jsonWriter.startObject();
        }

        JSONWriter.Context context = jsonWriter.getContext();
        boolean ignoreNonFieldGetter = ((context.getFeatures() | features) & JSONWriter.Feature.IgnoreNonFieldGetter.mask) != 0;

        BeforeFilter beforeFilter = context.getBeforeFilter();
        if (beforeFilter != null) {
            beforeFilter.writeBefore(jsonWriter, object);
        }

        PropertyPreFilter propertyPreFilter = context.getPropertyPreFilter();
        NameFilter nameFilter = context.getNameFilter();
        ContextNameFilter contextNameFilter = context.getContextNameFilter();
        ValueFilter valueFilter = context.getValueFilter();
        ContextValueFilter contextValueFilter = context.getContextValueFilter();
        PropertyFilter propertyFilter = context.getPropertyFilter();
        LabelFilter labelFilter = context.getLabelFilter();

        List<FieldWriter> fieldWriters = getFieldWriters();
        for (int i = 0, size = fieldWriters.size(); i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);

            Field field = fieldWriter.getField();
            if (ignoreNonFieldGetter && fieldWriter.getMethod() != null && field == null) {
                continue;
            }

            // pre property filter
            final String fieldWriterFieldName = fieldWriter.getFieldName();
            if (propertyPreFilter != null
                    && !propertyPreFilter.process(jsonWriter, object, fieldWriterFieldName)) {
                continue;
            }

            if (labelFilter != null) {
                String label = fieldWriter.getLabel();
                if (label != null && !label.isEmpty()) {
                    if (!labelFilter.apply(label)) {
                        continue;
                    }
                }
            }

            // fast return
            if (nameFilter == null && propertyFilter == null && valueFilter == null && contextValueFilter == null && contextNameFilter == null) {
                fieldWriter.write(jsonWriter, object);
                continue;
            }

            Object fieldValue = fieldWriter.getFieldValue(object);
            if (fieldValue == null && !jsonWriter.isWriteNulls()) {
                continue;
            }

            BeanContext beanContext = null;

            // name filter
            String filteredName = fieldWriterFieldName;
            if (nameFilter != null) {
                filteredName = nameFilter.process(object, filteredName, fieldValue);
            }

            if (contextNameFilter != null) {
                beanContext = new BeanContext(
                        objectType,
                        fieldWriter.getMethod(),
                        field,
                        fieldWriter.getFieldName(),
                        fieldWriter.getLabel(),
                        fieldWriter.getFieldClass(),
                        fieldWriter.getFieldType(),
                        fieldWriter.getFeatures(),
                        fieldWriter.getFormat()
                );
                filteredName = contextNameFilter.process(beanContext, object, filteredName, fieldValue);
            }

            // property filter
            if (propertyFilter != null
                    && !propertyFilter.apply(object, fieldWriterFieldName, fieldValue)) {
                continue;
            }

            boolean nameChanged = filteredName != null && filteredName != fieldWriterFieldName;

            Object filteredValue = fieldValue;
            if (valueFilter != null) {
                filteredValue = valueFilter.apply(object, fieldWriterFieldName, fieldValue);
            }
            if (contextValueFilter != null) {
                if (beanContext == null) {
                    beanContext = new BeanContext(
                            objectType,
                            fieldWriter.getMethod(),
                            field,
                            fieldWriter.getFieldName(),
                            fieldWriter.getLabel(),
                            fieldWriter.getFieldClass(),
                            fieldWriter.getFieldType(),
                            fieldWriter.getFeatures(),
                            fieldWriter.getFormat()
                    );
                }
                filteredValue = contextValueFilter.process(beanContext, object, filteredName, filteredValue);
            }

            if (filteredValue != fieldValue) {
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

        AfterFilter afterFilter = context.getAfterFilter();
        if (afterFilter != null) {
            afterFilter.writeAfter(jsonWriter, object);
        }

        jsonWriter.endObject();
    }

    public JSONObject toJSONObject(T object) {
        JSONObject jsonObject = new JSONObject();

        for (FieldWriter fieldWriter : fieldWriters) {
            Object fieldValue = fieldWriter.getFieldValue(object);

            long fieldFeatures = fieldWriter.getFeatures();
            if ((fieldFeatures & FieldInfo.UNWRAPPED_MASK) != 0) {
                if (fieldValue instanceof Map) {
                    jsonObject.putAll((Map) fieldValue);
                    continue;
                }

                ObjectWriter fieldObjectWriter = fieldWriter.getInitWriter();
                if (fieldObjectWriter == null) {
                    fieldObjectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(fieldWriter.getFieldClass());
                }
                List<FieldWriter> unwrappedFieldWriters = fieldObjectWriter.getFieldWriters();
                for (FieldWriter unwrappedFieldWriter : unwrappedFieldWriters) {
                    Object unwrappedFieldValue = unwrappedFieldWriter.getFieldValue(fieldValue);
                    jsonObject.put(unwrappedFieldWriter.getFieldName(), unwrappedFieldValue);
                }
                continue;
            }
            jsonObject.put(fieldWriter.getFieldName(), fieldValue);
        }

        return jsonObject;
    }

    @Override
    public String toString() {
        return objectType.getName();
    }

    protected void errorOnNoneSerializable() {
        throw new JSONException("not support none serializable class " + objectType.getName());
    }
}
