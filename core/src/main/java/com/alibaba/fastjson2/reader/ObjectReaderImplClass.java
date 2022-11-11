package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;

final class ObjectReaderImplClass
        extends ObjectReaderPrimitive {
    static final ObjectReaderImplClass INSTANCE = new ObjectReaderImplClass();
    static final long TYPE_HASH = Fnv.hashCode64("java.lang.Class");

    ObjectReaderImplClass() {
        super(Class.class);
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfMatch(JSONB.Constants.BC_TYPED_ANY)) {
            long valueHashCode = jsonReader.readTypeHashCode();
            if (valueHashCode != TYPE_HASH) {
                throw new JSONException(jsonReader.info("not support autoType : " + jsonReader.getString()));
            }
        }
        return readObject(jsonReader, fieldType, fieldName, features);
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        long classNameHash = jsonReader.readValueHashCode();

        JSONReader.Context context = jsonReader.getContext();
        JSONReader.AutoTypeBeforeHandler typeFilter = context.getContextAutoTypeBeforeHandler();
        if (typeFilter != null) {
            Class<?> filterClass = typeFilter.apply(classNameHash, Class.class, features);
            if (filterClass == null) {
                String className = jsonReader.getString();
                filterClass = typeFilter.apply(className, Class.class, features);
            }

            if (filterClass != null) {
                return filterClass;
            }
        }

        String className = jsonReader.getString();
        boolean classForName = ((context.getFeatures() | features) & JSONReader.Feature.SupportClassForName.mask) != 0;
        if (!classForName) {
            String msg = jsonReader.info("not support ClassForName : " + className + ", you can config 'JSONReader.Feature.SupportClassForName'");
            throw new JSONException(msg);
        }

        Class mappingClass = TypeUtils.getMapping(className);
        if (mappingClass != null) {
            return mappingClass;
        }

        ObjectReaderProvider provider = context.getProvider();
        Class<?> resolvedClass = provider.checkAutoType(className, null, JSONReader.Feature.SupportAutoType.mask);
        if (resolvedClass == null) {
            throw new JSONException(jsonReader.info("class not found " + className));
        }
        return resolvedClass;
    }
}
