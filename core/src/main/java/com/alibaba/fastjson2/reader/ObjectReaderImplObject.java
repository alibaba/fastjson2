package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.util.Map;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.*;

final class ObjectReaderImplObject
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplObject INSTANCE = new ObjectReaderImplObject();

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return jsonReader.readAny();
        }

        JSONReader.Context context = jsonReader.getContext();

        if (jsonReader.isObject()) {
            jsonReader.next();

            long hash = jsonReader.readFieldNameHashCode();

            if (hash == HASH_TYPE) {
                boolean supportAutoType = context.isEnabled(JSONReader.Feature.SupportAutoType);

                ObjectReader autoTypeObjectReader;

                if (supportAutoType) {
                    long typeHash = jsonReader.readTypeHashCode();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);

                    if (autoTypeObjectReader != null) {
                        Class objectClass = autoTypeObjectReader.getObjectClass();
                        if (objectClass != null) {
                            ClassLoader objectClassLoader = objectClass.getClassLoader();
                            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                            if (objectClassLoader != contextClassLoader) {
                                Class contextClass = null;

                                String typeName = jsonReader.getString();
                                try {
                                    contextClass = contextClassLoader.loadClass(typeName);
                                } catch (ClassNotFoundException ignored) {
                                }

                                if (!objectClass.equals(contextClass)) {
                                    autoTypeObjectReader = context.getObjectReader(contextClass);
                                }
                            }
                        }
                    }

                    if (autoTypeObjectReader == null) {
                        String typeName = jsonReader.getString();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);
                    }
                } else {
                    String typeName = jsonReader.readString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);
                }

                if (autoTypeObjectReader != null) {
                    jsonReader.setTypeRedirect(true);

                    return autoTypeObjectReader.readObject(jsonReader, features);
                }
            }

            Map object;
            Supplier<Map> objectSupplier = jsonReader.getContext().getObjectSupplier();
            if (objectSupplier != null) {
                object = objectSupplier.get();
            } else {
                object = (Map) ObjectReaderImplMap.INSTANCE_OBJECT.createInstance(jsonReader.features(features));
            }

            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfMatch('}')) {
                    break;
                }

                String name;
                if (i == 0) {
                    name = jsonReader.getFieldName();
                } else {
                    name = jsonReader.readFieldName();
                }
                if (name == null) {
                    name = jsonReader.readFieldNameUnquote();
                    if (jsonReader.current() == ':') {
                        jsonReader.next();
                    }
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
                    case '{':
                        value = jsonReader.readObject();
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
                        throw new JSONException("error, offset " + jsonReader.getOffset() + ", char " + jsonReader.current());
                }
                object.put(name, value);
            }

            jsonReader.nextIfMatch(',');

            return object;
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
                throw new JSONException("illegal input : " + jsonReader.current() + ", offset " + jsonReader.getOffset());
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
