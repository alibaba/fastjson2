package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

final class ObjectReaderImplClass extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplClass INSTANCE = new ObjectReaderImplClass();
    static final long TYPE_HASH = Fnv.hashCode64("java.lang.Class");

    @Override
    public Class getObjectClass() {
        return Class.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long valueHashCode = jsonReader.readTypeHashCode();
            if (valueHashCode != TYPE_HASH) {
                throw new JSONException("not support autoType : " + jsonReader.getString());
            }
        }
        return readObject(jsonReader, features);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        String className = jsonReader.readString();

        JSONReader.Context context = jsonReader.getContext();
        if (!context.isEnable(JSONReader.Feature.SupportClassForName)) {
            throw new JSONException("not support autoType : " + jsonReader.getString());
        }

        Class mappingClass = TypeUtils.getMapping(className);
        if (mappingClass != null) {
            return mappingClass;
        }

        ObjectReaderProvider provider = context.getProvider();
        Class<?> resolvedClass = provider.checkAutoType(className, null, JSONReader.Feature.SupportAutoType.mask);
        if (resolvedClass == null) {
            throw new JSONException("class not found " + className);
        }
        return resolvedClass;
    }
}
