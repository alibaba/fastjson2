package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

final class ObjectWriterImplCollection
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplCollection INSTANCE = new ObjectWriterImplCollection();

    static final byte[] LINKED_HASH_SET_JSONB_TYPE_NAME_BYTES = JSONB.toBytes(TypeUtils.getTypeName(LinkedHashSet.class));
    static final long LINKED_HASH_SET_JSONB_TYPE_HASH = Fnv.hashCode64(TypeUtils.getTypeName(LinkedHashSet.class));

    static final byte[] TREE_SET_JSONB_TYPE_NAME_BYTES = JSONB.toBytes(TypeUtils.getTypeName(TreeSet.class));
    static final long TREE_SET_JSONB_TYPE_HASH = Fnv.hashCode64(TypeUtils.getTypeName(TreeSet.class));

    Type itemType;
    long features;

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        Type fieldItemType = null;
        Class fieldClass = null;
        if (fieldType instanceof Class) {
            fieldClass = (Class) fieldType;
        } else if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 1) {
                fieldItemType = actualTypeArguments[0];
            }

            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                fieldClass = (Class) rawType;
            }
        }

        Collection collection = (Collection) object;

        Class<?> objectClass = object.getClass();
        boolean writeTypeInfo = jsonWriter.isWriteTypeInfo(object, fieldClass);
        if (writeTypeInfo) {
            if (fieldClass == Set.class && objectClass == HashSet.class) {
                writeTypeInfo = false;
            } else if (fieldType == Collection.class && objectClass == ArrayList.class) {
                writeTypeInfo = false;
            }
        }

        if (writeTypeInfo) {
            if (objectClass == LinkedHashSet.class) {
                jsonWriter.writeTypeName(LINKED_HASH_SET_JSONB_TYPE_NAME_BYTES, LINKED_HASH_SET_JSONB_TYPE_HASH);
            } else if (objectClass == TreeSet.class) {
                jsonWriter.writeTypeName(TREE_SET_JSONB_TYPE_NAME_BYTES, TREE_SET_JSONB_TYPE_HASH);
            } else {
                jsonWriter.writeTypeName(TypeUtils.getTypeName(objectClass));
            }
        }

//        if (collection.size() > 1) {
//            Object first = collection.iterator().next();
//            if (first != null) {
//                Class firstClass = first.getClass();
//                if (!TypeUtils.isPrimitive(firstClass)) {
//                    ObjectWriter firstWriter = jsonWriter.getObjectWriter(firstClass);
//                    if (firstWriter.writeJSONBTable(jsonWriter, collection, fieldName, fieldType, fieldItemClass, features)) {
//                        return;
//                    }
//                }
//            }
//        }

        boolean refDetect = jsonWriter.isRefDetect();
        if (collection.size() > 1 && !(collection instanceof SortedSet) && !(collection instanceof LinkedHashSet)) {
            refDetect = false;
        }

        jsonWriter.startArray(collection.size());

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        int i = 0;
        for (Iterator it = collection.iterator(); it.hasNext(); ++i) {
            Object item = it.next();
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            boolean itemRefDetect = refDetect && !ObjectWriterProvider.isNotReferenceDetect(itemClass);

            if (itemRefDetect) {
                String refPath = jsonWriter.setPath(i, item);
                if (refPath != null) {
                    jsonWriter.writeReference(refPath);
                    jsonWriter.popPath(item);
                    continue;
                }
            }

            itemObjectWriter.writeJSONB(jsonWriter, item, i, fieldItemType, features);

            if (itemRefDetect) {
                jsonWriter.popPath(item);
            }
        }
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.jsonb) {
            writeJSONB(jsonWriter, object, fieldName, fieldType, features);
            return;
        }

        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (object instanceof Set && jsonWriter.isWriteTypeInfo(object, features | this.features)) {
            jsonWriter.writeRaw("Set");
        }

        Iterable iterable = (Iterable) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        jsonWriter.startArray();
        int i = 0;
        for (Iterator it = iterable.iterator(); it.hasNext(); ) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            Object item = it.next();
            if (item == null) {
                jsonWriter.writeNull();
                continue;
            }
            Class<?> itemClass = item.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            itemObjectWriter.write(jsonWriter, item, i, this.itemType, this.features);

            ++i;
        }
        jsonWriter.endArray();
    }
}
