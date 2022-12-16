package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.JsonFactory;
import com.alibaba.fastjson2.adapter.jackson.core.JsonProcessingException;
import com.alibaba.fastjson2.support.csv.CSVWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class ObjectWriter {
    final SerializationConfig config;
    final Type objectType;
    final Class objectClass;
    final JsonFactory jsonFactory;

    public ObjectWriter(
            ObjectMapper mapper,
            SerializationConfig config,
            Type objectType
    ) {
        this.config = config;
        this.objectType = objectType;
        this.objectClass = TypeUtils.getClass(objectType);
        this.jsonFactory = mapper.factory;
    }

    public String writeValueAsString(Object value)
            throws IOException {
        if (jsonFactory.isCSV()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CSVWriter writer = CSVWriter.of(out, CSVWriter.Feature.AlwaysQuoteStrings);
            writer.writeRowObject(value);
            writer.close();
            String str = new String(out.toByteArray(), StandardCharsets.UTF_8);
            return str;
        }

        JSONWriter jsonWriter = jsonFactory.createJSONWriter();
        JSONWriter.Context context = jsonWriter.getContext();
        context.getObjectWriter(objectType, objectClass)
                .write(jsonWriter, value, null, null, 0L);
        return jsonWriter.toString();
    }

    public void writeValue(File resultFile, Object value) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(resultFile)) {
            if (jsonFactory.isCSV()) {
                CSVWriter writer = CSVWriter.of(fileOut, CSVWriter.Feature.AlwaysQuoteStrings);
                writer.writeRowObject(value);
                writer.close();
                return;
            }

            JSONWriter jsonWriter = jsonFactory.createJSONWriter();
            JSONWriter.Context context = jsonWriter.getContext();
            context.getObjectWriter(objectType, objectClass)
                    .write(jsonWriter, value, null, null, 0L);
            jsonWriter.flushTo(fileOut);
        }
    }

    public ObjectWriter withDefaultPrettyPrinter() {
        // TODO withDefaultPrettyPrinter
        return this;
    }

    public ObjectWriter withAttribute(Object key, Object value) {
        return this;
    }

    public byte[] writeValueAsBytes(Object value)
            throws JsonProcessingException {
        throw new JSONException("TODO");
    }

    public void writeValue(OutputStream out, Object value)
            throws IOException, DatabindException {
        throw new JSONException("TODO");
    }
}
