package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;

public abstract class ObjectReaderBean<T> implements ObjectReader<T> {
    final protected Class objectClass;
    final protected String typeName;
    final protected long typeNameHash;

    protected ObjectReaderBean(Class objectClass, String typeName) {
        if (typeName == null) {
            if (objectClass != null) {
                typeName = TypeUtils.getTypeName(objectClass);
            }
        }

        this.objectClass = objectClass;
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;
    }

    public ObjectReader checkAutoType(JSONReader jsonReader, Class listClass, long features) {
        ObjectReader autoTypeObjectReader = null;
        if (jsonReader.nextIfMatch(BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            JSONReader.Context context = jsonReader.getContext();
            autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);

            if (autoTypeObjectReader == null) {
                String typeName = jsonReader.getString();
                autoTypeObjectReader = context.getObjectReaderAutoType(typeName, listClass, features);
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException("auotype not support : " + jsonReader.getString());
            }

            if (typeHash == this.typeNameHash) {
                return this;
            }

            boolean isSupportAutoType = ((context.getFeatures() | features) & JSONReader.Feature.SupportAutoType.mask) != 0;
            if (!isSupportAutoType) {
                return null;
//                throw new JSONException("autoType not support input " + jsonReader.getString());
            }
        }
        return autoTypeObjectReader;
    }
}
