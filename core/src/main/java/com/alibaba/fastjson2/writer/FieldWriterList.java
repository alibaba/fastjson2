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

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

public abstract class FieldWriterList<T>
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

    FieldWriterList(
            String name,
            Type itemType,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Class<?> contentAs
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method);
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
        if (contentAs != null) {
            ObjectWriter itemObjectWriter = this.itemObjectWriter;
            if (itemObjectWriter != null) {
                return itemObjectWriter;
            }
            return this.itemObjectWriter = jsonWriter.getObjectWriter(this.contentAs, contentAs);
        }
        if (itemType == null || itemType == this.itemType) {
            if (itemObjectWriter != null) {
                return itemObjectWriter;
            }

            if (format != null) {
                return jsonWriter.getContext()
                        .getProvider()
                        .getObjectWriter(itemType, format, null);
            }

            return itemObjectWriter = jsonWriter
                    .getObjectWriter(this.itemType, itemClass);
        }

        return jsonWriter
                .getObjectWriter(itemType, TypeUtils.getClass(itemType));
    }

    @Override
    public final ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        ObjectWriter listWriter = this.listWriter;
        if (listWriter != null && fieldClass.isAssignableFrom(valueClass)) {
            return listWriter;
        }

        if (listWriter == null && valueClass == fieldClass) {
            return this.listWriter = jsonWriter.getObjectWriter(valueClass);
        }

        return jsonWriter.getObjectWriter(valueClass);
    }

    @Override
    public final void writeListValueJSONB(JSONWriter jsonWriter, List list) {
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
                if (itemClass == this.itemType && this.itemObjectWriter != null) {
                    previousObjectWriter = this.itemObjectWriter;
                } else {
                    previousObjectWriter = getItemWriter(jsonWriter, itemClass);
                }
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
            writeListJSONB(jsonWriter, list);
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
                if (itemClass == this.itemType && this.itemObjectWriter != null) {
                    previousObjectWriter = this.itemObjectWriter;
                } else {
                    previousObjectWriter = getItemWriter(jsonWriter, itemClass);
                }
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
}
