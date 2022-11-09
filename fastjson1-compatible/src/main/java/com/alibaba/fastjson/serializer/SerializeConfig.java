package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.IOException;
import java.lang.reflect.Type;

public class SerializeConfig {
    public static SerializeConfig global = new SerializeConfig(null);

    public final boolean fieldBased;
    public PropertyNamingStrategy propertyNamingStrategy;

    private ObjectWriterProvider provider;

    public static SerializeConfig getGlobalInstance() {
        return global;
    }

    public SerializeConfig() {
        this(new ObjectWriterProvider());
    }

    public SerializeConfig(ObjectWriterProvider provider) {
        this.fieldBased = false;
        this.provider = provider;
    }

    public SerializeConfig(boolean fieldBased) {
        this.fieldBased = fieldBased;
    }

    public ObjectWriterProvider getProvider() {
        ObjectWriterProvider provider = this.provider;
        if (provider == null) {
            provider = JSONFactory.getDefaultObjectWriterProvider();
        }
        return provider;
    }

    public boolean put(Type type, ObjectSerializer value) {
        ObjectWriterProvider provider = this.provider;
        if (provider == null) {
            provider = JSONFactory.getDefaultObjectWriterProvider();
        }
        return provider.register(type, new ObjectSerializerAdapter(value)) == null;
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
