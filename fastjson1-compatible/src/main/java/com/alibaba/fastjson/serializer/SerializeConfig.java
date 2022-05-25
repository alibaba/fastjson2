package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class SerializeConfig {
    public static SerializeConfig global = new SerializeConfig();
    public PropertyNamingStrategy propertyNamingStrategy;

    public static SerializeConfig getGlobalInstance() {
        return global;
    }

    public boolean put(Type type, ObjectSerializer value) {
        return JSONFactory
                .getDefaultObjectWriterProvider()
                .register(type, new ObjectSerializerAdapter(value));
    }

    public void setAsmEnable(boolean value) {
        // skip
    }

    static final class ObjectSerializerAdapter
            implements ObjectWriter {
        final ObjectSerializer serializer;

        public ObjectSerializerAdapter(ObjectSerializer serializer) {
            this.serializer = serializer;
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            JSONSerializer serializer = new JSONSerializer(jsonWriter);

            try {
                this.serializer.write(serializer, object, fieldName, fieldType, 0);
            } catch (IOException e) {
                throw new JSONException("serializer write error", e);
            }
        }
    }
}
