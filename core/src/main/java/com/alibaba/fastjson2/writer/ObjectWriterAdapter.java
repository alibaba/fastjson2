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

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;

public class ObjectWriterAdapter<T>
        implements ObjectWriter<T> {
    boolean hasFilter;
    PropertyPreFilter propertyPreFilter;
    PropertyFilter propertyFilter;
    NameFilter nameFilter;
    ValueFilter valueFilter;

    static final String TYPE = "@type";

    final Class objectClass;
    final List<FieldWriter> fieldWriters;
    protected final FieldWriter[] fieldWriterArray;

    final String typeKey;
    byte[] typeKeyJSONB;
    protected final String typeName;
    protected final long typeNameHash;
    protected long typeNameSymbolCache;
    protected byte[] typeNameJSONB;

    byte[] nameWithColonUTF8;
    char[] nameWithColonUTF16;

    final long features;

    final long[] hashCodes;
    final short[] mapping;

    final boolean hasValueField;
    final boolean serializable;
    final boolean containsNoneFieldGetter;
    final boolean googleCollection;

    public ObjectWriterAdapter(Class<T> objectClass, List<FieldWriter> fieldWriters) {
        this(objectClass, null, null, 0, fieldWriters);
    }

    public ObjectWriterAdapter(
            Class<T> objectClass,
            String typeKey,
            String typeName,
            long features,
            List<FieldWriter> fieldWriters
    ) {
        if (typeName == null && objectClass != null) {
            if (Enum.class.isAssignableFrom(objectClass) && !objectClass.isEnum()) {
                typeName = objectClass.getSuperclass().getName();
            } else {
                typeName = TypeUtils.getTypeName(objectClass);
            }
        }

        this.objectClass = objectClass;
        this.typeKey = typeKey == null || typeKey.isEmpty() ? TYPE : typeKey;
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;
        this.features = features;
        this.fieldWriters = fieldWriters;
        this.serializable = java.io.Serializable.class.isAssignableFrom(objectClass);
        this.googleCollection =
                "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList".equals(typeName)
                || "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet".equals(typeName);

        this.fieldWriterArray = new FieldWriter[fieldWriters.size()];
        fieldWriters.toArray(fieldWriterArray);

        this.hasValueField = fieldWriterArray.length == 1 && (fieldWriterArray[0].features & FieldInfo.VALUE_MASK) != 0;

        boolean containsNoneFieldGetter = false;
        long[] hashCodes = new long[fieldWriterArray.length];
        for (int i = 0; i < fieldWriterArray.length; i++) {
            FieldWriter fieldWriter = fieldWriterArray[i];
            long hashCode = Fnv.hashCode64(fieldWriter.fieldName);
            hashCodes[i] = hashCode;

            if (fieldWriter.method != null && (fieldWriter.features & FieldInfo.FIELD_MASK) == 0) {
                containsNoneFieldGetter = true;
            }
        }
        this.containsNoneFieldGetter = containsNoneFieldGetter;

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

    public ObjectWriterAdapter(Class<T> objectClass, long features, FieldWriter... fieldWriters) {
        this.objectClass = objectClass;
        this.typeKey = TYPE;
        this.fieldWriters = Arrays.asList(fieldWriters);
        this.fieldWriterArray = fieldWriters;
        this.features = features;
        this.hasValueField = fieldWriterArray.length == 1
                && (fieldWriterArray[0].features & FieldInfo.VALUE_MASK) != 0;
        this.serializable = objectClass == null || java.io.Serializable.class.isAssignableFrom(objectClass);

        String typeName = null;
        if (objectClass != null) {
            if (Enum.class.isAssignableFrom(objectClass) && !objectClass.isEnum()) {
                typeName = objectClass.getSuperclass().getName();
            } else {
                typeName = TypeUtils.getTypeName(objectClass);
            }
        }
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;

        this.googleCollection =
                "com.google.common.collect.AbstractMapBasedMultimap$RandomAccessWrappedList".equals(typeName)
                        || "com.google.common.collect.AbstractMapBasedMultimap$WrappedSet".equals(typeName);

        boolean containsNoneFieldGetter = false;
        long[] hashCodes = new long[fieldWriterArray.length];
        for (int i = 0; i < fieldWriterArray.length; i++) {
            FieldWriter fieldWriter = fieldWriterArray[i];
            long hashCode = Fnv.hashCode64(
                    fieldWriter.fieldName
            );
            hashCodes[i] = hashCode;

            if (fieldWriter.method != null && (fieldWriter.features & FieldInfo.FIELD_MASK) == 0) {
                containsNoneFieldGetter = true;
            }
        }
        this.containsNoneFieldGetter = containsNoneFieldGetter;

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
    public final boolean hasFilter(JSONWriter jsonWriter) {
        return hasFilter
                || (containsNoneFieldGetter ? jsonWriter.hasFilter(IgnoreNonFieldGetter.mask) : jsonWriter.hasFilter());
    }

    public void setPropertyFilter(PropertyFilter propertyFilter) {
        this.propertyFilter = propertyFilter;
        if (propertyFilter != null) {
            hasFilter = true;
        }
    }

    public void setValueFilter(ValueFilter valueFilter) {
        this.valueFilter = valueFilter;
        if (valueFilter != null) {
            hasFilter = true;
        }
    }

    public void setNameFilter(NameFilter nameFilter) {
        this.nameFilter = nameFilter;
        if (nameFilter != null) {
            hasFilter = true;
        }
    }

    public void setPropertyPreFilter(PropertyPreFilter propertyPreFilter) {
        this.propertyPreFilter = propertyPreFilter;
        if (propertyPreFilter != null) {
            hasFilter = true;
        }
    }

    @Override
    public void writeArrayMappingJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            writeClassInfo(jsonWriter);
        }

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
        SymbolTable symbolTable = jsonWriter.symbolTable;
        if (symbolTable != null) {
            int symbolTableIdentity = System.identityHashCode(symbolTable);

            int symbol;
            if (typeNameSymbolCache == 0) {
                symbol = symbolTable.getOrdinalByHashCode(typeNameHash);
                if (symbol != -1) {
                    typeNameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                }
            } else {
                int identity = (int) typeNameSymbolCache;
                if (identity == symbolTableIdentity) {
                    symbol = (int) (typeNameSymbolCache >> 32);
                } else {
                    symbol = symbolTable.getOrdinalByHashCode(typeNameHash);
                    if (symbol != -1) {
                        typeNameSymbolCache = ((long) symbol << 32) | symbolTableIdentity;
                    }
                }
            }

            if (symbol != -1) {
                jsonWriter.writeRaw(BC_TYPED_ANY);
                jsonWriter.writeInt32(-symbol);
                return;
            }
        }

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

        long featuresAll = features | this.features | jsonWriter.getFeatures();
        boolean beanToArray = (featuresAll & BeanToArray.mask) != 0;

        if (jsonWriter.jsonb) {
            if (beanToArray) {
                writeArrayMappingJSONB(jsonWriter, object, fieldName, fieldType, features);
                return;
            }

            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (googleCollection) {
            Collection collection = (Collection) object;
            ObjectWriterImplCollection.INSTANCE.write(jsonWriter, collection, fieldName, fieldType, features);
            return;
        }

        if (beanToArray) {
            writeArrayMapping(jsonWriter, object, fieldName, fieldType, features);
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
            writeWithFilter(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        jsonWriter.startObject();

        if (((features | this.features) & WriteClassName.mask) != 0 || jsonWriter.isWriteTypeInfo(object, features)) {
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
                    fieldWriter.fieldName,
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
        if (jsonWriter.utf8) {
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
        } else if (jsonWriter.utf16) {
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
        } else if (jsonWriter.jsonb) {
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
            if (jsonWriter.jsonb) {
                writeClassInfo(jsonWriter);
                jsonWriter.startObject();
            } else {
                jsonWriter.startObject();
                writeTypeInfo(jsonWriter);
            }
        } else {
            jsonWriter.startObject();
        }

        JSONWriter.Context context = jsonWriter.context;
        boolean ignoreNonFieldGetter = ((context.getFeatures() | features) & IgnoreNonFieldGetter.mask) != 0;

        BeforeFilter beforeFilter = context.getBeforeFilter();
        if (beforeFilter != null) {
            beforeFilter.writeBefore(jsonWriter, object);
        }

        PropertyPreFilter propertyPreFilter = context.getPropertyPreFilter();
        if (propertyPreFilter == null) {
            propertyPreFilter = this.propertyPreFilter;
        }

        NameFilter nameFilter = context.getNameFilter();
        if (nameFilter == null) {
            nameFilter = this.nameFilter;
        } else {
            if (this.nameFilter != null) {
                nameFilter = NameFilter.compose(this.nameFilter, nameFilter);
            }
        }

        ContextNameFilter contextNameFilter = context.getContextNameFilter();

        ValueFilter valueFilter = context.getValueFilter();
        if (valueFilter == null) {
            valueFilter = this.valueFilter;
        } else {
            if (this.valueFilter != null) {
                valueFilter = ValueFilter.compose(this.valueFilter, valueFilter);
            }
        }

        ContextValueFilter contextValueFilter = context.getContextValueFilter();

        PropertyFilter propertyFilter = context.getPropertyFilter();
        if (propertyFilter == null) {
            propertyFilter = this.propertyFilter;
        }

        LabelFilter labelFilter = context.getLabelFilter();

        for (int i = 0, size = fieldWriters.size(); i < size; ++i) {
            FieldWriter fieldWriter = fieldWriters.get(i);

            Field field = fieldWriter.field;
            if (ignoreNonFieldGetter
                    && fieldWriter.method != null
                    && (fieldWriter.features & FieldInfo.FIELD_MASK) == 0) {
                continue;
            }

            // pre property filter
            final String fieldWriterFieldName = fieldWriter.fieldName;
            if (propertyPreFilter != null
                    && !propertyPreFilter.process(jsonWriter, object, fieldWriterFieldName)) {
                continue;
            }

            if (labelFilter != null) {
                String label = fieldWriter.label;
                if (label != null && !label.isEmpty()) {
                    if (!labelFilter.apply(label)) {
                        continue;
                    }
                }
            }

            // fast return
            if (nameFilter == null
                    && propertyFilter == null
                    && valueFilter == null
                    && contextValueFilter == null
                    && contextNameFilter == null
                    && valueFilter == null
            ) {
                fieldWriter.write(jsonWriter, object);
                continue;
            }

            Object fieldValue;
            try {
                fieldValue = fieldWriter.getFieldValue(object);
            } catch (Throwable e) {
                if ((context.getFeatures() & JSONWriter.Feature.IgnoreErrorGetter.mask) != 0) {
                    continue;
                }
                throw e;
            }

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
                        objectClass,
                        fieldWriter.method,
                        field,
                        fieldWriter.fieldName,
                        fieldWriter.label,
                        fieldWriter.fieldClass,
                        fieldWriter.fieldType,
                        fieldWriter.features,
                        fieldWriter.format
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
                            objectClass,
                            fieldWriter.method,
                            field,
                            fieldWriter.fieldName,
                            fieldWriter.label,
                            fieldWriter.fieldClass,
                            fieldWriter.fieldType,
                            fieldWriter.features,
                            fieldWriter.format
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
                        ObjectWriter fieldValueWriter = fieldWriter.getObjectWriter(jsonWriter, fieldWriter.fieldClass);
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

            long fieldFeatures = fieldWriter.features;
            if ((fieldFeatures & FieldInfo.UNWRAPPED_MASK) != 0) {
                if (fieldValue instanceof Map) {
                    jsonObject.putAll((Map) fieldValue);
                    continue;
                }

                ObjectWriter fieldObjectWriter = fieldWriter.getInitWriter();
                if (fieldObjectWriter == null) {
                    fieldObjectWriter = JSONFactory.getDefaultObjectWriterProvider().getObjectWriter(fieldWriter.fieldClass);
                }
                List<FieldWriter> unwrappedFieldWriters = fieldObjectWriter.getFieldWriters();
                for (FieldWriter unwrappedFieldWriter : unwrappedFieldWriters) {
                    Object unwrappedFieldValue = unwrappedFieldWriter.getFieldValue(fieldValue);
                    jsonObject.put(unwrappedFieldWriter.fieldName, unwrappedFieldValue);
                }
                continue;
            }
            jsonObject.put(fieldWriter.fieldName, fieldValue);
        }

        return jsonObject;
    }

    @Override
    public String toString() {
        return objectClass.getName();
    }

    protected void errorOnNoneSerializable() {
        throw new JSONException("not support none serializable class " + objectClass.getName());
    }
}
