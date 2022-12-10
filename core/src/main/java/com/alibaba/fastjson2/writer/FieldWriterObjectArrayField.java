package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

final class FieldWriterObjectArrayField<T>
        extends FieldWriter<T> {
    final Type itemType;
    final Class itemClass;
    ObjectWriter itemObjectWriter;

    protected FieldWriterObjectArrayField(
            String fieldName,
            Type itemType,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field
    ) {
        super(fieldName, ordinal, features, format, label, fieldType, fieldClass, field, null);
        this.itemType = itemType;
        if (itemType instanceof Class) {
            itemClass = (Class) itemType;
        } else {
            itemClass = TypeUtils.getMapping(itemType);
        }
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public ObjectWriter getItemWriter(JSONWriter jsonWriter, Type itemType) {
        if (itemType == null || itemType == this.itemType) {
            if (itemObjectWriter != null) {
                return itemObjectWriter;
            }
            return itemObjectWriter = jsonWriter
                    .getObjectWriter(this.itemType, itemClass);
        }
        return jsonWriter
                .getObjectWriter(itemType, TypeUtils.getClass(itemType));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Object[] value = (Object[]) getFieldValue(object);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (WriteNulls.mask | NullAsDefaultValue.mask | WriteNullListAsEmpty.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeArrayNull();
                return true;
            } else {
                return false;
            }
        }

        writeArray(jsonWriter, true, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Object[] value = (Object[]) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        writeArray(jsonWriter, false, value);
    }

    public void writeArray(JSONWriter jsonWriter, boolean writeFieldName, Object[] array) {
        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        long features = jsonWriter.getFeatures();
        boolean refDetect = (features & JSONWriter.Feature.ReferenceDetection.mask) != 0;
        boolean previousItemRefDetect = refDetect;

        if (writeFieldName) {
            if (array.length == 0 && (features & JSONWriter.Feature.NotWriteEmptyArray.mask) != 0) {
                return;
            }

            writeFieldName(jsonWriter);
        }

        if (refDetect) {
            String path = jsonWriter.setPath(fieldName, array);
            if (path != null) {
                jsonWriter.writeReference(path);
                return;
            }
        }

        if (jsonWriter.jsonb) {
            Class arrayClass = array.getClass();
            if (arrayClass != this.fieldClass) {
                jsonWriter.writeTypeName(
                        TypeUtils.getTypeName(arrayClass));
            }

            int size = array.length;
            jsonWriter.startArray(size);
            for (int i = 0; i < size; i++) {
                Object item = array[i];
                if (item == null) {
                    jsonWriter.writeNull();
                    continue;
                }

                boolean itemRefDetect;
                Class<?> itemClass = item.getClass();
                ObjectWriter itemObjectWriter;
                if (itemClass != previousClass) {
                    itemRefDetect = jsonWriter.isRefDetect();
                    previousObjectWriter = getItemWriter(jsonWriter, itemClass);
                    previousClass = itemClass;
                    if (itemRefDetect) {
                        itemRefDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                    }
                    previousItemRefDetect = itemRefDetect;
                } else {
                    itemRefDetect = previousItemRefDetect;
                }

                itemObjectWriter = previousObjectWriter;

                if (itemRefDetect) {
                    String refPath = jsonWriter.setPath(i, item);
                    if (refPath != null) {
                        jsonWriter.writeReference(refPath);
                        jsonWriter.popPath(item);
                        continue;
                    }
                }

                itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, this.features);

                if (itemRefDetect) {
                    jsonWriter.popPath(item);
                }
            }

            if (refDetect) {
                jsonWriter.popPath(array);
            }

            return;
        }

        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = array[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            itemObjectWriter.write(jsonWriter, item);
        }
        jsonWriter.endArray();
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == String[].class) {
            return ObjectWriterImplStringArray.INSTANCE;
        }

        return jsonWriter.getObjectWriter(valueClass);
    }
}
