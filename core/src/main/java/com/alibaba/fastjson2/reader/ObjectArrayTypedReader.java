package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Array;
import java.util.Arrays;

final class ObjectArrayTypedReader extends ObjectReaderBaseModule.PrimitiveImpl {
    final Class objectClass;
    final Class componentType;
    final Class componentClass;
    final long componentClassHash;
    ObjectReader itemObjectReader = null;
    final String typeName;
    final long typeNameHashCode;

    ObjectArrayTypedReader(Class objectClass) {
        this.objectClass = objectClass;
        this.componentType = objectClass.getComponentType();
        String componentTypeName = TypeUtils.getTypeName(componentType);
        this.componentClassHash = Fnv.hashCode64(componentTypeName);
        this.typeName = '[' + componentTypeName;
        typeNameHashCode = Fnv.hashCode64(this.typeName);

        this.componentClass = TypeUtils.getClass(componentType);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, 0);
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

        throw new JSONException("TODO");
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
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
                        throw new JSONException("auotype not support : " + jsonReader.getString());
                    }

                    return autoTypeObjectReader.readObject(jsonReader, features);
                }

                throw new JSONException("not support autotype : " + jsonReader.getString());
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
                if (reference.equals("..")) {
                    value = values;
                } else {
                    value = null;
                    jsonReader.addResolveTask(values, i, JSONPath.of(reference));
                }
            } else {
                ObjectReader autoTypeReader = jsonReader.checkAutoType(componentClass, componentClassHash, features);
                if (autoTypeReader != null) {
                    value = autoTypeReader.readJSONBObject(jsonReader, features);
                } else {
                    value = jsonReader.read(componentType);
                }
            }

            values[i] = value;
        }
        return values;
    }
}
