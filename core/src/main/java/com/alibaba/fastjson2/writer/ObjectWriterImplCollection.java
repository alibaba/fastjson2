package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * ObjectWriterImplCollection provides serialization support for Collection types to JSON format.
 * This writer handles various Collection implementations including ArrayList, HashSet, LinkedHashSet,
 * TreeSet, and other custom collection types.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>Generic Collection serialization as JSON arrays</li>
 *   <li>Type information writing for specific collection types</li>
 *   <li>Support for homogeneous and heterogeneous collections</li>
 *   <li>Optimized serialization for common collection types</li>
 *   <li>Empty collection handling</li>
 *   <li>Reference detection for collection elements</li>
 *   <li>JSONB format with optimized encoding</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Serialize a List
 * List<String> list = Arrays.asList("apple", "banana", "cherry");
 * String json = JSON.toJSONString(list); // ["apple","banana","cherry"]
 *
 * // Serialize a Set
 * Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
 * String json = JSON.toJSONString(set); // [1,2,3]
 *
 * // Serialize with type information
 * LinkedHashSet<String> linkedSet = new LinkedHashSet<>();
 * linkedSet.add("first");
 * linkedSet.add("second");
 * String json = JSON.toJSONString(linkedSet, JSONWriter.Feature.WriteClassName);
 * }</pre>
 *
 * @since 2.0.0
 */
final class ObjectWriterImplCollection
        extends ObjectWriterPrimitiveImpl {
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

        long features3;
        if (object instanceof Set
                && jsonWriter.isWriteTypeInfo(object, features3 = jsonWriter.getFeatures(features | this.features))
        ) {
            if ((features3 & JSONWriter.Feature.NotWriteSetClassName.mask) == 0) {
                jsonWriter.writeRaw("Set");
            }
        }

        Iterable iterable = (Iterable) object;

        Class previousClass = null;
        ObjectWriter previousObjectWriter = null;
        jsonWriter.startArray();
        int i = 0;
        for (Object o : iterable) {
            if (i != 0) {
                jsonWriter.writeComma();
            }

            if (o == null) {
                jsonWriter.writeNull();
                i++;
                continue;
            }
            Class<?> itemClass = o.getClass();
            ObjectWriter itemObjectWriter;
            if (itemClass == previousClass) {
                itemObjectWriter = previousObjectWriter;
            } else {
                itemObjectWriter = jsonWriter.getObjectWriter(itemClass);
                previousClass = itemClass;
                previousObjectWriter = itemObjectWriter;
            }

            itemObjectWriter.write(jsonWriter, o, i, this.itemType, this.features);

            ++i;
        }
        jsonWriter.endArray();
    }
}
