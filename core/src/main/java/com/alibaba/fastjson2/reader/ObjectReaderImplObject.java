package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import static com.alibaba.fastjson2.JSONB.Constants.*;

final class ObjectReaderImplObject extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplObject INSTANCE = new ObjectReaderImplObject();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return jsonReader.readAny();
        }

        JSONReader.Context context = jsonReader.getContext();

        if (jsonReader.isObject()) {
            boolean supportAutoType = context.isEnable(JSONReader.Feature.SupportAutoType);

            if (!supportAutoType) {
                return ObjectReaderImplMap.INSTANCE_OBJECT.readObject(jsonReader, features);
            }

            jsonReader.next();

            long hash = jsonReader.readFieldNameHashCode();

            if (hash == HASH_TYPE && supportAutoType) {
                long typeHash = jsonReader.readTypeHashCode();
                ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                    if (autoTypeObjectReader == null) {
                        throw new JSONException("auotype not support : " + typeName);
                    }
                }

                jsonReader.setTypeRedirect(true);

                return autoTypeObjectReader.readObject(jsonReader, features);
            }

            return ObjectReaderImplMap.INSTANCE_OBJECT.readObject(jsonReader, features);
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
                throw new JSONException("TODO : " + jsonReader.current());
        }

        return value;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        byte type = jsonReader.getType();
        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_UTF16BE) {
            return jsonReader.readString();
        }

        if (type == BC_TYPED_ANY) {
            ObjectReader autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, features);
            return autoTypeObjectReader.readJSONBObject(jsonReader, features);
        }

        if (type == BC_NULL) {
            jsonReader.next();
            return null;
        }

        return jsonReader.readAny();
    }
}
