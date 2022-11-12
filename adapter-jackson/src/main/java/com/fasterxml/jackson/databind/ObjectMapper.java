package com.fasterxml.jackson.databind;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.adapter.jackson.TreeNodeUtils;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.fastjson2.JSONReader.Feature.IgnoreCheckClose;

public class ObjectMapper extends ObjectCodec {
    private JsonFactory factory;
    private final ObjectReaderProvider readerProvider;
    private final ObjectWriterProvider writerProvider;

    protected SerializationConfig serializationConfig;
    protected DeserializationConfig deserializationConfig;

    public ObjectMapper() {
        readerProvider = new ObjectReaderProvider();
        writerProvider = new ObjectWriterProvider();
        serializationConfig = new SerializationConfig();
        deserializationConfig = new DeserializationConfig();
    }

    public ObjectMapper(JsonFactory factory) {
        this();
        this.factory = factory;
    }

    public String writeValueAsString(Object object) {
        JSONWriter.Context context = JSONFactory.createWriteContext(writerProvider);

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
                    ObjectWriter<?> objectWriter = writerProvider.getObjectWriter(valueClass, valueClass, fieldBased);
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
            ObjectReader<T> objectReader = readerProvider.getObjectReader(valueType, fieldBased);

            T object = objectReader.readObject(reader, null, null, 0);
            reader.handleResolveTasks(object);
            if ((!reader.isEnd()) && (features & IgnoreCheckClose.mask) == 0) {
                throw new JSONException(reader.info("input not end"));
            }
            return object;
        }
    }

    private JSONReader.Context createReaderContext() {
        JSONReader.Context context = JSONFactory.createReadContext(readerProvider);
        deserializationConfig.configTo(context);
        return context;
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
            boolean fieldBased = (features & JSONReader.Feature.FieldBased.mask) != 0;
            ObjectReader<T> objectReader = readerProvider.getObjectReader(valueType, fieldBased);

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

    public <T> T readValue(InputStream input, JavaType valueType) {
        // TODO
        throw new JSONException("TODO");
    }

    public <T> T readValue(Reader input, JavaType valueType) {
        // TODO
        throw new JSONException("TODO");
    }

    @Override
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

    @Deprecated
    public ObjectMapper configure(MapperFeature f, boolean state) {
        serializationConfig = state ?
                serializationConfig.with(f) : serializationConfig.without(f);
        deserializationConfig = state ?
                deserializationConfig.with(f) : deserializationConfig.without(f);
        return this;
    }

    public boolean canDeserialize(JavaType type, AtomicReference<Throwable> cause) {
        // TODO
        throw new JSONException("TODO");
    }

    public com.fasterxml.jackson.databind.ObjectReader readerWithView(Class<?> view) {
        // TODO
        throw new JSONException("TODO");
    }

    public JsonFactory getFactory() {
        // TODO
        throw new JSONException("TODO");
    }

    public com.fasterxml.jackson.databind.ObjectWriter writerWithView(Class<?> serializationView) {
        // TODO
        throw new JSONException("TODO");
    }

    public com.fasterxml.jackson.databind.ObjectWriter writer() {
        // TODO
        throw new JSONException("TODO");
    }

    public JavaType constructType(Type t) {
        // TODO
        throw new JSONException("TODO");
    }

    public JavaType constructType(com.fasterxml.jackson.core.type.TypeReference<?> typeRef) {
        // TODO
        throw new JSONException("TODO");
    }

    public boolean canSerialize(Class<?> type, AtomicReference<Throwable> cause) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectMapper registerModules(Iterable<? extends Module> modules) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectMapper registerModules(Module... modules) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectMapper registerModule(Module module) {
        ObjectWriterModule writerModule = module.getWriterModule();
        if (writerModule != null) {
            writerProvider.register(writerModule);
        }
        ObjectReaderModule readerModule = module.getReaderModule();
        if (readerModule != null) {
            readerProvider.register(readerModule);
        }
        return this;
    }

    public static List<Module> findModules(ClassLoader classLoader) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectMapper setDateFormat(DateFormat dateFormat) {
        deserializationConfig = deserializationConfig.with(dateFormat);
        serializationConfig = serializationConfig.with(dateFormat);
        return this;
    }

    public ObjectMapper setLocale(Locale locale) {
        deserializationConfig = deserializationConfig.with(locale);
        serializationConfig = serializationConfig.with(locale);
        return this;
    }

    public ObjectMapper setTimeZone(TimeZone tz) {
        deserializationConfig = deserializationConfig.with(tz);
        serializationConfig = serializationConfig.with(tz);
        return this;
    }

    public ObjectMapper setAnnotationIntrospector(AnnotationIntrospector ai) {
        deserializationConfig = deserializationConfig.with(ai);
        serializationConfig = serializationConfig.with(ai);
        return this;
    }

    public ObjectMapper setPropertyNamingStrategy(PropertyNamingStrategy s) {
        deserializationConfig = deserializationConfig.with(s);
        serializationConfig = serializationConfig.with(s);
        return this;
    }

    public ObjectMapper setDefaultTyping(TypeResolverBuilder<?> typer) {
        deserializationConfig = deserializationConfig.with(typer);
        serializationConfig = serializationConfig.with(typer);
        return this;
    }

    public Object setHandlerInstantiator(HandlerInstantiator hi) {
        deserializationConfig = deserializationConfig.with(hi);
        serializationConfig = serializationConfig.with(hi);
        return this;
    }

    public ObjectMapper setDefaultPropertyInclusion(JsonInclude.Include incl) {
        return this;
    }

    public ObjectMapper setDefaultPropertyInclusion(JsonInclude.Value incl) {
        return this;
    }

    public ObjectMapper setDefaultSetterInfo(JsonSetter.Value v) {
        return this;
    }

    public ObjectMapper setFilterProvider(FilterProvider filterProvider) {
        serializationConfig = serializationConfig.withFilters(filterProvider);
        return this;
    }

    public ObjectMapper addMixIn(Class<?> target, Class<?> mixinSource) {
        readerProvider.mixIn(target, mixinSource);
        readerProvider.mixIn(target, mixinSource);
        return this;
    }

    public ObjectMapper setVisibility(PropertyAccessor forMethod, JsonAutoDetect.Visibility visibility) {
        // TODO
        throw new JSONException("TODO");
    }

    @Override
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        // TODO
        throw new JSONException("TODO");
    }

    public JsonNode readTree(String content) {
        JSONReader jsonReader = JSONReader.of(content);
        Object any = jsonReader.readAny();
        return TreeNodeUtils.as(any);
    }
}
