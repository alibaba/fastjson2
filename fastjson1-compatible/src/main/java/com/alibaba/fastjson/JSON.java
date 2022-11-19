package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.*;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.filter.PropertyFilter;
import com.alibaba.fastjson2.filter.PropertyPreFilter;
import com.alibaba.fastjson2.filter.ValueFilter;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.support.AwtRederModule;
import com.alibaba.fastjson2.support.AwtWriterModule;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

public abstract class JSON {
    private static TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
    public static final String VERSION = com.alibaba.fastjson2.JSON.VERSION;
    static final Cache CACHE = new Cache();
    static final AtomicReferenceFieldUpdater<Cache, char[]> CHARS_UPDATER
            = AtomicReferenceFieldUpdater.newUpdater(Cache.class, char[].class, "chars");
    public static TimeZone defaultTimeZone = DEFAULT_TIME_ZONE;
    public static Locale defaultLocale = Locale.getDefault();
    public static String DEFAULT_TYPE_KEY = "@type";
    public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static int DEFAULT_PARSER_FEATURE;
    public static int DEFAULT_GENERATE_FEATURE;

    static {
        int features = 0;
        features |= Feature.AutoCloseSource.getMask();
        features |= Feature.InternFieldNames.getMask();
        features |= Feature.UseBigDecimal.getMask();
        features |= Feature.AllowUnQuotedFieldNames.getMask();
        features |= Feature.AllowSingleQuotes.getMask();
        features |= Feature.AllowArbitraryCommas.getMask();
        features |= Feature.SortFeidFastMatch.getMask();
        features |= Feature.IgnoreNotMatch.getMask();
        DEFAULT_PARSER_FEATURE = features;
    }

    static {
        int features = 0;
        features |= SerializerFeature.QuoteFieldNames.getMask();
        features |= SerializerFeature.SkipTransientField.getMask();
        features |= SerializerFeature.WriteEnumUsingName.getMask();
        features |= SerializerFeature.SortField.getMask();

        DEFAULT_GENERATE_FEATURE = features;
    }

    static final Supplier<List> arraySupplier = JSONArray::new;
    static final Supplier<Map> defaultSupplier = JSONObject::new;
    static final Supplier<Map> orderedSupplier = () -> new JSONObject(true);

    static {
        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        readerProvider.register(AwtRederModule.INSTANCE);
        readerProvider.register(new Fastjson1xReaderModule(readerProvider));

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        writerProvider.register(AwtWriterModule.INSTANCE);
        writerProvider.register(new Fastjson1xWriterModule(writerProvider));
    }

    public static JSONReader.Context createReadContext(int featuresValue, Feature... features) {
        return createReadContext(JSONFactory.getDefaultObjectReaderProvider(), featuresValue, features);
    }

    static JSONReader.Context createReadContext(ObjectReaderProvider provider, int featuresValue, Feature... features) {
        for (Feature feature : features) {
            featuresValue |= feature.mask;
        }

        JSONReader.Context context = new JSONReader.Context(provider);

        if ((featuresValue & Feature.UseBigDecimal.mask) == 0) {
            context.config(JSONReader.Feature.UseBigDecimalForDoubles);
        }

        if ((featuresValue & Feature.SupportArrayToBean.mask) != 0) {
            context.config(JSONReader.Feature.SupportArrayToBean);
        }

        if ((featuresValue & Feature.ErrorOnEnumNotMatch.mask) != 0) {
            context.config(JSONReader.Feature.ErrorOnEnumNotMatch);
        }

        if ((featuresValue & Feature.SupportNonPublicField.mask) != 0) {
            context.config(JSONReader.Feature.FieldBased);
        }

        if ((featuresValue & Feature.SupportClassForName.mask) != 0) {
            context.config(JSONReader.Feature.SupportClassForName);
        }

        if ((featuresValue & Feature.TrimStringFieldValue.mask) != 0) {
            context.config(JSONReader.Feature.TrimString);
        }

        if ((featuresValue & Feature.ErrorOnNotSupportAutoType.mask) != 0) {
            context.config(JSONReader.Feature.ErrorOnNotSupportAutoType);
        }

        if ((featuresValue & Feature.AllowUnQuotedFieldNames.mask) != 0) {
            context.config(JSONReader.Feature.AllowUnQuotedFieldNames);
        }

        if ((featuresValue & Feature.UseNativeJavaObject.mask) != 0) {
            context.config(JSONReader.Feature.UseNativeObject);
        } else {
            context.setArraySupplier(arraySupplier);
            context.setObjectSupplier(
                    (featuresValue & Feature.OrderedField.mask) != 0
                            ? orderedSupplier
                            : defaultSupplier
            );
        }

        if ((featuresValue & Feature.NonStringKeyAsString.mask) != 0) {
            context.config(JSONReader.Feature.NonStringKeyAsString);
        }

        if ((featuresValue & Feature.DisableFieldSmartMatch.mask) == 0) {
            context.config(JSONReader.Feature.SupportSmartMatch);
        }

        if ((featuresValue & Feature.SupportAutoType.mask) != 0) {
            context.config(JSONReader.Feature.SupportAutoType);
        }

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        context.config(JSONReader.Feature.Base64StringAsByteArray);

        return context;
    }

    public static JSONObject parseObject(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE
        );
        JSONReader reader = JSONReader.of(str, context);

        try {
            Map<String, Object> map = new HashMap<>();
            reader.read(map, 0);
            JSONObject jsonObject = new JSONObject(map);
            reader.handleResolveTasks(jsonObject);
            return jsonObject;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static JSONObject parseObject(String text, Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader reader = JSONReader.of(text, context);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        boolean ordered = false;
        for (Feature feature : features) {
            if (feature == Feature.OrderedField) {
                ordered = true;
                break;
            }
        }

        try {
            Map<String, Object> map = ordered ? new LinkedHashMap() : new HashMap<>();
            reader.read(map, 0);
            JSONObject jsonObject = new JSONObject(map);
            reader.handleResolveTasks(jsonObject);
            return jsonObject;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(
            byte[] bytes,
            Charset charset,
            Type objectClass,
            ParserConfig config,
            ParseProcess processor,
            int featureValues,
            Feature... features
    ) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        if (config == null) {
            config = ParserConfig.global;
        }

        JSONReader.Context context = createReadContext(
                config.getProvider(),
                featureValues,
                features
        );
        if (processor != null) {
            context.config(processor);
        }

        JSONReader jsonReader = JSONReader.of(bytes, 0, bytes.length, charset, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectClass);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(
            byte[] bytes,
            int offset,
            int len,
            Charset charset,
            Type objectType,
            ParserConfig config,
            ParseProcess processor,
            int featureValues,
            Feature... features
    ) {
        if (bytes == null || bytes.length == 0 || len == 0) {
            return null;
        }

        if (config == null) {
            config = ParserConfig.global;
        }

        JSONReader.Context context = createReadContext(
                config.getProvider(),
                featureValues,
                features
        );
        if (processor != null) {
            context.config(processor);
        }

        JSONReader jsonReader = JSONReader.of(bytes, offset, len, charset, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(char[] str, int length, Type objectType, Feature... features) {
        if (str == null || str.length == 0) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(str, 0, length, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(char[] str, Class<T> objectClass, Feature... features) {
        if (str == null || str.length == 0) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(str, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectClass);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(
            String str,
            Type objectClass,
            ParserConfig config,
            ParseProcess processor,
            int featureValues,
            Feature... features
    ) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        if (config == null) {
            config = ParserConfig.global;
        }

        JSONReader.Context context = createReadContext(
                config.getProvider(),
                featureValues,
                features
        );
        JSONReader jsonReader = JSONReader.of(str, context);
        context.config(processor);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectClass);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(String input, Type clazz, ParserConfig config, int featureValues,
                                    Feature... features) {
        return parseObject(input, clazz, config, null, featureValues, features);
    }

    public static <T> T parseObject(String str, Type objectType, ParseProcess processor, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(str, context);
        context.config(processor);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseObject(String str, Class<T> objectClass, ParseProcess processor, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(str, context);
        context.config(processor);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectClass);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(String str, TypeReference typeReference, Feature... features) {
        return parseObject(str, typeReference.getType(), features);
    }

    public static <T> T parseObject(String input, Type clazz, int featureValues, Feature... features) {
        return (T) parseObject(input, clazz, ParserConfig.global, featureValues, features);
    }

    public static <T> T parseObject(String str, Class<T> objectClass) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE
        );
        JSONReader jsonReader = JSONReader.of(str, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectClass);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(String str, Class<T> objectType, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(str, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(String str, Type objectType, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(str, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    /**
     * @since 1.2.11
     */
    public static <T> T parseObject(String str, Type objectType, ParserConfig config, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                config.getProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );

        if (config.fieldBase) {
            context.config(JSONReader.Feature.FieldBased);
        }

        JSONReader jsonReader = JSONReader.of(str, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(
            InputStream is,
            Type objectType,
            Feature... features) throws IOException {
        return parseObject(is, StandardCharsets.UTF_8, objectType, features);
    }

    public static <T> T parseObject(
            InputStream is,
            Class<T> objectType,
            Feature... features) throws IOException {
        return parseObject(is, StandardCharsets.UTF_8, objectType, features);
    }

    public static <T> T parseObject(
            InputStream is,
            Charset charset,
            Type objectType,
            ParserConfig config,
            ParseProcess processor,
            int featureValues,
            Feature... features
    ) throws IOException {
        if (is == null) {
            return null;
        }

        if (config == null) {
            config = ParserConfig.global;
        }

        JSONReader.Context context = createReadContext(
                config.getProvider(),
                featureValues,
                features
        );

        if (processor != null) {
            context.config(processor);
        }

        JSONReader jsonReader = JSONReader.of(is, charset, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(
            InputStream is,
            Charset charset,
            Type type,
            ParserConfig config,
            Feature... features
    ) throws IOException {
        return (T) parseObject(is, charset, type, config, null, DEFAULT_PARSER_FEATURE, features);
    }

    public static <T> T parseObject(
            InputStream is,
            Charset charset,
            Type objectType,
            Feature... features) throws IOException {
        if (is == null) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(is, charset, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(objectType);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    /**
     * @since 2.0.7
     */
    public static <T> JSONObject parseObject(byte[] jsonBytes, Feature... features) {
        if (jsonBytes == null || jsonBytes.length == 0) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader reader = JSONReader.of(jsonBytes, context);

        boolean ordered = false;
        for (Feature feature : features) {
            if (feature == Feature.OrderedField) {
                ordered = true;
                break;
            }
        }

        try {
            Map<String, Object> map = ordered ? new LinkedHashMap<>() : new HashMap<>();
            reader.read(map, 0);
            JSONObject jsonObject = new JSONObject(map);
            reader.handleResolveTasks(jsonObject);
            return jsonObject;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(byte[] jsonBytes, Type type, Feature... features) {
        if (jsonBytes == null) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(jsonBytes, context);

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(type);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> T parseObject(byte[] jsonBytes, Type type, SerializeFilter filter, Feature... features) {
        if (jsonBytes == null) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        JSONReader jsonReader = JSONReader.of(jsonBytes, context);

        if (filter instanceof Filter) {
            context.config((Filter) filter);
        }

        try {
            ObjectReader<T> objectReader = jsonReader.getObjectReader(type);
            T object = objectReader.readObject(jsonReader, null, null, 0);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static Object parse(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE
        );
        try (JSONReader jsonReader = JSONReader.of(str, context)) {
            if (jsonReader.isObject() && !jsonReader.isSupportAutoType(0)) {
                return jsonReader.read(JSONObject.class);
            }
            return jsonReader.readAny();
        } catch (Exception ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
    }

    public static Object parse(String text, int features) {
        return parse(text, ParserConfig.global, features);
    }

    public static Object parse(String str, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        try (JSONReader jsonReader = JSONReader.of(str, context)) {
            if (jsonReader.isObject() && !jsonReader.isSupportAutoType(0)) {
                return jsonReader.read(JSONObject.class);
            }
            return jsonReader.readAny();
        } catch (Exception ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
    }

    public static Object parse(String str, ParserConfig config, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(config.getProvider(), DEFAULT_PARSER_FEATURE, features);
        try (JSONReader jsonReader = JSONReader.of(str, context)) {
            if (jsonReader.isObject() && !jsonReader.isSupportAutoType(0)) {
                return jsonReader.read(JSONObject.class);
            }
            return jsonReader.read(Object.class);
        } catch (Exception ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
    }

    public static Object parse(String str, ParserConfig config) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(config.getProvider(), DEFAULT_PARSER_FEATURE);
        try (JSONReader jsonReader = JSONReader.of(str, context)) {
            if (jsonReader.isObject() && !jsonReader.isSupportAutoType(0)) {
                return jsonReader.read(JSONObject.class);
            }
            return jsonReader.readAny();
        } catch (Exception ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
    }

    public static Object parse(String str, ParserConfig config, int features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(config.getProvider(), features);
        try (JSONReader jsonReader = JSONReader.of(str, context)) {
            if (jsonReader.isObject() && !jsonReader.isSupportAutoType(0)) {
                return jsonReader.read(JSONObject.class);
            }
            return jsonReader.readAny();
        } catch (Exception ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
    }

    public static Object parse(byte[] utf8Bytes, Feature... features) {
        if (utf8Bytes == null || utf8Bytes.length == 0) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE,
                features
        );
        try (JSONReader jsonReader = JSONReader.of(utf8Bytes, context)) {
            if (jsonReader.isObject() && !jsonReader.isSupportAutoType(0)) {
                return jsonReader.read(JSONObject.class);
            }
            return jsonReader.readAny();
        } catch (Exception ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
    }

    public static Object parse(byte[] input, int off, int len, CharsetDecoder charsetDecoder, Feature... features) {
        if (input == null || input.length == 0) {
            return null;
        }

        int featureValues = DEFAULT_PARSER_FEATURE;
        for (Feature feature : features) {
            featureValues = Feature.config(featureValues, feature, true);
        }

        return parse(input, off, len, charsetDecoder, featureValues);
    }

    public static Object parse(byte[] input, int off, int len, CharsetDecoder charsetDecoder, int features) {
        charsetDecoder.reset();

        int scaleLength = (int) (len * (double) charsetDecoder.maxCharsPerByte());
        char[] chars = CHARS_UPDATER.getAndSet(CACHE, null);
        if (chars == null || chars.length < scaleLength) {
            chars = new char[scaleLength];
        }

        try {
            ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
            CharBuffer charBuf = CharBuffer.wrap(chars);
            IOUtils.decode(charsetDecoder, byteBuf, charBuf);

            int position = charBuf.position();

            JSONReader.Context context = createReadContext(
                    JSONFactory.getDefaultObjectReaderProvider(),
                    features
            );
            JSONReader jsonReader = JSONReader.of(chars, 0, position, context);

            for (Feature feature : Feature.values()) {
                if ((features & feature.mask) != 0) {
                    switch (feature) {
                        case SupportArrayToBean:
                            context.config(JSONReader.Feature.SupportArrayToBean);
                            break;
                        case SupportAutoType:
                            context.config(JSONReader.Feature.SupportAutoType);
                            break;
                        case ErrorOnEnumNotMatch:
                            context.config(JSONReader.Feature.ErrorOnEnumNotMatch);
                        case SupportNonPublicField:
                            context.config(JSONReader.Feature.FieldBased);
                        default:
                            break;
                    }
                }
            }

            Object object = jsonReader.read(Object.class);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } finally {
            if (chars.length <= 1024 * 64) {
                CHARS_UPDATER.set(CACHE, chars);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T parseObject(
            byte[] input,
            int off,
            int len,
            CharsetDecoder charsetDecoder,
            Type clazz,
            Feature... features) {
        charsetDecoder.reset();

        int scaleLength = (int) (len * (double) charsetDecoder.maxCharsPerByte());

        char[] chars = CHARS_UPDATER.getAndSet(CACHE, null);
        if (chars == null || chars.length < scaleLength) {
            chars = new char[scaleLength];
        }

        try {
            ByteBuffer byteBuf = ByteBuffer.wrap(input, off, len);
            CharBuffer charByte = CharBuffer.wrap(chars);
            IOUtils.decode(charsetDecoder, byteBuf, charByte);

            int position = charByte.position();

            JSONReader.Context context = createReadContext(
                    JSONFactory.getDefaultObjectReaderProvider(),
                    DEFAULT_PARSER_FEATURE,
                    features
            );
            JSONReader jsonReader = JSONReader.of(chars, 0, position, context);

            T object = jsonReader.read(clazz);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        } finally {
            if (chars.length <= 1024 * 64) {
                CHARS_UPDATER.set(CACHE, chars);
            }
        }
    }

    public static <T> T parseObject(
            byte[] input,
            int off,
            int len,
            Charset charset,
            Type clazz,
            Feature... features) {
        try (JSONReader jsonReader =
                     JSONReader.of(
                             input,
                             off,
                             len,
                             charset,
                             createReadContext(
                                     JSONFactory.getDefaultObjectReaderProvider(),
                                     DEFAULT_PARSER_FEATURE,
                                     features
                             )
                     )
        ) {
            T object = jsonReader.read(clazz);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        }
    }

    public static JSONWriter.Context createWriteContext(
            SerializeConfig config,
            int featuresValue,
            SerializerFeature... features
    ) {
        for (SerializerFeature feature : features) {
            featuresValue |= feature.mask;
        }

        JSONWriter.Context context = new JSONWriter.Context(config.getProvider());

        if (config.fieldBased) {
            context.config(JSONWriter.Feature.FieldBased);
        }

        if (config.propertyNamingStrategy != null
                && config.propertyNamingStrategy != PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue) {
            NameFilter nameFilter = NameFilter.of(config.propertyNamingStrategy);
            configFilter(context, nameFilter);
        }

        if ((featuresValue & SerializerFeature.DisableCircularReferenceDetect.mask) == 0) {
            context.config(JSONWriter.Feature.ReferenceDetection);
        }

        if ((featuresValue & SerializerFeature.UseISO8601DateFormat.mask) != 0) {
            context.setDateFormat("iso8601");
        } else {
            context.setDateFormat("millis");
        }

        if ((featuresValue & SerializerFeature.WriteMapNullValue.mask) != 0) {
            context.config(JSONWriter.Feature.WriteMapNullValue);
        }

        if ((featuresValue & SerializerFeature.WriteNullListAsEmpty.mask) != 0) {
            context.config(JSONWriter.Feature.WriteNullListAsEmpty);
        }

        if ((featuresValue & SerializerFeature.WriteNullStringAsEmpty.mask) != 0) {
            context.config(JSONWriter.Feature.WriteNullStringAsEmpty);
        }

        if ((featuresValue & SerializerFeature.WriteNullNumberAsZero.mask) != 0) {
            context.config(JSONWriter.Feature.WriteNullNumberAsZero);
        }

        if ((featuresValue & SerializerFeature.WriteNullBooleanAsFalse.mask) != 0) {
            context.config(JSONWriter.Feature.WriteNullBooleanAsFalse);
        }

        if ((featuresValue & SerializerFeature.BrowserCompatible.mask) != 0) {
            context.config(JSONWriter.Feature.BrowserCompatible);
        }

        if ((featuresValue & SerializerFeature.WriteClassName.mask) != 0) {
            context.config(JSONWriter.Feature.WriteClassName);
        }

        if ((featuresValue & SerializerFeature.WriteNonStringValueAsString.mask) != 0) {
            context.config(JSONWriter.Feature.WriteNonStringValueAsString);
        }

        if ((featuresValue & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
            context.config(JSONWriter.Feature.WriteEnumUsingToString);
        }

        if ((featuresValue & SerializerFeature.WriteEnumUsingName.mask) != 0) {
            context.config(JSONWriter.Feature.WriteEnumsUsingName);
        }

        if ((featuresValue & SerializerFeature.NotWriteRootClassName.mask) != 0) {
            context.config(JSONWriter.Feature.NotWriteRootClassName);
        }

        if ((featuresValue & SerializerFeature.IgnoreErrorGetter.mask) != 0) {
            context.config(JSONWriter.Feature.IgnoreErrorGetter);
        }

        if ((featuresValue & SerializerFeature.WriteDateUseDateFormat.mask) != 0) {
            context.setDateFormat(JSON.DEFFAULT_DATE_FORMAT);
        }

        if ((featuresValue & SerializerFeature.BeanToArray.mask) != 0) {
            context.config(JSONWriter.Feature.BeanToArray);
        }

        if ((featuresValue & SerializerFeature.UseSingleQuotes.mask) != 0) {
            context.config(JSONWriter.Feature.UseSingleQuotes);
        }

        if ((featuresValue & SerializerFeature.MapSortField.mask) != 0) {
            context.config(JSONWriter.Feature.MapSortField);
        }

        if ((featuresValue & SerializerFeature.PrettyFormat.mask) != 0) {
            context.config(JSONWriter.Feature.PrettyFormat);
        }

        if ((featuresValue & SerializerFeature.WriteNonStringKeyAsString.mask) != 0) {
            context.config(JSONWriter.Feature.WriteNonStringKeyAsString);
        }

        if ((featuresValue & SerializerFeature.IgnoreNonFieldGetter.mask) != 0) {
            context.config(JSONWriter.Feature.IgnoreNonFieldGetter);
        }

        if ((featuresValue & SerializerFeature.NotWriteDefaultValue.mask) != 0) {
            context.config(JSONWriter.Feature.NotWriteDefaultValue);
        }

        if ((featuresValue & SerializerFeature.WriteBigDecimalAsPlain.mask) != 0) {
            context.config(JSONWriter.Feature.WriteBigDecimalAsPlain);
        }

        if (defaultTimeZone != null && defaultTimeZone != DEFAULT_TIME_ZONE) {
            context.setZoneId(defaultTimeZone.toZoneId());
        }

        context.config(JSONWriter.Feature.WriteByteArrayAsBase64);

        return context;
    }

    public static String toJSONString(
            Object object,
            SerializeConfig config,
            SerializeFilter[] filters,
            String dateFormat,
            int defaultFeatures,
            SerializerFeature... features
    ) {
        JSONWriter.Context context = createWriteContext(config, defaultFeatures, features);
        if (dateFormat != null && !dateFormat.isEmpty()) {
            context.setDateFormat(dateFormat);
        }

        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);
            for (SerializeFilter filter : filters) {
                configFilter(context, filter);
            }

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.toString();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    public static String toJSONString(
            Object object,
            SerializeConfig config,
            SerializeFilter[] filters,
            SerializerFeature... features
    ) {
        return toJSONString(object, config, filters, null, DEFAULT_GENERATE_FEATURE, features);
    }

    public static String toJSONString(Object object, SerializeFilter[] filters, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);

        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);
            for (SerializeFilter filter : filters) {
                configFilter(context, filter);
            }

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.toString();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    public static void configFilter(JSONWriter.Context context, SerializeFilter filter) {
        if (filter instanceof NameFilter) {
            context.setNameFilter((NameFilter) filter);
        }

        if (filter instanceof ValueFilter) {
            context.setValueFilter((ValueFilter) filter);
        }

        if (filter instanceof PropertyPreFilter) {
            context.setPropertyPreFilter((PropertyPreFilter) filter);
        }

        if (filter instanceof PropertyFilter) {
            context.setPropertyFilter((PropertyFilter) filter);
        }

        if (filter instanceof BeforeFilter) {
            context.setBeforeFilter((BeforeFilter) filter);
        }

        if (filter instanceof AfterFilter) {
            context.setAfterFilter((AfterFilter) filter);
        }

        if (filter instanceof LabelFilter) {
            context.setLabelFilter((LabelFilter) filter);
        }

        if (filter instanceof ContextValueFilter) {
            context.setContextValueFilter((ContextValueFilter) filter);
        }
    }

    public static byte[] toJSONBytes(
            Object object,
            SerializeConfig config,
            SerializeFilter[] filters,
            int defaultFeatures,
            SerializerFeature... features
    ) {
        JSONWriter.Context context = createWriteContext(config, defaultFeatures, features);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);
            for (SerializeFilter filter : filters) {
                configFilter(context, filter);
            }

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(
            Object object,
            SerializeConfig config,
            SerializeFilter filter,
            SerializerFeature... features
    ) {
        JSONWriter.Context context = createWriteContext(config, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);
            configFilter(context, filter);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(
            Charset charset,
            Object object,
            SerializeConfig config,
            SerializeFilter[] filters,
            String dateFormat,
            int defaultFeatures,
            SerializerFeature... features
    ) {
        JSONWriter.Context context = createWriteContext(config, defaultFeatures, features);
        if (dateFormat != null && !dateFormat.isEmpty()) {
            context.setDateFormat(dateFormat);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);
            for (SerializeFilter filter : filters) {
                configFilter(context, filter);
            }

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes(charset);
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(Object object, SerializeFilter[] filters, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);
            for (SerializeFilter filter : filters) {
                configFilter(context, filter);
            }

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(Object object, SerializeConfig config, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(config, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static String toJSONString(Object object, boolean prettyFormat) {
        SerializerFeature[] features = prettyFormat
                ? new SerializerFeature[]{SerializerFeature.PrettyFormat}
                : new SerializerFeature[0];
        JSONWriter.Context context =
                createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);

        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.toString();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    public static String toJSONString(Object object) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE);
        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);

                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.toString();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException(ex.getMessage(), cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    public static String toJSONString(Object object,
                                      SerializeFilter filter0,
                                      SerializeFilter filter1,
                                      SerializeFilter... filters) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE);
        configFilter(context, filter0);
        configFilter(context, filter1);
        for (SerializeFilter filter : filters) {
            configFilter(context, filter);
        }

        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.toString();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    public static String toJSONString(Object object, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);

        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.toString();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    public static byte[] toJSONBytes(Object object) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE);
        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(Object object, SerializeFilter... filters) {
        return toJSONBytes(object, filters, new SerializerFeature[0]);
    }

    public static byte[] toJSONBytes(Object object, int defaultFeatures, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, defaultFeatures, features);

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(Object object, SerializeFilter filter) {
        return toJSONBytes(object, SerializeConfig.global, new SerializeFilter[] {filter}, DEFAULT_GENERATE_FEATURE);
    }

    public static byte[] toJSONBytes(Object object, SerializeFilter filter, SerializerFeature... features) {
        return toJSONBytes(
                object,
                SerializeConfig.global,
                new SerializeFilter[]{filter},
                DEFAULT_GENERATE_FEATURE,
                features
        );
    }

    public static byte[] toJSONBytes(
            Object object,
            SerializeConfig config,
            SerializeFilter[] filters,
            String dateFormat,
            int defaultFeatures,
            SerializerFeature... features
    ) {
        JSONWriter.Context context = createWriteContext(config, defaultFeatures, features);

        if (dateFormat != null && !dateFormat.isEmpty()) {
            context.setDateFormat(dateFormat);
        }

        for (SerializeFilter filter : filters) {
            configFilter(context, filter);
        }

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(Object object, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);

            if (object == null) {
                writer.writeNull();
            } else {
                Class<?> valueClass = object.getClass();
                ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }

            return writer.getBytes();
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("toJSONBytes error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONBytes error", ex);
        }
    }

    public static byte[] toJSONBytes(
            Object object,
            SerializeConfig config,
            int defaultFeatures,
            SerializerFeature... features
    ) {
        return toJSONBytes(object, config, new SerializeFilter[0], defaultFeatures, features);
    }

    public static String toJSONString(Object object, SerializeConfig config, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(config, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.writeAny(object);
            return writer.toString();
        }
    }

    public static String toJSONStringZ(Object object, SerializeConfig mapping, SerializerFeature... features) {
        return toJSONString(object, mapping, new SerializeFilter[0], null, 0, features);
    }

    public static String toJSONString(
            Object object,
            SerializeConfig config,
            SerializeFilter filter,
            SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(config, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter writer = JSONWriter.of(context)) {
            if (config.propertyNamingStrategy != null
                    && config.propertyNamingStrategy != PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue) {
                NameFilter nameFilter = NameFilter.of(config.propertyNamingStrategy);
                if (filter instanceof NameFilter) {
                    filter = NameFilter.compose(nameFilter, (NameFilter) filter);
                } else {
                    configFilter(context, nameFilter);
                }
            }

            configFilter(context, filter);

            if (object == null) {
                writer.writeNull();
            } else {
                writer.setRootObject(object);
                Class<?> valueClass = object.getClass();
                ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
                objectWriter.write(writer, object, null, null, 0);
            }
            return writer.toString();
        }
    }

    public static String toJSONString(
            Object object,
            SerializeFilter filter,
            SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);
        configFilter(context, filter);

        try (JSONWriter jsonWriter = JSONWriter.of(context)) {
            if (object == null) {
                jsonWriter.writeNull();
            } else {
                jsonWriter.setRootObject(object);
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            return jsonWriter.toString();
        }
    }

    public static String toJSONString(Object object, int defaultFeatures, SerializerFeature... features) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, defaultFeatures, features);
        try (JSONWriter jsonWriter = JSONWriter.of(context)) {
            if (object == null) {
                jsonWriter.writeNull();
            } else {
                jsonWriter.setRootObject(object);
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            return jsonWriter.toString();
        }
    }

    public static String toJSONStringWithDateFormat(
            Object object, String dateFormat,
            SerializerFeature... features
    ) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter jsonWriter = JSONWriter.of(context)) {
            context.setDateFormat(dateFormat);

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            return jsonWriter.toString();
        }
    }

    public static final int writeJSONString(
            OutputStream os,
            Object object,
            SerializerFeature... features) throws IOException {
        return writeJSONString(os, object, new SerializeFilter[0], features);
    }

    public static final int writeJSONString(
            OutputStream os,
            Object object,
            SerializeFilter[] filters) throws IOException {
        return writeJSONString(os, object, filters, new SerializerFeature[0]);
    }

    public static final int writeJSONString(
            OutputStream os,
            Charset charset,
            Object object,
            SerializerFeature... features
    ) throws IOException {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);

        try (JSONWriter jsonWriter = JSONWriter.ofUTF8(context)) {
            jsonWriter.setRootObject(object);

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            byte[] bytes = jsonWriter.getBytes(charset);
            os.write(bytes);
            return bytes.length;
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("writeJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("writeJSONString error", ex);
        }
    }

    public static void writeJSONString(Writer writer, Object object, SerializerFeature... features) {
        writeJSONString(writer, object, DEFAULT_GENERATE_FEATURE, features);
    }

    public static void writeJSONString(
            Writer writer,
            Object object,
            int defaultFeatures,
            SerializerFeature... features
    ) {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, defaultFeatures, features);

        try (JSONWriter jsonWriter = JSONWriter.ofUTF8(context)) {
            jsonWriter.setRootObject(object);

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }

            jsonWriter.flushTo(writer);
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("writeJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("writeJSONString error", ex);
        }
    }

    public static final int writeJSONString(
            OutputStream os,
            Object object,
            int defaultFeatures,
            SerializerFeature... features
    ) throws IOException {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, defaultFeatures, features);

        try (JSONWriter jsonWriter = JSONWriter.ofUTF8(context)) {
            jsonWriter.setRootObject(object);

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            byte[] bytes = jsonWriter.getBytes();
            os.write(bytes);
            return bytes.length;
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("writeJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("writeJSONString error", ex);
        }
    }

    public static final int writeJSONString(
            OutputStream os,
            Charset charset,
            Object object,
            SerializeConfig config,
            SerializeFilter[] filters,
            String dateFormat,
            int defaultFeatures,
            SerializerFeature... features
    ) throws IOException {
        JSONWriter.Context context = createWriteContext(config, defaultFeatures, features);
        if (dateFormat != null && !dateFormat.isEmpty()) {
            context.setDateFormat(dateFormat);
        }

        try (JSONWriter jsonWriter = JSONWriter.ofUTF8(context)) {
            jsonWriter.setRootObject(object);
            for (SerializeFilter filter : filters) {
                configFilter(context, filter);
            }

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            byte[] bytes = jsonWriter.getBytes(charset);
            os.write(bytes);
            return bytes.length;
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("writeJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("writeJSONString error", ex);
        }
    }

    public static final int writeJSONString(
            OutputStream os,
            Object object,
            SerializeFilter[] filters,
            SerializerFeature... features
    ) throws IOException {
        JSONWriter.Context context = createWriteContext(SerializeConfig.global, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter jsonWriter = JSONWriter.ofUTF8(context)) {
            jsonWriter.setRootObject(object);
            for (SerializeFilter filter : filters) {
                configFilter(context, filter);
            }

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            byte[] bytes = jsonWriter.getBytes();
            os.write(bytes);
            return bytes.length;
        } catch (com.alibaba.fastjson2.JSONException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new JSONException("writeJSONString error", cause);
        } catch (RuntimeException ex) {
            throw new JSONException("writeJSONString error", ex);
        }
    }

    public static JSONArray parseArray(String str, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(
                str,
                createReadContext(JSONFactory.getDefaultObjectReaderProvider(), DEFAULT_PARSER_FEATURE, features))
        ) {
            if (jsonReader.nextIfNull()) {
                return null;
            }

            JSONArray jsonArray = new JSONArray();
            jsonReader.read(jsonArray);
            return jsonArray;
        }
    }

    public static JSONArray parseArray(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader.Context context = createReadContext(
                JSONFactory.getDefaultObjectReaderProvider(),
                DEFAULT_PARSER_FEATURE
        );
        JSONReader reader = JSONReader.of(str, context);

        try {
            List list = new ArrayList();
            reader.read(list);
            JSONArray jsonArray = new JSONArray(list);
            reader.handleResolveTasks(jsonArray);
            return jsonArray;
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> type) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{type}, null, List.class);

        try (JSONReader reader = JSONReader.of(
                text,
                createReadContext(JSONFactory.getDefaultObjectReaderProvider(), DEFAULT_PARSER_FEATURE))
        ) {
            return reader.read(paramType);
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz, ParserConfig config) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        if (config == null) {
            config = ParserConfig.global;
        }

        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{clazz}, null, List.class);

        try (JSONReader reader = JSONReader.of(
                text,
                createReadContext(config.getProvider(), DEFAULT_PARSER_FEATURE))
        ) {
            return reader.read(paramType);
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> type, Feature... features) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{type}, null, List.class);

        try (JSONReader reader = JSONReader.of(
                text,
                createReadContext(JSONFactory.getDefaultObjectReaderProvider(), DEFAULT_PARSER_FEATURE, features))
        ) {
            return reader.read(paramType);
        } catch (com.alibaba.fastjson2.JSONException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new JSONException(e.getMessage(), cause);
        }
    }

    public static boolean isValid(String str) {
        return com.alibaba.fastjson2.JSON.isValid(str);
    }

    public static boolean isValidArray(String str) {
        return com.alibaba.fastjson2.JSON.isValidArray(str);
    }

    public static boolean isValidObject(String str) {
        return com.alibaba.fastjson2.JSON.isValidObject(str);
    }

    public abstract <T> T toJavaObject(Class<T> objectClass);

    public abstract <T> T toJavaObject(Type type);

    public <T> T toJavaObject(TypeReference typeReference) {
        Type type = typeReference != null ? typeReference.getType() : Object.class;
        return toJavaObject(type);
    }

    public static <T> T toJavaObject(JSON json, Class<T> clazz) {
        if (json instanceof JSONObject) {
            return ((JSONObject) json).toJavaObject(clazz);
        }

        String str = toJSONString(json);
        return parseObject(str, clazz);
    }

    public static Object toJSON(Object javaObject) {
        if (javaObject instanceof JSON) {
            return javaObject;
        }

        String str = JSON.toJSONString(javaObject);
        Object object = JSON.parse(str);
        if (object instanceof List) {
            return new JSONArray((List) object);
        }
        return object;
    }

    public static Object toJSON(Object javaObject, SerializeConfig config) {
        if (javaObject instanceof JSON) {
            return javaObject;
        }

        String str = JSON.toJSONString(javaObject, config);
        Object object = JSON.parse(str);
        if (object instanceof List) {
            return new JSONArray((List) object);
        }
        return object;
    }

    public static Object toJSON(Object javaObject, ParserConfig config) {
        if (javaObject instanceof JSON) {
            return javaObject;
        }

        String str = JSON.toJSONString(javaObject);
        Object object = JSON.parse(str, config);
        if (object instanceof List) {
            return new JSONArray((List) object);
        }
        return object;
    }

    public static List<Object> parseArray(String text, Type[] types) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        List array = new JSONArray(types.length);

        try (JSONReader reader = JSONReader.of(
                text,
                createReadContext(JSONFactory.getDefaultObjectReaderProvider(), DEFAULT_GENERATE_FEATURE))
        ) {
            reader.startArray();
            for (Type itemType : types) {
                array.add(
                        reader.read(itemType)
                );
            }
            reader.endArray();
            reader.handleResolveTasks(array);
            return array;
        }
    }

    public static void addMixInAnnotations(Type target, Type mixinSource) {
        if (target != null && mixinSource != null) {
            ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
            readerProvider.mixIn((Class) target, (Class) mixinSource);

            ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
            writerProvider.mixIn((Class) target, (Class) mixinSource);
        }
    }

    public static void removeMixInAnnotations(Type target) {
        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        readerProvider.mixIn((Class) target, null);

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        writerProvider.mixIn((Class) target, null);
    }

    public static void clearMixInAnnotations() {
        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        readerProvider.cleanupMixIn();

        ObjectWriterProvider writerProvider = JSONFactory.getDefaultObjectWriterProvider();
        writerProvider.cleanupMixIn();
    }

    public static Type getMixInAnnotations(Type target) {
        ObjectReaderProvider readerProvider = JSONFactory.getDefaultObjectReaderProvider();
        Class mixIn = readerProvider.getMixIn((Class) target);
        if (mixIn == null) {
            mixIn = JSONFactory.getDefaultObjectWriterProvider().getMixIn((Class) target);
        }
        return mixIn;
    }

    static class Cache {
        volatile char[] chars;
    }

    public String toJSONString() {
        return com.alibaba.fastjson2.JSON.toJSONString(this, JSONWriter.Feature.ReferenceDetection);
    }

    public String toString(SerializerFeature... features) {
        return JSON.toJSONString(this, features);
    }

    public void writeJSONString(Appendable appendable) {
        if (appendable instanceof Writer) {
            JSON.writeJSONString((Writer) appendable, this);
            return;
        }

        try {
            appendable.append(JSON.toJSONString(this));
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }
}
