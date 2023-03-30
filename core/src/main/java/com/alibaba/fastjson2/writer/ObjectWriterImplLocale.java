package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.Locale;

final class ObjectWriterImplLocale
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplLocale INSTANCE = new ObjectWriterImplLocale();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeString(((Locale) object).toString());
    }
}
