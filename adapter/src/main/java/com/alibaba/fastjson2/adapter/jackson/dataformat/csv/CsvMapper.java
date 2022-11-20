package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader;
import com.alibaba.fastjson2.adapter.jackson.databind.ObjectWriter;

public class CsvMapper
        extends ObjectMapper {
    public CsvMapper() {
        this(new CsvFactory());
    }

    public CsvMapper(CsvFactory f) {
        super(f);
    }

    public ObjectWriter writerWithSchemaFor(Class<?> pojoType) {
        return new ObjectWriter(this, serializationConfig, pojoType);
    }

    public ObjectReader readerWithSchemaFor(Class<?> pojoType) {
        return new ObjectReader(this, deserializationConfig, pojoType);
    }
}
