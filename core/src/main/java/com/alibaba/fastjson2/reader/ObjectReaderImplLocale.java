package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.util.Locale;

class ObjectReaderImplLocale
        extends ObjectReaderBaseModule.PrimitiveImpl {
    static final ObjectReaderImplLocale INSTANCE = new ObjectReaderImplLocale();

    @Override
    public Class getObjectClass() {
        return Locale.class;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, long features) {
        String strVal = jsonReader.readString();
        if (strVal == null || strVal.isEmpty()) {
            return null;
        }
        String[] items = strVal.split("_");
        if (items.length == 1) {
            return new Locale(items[0]);
        }
        if (items.length == 2) {
            return new Locale(items[0], items[1]);
        }
        return new Locale(items[0], items[1], items[2]);
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        String strVal = jsonReader.readString();
        if (strVal == null || strVal.isEmpty()) {
            return null;
        }
        String[] items = strVal.split("_");
        if (items.length == 1) {
            return new Locale(items[0]);
        }
        if (items.length == 2) {
            return new Locale(items[0], items[1]);
        }
        return new Locale(items[0], items[1], items[2]);
    }
}
