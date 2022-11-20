package com.alibaba.fastjson2.adapter.gson;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.gson.reflect.TypeToken;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;

public class Gson {
    ObjectReaderProvider readerProvider;
    ObjectWriterProvider writerProvider;

    public Gson() {
    }

    protected ObjectReaderProvider getReaderProvider() {
        if (readerProvider == null) {
            readerProvider = new ObjectReaderProvider();
        }
        return readerProvider;
    }

    public ObjectWriterProvider getWriterProvider() {
        if (writerProvider == null) {
            writerProvider = new ObjectWriterProvider();
        }
        return writerProvider;
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        ObjectReaderProvider provider = getReaderProvider();
        JSONReader.Context context = JSONFactory.createReadContext(provider);
        ObjectReader objectReader = provider.getObjectReader(classOfT);
        try (JSONReader jsonReader = JSONReader.of(json, context)) {
            Object object = objectReader.readObject(jsonReader, classOfT, null, 0L);
            jsonReader.handleResolveTasks(object);
            return (T) object;
        }
    }

    public <T> T fromJson(String json, Type typeOfT) {
        ObjectReaderProvider provider = getReaderProvider();
        JSONReader.Context context = JSONFactory.createReadContext(provider);
        ObjectReader objectReader = provider.getObjectReader(typeOfT);
        try (JSONReader jsonReader = JSONReader.of(json, context)) {
            Object object = objectReader.readObject(jsonReader, typeOfT, null, 0L);
            jsonReader.handleResolveTasks(object);
            return (T) object;
        }
    }

    public <T> T fromJson(String json, TypeToken<T> typeToken) {
        return fromJson(json, typeToken.getType());
    }

    public String toJson(Object src) {
        ObjectWriterProvider provider = getWriterProvider();
        JSONWriter.Context context = JSONFactory.createWriteContext(provider);
        JSONWriter writer = JSONWriter.of(context);
        writer.writeAny(src);
        return writer.toString();
    }
}
