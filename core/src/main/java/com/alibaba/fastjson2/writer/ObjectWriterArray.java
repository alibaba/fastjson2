package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

final class ObjectWriterArray
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterArray INSTANCE = new ObjectWriterArray(Object.class);

    final byte[] typeNameBytes;
    final long typeNameHash;
    final Type itemType;

    final char[] prefixChars;
    final byte[] prefixBytes;

    public ObjectWriterArray(Type itemType) {
        this.itemType = itemType;
        String prefix = "{\"@type\":\"";
        if (itemType == Object.class) {
            typeNameBytes = JSONB.toBytes("[O");
            typeNameHash = Fnv.hashCode64("[0");
            prefix += "[O";
        } else {
            String typeName = '[' + TypeUtils.getTypeName((Class) itemType);
            typeNameBytes = JSONB.toBytes(typeName);
            typeNameHash = Fnv.hashCode64(typeName);
            prefix += typeName;
        }
        prefix += "\",\"@value\":[";
        prefixChars = prefix.toCharArray();
        prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
    }

    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter instanceof JSONWriterUTF16) {
            writeUTF16((JSONWriterUTF16) jsonWriter, object, fieldName, fieldType, features);
        } else if (jsonWriter instanceof JSONWriterUTF8) {
            writeUTF8((JSONWriterUTF8) jsonWriter, object, fieldName, fieldType, features);
        } else {
            writeJSONB((JSONWriterJSONB) jsonWriter, object, fieldName, fieldType, features);
        }
    }

    @Override
    public void writeUTF16(JSONWriterUTF16 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        boolean isWriteTypeInfo = jsonWriter.isWriteTypeInfo(object, fieldType);
        if (isWriteTypeInfo) {
            if (jsonWriter.utf16) {
                jsonWriter.writeRaw(prefixChars);
            } else {
                jsonWriter.writeRaw(prefixBytes);
            }
        } else {
            jsonWriter.startArray();
        }

        boolean refDetect = jsonWriter.isRefDetect();

        Object[] list = (Object[]) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        for (int i = 0; i < list.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                refDetect = jsonWriter.isRefDetect();
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;

                if (refDetect) {
                    refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                }
            }

            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.writeUTF16(jsonWriter, item, i, this.itemType, features);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
        if (isWriteTypeInfo) {
            jsonWriter.endObject();
        }
    }

    @Override
    public void writeUTF8(JSONWriterUTF8 jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        boolean isWriteTypeInfo = jsonWriter.isWriteTypeInfo(object, fieldType);
        if (isWriteTypeInfo) {
            if (jsonWriter.utf16) {
                jsonWriter.writeRaw(prefixChars);
            } else {
                jsonWriter.writeRaw(prefixBytes);
            }
        } else {
            jsonWriter.startArray();
        }

        boolean refDetect = jsonWriter.isRefDetect();

        Object[] list = (Object[]) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        for (int i = 0; i < list.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = list[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                refDetect = jsonWriter.isRefDetect();
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;

                if (refDetect) {
                    refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                }
            }

            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.writeUTF8(jsonWriter, item, i, this.itemType, features);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
        jsonWriter.endArray();
        if (isWriteTypeInfo) {
            jsonWriter.endObject();
        }
    }

    @Override
    public void writeJSONB(JSONWriterJSONB jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        boolean refDetect = jsonWriter.isRefDetect();

        Object[] list = (Object[]) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(typeNameBytes, typeNameHash);
        }

        jsonWriter.startArray(list.length);
        for (int i = 0; i < list.length; i++) {
            Object item = list[i];
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }

            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                refDetect = jsonWriter.isRefDetect();
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;

                if (refDetect) {
                    refDetect = !ObjectWriterProvider.isNotReferenceDetect(itemClass);
                }
            }

            if (refDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.writeJSONB(jsonWriter, item, i, this.itemType, 0);

            if (refDetect) {
                jsonWriter.popPath(item);
            }
        }
    }
}
