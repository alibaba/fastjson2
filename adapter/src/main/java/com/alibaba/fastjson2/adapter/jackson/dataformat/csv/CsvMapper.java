package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectWriter;

public class CsvMapper
        extends ObjectMapper {
    private CsvFactory factory;
    public CsvMapper() {
        this(new CsvFactory());
    }

    public CsvMapper(CsvFactory factory) {
        super(factory);
        this.factory = factory;
    }

    public ObjectWriter writerWithSchemaFor(Class<?> pojoType) {
        return new ObjectWriter(this, serializationConfig, pojoType);
    }

    public ObjectReader readerWithSchemaFor(Class<?> pojoType) {
        return new ObjectReader(this, deserializationConfig, pojoType);
    }

    public CsvMapper enable(CsvParser.Feature f) {
        factory.enable(f);
        return this;
    }

    public final CsvSchema schemaFor(Class<?> pojoType) {
        throw new JSONException("TODO");
    }
}
