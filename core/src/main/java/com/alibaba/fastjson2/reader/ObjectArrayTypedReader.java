package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

final class ObjectArrayTypedReader
        extends ObjectReaderPrimitive {
    final Class componentType;
    final Class componentClass;
    final long componentClassHash;
    final String typeName;
    final long typeNameHashCode;

    ObjectArrayTypedReader(Class objectClass) {
        super(objectClass);
        this.componentType = objectClass.getComponentType();
        String componentTypeName = TypeUtils.getTypeName(componentType);
        this.componentClassHash = Fnv.hashCode64(componentTypeName);
        this.typeName = '[' + componentTypeName;
        typeNameHashCode = Fnv.hashCode64(this.typeName);

        this.componentClass = TypeUtils.getClass(componentType);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.readIfNull()) {
            return null;
        }

        if (jsonReader.nextIfMatch('[')) {
            Object[] values = (Object[]) Array.newInstance(componentType, 16);
            int size = 0;
            for (; ; ) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                int minCapacity = size + 1;
                if (minCapacity - values.length > 0) {
                    int oldCapacity = values.length;
                    int newCapacity = oldCapacity + (oldCapacity >> 1);
                    if (newCapacity - minCapacity < 0) {
                        newCapacity = minCapacity;
                    }

                    values = Arrays.copyOf(values, newCapacity);
                }

                Object value = jsonReader.read(componentType);
                values[size++] = value;

                jsonReader.nextIfMatch(',');
            }
            jsonReader.nextIfMatch(',');

            return Arrays.copyOf(values, size);
        }

        if (jsonReader.current() == '"') {
            String str = jsonReader.readString();
            if (str.isEmpty()) {
                return null;
            }
        }

        throw new JSONException(jsonReader.info("TODO"));
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.getType() == JSONB.Constants.BC_TYPED_ANY) {
            jsonReader.next();
            long typeHash = jsonReader.readTypeHashCode();
            if (typeHash == ObjectArrayReader.TYPE_HASH_CODE || typeHash == typeNameHashCode) {
                // skip
            } else {
                JSONReader.Context context = jsonReader.getContext();
                if (jsonReader.isSupportAutoType(features)) {
                    ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                    if (autoTypeObjectReader == null) {
                        String typeName = jsonReader.getString();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass, features);
                    }

                    if (autoTypeObjectReader == null) {
                        throw new JSONException(jsonReader.info("auotype not support : " + jsonReader.getString()));
                    }

                    return autoTypeObjectReader.readObject(jsonReader, fieldType, fieldName, features);
                }

                throw new JSONException(jsonReader.info("not support autotype : " + jsonReader.getString()));
            }
        }

        int entryCnt = jsonReader.startArray();
        if (entryCnt == -1) {
            return null;
        }

        Object[] values = (Object[]) Array.newInstance(componentClass, entryCnt);
        for (int i = 0; i < entryCnt; ++i) {
            Object value;

            if (jsonReader.isReference()) {
                String reference = jsonReader.readReference();
                if ("..".equals(reference)) {
                    value = values;
                } else {
                    value = null;
                    jsonReader.addResolveTask(values, i, JSONPath.of(reference));
                }
            } else {
                ObjectReader autoTypeReader = jsonReader.checkAutoType(componentClass, componentClassHash, features);
                if (autoTypeReader != null) {
                    value = autoTypeReader.readJSONBObject(jsonReader, null, null, features);
                } else {
                    value = jsonReader.read(componentType);
                }
            }

            values[i] = value;
        }
        return values;
    }

    @Override
    public Object createInstance(Collection collection) {
        Object[] values = (Object[]) Array.newInstance(componentClass, collection.size());
        int index = 0;
        for (Object item : collection) {
            if (item != null) {
                Class<?> valueClass = item.getClass();
                if (valueClass != componentType) {
                    ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
                    Function typeConvert = provider.getTypeConvert(valueClass, componentType);
                    if (typeConvert != null) {
                        item = typeConvert.apply(item);
                    }
                }
            }

            if (componentType.isInstance(item)) {
                values[index++] = item;
            } else {
                ObjectReader objectReader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(componentType);
                if (item instanceof Map) {
                    item = objectReader.createInstance((Map) item);
                } else if (item instanceof Collection) {
                    item = objectReader.createInstance((Collection) item);
                } else if (item instanceof Object[]) {
                    item = objectReader.createInstance(JSONArray.of((Object[]) item));
                } else if (item != null) {
                    Class<?> itemClass = item.getClass();
                    if (itemClass.isArray()) {
                        int length = Array.getLength(item);
                        JSONArray array = new JSONArray(length);
                        for (int i = 0; i < length; i++) {
                            array.add(Array.get(item, i));
                        }
                        item = objectReader.createInstance(array);
                    } else {
                        throw new JSONException("component type not match, expect " + componentType.getName() + ", but " + itemClass);
                    }
                }
                values[index++] = item;
            }
        }
        return values;
    }
}
