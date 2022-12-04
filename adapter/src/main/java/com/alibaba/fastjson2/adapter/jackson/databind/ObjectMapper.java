package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.adapter.jackson.core.*;
import com.alibaba.fastjson2.adapter.jackson.databind.exc.StreamReadException;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ArrayNode;
import com.alibaba.fastjson2.adapter.jackson.databind.node.JsonNodeFactory;
import com.alibaba.fastjson2.adapter.jackson.databind.node.ObjectNode;
import com.alibaba.fastjson2.adapter.jackson.databind.node.TreeNodeUtils;
import com.alibaba.fastjson2.adapter.jackson.databind.type.TypeFactory;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.JSONReader.Feature.IgnoreCheckClose;

public class ObjectMapper
        extends ObjectCodec {
    protected final JsonFactory factory;
    protected TypeFactory typeFactory;
    protected SerializationConfig serializationConfig;
    protected DeserializationConfig deserializationConfig;

    public ObjectMapper() {
        this(new JsonFactory());
    }

    public ObjectMapper(JsonFactory factory) {
        this.factory = factory;
        this.typeFactory = new TypeFactory();

        serializationConfig = new SerializationConfig();
        deserializationConfig = new DeserializationConfig();
    }

    public String writeValueAsString(Object value) throws JsonProcessingException {
        JSONWriter.Context context = createWriterContext();

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (value == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(value);

                Class<?> valueClass = value.getClass();
                if (valueClass == JSONObject.class) {
                    writer.write((JSONObject) value);
                } else {
                    boolean fieldBased = (context.getFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
                    com.alibaba.fastjson2.writer.ObjectWriter<?> objectWriter = serializationConfig.writerProvider.getObjectWriter(valueClass, valueClass, fieldBased);
                    objectWriter.write(writer, value, null, null, 0);
                }
            }
            return writer.toString();
        }
    }

    private JSONWriter.Context createWriterContext() {
        return serializationConfig.createWriterContext();
    }

    public byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
        JSONWriter.Context context = createWriterContext();

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (value == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(value);

                Class<?> valueClass = value.getClass();
                if (valueClass == JSONObject.class) {
                    writer.write((JSONObject) value);
                } else {
                    boolean fieldBased = (context.getFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
                    com.alibaba.fastjson2.writer.ObjectWriter<?> objectWriter = serializationConfig.writerProvider.getObjectWriter(valueClass, valueClass, fieldBased);
                    objectWriter.write(writer, value, null, null, 0);
                }
            }
            return writer.getBytes();
        }
    }

    public void writeValue(OutputStream out, Object value) throws IOException {
        JSONWriter.Context context = createWriterContext();

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (value == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(value);

                Class<?> valueClass = value.getClass();
                if (valueClass == JSONObject.class) {
                    writer.write((JSONObject) value);
                } else {
                    boolean fieldBased = (context.getFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
                    com.alibaba.fastjson2.writer.ObjectWriter<?> objectWriter = serializationConfig.writerProvider.getObjectWriter(valueClass, valueClass, fieldBased);
                    objectWriter.write(writer, value, null, null, 0);
                }
            }
            writer.flushTo(out);
        }
    }

    public void writeValue(File resultFile, Object value) throws IOException {
        JSONWriter.Context context = createWriterContext();

        try (JSONWriter writer = JSONWriter.of(context); FileOutputStream out = new FileOutputStream(resultFile)) {
            if (value == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(value);

                Class<?> valueClass = value.getClass();
                if (valueClass == JSONObject.class) {
                    writer.write((JSONObject) value);
                } else {
                    boolean fieldBased = (context.getFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
                    com.alibaba.fastjson2.writer.ObjectWriter<?> objectWriter = serializationConfig.writerProvider.getObjectWriter(valueClass, valueClass, fieldBased);
                    objectWriter.write(writer, value, null, null, 0);
                }
            }
            writer.flushTo(out);
        }
    }

    public void writeValue(Writer w, Object value)
            throws IOException, DatabindException {
        JSONWriter.Context context = createWriterContext();

        try (JSONWriter writer = JSONWriter.of(context)) {
            if (value == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(value);

                Class<?> valueClass = value.getClass();
                if (valueClass == JSONObject.class) {
                    writer.write((JSONObject) value);
                } else {
                    boolean fieldBased = (context.getFeatures() & JSONWriter.Feature.FieldBased.mask) != 0;
                    com.alibaba.fastjson2.writer.ObjectWriter<?> objectWriter = serializationConfig.writerProvider.getObjectWriter(valueClass, valueClass, fieldBased);
                    objectWriter.write(writer, value, null, null, 0);
                }
            }
            writer.flushTo(w);
        }
    }

    public <T> T readValue(String content, Class<T> valueType)
            throws JsonMappingException, JsonParseException {
        if (content == null || content.isEmpty()) {
            throw new JsonParseException("input is null or empty");
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
        } catch (JSONException e) {
            throw new JsonMappingException(e.getMessage(), e.getCause());
        }
    }

    public <T> T readValue(byte[] content, Class<T> valueType)
            throws JsonMappingException, JsonParseException {
        if (content == null || content.length == 0) {
            throw new JsonParseException("input is null or empty");
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
        } catch (JSONException e) {
            throw new JsonMappingException(e.getMessage(), e.getCause());
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

        if (valueType instanceof JavaType) {
            valueType = ((JavaType) valueType).getType();
        }

        JSONReader.Context context = createReaderContext();
        long features = context.getFeatures();

        try (JSONReader reader = JSONReader.of(content, context)) {
            ObjectReader<T> objectReader = context.getObjectReader(valueType);

            T object = objectReader.readObject(reader, valueType, null, 0);
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

    public <T> T readValue(InputStream input, Class<T> valueType)
            throws JsonMappingException, JsonParseException {
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

    public <T> T readValue(JsonParser parser, JavaType javaType) throws IOException {
        Type type = javaType.getType();
        return parser.getJSONReader().read(type);
    }

    public ObjectNode createObjectNode() {
        return new ObjectNode();
    }

    public ArrayNode createArrayNode() {
        return new ArrayNode();
    }

    public ObjectMapper configure(SerializationFeature f, boolean state) {
        serializationConfig = state
                ? serializationConfig.with(f)
                : serializationConfig.without(f);
        return this;
    }

    @Deprecated
    public ObjectMapper configure(MapperFeature f, boolean state) {
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
        Object any = p.getJSONReader().readAny();
        return (T) TreeNodeUtils.as(any);
    }

    public JsonNode readTree(String content) throws JsonProcessingException, JsonMappingException {
        JSONReader jsonReader = JSONReader.of(content);
        Object any = jsonReader.readAny();
        return TreeNodeUtils.as(any);
    }

    public JsonNode readTree(InputStream in) throws IOException {
        JSONReader jsonReader = JSONReader.of(in, StandardCharsets.UTF_8);
        Object any = jsonReader.readAny();
        return TreeNodeUtils.as(any);
    }

    public JsonNode readTree(Reader r) throws IOException {
        JSONReader jsonReader = JSONReader.of(r);
        Object any = jsonReader.readAny();
        return TreeNodeUtils.as(any);
    }

    public ObjectMapper disable(SerializationFeature f) {
        this.serializationConfig = this.serializationConfig.without(f);
        return this;
    }

    public ObjectMapper disable(DeserializationFeature f) {
        this.deserializationConfig = this.deserializationConfig.without(f);
        return this;
    }

    public ObjectMapper disable(MapperFeature f) {
        return this;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public ObjectMapper setTypeFactory(TypeFactory f) {
        this.typeFactory = f;
        return this;
    }

    public <T extends JsonNode> T valueToTree(Object fromValue)
            throws IllegalArgumentException {
        return (T) TreeNodeUtils.as(fromValue);
    }

    public <T> T treeToValue(TreeNode n, Class<T> valueType)
            throws IllegalArgumentException,
            JsonProcessingException {
        if (n instanceof ObjectNode) {
            return ((ObjectNode) n).getJSONObject().to(valueType);
        }

        if (n instanceof ArrayNode) {
            return ((ArrayNode) n).getJSONArray().to(valueType);
        }

        // TODO treeToValue
        throw new JSONException("TODO");
    }

    public JsonNode readTree(byte[] content) throws IOException {
        Object value = JSON.parse(content);
        return TreeNodeUtils.as(value);
    }

    public ObjectWriter writer() {
        // TODO writer
        throw new JSONException("TODO");
    }

    public ObjectWriter writer(PrettyPrinter pp) {
        // TODO writer
        throw new JSONException("TODO");
    }

    public ObjectWriter writer(FormatSchema schema) {
        throw new JSONException("TODO");
    }

    public ObjectMapper enable(DeserializationFeature feature) {
        deserializationConfig.with(feature);
        return this;
    }

    public ObjectMapper enable(MapperFeature feature) {
        return this;
    }

    public ObjectMapper enable(SerializationFeature feature) {
        serializationConfig.with(feature);
        return this;
    }

    public ObjectMapper enable(DeserializationFeature first,
                               DeserializationFeature... features) {
        deserializationConfig.with(first);
        for (DeserializationFeature feature : features) {
            deserializationConfig.with(feature);
        }
        return this;
    }

    public JsonParser treeAsTokens(TreeNode n) {
        String str = n.toString();
        JSONReader jsonReader = JSONReader.of(str);
        return new JsonParser(jsonReader);
    }

    public JavaType constructType(Type t) {
        return typeFactory.constructType(t);
    }

    public JsonNodeFactory getNodeFactory() {
        return deserializationConfig.getNodeFactory();
    }

    public JsonFactory getFactory() {
        return factory;
    }

    public JsonParser createParser(String content) throws IOException {
        JSONReader jsonReader = JSONReader.of(content, createReaderContext());
        return new JsonParser(jsonReader);
    }

    public JsonParser createParser(URL src) throws IOException {
        JSONReader jsonReader = JSONReader.of(src, createReaderContext());
        return new JsonParser(jsonReader);
    }

    public JsonParser createParser(byte[] jsonBytes) throws IOException {
        JSONReader jsonReader = JSONReader.of(jsonBytes, createReaderContext());
        return new JsonParser(jsonReader);
    }

    public JsonParser createParser(InputStream in) throws IOException {
        JSONReader.Context context = createReaderContext();
        JSONReader jsonReader = JSONReader.of(in, StandardCharsets.UTF_8, context);
        return new JsonParser(jsonReader);
    }

    public com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader reader() {
        return new com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader(
                this,
                deserializationConfig, null
        );
    }

    public com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader reader(Class<?> type) {
        return new com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader(
                this,
                deserializationConfig, type
        );
    }

    public com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader readerFor(Class type) {
        return new com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader(
                this,
                deserializationConfig, type
        );
    }

    public com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader readerFor(JavaType type) {
        return new com.alibaba.fastjson2.adapter.jackson.databind.ObjectReader(
                this,
                deserializationConfig, type.getType()
        );
    }

    public ObjectWriter writerWithDefaultPrettyPrinter() {
        return new ObjectWriter(this, serializationConfig, null);
    }

    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        throw new JSONException("TODO");
    }

    public void writeTree(JsonGenerator g, JsonNode rootNode)
            throws IOException {
        throw new JSONException("TODO");
    }

    public <T> T readValue(JsonParser p, Class<T> valueType)
            throws IOException, StreamReadException, DatabindException {
        return p.getJSONReader().read(valueType);
    }
}
