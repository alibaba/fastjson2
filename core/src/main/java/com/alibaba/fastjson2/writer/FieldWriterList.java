package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.JSONWriter.MASK_WRITE_NULL_LIST_AS_EMPTY;

public class FieldWriterList<T>
        extends FieldWriter<T> {
    private static final Class<?> EMPTY_LIST_CLASS = Collections.emptyList().getClass();
    private static final Class<?> EMPTY_SET_CLASS = Collections.emptySet().getClass();

    final Type itemType;
    final Class itemClass;
    final boolean itemClassNotReferenceDetect;
    ObjectWriter listWriter;
    ObjectWriter itemObjectWriter;
    final boolean writeAsString;
    final Class<?> contentAs;

    public FieldWriterList(
            String name,
            Type itemType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function,
            Class<?> contentAs
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
        this.contentAs = contentAs;

        writeAsString = (features & WriteNonStringValueAsString.mask) != 0;

        this.itemType = itemType == null ? Object.class : itemType;
        if (this.itemType instanceof Class) {
            itemClass = (Class) itemType;
            if (itemClass != null) {
                if (Enum.class.isAssignableFrom(this.itemClass)) {
                    listWriter = new ObjectWriterImplListEnum(fieldClass, itemClass, features);
                } else if (itemClass == String.class) {
                    listWriter = ObjectWriterImplListStr.INSTANCE;
                } else {
                    listWriter = new ObjectWriterImplList(fieldClass, fieldType, itemClass, itemType, features);
                }
            }
        } else {
            itemClass = TypeUtils.getMapping(itemType);
        }
        this.itemClassNotReferenceDetect = itemClass != null && ObjectWriterProvider.isNotReferenceDetect(itemClass);

        if (format != null) {
            if (itemClass == Date.class) {
                itemObjectWriter = new ObjectWriterImplDate(format, null);
            }
        }
    }

    @Override
    public final Type getItemType() {
        return itemType;
    }

    @Override
    public final Class getItemClass() {
        return itemClass;
    }

    @Override
    public final ObjectWriter getItemWriter(JSONWriter jsonWriter, Type itemType) {
        boolean sortVariant = ObjectWriterProvider.isFieldNamesSorted(this.features | jsonWriter.getFeatures());
        long resolvedFeatures = this.features | jsonWriter.getFeatures();

        if (contentAs != null) {
            ObjectWriter itemObjectWriter = this.itemObjectWriter;
            if (itemObjectWriter != null && (!sortVariant || (format != null && itemClass == Date.class))) {
                // format-aware Date presets are sort-invariant; other entries may have been stored
                // by the natural variant when registered variants share this field writer
                return itemObjectWriter;
            }
            // resolve with the merged word so a field-level sort request reaches the provider;
            // only the store is gated, never the resolution
            ObjectWriter resolved = jsonWriter.getContext().getProvider()
                    .getObjectWriter(this.contentAs, contentAs, resolvedFeatures);
            if (sortVariant) {
                // never store sorted writers in this cross-context field
                return resolved;
            }
            return this.itemObjectWriter = resolved;
        }

        if (itemType == null || itemType == this.itemType) {
            ObjectWriter itemObjectWriter = this.itemObjectWriter;
            if (itemObjectWriter != null && (!sortVariant || (format != null && itemClass == Date.class))) {
                // constructor presets (e.g. format-aware Date writers) are sort-invariant; other
                // entries may have been stored by the natural variant sharing this field writer
                return itemObjectWriter;
            }
            if (format != null) {
                return jsonWriter.getContext()
                        .getProvider()
                        .getObjectWriter(itemType, format, null);
            }
            if (sortVariant) {
                return jsonWriter.getContext().getProvider()
                        .getObjectWriter(this.itemType, itemClass, resolvedFeatures);
            }
            return this.itemObjectWriter = jsonWriter.getContext().getProvider()
                    .getObjectWriter(this.itemType, itemClass, resolvedFeatures);
        }

        return jsonWriter.getContext().getProvider()
                .getObjectWriter(itemType, TypeUtils.getClass(itemType), resolvedFeatures);
    }

    @Override
    public final ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        // constructor-built list writers are sort-invariant and always honoured first
        ObjectWriter listWriter = this.listWriter;
        if (listWriter != null && fieldClass.isAssignableFrom(valueClass)) {
            return listWriter;
        }

        long resolvedFeatures = this.features | jsonWriter.getFeatures();
        if (listWriter == null && valueClass == fieldClass) {
            ObjectWriter resolved = jsonWriter.getContext().getProvider()
                    .getObjectWriter(valueClass, valueClass, resolvedFeatures);
            if (!ObjectWriterProvider.isFieldNamesSorted(resolvedFeatures)) {
                this.listWriter = resolved;
            }
            return resolved;
        }

        return jsonWriter.getContext().getProvider()
                .getObjectWriter(valueClass, valueClass, resolvedFeatures);
    }

    @Override
    public void writeListValueJSONB(JSONWriter jsonWriter, List list) {
        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        long features = jsonWriter.getFeatures(this.features);
        boolean beanToArray = (features & JSONWriter.Feature.BeanToArray.mask) != 0;

        int size = list.size();

        boolean refDetect = (features & ReferenceDetection.mask) != 0;

        if (jsonWriter.isWriteTypeInfo(list, fieldClass)) {
            jsonWriter.writeTypeName(
                    TypeUtils.getTypeName(list.getClass()));
        }

        jsonWriter.startArray(size);
        for (int i = 0; i < size; i++) {
            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass != previousClass) {
                refDetect = jsonWriter.isRefDetect();
                previousObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                if (refDetect) {
                    if (itemClass == this.itemClass) {
                        refDetect = !itemClassNotReferenceDetect;
                    } else {
                        refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                    }
                }
            }
            itemObjectWriter = previousObjectWriter;

            if (refDetect && jsonWriter.writeReference(i, item)) {
                continue;
            }

            if (beanToArray) {
                itemObjectWriter.writeArrayMappingJSONB(jsonWriter, item, i, itemType, features);
            } else {
                itemObjectWriter.writeJSONB(jsonWriter, item, i, itemType, features);
            }

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
    }

    @Override
    public final void writeListValue(JSONWriter jsonWriter, List list) {
        if (jsonWriter.jsonb) {
            writeListValueJSONB(jsonWriter, list);
            return;
        }

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        long features = jsonWriter.getFeatures(this.features);

        boolean previousItemRefDetect = (features & ReferenceDetection.mask) != 0;

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            Class<?> itemClass = item.getClass();
            if (itemClass == String.class) {
                jsonWriter.writeString((String) item);
                continue;
            } else if (writeAsString) {
                jsonWriter.writeString(item.toString());
                continue;
            }

            boolean itemRefDetect;
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
                itemRefDetect = previousItemRefDetect;
            } else {
                itemRefDetect = (features & ReferenceDetection.mask) != 0;
                itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
                if (itemRefDetect) {
                    itemRefDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                }
                previousItemRefDetect = itemRefDetect;
            }

            if (itemRefDetect) {
                if (jsonWriter.writeReference(i, item)) {
                    continue;
                }
            }

            if (managedReference) {
                jsonWriter.addManagerReference(item);
            }

            itemObjectWriter.write(jsonWriter, item, null, itemType, features);

            if (itemRefDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
    }

    public final void writeListJSONB(JSONWriter jsonWriter, List list) {
        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        long features = jsonWriter.getFeatures(this.features);
        boolean beanToArray = (features & JSONWriter.Feature.BeanToArray.mask) != 0;

        int size = list.size();

        if ((features & NotWriteEmptyArray.mask) != 0 && size == 0) {
            return;
        }

        writeFieldName(jsonWriter);

        boolean refDetect = (features & ReferenceDetection.mask) != 0;
        if (jsonWriter.isWriteTypeInfo(list, fieldClass)) {
            jsonWriter.writeTypeName(
                    TypeUtils.getTypeName(list.getClass()));
        }

        jsonWriter.startArray(size);
        for (int i = 0; i < size; i++) {
            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass != previousClass) {
                refDetect = jsonWriter.isRefDetect();
                previousObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                if (refDetect) {
                    if (itemClass == this.itemClass) {
                        refDetect = !itemClassNotReferenceDetect;
                    } else {
                        refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                    }
                }
            }
            itemObjectWriter = previousObjectWriter;

            if (refDetect && jsonWriter.writeReference(i, item)) {
                continue;
            }

            if (beanToArray) {
                itemObjectWriter.writeArrayMappingJSONB(jsonWriter, item, i, itemType, features);
            } else {
                itemObjectWriter.writeJSONB(jsonWriter, item, i, itemType, features);
            }

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
    }

    @Override
    public final void writeList(JSONWriter jsonWriter, List list) {
        if (jsonWriter.jsonb) {
            writeListJSONB(jsonWriter, list);
            return;
        }

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        long features = jsonWriter.getFeatures(this.features);

        if ((features & NotWriteEmptyArray.mask) != 0 && list.isEmpty()) {
            return;
        }

        writeFieldName(jsonWriter);
        boolean previousItemRefDetect = (features & ReferenceDetection.mask) != 0;

        jsonWriter.startArray();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list.get(i);
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            Class<?> itemClass = item.getClass();
            if (itemClass == String.class) {
                jsonWriter.writeString((String) item);
                continue;
            }

            boolean itemRefDetect;
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
                itemRefDetect = previousItemRefDetect;
            } else {
                itemRefDetect = jsonWriter.isRefDetect();
                itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
                if (itemRefDetect) {
                    itemRefDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                }
                previousItemRefDetect = itemRefDetect;
            }

            if (itemRefDetect) {
                if (jsonWriter.writeReference(i, item)) {
                    continue;
                }
            } else if (this.managedReference) {
                jsonWriter.addManagerReference(item);
            }

            itemObjectWriter.write(jsonWriter, item, null, itemType, features);

            if (itemRefDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
    }

    @Override
    public final void writeListStr(JSONWriter jsonWriter, boolean writeFieldName, List<String> list) {
        if (list.isEmpty() && (jsonWriter.getFeatures(this.features) & MASK_NOT_WRITE_EMPTY_ARRAY) != 0) {
            return;
        }
        if (writeFieldName) {
            writeFieldName(jsonWriter);
        }

        if (jsonWriter.jsonb) {
            if (jsonWriter.isWriteTypeInfo(list, fieldClass)) {
                jsonWriter.writeTypeName(
                        TypeUtils.getTypeName(list.getClass()));
            }
        }

        jsonWriter.writeString(list);
    }

    public final boolean isRefDetect(Object object, long features) {
        Class<?> objectClass;
        features |= this.features;
        return (features & ReferenceDetection.mask) != 0
                && (features & FieldInfo.DISABLE_REFERENCE_DETECT) == 0
                && object != null
                && ((objectClass = object.getClass()) != EMPTY_LIST_CLASS) && (objectClass != EMPTY_SET_CLASS);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return TypeUtils.toList(propertyAccessor.getObject(object));
        } catch (Throwable e) {
            throw errorOnGet(e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        List value;
        try {
            value = (List) getFieldValue(object);
        } catch (Exception error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & (MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_LIST_AS_EMPTY)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull(features);
                return true;
            } else {
                return false;
            }
        }

        String refPath = jsonWriter.setPath(this, value);
        if (refPath != null) {
            writeFieldName(jsonWriter);
            jsonWriter.writeReference(refPath);
            jsonWriter.popPath(value);
            return true;
        }

        if (itemType == String.class) {
            writeListStr(jsonWriter, true, value);
        } else {
            writeList(jsonWriter, value);
        }
        jsonWriter.popPath(value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        List value = (List) propertyAccessor.getObject(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        boolean refDetect = jsonWriter.isRefDetect();

        if (refDetect) {
            String refPath = jsonWriter.setPath(fieldName, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        writeListValue(jsonWriter, value);

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}
