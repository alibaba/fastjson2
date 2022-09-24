package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;

class ObjectReaderInterfaceImpl
        extends ObjectReaderPrimitive {
    final Type interfaceType;

    public ObjectReaderInterfaceImpl(Type interfaceType) {
        super(TypeUtils.getClass(interfaceType));
        this.interfaceType = interfaceType;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfMatch('{')) {
            long hash = jsonReader.readFieldNameHashCode();
            JSONReader.Context context = jsonReader.getContext();

            if (hash == HASH_TYPE && context.isEnabled(JSONReader.Feature.SupportAutoType)) {
                long typeHash = jsonReader.readTypeHashCode();
                ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass);

                    if (autoTypeObjectReader == null) {
                        throw new JSONException(jsonReader.info("auoType not support : " + typeName));
                    }
                }

                return autoTypeObjectReader.readObject(jsonReader, fieldType, fieldName, 0);
            }

            return ObjectReaderImplMap.INSTANCE.readObject(jsonReader, fieldType, fieldName, 0);
        }

        Object value;
        switch (jsonReader.current()) {
            case '-':
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '.':
                value = jsonReader.readNumber();
                break;
            case '[':
                value = jsonReader.readArray();
                break;
            case '"':
            case '\'':
                value = jsonReader.readString();
                break;
            case 't':
            case 'f':
                value = jsonReader.readBoolValue();
                break;
            case 'n':
                jsonReader.readNull();
                value = null;
                break;
            default:
                throw new JSONException(jsonReader.info());
        }

        return value;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        return jsonReader.readAny();
    }
}
