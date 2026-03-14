package com.alibaba.fastjson3;

import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.PropertyFilter;
import com.alibaba.fastjson3.filter.ValueFilter;
import com.alibaba.fastjson3.modules.ObjectReaderModule;
import com.alibaba.fastjson3.modules.ObjectWriterModule;
import com.alibaba.fastjson3.reader.ObjectReaderCreator;
import com.alibaba.fastjson3.writer.ObjectWriterCreator;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Immutable, thread-safe JSON object mapper. Central entry point for JSON-to-Object
 * and Object-to-JSON conversion with full type support.
 *
 * <p>Replaces fastjson2's ObjectReaderProvider + ObjectWriterProvider with a single unified API,
 * inspired by Jackson 3's immutable ObjectMapper design.</p>
 *
 * <h3>Basic usage:</h3>
 * <pre>
 * // Use shared default instance
 * ObjectMapper mapper = ObjectMapper.shared();
 * User user = mapper.readValue("{\"name\":\"test\"}", User.class);
 * String json = mapper.writeValueAsString(user);
 * </pre>
 *
 * <h3>Custom configuration (immutable builder):</h3>
 * <pre>
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .enableRead(ReadFeature.AllowComments, ReadFeature.SupportSmartMatch)
 *     .enableWrite(WriteFeature.PrettyFormat, WriteFeature.WriteNulls)
 *     .addReaderModule(myModule)
 *     .build();
 *
 * // Derive variant from existing mapper
 * ObjectMapper pretty = mapper.rebuild()
 *     .enableWrite(WriteFeature.PrettyFormat)
 *     .build();
 * </pre>
 *
 * <h3>Per-call configuration (mutant factory):</h3>
 * <pre>
 * // Create a per-call reader with extra features
 * User user = mapper.readerFor(User.class)
 *     .with(ReadFeature.SupportSmartMatch)
 *     .readValue(json);
 *
 * // Create a per-call writer with extra features
 * String json = mapper.writerFor(User.class)
 *     .with(WriteFeature.PrettyFormat)
 *     .writeValueAsString(user);
 * </pre>
 */
public final class ObjectMapper {
    private static final PropertyFilter[] NO_PROPERTY_FILTERS = {};
    private static final ValueFilter[] NO_VALUE_FILTERS = {};
    private static final NameFilter[] NO_NAME_FILTERS = {};

    private static final ObjectMapper SHARED = new ObjectMapper(0, 0,
            Collections.<ObjectReaderModule>emptyList(),
            Collections.<ObjectWriterModule>emptyList(),
            null, null,
            NO_PROPERTY_FILTERS, NO_VALUE_FILTERS, NO_NAME_FILTERS,
            Collections.<Class<?>, Class<?>>emptyMap());

    private final long readFeatures;
    private final long writeFeatures;
    private final List<ObjectReaderModule> readerModules;
    private final List<ObjectWriterModule> writerModules;

    // Creator functions: Class -> ObjectReader/ObjectWriter
    // null means use default auto-detection (ASM > Reflection)
    private final Function<Class<?>, ObjectReader<?>> readerCreator;
    private final Function<Class<?>, ObjectWriter<?>> writerCreator;

    // Filters (empty arrays = no overhead)
    private final PropertyFilter[] propertyFilters;
    private final ValueFilter[] valueFilters;
    private final NameFilter[] nameFilters;

    // Mixin mappings: target class → mixin source class
    private final Map<Class<?>, Class<?>> mixInCache;

    // Thread-safe caches for ObjectReader/ObjectWriter instances
    private final ConcurrentHashMap<Type, ObjectReader<?>> readerCache;
    private final ConcurrentHashMap<Type, ObjectWriter<?>> writerCache;

    private ObjectMapper(
            long readFeatures,
            long writeFeatures,
            List<ObjectReaderModule> readerModules,
            List<ObjectWriterModule> writerModules,
            Function<Class<?>, ObjectReader<?>> readerCreator,
            Function<Class<?>, ObjectWriter<?>> writerCreator,
            PropertyFilter[] propertyFilters,
            ValueFilter[] valueFilters,
            NameFilter[] nameFilters,
            Map<Class<?>, Class<?>> mixInCache
    ) {
        this.readFeatures = readFeatures;
        this.writeFeatures = writeFeatures;
        this.readerModules = readerModules;
        this.writerModules = writerModules;
        this.readerCreator = readerCreator;
        this.writerCreator = writerCreator;
        this.propertyFilters = propertyFilters;
        this.valueFilters = valueFilters;
        this.nameFilters = nameFilters;
        this.mixInCache = mixInCache;
        this.readerCache = new ConcurrentHashMap<Type, ObjectReader<?>>();
        this.writerCache = new ConcurrentHashMap<Type, ObjectWriter<?>>();
    }

    // ==================== Factory methods ====================

    /**
     * Get the shared default mapper (no features, no custom modules).
     * Thread-safe singleton — suitable for most use cases.
     */
    public static ObjectMapper shared() {
        return SHARED;
    }

    /**
     * Create a new builder for configuring a custom mapper.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder pre-configured with this mapper's settings.
     * Use this to derive variants from an existing mapper.
     */
    public Builder rebuild() {
        Builder b = new Builder();
        b.readFeatures = this.readFeatures;
        b.writeFeatures = this.writeFeatures;
        b.readerModules.addAll(this.readerModules);
        b.writerModules.addAll(this.writerModules);
        b.readerCreator = this.readerCreator;
        b.writerCreator = this.writerCreator;
        Collections.addAll(b.propertyFilters, this.propertyFilters);
        Collections.addAll(b.valueFilters, this.valueFilters);
        Collections.addAll(b.nameFilters, this.nameFilters);
        b.mixIns.putAll(this.mixInCache);
        return b;
    }

    // ==================== Read: String input ====================

    /**
     * Parse JSON string to auto-detected type (JSONObject, JSONArray, String, Number, Boolean, null).
     */
    public Object readValue(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            return parser.readAny();
        }
    }

    /**
     * Parse JSON string to typed Java object.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            try (JSONParser parser = JSONParser.of(json)) {
                return objectReader.readObject(parser, type, null, readFeatures);
            }
        }
        try (JSONParser parser = JSONParser.of(json)) {
            return parser.read(type);
        }
    }

    /**
     * Parse JSON string to generic type.
     *
     * <pre>
     * List&lt;User&gt; users = mapper.readValue(json, new TypeReference&lt;List&lt;User&gt;&gt;(){}.getType());
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(String json, Type type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            try (JSONParser parser = JSONParser.of(json)) {
                return objectReader.readObject(parser, type, null, readFeatures);
            }
        }
        try (JSONParser parser = JSONParser.of(json)) {
            return parser.read(type);
        }
    }

    /**
     * Parse JSON string to generic type using TypeReference.
     *
     * <pre>
     * List&lt;User&gt; users = mapper.readValue(json, new TypeReference&lt;List&lt;User&gt;&gt;(){});
     * </pre>
     */
    public <T> T readValue(String json, TypeReference<T> typeRef) {
        return readValue(json, typeRef.getType());
    }

    // ==================== Read: byte[] input ====================

    /**
     * Parse UTF-8 JSON bytes to typed Java object.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] jsonBytes, Class<T> type) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            try (JSONParser parser = JSONParser.of(jsonBytes)) {
                return objectReader.readObject(parser, type, null, readFeatures);
            }
        }
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            return parser.read(type);
        }
    }

    /**
     * Parse UTF-8 JSON bytes to generic type.
     */
    @SuppressWarnings("unchecked")
    public <T> T readValue(byte[] jsonBytes, Type type) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
        if (objectReader != null) {
            try (JSONParser parser = JSONParser.of(jsonBytes)) {
                return objectReader.readObject(parser, type, null, readFeatures);
            }
        }
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            return parser.read(type);
        }
    }

    /**
     * Parse UTF-8 JSON bytes to generic type using TypeReference.
     */
    public <T> T readValue(byte[] jsonBytes, TypeReference<T> typeRef) {
        return readValue(jsonBytes, typeRef.getType());
    }

    // ==================== Read: JSONObject / JSONArray ====================

    /**
     * Parse JSON string to JSONObject.
     */
    public JSONObject readObject(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            return parser.readObject();
        }
    }

    /**
     * Parse UTF-8 JSON bytes to JSONObject.
     */
    public JSONObject readObject(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            return parser.readObject();
        }
    }

    /**
     * Parse JSON string to JSONArray.
     */
    public JSONArray readArray(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            return parser.readArray();
        }
    }

    /**
     * Parse UTF-8 JSON bytes to JSONArray.
     */
    public JSONArray readArray(byte[] jsonBytes) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(jsonBytes)) {
            return parser.readArray();
        }
    }

    // ==================== Read: typed list ====================

    /**
     * Parse JSON string to typed list.
     *
     * <pre>
     * List&lt;User&gt; users = mapper.readList(json, User.class);
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> readList(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try (JSONParser parser = JSONParser.of(json)) {
            JSONArray array = parser.readArray();
            if (array == null) {
                return null;
            }
            ObjectReader<T> objectReader = (ObjectReader<T>) getObjectReader(type);
            List<T> list = new ArrayList<>(array.size());
            for (int i = 0, size = array.size(); i < size; i++) {
                Object item = array.get(i);
                if (item == null) {
                    list.add(null);
                } else if (type.isInstance(item)) {
                    list.add(type.cast(item));
                } else if (item instanceof JSONObject jsonObj && objectReader != null) {
                    // Convert JSONObject to target type via ObjectReader
                    String itemJson = JSON.toJSONString(jsonObj);
                    try (JSONParser itemParser = JSONParser.of(itemJson)) {
                        list.add(objectReader.readObject(itemParser, type, null, readFeatures));
                    }
                } else {
                    list.add((T) item);
                }
            }
            return list;
        }
    }

    // ==================== Write: String output ====================

    /**
     * Serialize object to JSON string.
     */
    public String writeValueAsString(Object obj) {
        if (obj == null) {
            return "null";
        }
        try (JSONGenerator generator = JSONGenerator.of()) {
            applyFilters(generator);
            writeValue0(generator, obj);
            return generator.toString();
        }
    }

    // ==================== Write: byte[] output ====================

    /**
     * Serialize object to UTF-8 JSON byte array.
     */
    public byte[] writeValueAsBytes(Object obj) {
        if (obj == null) {
            return "null".getBytes(StandardCharsets.UTF_8);
        }
        try (JSONGenerator generator = JSONGenerator.ofUTF8()) {
            applyFilters(generator);
            writeValue0(generator, obj);
            return generator.toByteArray();
        }
    }

    // ==================== Write: OutputStream ====================

    /**
     * Serialize object to OutputStream as UTF-8 JSON.
     */
    public void writeValue(OutputStream out, Object obj) {
        byte[] bytes = writeValueAsBytes(obj);
        try {
            out.write(bytes);
        } catch (java.io.IOException e) {
            throw new JSONException("write to OutputStream error", e);
        }
    }

    // ==================== Per-call ObjectReader / ObjectWriter ====================

    /**
     * Create a per-call reader builder for the given type.
     * The returned reader is a lightweight, immutable object.
     *
     * <pre>
     * User user = mapper.readerFor(User.class)
     *     .with(ReadFeature.SupportSmartMatch)
     *     .readValue(json);
     * </pre>
     */
    public <T> ValueReader<T> readerFor(Class<T> type) {
        return new ValueReader<T>(this, type, readFeatures);
    }

    /**
     * Create a per-call writer builder.
     * The returned writer is a lightweight, immutable object.
     *
     * <pre>
     * String json = mapper.writer()
     *     .with(WriteFeature.PrettyFormat)
     *     .writeValueAsString(user);
     * </pre>
     */
    public ValueWriter writer() {
        return new ValueWriter(this, writeFeatures);
    }

    /**
     * Create a per-call writer builder for a specific type.
     */
    public ValueWriter writerFor(Class<?> type) {
        return new ValueWriter(this, writeFeatures);
    }

    // ==================== ObjectReader/ObjectWriter registry ====================

    /**
     * Look up or create an ObjectReader for the given type.
     * Results are cached for reuse.
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectReader<T> getObjectReader(Type type) {
        ObjectReader<?> reader = readerCache.get(type);
        if (reader != null) {
            return (ObjectReader<T>) reader;
        }

        // Try modules
        for (ObjectReaderModule module : readerModules) {
            reader = module.getObjectReader(type);
            if (reader != null) {
                readerCache.putIfAbsent(type, reader);
                return (ObjectReader<T>) reader;
            }
        }

        // Auto-create reader (only for POJO types)
        if (!(type instanceof Class<?> clazz) || isBasicType(clazz)) {
            return null;
        }
        try {
            if (readerCreator != null) {
                reader = readerCreator.apply(clazz);
            } else {
                Class<?> mixIn = mixInCache.get(clazz);
                reader = ObjectReaderCreator.createObjectReader(clazz, mixIn);
            }
        } catch (Exception e) {
            return null;
        }
        if (reader != null) {
            readerCache.putIfAbsent(type, reader);
            return (ObjectReader<T>) reader;
        }
        return null;
    }

    /**
     * Look up or create an ObjectWriter for the given type.
     * Results are cached for reuse.
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectWriter<T> getObjectWriter(Type type) {
        ObjectWriter<?> writer = writerCache.get(type);
        if (writer != null) {
            return (ObjectWriter<T>) writer;
        }

        Class<?> rawType;
        if (type instanceof Class) {
            rawType = (Class<?>) type;
        } else {
            rawType = null;
        }

        // Try modules
        for (ObjectWriterModule module : writerModules) {
            writer = module.getObjectWriter(type, rawType);
            if (writer != null) {
                writerCache.putIfAbsent(type, writer);
                return (ObjectWriter<T>) writer;
            }
        }

        // Auto-create writer (only for POJO types)
        if (rawType != null && !isBasicType(rawType)) {
            try {
                if (writerCreator != null) {
                    writer = writerCreator.apply(rawType);
                } else {
                    Class<?> mixIn = mixInCache.get(rawType);
                    writer = ObjectWriterCreator.createObjectWriter(rawType, mixIn);
                }
            } catch (Exception e) {
                return null;
            }
            if (writer != null) {
                writerCache.putIfAbsent(type, writer);
                return (ObjectWriter<T>) writer;
            }
        }
        return null;
    }

    /**
     * Register a custom ObjectReader for a specific type.
     * Useful for runtime customization on a per-mapper basis.
     */
    public <T> void registerReader(Type type, ObjectReader<T> reader) {
        readerCache.put(type, reader);
    }

    /**
     * Register a custom ObjectWriter for a specific type.
     */
    public <T> void registerWriter(Type type, ObjectWriter<T> writer) {
        writerCache.put(type, writer);
    }

    // ==================== Feature queries ====================

    public boolean isReadEnabled(ReadFeature feature) {
        return (readFeatures & feature.mask) != 0;
    }

    public boolean isWriteEnabled(WriteFeature feature) {
        return (writeFeatures & feature.mask) != 0;
    }

    public long getReadFeatures() {
        return readFeatures;
    }

    public long getWriteFeatures() {
        return writeFeatures;
    }

    /**
     * Get the mixin source class for the given target class, or null if none registered.
     */
    public Class<?> getMixIn(Class<?> target) {
        return mixInCache.get(target);
    }

    // ==================== Internal ====================

    private static boolean isBasicType(Class<?> type) {
        return type == String.class
                || type == Object.class
                || type.isPrimitive()
                || type == Integer.class || type == Long.class || type == Double.class
                || type == Float.class || type == Boolean.class || type == Short.class
                || type == Byte.class || type == Character.class
                || type == java.math.BigDecimal.class || type == java.math.BigInteger.class
                || type == JSONObject.class || type == JSONArray.class
                || type == java.util.concurrent.atomic.AtomicInteger.class
                || type == java.util.concurrent.atomic.AtomicLong.class
                || type == java.util.concurrent.atomic.AtomicBoolean.class
                || type == java.util.concurrent.atomic.AtomicIntegerArray.class
                || type == java.util.concurrent.atomic.AtomicLongArray.class
                || type == java.util.concurrent.atomic.AtomicReference.class
                || type.isArray() || type.isEnum()
                || java.util.Collection.class.isAssignableFrom(type)
                || java.util.Map.class.isAssignableFrom(type)
                || java.time.temporal.Temporal.class.isAssignableFrom(type)
                || type == java.util.Date.class;
    }

    public PropertyFilter[] getPropertyFilters() {
        return propertyFilters;
    }

    public ValueFilter[] getValueFilters() {
        return valueFilters;
    }

    public NameFilter[] getNameFilters() {
        return nameFilters;
    }

    private void applyFilters(JSONGenerator generator) {
        if (propertyFilters.length > 0 || valueFilters.length > 0 || nameFilters.length > 0) {
            generator.setFilters(propertyFilters, valueFilters, nameFilters);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeValue0(JSONGenerator jsonGenerator, Object obj) {
        ObjectWriter<Object> objectWriter = (ObjectWriter<Object>) getObjectWriter(obj.getClass());
        if (objectWriter != null) {
            objectWriter.write(jsonGenerator, obj, null, null, writeFeatures);
        } else {
            jsonGenerator.writeAny(obj);
        }
    }

    // ==================== Per-call ValueReader ====================

    /**
     * Immutable, per-call deserialization configurator. Lightweight — safe to create per request.
     * Uses "mutant factory" pattern: each {@code with()} returns a new instance.
     */
    public static final class ValueReader<T> {
        private final ObjectMapper mapper;
        private final Class<T> type;
        private final long features;

        ValueReader(ObjectMapper mapper, Class<T> type, long features) {
            this.mapper = mapper;
            this.type = type;
            this.features = features;
        }

        /**
         * Return a new ValueReader with the given features enabled.
         */
        public ValueReader<T> with(ReadFeature... readFeatures) {
            long f = this.features;
            for (ReadFeature rf : readFeatures) {
                f |= rf.mask;
            }
            return f == this.features ? this : new ValueReader<T>(mapper, type, f);
        }

        /**
         * Return a new ValueReader with the given features disabled.
         */
        public ValueReader<T> without(ReadFeature... readFeatures) {
            long f = this.features;
            for (ReadFeature rf : readFeatures) {
                f &= ~rf.mask;
            }
            return f == this.features ? this : new ValueReader<T>(mapper, type, f);
        }

        /**
         * Read from JSON string.
         */
        @SuppressWarnings("unchecked")
        public T readValue(String json) {
            if (json == null || json.isEmpty()) {
                return null;
            }
            ObjectReader<T> reader = (ObjectReader<T>) mapper.getObjectReader(type);
            if (reader != null) {
                try (JSONParser jr = JSONParser.of(json)) {
                    return reader.readObject(jr, type, null, features);
                }
            }
            try (JSONParser jr = JSONParser.of(json)) {
                return jr.read(type);
            }
        }

        /**
         * Read from UTF-8 JSON bytes.
         */
        @SuppressWarnings("unchecked")
        public T readValue(byte[] jsonBytes) {
            if (jsonBytes == null || jsonBytes.length == 0) {
                return null;
            }
            ObjectReader<T> reader = (ObjectReader<T>) mapper.getObjectReader(type);
            if (reader != null) {
                try (JSONParser jr = JSONParser.of(jsonBytes)) {
                    return reader.readObject(jr, type, null, features);
                }
            }
            try (JSONParser jr = JSONParser.of(jsonBytes)) {
                return jr.read(type);
            }
        }
    }

    // ==================== Per-call ValueWriter ====================

    /**
     * Immutable, per-call serialization configurator. Lightweight — safe to create per request.
     * Uses "mutant factory" pattern: each {@code with()} returns a new instance.
     */
    public static final class ValueWriter {
        private final ObjectMapper mapper;
        private final long features;

        ValueWriter(ObjectMapper mapper, long features) {
            this.mapper = mapper;
            this.features = features;
        }

        /**
         * Return a new ValueWriter with the given features enabled.
         */
        public ValueWriter with(WriteFeature... writeFeatures) {
            long f = this.features;
            for (WriteFeature wf : writeFeatures) {
                f |= wf.mask;
            }
            return f == this.features ? this : new ValueWriter(mapper, f);
        }

        /**
         * Return a new ValueWriter with the given features disabled.
         */
        public ValueWriter without(WriteFeature... writeFeatures) {
            long f = this.features;
            for (WriteFeature wf : writeFeatures) {
                f &= ~wf.mask;
            }
            return f == this.features ? this : new ValueWriter(mapper, f);
        }

        /**
         * Serialize to JSON string.
         */
        public String writeValueAsString(Object obj) {
            if (obj == null) {
                return "null";
            }
            try (JSONGenerator generator = JSONGenerator.of()) {
                writeValue0(generator, obj);
                return generator.toString();
            }
        }

        /**
         * Serialize to UTF-8 JSON byte array.
         */
        public byte[] writeValueAsBytes(Object obj) {
            if (obj == null) {
                return "null".getBytes(StandardCharsets.UTF_8);
            }
            try (JSONGenerator generator = JSONGenerator.ofUTF8()) {
                writeValue0(generator, obj);
                return generator.toByteArray();
            }
        }

        @SuppressWarnings("unchecked")
        private void writeValue0(JSONGenerator jsonGenerator, Object obj) {
            ObjectWriter<Object> objectWriter = (ObjectWriter<Object>) mapper.getObjectWriter(obj.getClass());
            if (objectWriter != null) {
                objectWriter.write(jsonGenerator, obj, null, null, features);
            } else {
                jsonGenerator.writeAny(obj);
            }
        }
    }

    // ==================== Builder ====================

    /**
     * Builder for creating immutable ObjectMapper instances.
     * All configuration happens here; the resulting mapper is thread-safe and immutable.
     */
    public static final class Builder {
        long readFeatures;
        long writeFeatures;
        final List<ObjectReaderModule> readerModules = new ArrayList<ObjectReaderModule>();
        final List<ObjectWriterModule> writerModules = new ArrayList<ObjectWriterModule>();
        Function<Class<?>, ObjectReader<?>> readerCreator;
        Function<Class<?>, ObjectWriter<?>> writerCreator;
        final List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        final List<ValueFilter> valueFilters = new ArrayList<ValueFilter>();
        final List<NameFilter> nameFilters = new ArrayList<NameFilter>();
        final Map<Class<?>, Class<?>> mixIns = new LinkedHashMap<Class<?>, Class<?>>();

        Builder() {
        }

        // ---- Features ----

        public Builder enableRead(ReadFeature... features) {
            for (ReadFeature f : features) {
                readFeatures |= f.mask;
            }
            return this;
        }

        public Builder disableRead(ReadFeature... features) {
            for (ReadFeature f : features) {
                readFeatures &= ~f.mask;
            }
            return this;
        }

        public Builder enableWrite(WriteFeature... features) {
            for (WriteFeature f : features) {
                writeFeatures |= f.mask;
            }
            return this;
        }

        public Builder disableWrite(WriteFeature... features) {
            for (WriteFeature f : features) {
                writeFeatures &= ~f.mask;
            }
            return this;
        }

        // ---- Modules ----

        /**
         * Add a reader module for custom deserialization.
         */
        public Builder addReaderModule(ObjectReaderModule module) {
            readerModules.add(module);
            return this;
        }

        /**
         * Add a writer module for custom serialization.
         */
        public Builder addWriterModule(ObjectWriterModule module) {
            writerModules.add(module);
            return this;
        }

        // ---- Creator strategy ----

        /**
         * Set a custom ObjectReader creator function.
         * Use this to plug in ASM or APT-based reader creation.
         */
        public Builder readerCreator(Function<Class<?>, ObjectReader<?>> creator) {
            this.readerCreator = creator;
            return this;
        }

        /**
         * Set a custom ObjectWriter creator function.
         * Use this to plug in ASM or APT-based writer creation.
         */
        public Builder writerCreator(Function<Class<?>, ObjectWriter<?>> creator) {
            this.writerCreator = creator;
            return this;
        }

        // ---- Filters ----

        /**
         * Add a property filter to control which properties are serialized.
         */
        public Builder addPropertyFilter(PropertyFilter filter) {
            propertyFilters.add(filter);
            return this;
        }

        /**
         * Add a value filter to transform property values during serialization.
         */
        public Builder addValueFilter(ValueFilter filter) {
            valueFilters.add(filter);
            return this;
        }

        /**
         * Add a name filter to transform property names during serialization.
         */
        public Builder addNameFilter(NameFilter filter) {
            nameFilters.add(filter);
            return this;
        }

        // ---- Mixins ----

        /**
         * Register a mixin class for a target type. Annotations from the mixin class
         * will be applied to the target class during serialization/deserialization.
         *
         * @param target the target class to augment
         * @param mixIn  the mixin class providing annotations
         */
        public Builder addMixIn(Class<?> target, Class<?> mixIn) {
            mixIns.put(target, mixIn);
            return this;
        }

        // ---- Custom readers/writers ----

        /**
         * Register a custom ObjectReader for a specific type.
         */
        public <T> Builder addReader(Class<T> type, ObjectReader<T> reader) {
            readerModules.add(new SingleTypeReaderModule(type, reader));
            return this;
        }

        /**
         * Register a custom ObjectWriter for a specific type.
         */
        public <T> Builder addWriter(Class<T> type, ObjectWriter<T> writer) {
            writerModules.add(new SingleTypeWriterModule(type, writer));
            return this;
        }

        // ---- Build ----

        /**
         * Build an immutable ObjectMapper from the current configuration.
         */
        public ObjectMapper build() {
            ObjectMapper mapper = new ObjectMapper(
                    readFeatures,
                    writeFeatures,
                    Collections.unmodifiableList(new ArrayList<ObjectReaderModule>(readerModules)),
                    Collections.unmodifiableList(new ArrayList<ObjectWriterModule>(writerModules)),
                    readerCreator,
                    writerCreator,
                    propertyFilters.toArray(NO_PROPERTY_FILTERS),
                    valueFilters.toArray(NO_VALUE_FILTERS),
                    nameFilters.toArray(NO_NAME_FILTERS),
                    Collections.unmodifiableMap(new LinkedHashMap<Class<?>, Class<?>>(mixIns))
            );
            // Initialize modules
            for (ObjectReaderModule module : mapper.readerModules) {
                module.init();
            }
            for (ObjectWriterModule module : mapper.writerModules) {
                module.init();
            }
            return mapper;
        }
    }

    // ==================== Internal module helpers ====================

    static final class SingleTypeReaderModule implements ObjectReaderModule {
        private final Type type;
        private final ObjectReader<?> reader;

        SingleTypeReaderModule(Type type, ObjectReader<?> reader) {
            this.type = type;
            this.reader = reader;
        }

        @Override
        public ObjectReader<?> getObjectReader(Type t) {
            return type.equals(t) ? reader : null;
        }
    }

    static final class SingleTypeWriterModule implements ObjectWriterModule {
        private final Type type;
        private final ObjectWriter<?> writer;

        SingleTypeWriterModule(Type type, ObjectWriter<?> writer) {
            this.type = type;
            this.writer = writer;
        }

        @Override
        public ObjectWriter<?> getObjectWriter(Type t, Class<?> rawType) {
            return type.equals(t) ? writer : null;
        }
    }
}
