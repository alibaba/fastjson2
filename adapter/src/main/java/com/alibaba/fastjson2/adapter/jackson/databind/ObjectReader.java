package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.adapter.jackson.core.FormatSchema;
import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.dataformat.csv.CSVMappingIterator;
import com.alibaba.fastjson2.support.csv.CSVParser;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;

public class ObjectReader {
    final ObjectMapper mapper;
    final DeserializationConfig config;
    Type objectType;
    Class objectClass;
    final JsonFactory jsonFactory;

    public ObjectReader(ObjectMapper mapper, DeserializationConfig config, Type objectType) {
        this.mapper = mapper;
        this.config = config;
        this.objectType = objectType;
        this.objectClass = TypeUtils.getClass(objectType);
        this.jsonFactory = mapper.factory;
    }

    public <T> T readValue(File src, Class<T> valueType) throws IOException {
        if (jsonFactory.isCSV()) {
            JSONReader.Context context = jsonFactory.createReaderContext();
            CSVParser csvParser = CSVParser.of(src, objectClass);
            return (T) csvParser.readLoneObject();
        }

        JSONReader jsonReader = this.jsonFactory.createJSONReader(src);
        JSONReader.Context context = jsonReader.getContext();
        com.alibaba.fastjson2.reader.ObjectReader objectReader = context.getObjectReader(objectType);

        Object object = objectReader
                .readObject(jsonReader, null, null, 0L);
        return (T) object;
    }

    public <T> T readValue(String src, Class<T> valueType) throws IOException {
        if (jsonFactory.isCSV()) {
            CSVParser csvParser = CSVParser.of(src, valueType);
            return (T) csvParser.readLoneObject();
        }

        JSONReader jsonReader = this.jsonFactory.createJSONReader(src);
        JSONReader.Context context = jsonReader.getContext();
        com.alibaba.fastjson2.reader.ObjectReader objectReader = context.getObjectReader(valueType);

        Object object = objectReader
                .readObject(jsonReader, null, null, 0L);
        return (T) object;
    }

    public <T> T readValue(JsonParser p) throws IOException {
        JSONReader jsonReader = p.getJSONReader();
        JSONReader.Context context = jsonReader.getContext();
        com.alibaba.fastjson2.reader.ObjectReader objectReader = context.getObjectReader(objectType);

        Object object = objectReader
                .readObject(jsonReader, null, null, 0L);
        return (T) object;
    }

    public ObjectReader with(FormatSchema schema) {
        return this;
    }

    public <T> MappingIterator<T> readValues(Reader src) throws IOException {
        if (jsonFactory.isCSV()) {
            CSVParser parser = CSVParser.of(src, objectClass);
            return new CSVMappingIterator(parser, objectClass);
        }
        throw new JSONException("TODO");
    }

    public <T> MappingIterator<T> readValues(InputStream src) throws IOException {
        if (jsonFactory.isCSV()) {
            CSVParser parser = CSVParser.of(src, objectClass);
            return new CSVMappingIterator(parser, objectClass);
        }
        throw new JSONException("TODO");
    }

    public ObjectReader withAttribute(Object key, Object value) {
        return this;
    }

    public ObjectReader with(InjectableValues values) {
        return this;
    }

    public <T> T readValue(byte[] content) throws IOException {
        JSONReader.Context context = mapper.deserializationConfig.createReaderContext();
        JSONReader jsonReader = JSONReader.of(content, context);
        com.alibaba.fastjson2.reader.ObjectReader objectReader = context.getObjectReader(objectType);

        Object object = objectReader
                .readObject(jsonReader, null, null, 0L);
        return (T) object;
    }
}
