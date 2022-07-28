package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

abstract class FieldWriterList<T>
        extends FieldWriterImpl<T> {
    final Type itemType;
    final Class itemClass;
    final boolean itemClassNotReferenceDetect;
    ObjectWriter listWriter;
    ObjectWriter itemObjectWriter;

    FieldWriterList(String name, Type itemType, int ordinal, long features, String format, String label, Type fieldType, Class fieldClass) {
        super(name, ordinal, features, format, label, fieldType, fieldClass);

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
        this.itemClassNotReferenceDetect = itemClass == null ? false : ObjectWriterProvider.isNotReferenceDetect(itemClass);

        if (format != null) {
            if (itemClass == Date.class) {
                itemObjectWriter = new ObjectWriterImplDate(format, null);
            }
        }
    }

    @Override
    public Type getItemType() {
        return itemType;
    }

    @Override
    public Class getItemClass() {
        return itemClass;
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
                .getObjectWriter(itemType, null);
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (listWriter != null && fieldClass.isAssignableFrom(valueClass)) {
            return listWriter;
        }

        if (listWriter == null && valueClass == fieldClass) {
            return listWriter = jsonWriter.getObjectWriter(valueClass);
        }

        return jsonWriter.getObjectWriter(valueClass);
    }

    @Override
    public void writeList(JSONWriter jsonWriter, boolean writeFieldName, List list) {
        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        long features = this.features | jsonWriter.getFeatures();
        boolean refDetect = (features & JSONWriter.Feature.ReferenceDetection.mask) != 0;
        boolean beanToArray = (features & JSONWriter.Feature.BeanToArray.mask) != 0;

        if ((features & JSONWriter.Feature.NotWriteEmptyArray.mask) != 0 && list.isEmpty() && writeFieldName) {
            return;
        }

        if (writeFieldName) {
            writeFieldName(jsonWriter);
        }

        if (jsonWriter.isJSONB()) {
            int size = list.size();

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

                if (refDetect) {
                    String refPath = jsonWriter.setPath(i, item);
                    if (refPath != null) {
                        jsonWriter.writeReference(refPath);
                        jsonWriter.popPath(item);
                        continue;
                    }
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
            return;
        }

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
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = getItemWriter(jsonWriter, itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.write(jsonWriter, item, null, itemType, features);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
    }

    @Override
    public void writeListStr(JSONWriter jsonWriter, boolean writeFieldName, List<String> list) {
        if (writeFieldName) {
            writeFieldName(jsonWriter);
        }

        if (jsonWriter.isJSONB()) {
            final int listSize = list.size();
            jsonWriter.startArray(listSize);
            for (int i = 0, size = listSize; i < size; i++) {
                String str = list.get(i);
                jsonWriter.writeString(str);
            }
            return;
        }

        jsonWriter.startArray();
        for (int i = 0, size = list.size(); i < size; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            String str = list.get(i);
            jsonWriter.writeString(str);
        }
        jsonWriter.endArray();
    }
}
