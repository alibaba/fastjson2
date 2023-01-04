package com.alibaba.fastjson;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

public class Fastjson1xReaderModule
        implements ObjectReaderModule {
    final ObjectReaderProvider provider;

    public Fastjson1xReaderModule(ObjectReaderProvider provider) {
        this.provider = provider;
    }

    @Override
    public ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        if (type == JSON.class) {
            return new JSONImpl();
        }
        return null;
    }

    static class JSONImpl
            implements ObjectReader {
        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            if (jsonReader.isObject()) {
                return jsonReader.read(JSONObject.class);
            }
            if (jsonReader.isArray()) {
                return jsonReader.read(JSONArray.class);
            }

            throw new JSONException("read json error");
        }

        @Override
        public Object createInstance(Collection collection) {
            return Collections.emptyList();
        }
    }
}
