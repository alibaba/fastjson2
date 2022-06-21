package com.alibaba.fastjson;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;

public class Fastjson1xWriterModule
        implements ObjectWriterModule {
    final ObjectWriterProvider provider;

    public Fastjson1xWriterModule(ObjectWriterProvider provider) {
        this.provider = provider;
    }

    @Override
    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (objectClass != null && JSONAware.class.isAssignableFrom(objectClass)) {
            return JSONAwareWriter.INSTANCE;
        }

        return null;
    }

    static class JSONAwareWriter
            implements ObjectWriter {
        static final JSONAwareWriter INSTANCE = new JSONAwareWriter();

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (object == null) {
                jsonWriter.writeNull();
                return;
            }

            JSONAware jsonAware = (JSONAware) object;
            String str = jsonAware.toJSONString();
            jsonWriter.writeRaw(str);
        }
    }
}
