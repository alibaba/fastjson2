package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class CollectionCodec
        implements ObjectSerializer, ObjectDeserializer {
    public static final CollectionCodec instance = new CollectionCodec();

    @Override
    public Object deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONReader reader = parser.getLexer().getReader();
        ObjectReader objectReader = reader.getContext().getObjectReader(type);
        return objectReader.readObject(reader, type, fieldName, 0L);
    }

    @Override
    public void write(
            JSONSerializer serializer,
            Object object,
            Object fieldName,
            Type fieldType,
            int features
    ) throws IOException {
        JSONWriter raw = serializer.out.raw;
        ObjectWriter<?> objectWriter = raw.getContext().getObjectWriter(object.getClass());
        objectWriter.write(raw, object, fieldName, fieldType, 0L);
    }

    @Override
    public long getFeatures() {
        return 0L;
    }
}
