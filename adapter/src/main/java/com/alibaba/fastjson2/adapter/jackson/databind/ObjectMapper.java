package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.adapter.jackson.core.*;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ObjectNode;
import com.alibaba.fastjson2.adapter.jackson.databind.node.TreeNodeUtils;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.JSONReader.Feature.IgnoreCheckClose;

public class ObjectMapper
        extends ObjectCodec {
    private JsonFactory factory;

    protected SerializationConfig serializationConfig;
    protected DeserializationConfig deserializationConfig;

    public ObjectMapper() {
        serializationConfig = new SerializationConfig();
        deserializationConfig = new DeserializationConfig();
    }

    public String writeValueAsString(Object object) {
        JSONWriter.Context context = JSONFactory.createWriteContext(serializationConfig.writerProvider);

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                if (valueClass == JSONObject.class) {
                    writer.write((JSONObject) object);
                } else {
                    boolean fieldBased = (context.getFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
                    ObjectWriter<?> objectWriter = serializationConfig.writerProvider.getObjectWriter(valueClass, valueClass, fieldBased);
                    objectWriter.write(writer, object, null, null, 0);
                }
            }
            return writer.toString();
        }
    }

    public <T> T readValue(String content, Class<T> valueType) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReaderContext();
        long features = context.getFeatures();

        try (JSONReader reader = JSONReader.of(content, context)) {
            boolean fieldBased = (features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = deserializationConfig.readerProvider.getObjectReader(valueType, fieldBased);

            T object = objectReader.readObject(reader, null, null, 0);
            reader.handleResolveTasks(object);
            if ((!reader.isEnd()) && (features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    private JSONReader.Context createReaderContext() {
        return deserializationConfig.createReaderContext();
    }

    public <T> T readValue(String content, TypeReference<T> valueTypeRef) {
        if (content == null) {
            throw new JSONException("content is null");
        }

        return readValue(content, valueTypeRef.getType());
    }

    public <T> T readValue(String content, Type valueType) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReaderContext();
        long features = context.getFeatures();

        try (JSONReader reader = JSONReader.of(content)) {
            ObjectReader<T> objectReader = context.getObjectReader(valueType);

            T object = objectReader.readObject(reader, null, null, 0);
            reader.handleResolveTasks(object);
            if ((!reader.isEnd()) && (features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    public <T> T readValue(URL url, Class<T> valueType) {
        if (url == null) {
            return null;
        }

        try (InputStream is = url.openStream()) {
            return readValue(is, valueType);
        } catch (IOException e) {
            throw new JSONException("can not readValue '" + url + "' to '" + valueType + "'", e);
        }
    }

    public <T> T readValue(File file, Class<T> valueType) {
        if (file == null) {
            return null;
        }

        try (InputStream is = new FileInputStream(file)) {
            return readValue(is, valueType);
        } catch (IOException e) {
            throw new JSONException("can not readValue '" + file + "' to '" + valueType + "'", e);
        }
    }

    public <T> T readValue(InputStream input, Class<T> valueType) {
        if (input == null) {
            return null;
        }

        JSONReader.Context context = createReaderContext();
        long features = context.getFeatures();

        try (JSONReader reader = JSONReader.of(input, StandardCharsets.UTF_8, context)) {
            if (reader.isEnd()) {
                return null;
            }

            ObjectReader<T> objectReader = reader.getObjectReader(valueType);

            T object = objectReader.readObject(reader, null, null, 0);
            reader.handleResolveTasks(object);
            if ((!reader.isEnd()) && (features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    public ObjectNode createObjectNode() {
        return new ObjectNode();
    }

    public ObjectMapper configure(SerializationFeature f, boolean state) {
        serializationConfig = state
                ? serializationConfig.with(f)
                : serializationConfig.without(f);
        return this;
    }

    public ObjectMapper configure(DeserializationFeature f, boolean state) {
        deserializationConfig = state
                ? deserializationConfig.with(f)
                : deserializationConfig.without(f);
        return this;
    }

    public ObjectMapper configure(JsonParser.Feature f, boolean state) {
        factory.configure(f, state);
        return this;
    }

    public ObjectMapper configure(JsonGenerator.Feature f, boolean state) {
        factory.configure(f, state);
        return this;
    }

    public ObjectMapper registerModule(Module module) {
        ObjectWriterModule writerModule = module.getWriterModule();
        if (writerModule != null) {
            serializationConfig.writerProvider.register(writerModule);
        }
        ObjectReaderModule readerModule = module.getReaderModule();
        if (readerModule != null) {
            deserializationConfig.readerProvider.register(readerModule);
        }
        return this;
    }

    public ObjectMapper addMixIn(Class<?> target, Class<?> mixinSource) {
        deserializationConfig.readerProvider.mixIn(target, mixinSource);
        deserializationConfig.readerProvider.mixIn(target, mixinSource);
        return this;
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        Object any = p.getRaw().readAny();
        return (T) TreeNodeUtils.as(any);
    }

    public JsonNode readTree(String content) {
        JSONReader jsonReader = JSONReader.of(content);
        Object any = jsonReader.readAny();
        return TreeNodeUtils.as(any);
    }
}
