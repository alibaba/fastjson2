package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

final class FieldWriterObjectArrayField<T> extends FieldWriterImpl<T> {
    final Field field;
    final Type itemType;
    final Class itemClass;
    ObjectWriter itemObjectWriter;

    protected FieldWriterObjectArrayField(
            String fieldName
            , Type itemType
            , int ordinal
            , long features
            , String format
            , Type fieldType
            , Class fieldClass
            , Field field
    ) {
        super(fieldName, ordinal, features, format, fieldType, fieldClass);
        this.field = field;
        this.itemType = itemType;
        if (itemType instanceof Class) {
            itemClass = (Class) itemType;
        } else {
            itemClass = TypeUtils.getMapping(itemType);
        }
    }

    @Override
    public Field getField() {
        return field;
    }

    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
        }
    }

    public ObjectWriter getItemWriter(JSONWriter jsonWriter, Type itemType) {
        if (itemType == null || itemType == this.itemType) {
            if (itemObjectWriter != null) {
                return itemObjectWriter;
            }
            return itemObjectWriter = jsonWriter
                    .getObjectWriter(this.itemType, itemClass);
        }
        return jsonWriter
                .getObjectWriter(itemType, null);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Object[] value = (Object[]) getFieldValue(object);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) != 0) {
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

        if (writeFieldName) {
            writeFieldName(jsonWriter);
        }

        boolean refDetect = jsonWriter.isRefDetect();

        if (refDetect) {
            String path = jsonWriter.setPath(name, array);
            if (path != null) {
                jsonWriter.writeReference(path);
                return;
            }
        }

        if (jsonWriter.isJSONB()) {
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

                boolean itemRefDetect = refDetect;
                Class<?> itemClass = item.getClass();
                ObjectWriter itemObjectWriter;
                if (itemClass != previousClass) {
                    itemRefDetect = jsonWriter.isRefDetect();
                    previousObjectWriter = getItemWriter(jsonWriter, itemClass);
                    previousClass = itemClass;
                    if (itemRefDetect) {
                        itemRefDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                    }
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

    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == String[].class) {
            return ObjectWriterImplStringArray.INSTANCE;
        }

        return jsonWriter.getObjectWriter(valueClass);
    }
}
