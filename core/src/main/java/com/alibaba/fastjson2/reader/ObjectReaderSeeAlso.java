package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

final class ObjectReaderSeeAlso<T> extends ObjectReaderAdapter<T> {

    final Class[] seeAlso;
    final String[] seeAlsoNames;

    final Map<Long, Class> seeAlsoMapping;

    ObjectReaderSeeAlso(Class objectType
            , Supplier<T> defaultCreator
            , String typeKey
            , Class[] seeAlso
            , String[] seeAlsoNames
            , FieldReader... fieldReaders
    ) {
        super(objectType, typeKey, null, JSONReader.Feature.SupportAutoType.mask, defaultCreator, null, fieldReaders);
        this.seeAlso = seeAlso;
        seeAlsoMapping = new HashMap<>(seeAlso.length);
        this.seeAlsoNames = new String[seeAlso.length];
        for (int i = 0; i < seeAlso.length; i++) {
            Class seeAlsoClass = seeAlso[i];

            String typeName = null;
            if (seeAlsoNames != null && seeAlsoNames.length >= i + 1) {
                typeName = seeAlsoNames[i];
            }
            if (typeName == null || typeName.isEmpty()) {
                typeName = seeAlsoClass.getSimpleName();
            }
            long hashCode = Fnv.hashCode64(typeName);
            seeAlsoMapping.put(hashCode, seeAlsoClass);
            this.seeAlsoNames[i] = typeName;
        }
    }

    @Override
    public T createInstance(long features) {
        if (creator == null) {
            return null;
        }
        return creator.get();
    }

    @Override
    public ObjectReader autoType(JSONReader.Context context, long typeHash) {
        Class seeAlsoClass = seeAlsoMapping.get(typeHash);
        if (seeAlsoClass == null) {
            return null;
        }

        return context.getObjectReader(seeAlsoClass);
    }

    @Override
    public ObjectReader autoType(ObjectReaderProvider provider, long typeHash) {
        Class seeAlsoClass = seeAlsoMapping.get(typeHash);
        if (seeAlsoClass == null) {
            return null;
        }

        return provider.getObjectReader(seeAlsoClass);
    }
}
