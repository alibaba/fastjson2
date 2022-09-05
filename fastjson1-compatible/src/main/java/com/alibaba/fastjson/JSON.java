package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
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
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

public class JSON {
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

    public static JSONObject parseObject(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader reader = JSONReader.of(str);
        JSONReader.Context context = reader.getContext();
        context.config(JSONReader.Feature.AllowUnQuotedFieldNames);
        context.setArraySupplier(arraySupplier);
        context.setObjectSupplier(defaultSupplier);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

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

        JSONReader reader = JSONReader.of(text);
        JSONReader.Context context = reader.getContext();
        context.setArraySupplier(arraySupplier);
        context.setObjectSupplier(defaultSupplier);
        context.config(JSONReader.Feature.AllowUnQuotedFieldNames);

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

        config(context, features);
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

    public static <T> T parseObject(char[] str, Class<T> objectClass, Feature... features) {
        if (str == null || str.length == 0) {
            return null;
        }

        JSONReader jsonReader = JSONReader.of(str);
        JSONReader.Context context = jsonReader.getContext();
        context.setArraySupplier(arraySupplier);
        context.setObjectSupplier(defaultSupplier);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        config(context, features);

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

    public static <T> T parseObject(String str, Class<T> objectClass) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        JSONReader jsonReader = JSONReader.of(str);
        JSONReader.Context context = jsonReader.getContext();
        context.setArraySupplier(arraySupplier);
        context.setObjectSupplier(defaultSupplier);
        context.config(JSONReader.Feature.SupportSmartMatch, JSONReader.Feature.AllowUnQuotedFieldNames);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

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

        boolean useNativeJavaObject = false;
        for (Feature feature : features) {
            if (feature == Feature.UseNativeJavaObject) {
                useNativeJavaObject = true;
            }
        }

        JSONReader jsonReader = JSONReader.of(str);
        JSONReader.Context context = jsonReader.getContext();
        if (!useNativeJavaObject) {
            context.setArraySupplier(arraySupplier);
            context.setObjectSupplier(defaultSupplier);
        }

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        config(context, features);

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

        boolean useNativeJavaObject = false;
        for (Feature feature : features) {
            if (feature == Feature.UseNativeJavaObject) {
                useNativeJavaObject = true;
            }
        }

        JSONReader jsonReader = JSONReader.of(str);
        JSONReader.Context context = jsonReader.getContext();
        if (!useNativeJavaObject) {
            context.setArraySupplier(arraySupplier);
            context.setObjectSupplier(defaultSupplier);
        }

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        config(context, features);

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
            Feature... features) throws IOException {
        if (is == null) {
            return null;
        }

        JSONReader jsonReader = JSONReader.of(is, charset);
        JSONReader.Context context = jsonReader.getContext();
        context.setArraySupplier(arraySupplier);
        context.setObjectSupplier(defaultSupplier);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        config(context, features);

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

        JSONReader reader = JSONReader.of(jsonBytes);
        JSONReader.Context context = reader.getContext();
        context.setArraySupplier(arraySupplier);
        context.setObjectSupplier(defaultSupplier);

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

        config(context, features);
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

        JSONReader jsonReader = JSONReader.of(jsonBytes);
        JSONReader.Context context = jsonReader.getContext();
        context.setObjectSupplier(defaultSupplier);
        context.setArraySupplier(arraySupplier);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        config(context, features);
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

        JSONReader jsonReader = JSONReader.of(jsonBytes);
        JSONReader.Context context = jsonReader.getContext();
        context.setObjectSupplier(defaultSupplier);
        context.setArraySupplier(arraySupplier);

        String defaultDateFormat = JSON.DEFFAULT_DATE_FORMAT;
        if (!"yyyy-MM-dd HH:mm:ss".equals(defaultDateFormat)) {
            context.setDateFormat(defaultDateFormat);
        }

        if (filter instanceof Filter) {
            context.config((Filter) filter);
        }

        config(context, features);
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

    public static Object parse(String str, Feature... features) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        try (JSONReader jsonReader = JSONReader.of(str)) {
            JSONReader.Context context = jsonReader.getContext();
            context.setObjectSupplier(defaultSupplier);
            context.setArraySupplier(arraySupplier);
            config(context, features);
            if (jsonReader.isObject() && !jsonReader.isSupportAutoType(0)) {
                return jsonReader.read(JSONObject.class);
            }
            return jsonReader.readAny();
        } catch (Exception ex) {
            throw new JSONException(ex.getMessage(), ex);
        }
    }

    protected static void config(JSONReader.Context context, Feature[] features) {
        for (Feature feature : features) {
            switch (feature) {
                case SupportArrayToBean:
                    context.config(JSONReader.Feature.SupportArrayToBean);
                    break;
                case SupportAutoType:
                    context.config(JSONReader.Feature.SupportAutoType);
                    break;
                case ErrorOnEnumNotMatch:
                    context.config(JSONReader.Feature.ErrorOnEnumNotMatch);
                    break;
                case SupportNonPublicField:
                    context.config(JSONReader.Feature.FieldBased);
                    break;
                case SupportClassForName:
                    context.config(JSONReader.Feature.SupportClassForName);
                    break;
                case TrimStringFieldValue:
                    context.config(JSONReader.Feature.TrimString);
                    break;
                case ErrorOnNotSupportAutoType:
                    context.config(JSONReader.Feature.ErrorOnNotSupportAutoType);
                    break;
                case AllowUnQuotedFieldNames:
                    context.config(JSONReader.Feature.AllowUnQuotedFieldNames);
                    break;
                case OrderedField:
                    context.setObjectSupplier(orderedSupplier);
                    break;
                case UseNativeJavaObject:
                    context.config(JSONReader.Feature.UseNativeObject);
                    break;
                case NonStringKeyAsString:
                    context.config(JSONReader.Feature.NonStringKeyAsString);
                    break;
                default:
                    break;
            }
        }

        boolean disableFieldSmartMatch = false;
        for (Feature feature : features) {
            if (feature == Feature.DisableFieldSmartMatch) {
                disableFieldSmartMatch = true;
                break;
            }
        }

        if (!disableFieldSmartMatch) {
            context.config(JSONReader.Feature.SupportSmartMatch);
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

            JSONReader jsonReader = JSONReader.of(chars, 0, position);
            JSONReader.Context context = jsonReader.getContext();

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

            JSONReader jsonReader = JSONReader.of(chars, 0, position);

            JSONReader.Context context = jsonReader.getContext();
            context.setObjectSupplier(defaultSupplier);
            context.setArraySupplier(arraySupplier);
            context.config(JSONReader.Feature.SupportSmartMatch, JSONReader.Feature.AllowUnQuotedFieldNames);

            config(context, features);
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
        try (JSONReader jsonReader = JSONReader.of(input, off, len, charset)) {
            config(jsonReader.getContext(), features);
            T object = jsonReader.read(clazz);
            if (object != null) {
                jsonReader.handleResolveTasks(object);
            }
            return object;
        }
    }

    public static String toJSONString(Object object, SerializeFilter[] filters, SerializerFeature... features) {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        config(context, features);

        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);
            configFilter(context, filters);

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

    public static void configFilter(JSONWriter.Context context, SerializeFilter... filters) {
        for (SerializeFilter filter : filters) {
            configFilter(context, filter);
        }
    }

    static void configFilter(JSONWriter.Context context, SerializeFilter filter) {
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

    public static byte[] toJSONBytes(Object object, SerializeFilter[] filters, SerializerFeature... features) {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        config(context, features);

        try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
            writer.setRootObject(object);
            configFilter(context, filters);

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
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.ReferenceDetection);
        if (prettyFormat) {
            context.config(JSONWriter.Feature.PrettyFormat);
        }
        context.setDateFormat("millis");
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
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.ReferenceDetection);
        context.setDateFormat("millis");
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
            throw new JSONException(ex.getMessage(), cause);
        } catch (RuntimeException ex) {
            throw new JSONException("toJSONString error", ex);
        }
    }

    public static String toJSONString(Object object, SerializeFilter filter0, SerializeFilter filter1, SerializeFilter... filters) {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setDateFormat("millis");
        configFilter(context, filter0);
        configFilter(context, filter1);
        configFilter(context, filters);

        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.setRootObject(object);
            configFilter(context, filters);

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
        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setDateFormat("millis");
        config(context, features);

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
        JSONWriter.Context context = JSONFactory.createWriteContext(JSONWriter.Feature.ReferenceDetection);
        context.setDateFormat("millis");
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

    public static byte[] toJSONBytes(Object object, SerializerFeature... features) {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        config(context, features);

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

    public static void config(JSONWriter.Context ctx, SerializerFeature[] features) {
        ctx.setDateFormat("millis");
        if (defaultTimeZone != null && defaultTimeZone != DEFAULT_TIME_ZONE) {
            ctx.setZoneId(defaultTimeZone.toZoneId());
        }

        boolean disableCircularReferenceDetect = false;
        for (SerializerFeature feature : features) {
            if (feature == SerializerFeature.DisableCircularReferenceDetect) {
                disableCircularReferenceDetect = true;
                break;
            }
        }
        if (!disableCircularReferenceDetect) {
            ctx.config(JSONWriter.Feature.ReferenceDetection);
        }

        for (SerializerFeature feature : features) {
            config(ctx, feature);
        }
    }

    static void config(JSONWriter.Context context, SerializerFeature feature) {
        switch (feature) {
            case UseISO8601DateFormat:
                context.setDateFormat("iso8601");
                break;
            case WriteMapNullValue:
                context.config(JSONWriter.Feature.WriteNulls);
                break;
            case WriteNullListAsEmpty:
                context.config(JSONWriter.Feature.WriteNullListAsEmpty);
                break;
            case WriteNullStringAsEmpty:
                context.config(JSONWriter.Feature.WriteNullStringAsEmpty);
                break;
            case WriteNullNumberAsZero:
                context.config(JSONWriter.Feature.WriteNullNumberAsZero);
                break;
            case WriteNullBooleanAsFalse:
                context.config(JSONWriter.Feature.WriteNullBooleanAsFalse);
                break;
            case BrowserCompatible:
                context.config(JSONWriter.Feature.BrowserCompatible);
                break;
            case WriteClassName:
                context.config(JSONWriter.Feature.WriteClassName);
                break;
            case WriteNonStringValueAsString:
                context.config(JSONWriter.Feature.WriteNonStringValueAsString);
                break;
            case WriteEnumUsingToString:
                context.config(JSONWriter.Feature.WriteEnumUsingToString);
                break;
            case NotWriteRootClassName:
                context.config(JSONWriter.Feature.NotWriteRootClassName);
                break;
            case IgnoreErrorGetter:
                context.config(JSONWriter.Feature.IgnoreErrorGetter);
                break;
            case WriteDateUseDateFormat:
                context.setDateFormat(JSON.DEFFAULT_DATE_FORMAT);
                break;
            case BeanToArray:
                context.config(JSONWriter.Feature.BeanToArray);
                break;
            case UseSingleQuotes:
                context.config(JSONWriter.Feature.UseSingleQuotes);
                break;
            case MapSortField:
                context.config(JSONWriter.Feature.MapSortField);
                break;
            case PrettyFormat:
                context.config(JSONWriter.Feature.PrettyFormat);
                break;
            case WriteNonStringKeyAsString:
                context.config(JSONWriter.Feature.WriteNonStringKeyAsString);
                break;
            case IgnoreNonFieldGetter:
                context.config(JSONWriter.Feature.IgnoreNonFieldGetter);
                break;
            default:
                break;
        }
    }

    public static String toJSONString(Object object, SerializeConfig config, SerializerFeature... features) {
        try (JSONWriter writer = JSONWriter.of()) {
            JSONWriter.Context context = writer.getContext();
            if (config.propertyNamingStrategy != null
                    && config.propertyNamingStrategy != PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue) {
                NameFilter nameFilter = NameFilter.of(config.propertyNamingStrategy);
                configFilter(context, nameFilter);
            }

            if (config.fieldBased) {
                context.config(JSONWriter.Feature.FieldBased);
            }

            config(context, features);
            writer.writeAny(object);
            return writer.toString();
        }
    }

    public static String toJSONString(
            Object object,
            SerializeConfig config,
            SerializeFilter filter,
            SerializerFeature... features) {
        try (JSONWriter writer = JSONWriter.of()) {
            JSONWriter.Context context = writer.getContext();

            if (config.propertyNamingStrategy != null
                    && config.propertyNamingStrategy != PropertyNamingStrategy.NeverUseThisValueExceptDefaultValue) {
                NameFilter nameFilter = NameFilter.of(config.propertyNamingStrategy);
                if (filter instanceof NameFilter) {
                    filter = NameFilter.compose(nameFilter, (NameFilter) filter);
                } else {
                    configFilter(context, nameFilter);
                }
            }

            if (config.fieldBased) {
                context.config(JSONWriter.Feature.FieldBased);
            }

            configFilter(context, filter);
            config(context, features);

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
        try (JSONWriter jsonWriter = JSONWriter.of()) {
            JSONWriter.Context context = jsonWriter.getContext();
            configFilter(context, filter);
            config(context, features);

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            return jsonWriter.toString();
        }
    }

    public static String toJSONString(Object object, int defaultFeatures, SerializerFeature... features) {
        try (JSONWriter jsonWriter = JSONWriter.of()) {
            JSONWriter.Context context = jsonWriter.getContext();

            if (defaultFeatures != 0) {
                for (SerializerFeature feature : SerializerFeature.values()) {
                    if ((defaultFeatures & feature.mask) != 0) {
                        config(context, feature);
                    }
                }
            }

            config(context, features);

            if (object == null) {
                jsonWriter.writeNull();
            } else {
                ObjectWriter<?> objectWriter = context.getObjectWriter(object.getClass());
                objectWriter.write(jsonWriter, object, null, null, 0);
            }
            return jsonWriter.toString();
        }
    }

    public static String toJSONStringWithDateFormat(Object object, String dateFormat,
                                                    SerializerFeature... features) {
        //return toJSONString(object, SerializeConfig.globalInstance, null, dateFormat, DEFAULT_GENERATE_FEATURE, features);
        try (JSONWriter jsonWriter = JSONWriter.of()) {
            for (SerializerFeature feature : features) {
                if (feature == SerializerFeature.WriteMapNullValue) {
                    jsonWriter.config(JSONWriter.Feature.WriteNulls);
                }
            }

            JSONWriter.Context context = jsonWriter.getContext();
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
            Object object,
            SerializeFilter[] filters,
            SerializerFeature... features) throws IOException {
        try (JSONWriter jsonWriter = JSONWriter.ofUTF8()) {
            JSONWriter.Context context = jsonWriter.getContext();
            jsonWriter.setRootObject(object);
            config(context, features);
            configFilter(context, filters);

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

        try (JSONReader jsonReader = JSONReader.of(str)) {
            JSONReader.Context context = jsonReader.getContext();
            context.setObjectSupplier(defaultSupplier);
            context.setArraySupplier(arraySupplier);
            config(context, features);

            if (jsonReader.nextIfNull()) {
                return null;
            }

            JSONArray jsonArray = new JSONArray();
            jsonReader.read(jsonArray);
            return jsonArray;
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> type) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        ParameterizedTypeImpl paramType = new ParameterizedTypeImpl(new Type[]{type}, null, List.class);

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.getContext();
            context.setObjectSupplier(defaultSupplier);
            context.setArraySupplier(arraySupplier);
            context.config(JSONReader.Feature.SupportSmartMatch, JSONReader.Feature.AllowUnQuotedFieldNames);
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

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.getContext();
            context.setObjectSupplier(defaultSupplier);
            context.setArraySupplier(arraySupplier);

            config(context, features);

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

    public static List<Object> parseArray(String text, Type[] types) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        List array = new JSONArray(types.length);

        try (JSONReader reader = JSONReader.of(text)) {
            JSONReader.Context context = reader.getContext();
            context.setObjectSupplier(defaultSupplier);
            context.setArraySupplier(arraySupplier);
            context.config(JSONReader.Feature.SupportSmartMatch, JSONReader.Feature.AllowUnQuotedFieldNames);

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

    static class Cache {
        volatile char[] chars;
    }

    public String toJSONString() {
        return com.alibaba.fastjson2.JSON.toJSONString(this, JSONWriter.Feature.ReferenceDetection);
    }
}
