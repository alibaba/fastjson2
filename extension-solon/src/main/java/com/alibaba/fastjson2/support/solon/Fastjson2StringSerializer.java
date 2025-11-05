package com.alibaba.fastjson2.support.solon;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.noear.solon.Utils;
import org.noear.solon.core.convert.Converter;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.util.Assert;
import org.noear.solon.core.util.MimeType;
import org.noear.solon.lang.Nullable;
import org.noear.solon.serialization.EntityStringSerializer;
import org.noear.solon.serialization.prop.JsonProps;
import org.noear.solon.serialization.prop.JsonPropsUtil2;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Fastjson2 serializer for Solon framework.
 * Provides JSON serialization and deserialization for Solon web framework with support
 * for JSON properties configuration.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Configuration
 * public class Config {
 *     @Bean
 *     public EntityStringSerializer serializer() {
 *         return new Fastjson2StringSerializer();
 *     }
 * }
 * }</pre>
 *
 * @author noear
 * @author 暮城留风
 * @since 1.10
 * @since 2.8
 */
public class Fastjson2StringSerializer
        implements EntityStringSerializer {
    private static final String label = "/json";
    private static final Fastjson2StringSerializer _default = new Fastjson2StringSerializer();

    /**
     * Gets the default singleton instance.
     *
     * @return the default Fastjson2StringSerializer instance
     */
    public static Fastjson2StringSerializer getDefault() {
        return _default;
    }

    private Fastjson2Decl<JSONWriter.Context, JSONWriter.Feature> serializeConfig;
    private Fastjson2Decl<JSONReader.Context, JSONReader.Feature> deserializeConfig;

    /**
     * Creates a new serializer with the specified JSON properties.
     *
     * @param jsonProps the JSON properties configuration
     */
    public Fastjson2StringSerializer(JsonProps jsonProps) {
        loadJsonProps(jsonProps);
    }

    /**
     * Creates a new serializer with default configuration.
     */
    public Fastjson2StringSerializer() { }

    /**
     * Gets the serialization configuration.
     * Creates a default configuration if not already initialized.
     *
     * @return the serialization configuration
     */
    public Fastjson2Decl<JSONWriter.Context, JSONWriter.Feature> getSerializeConfig() {
        if (serializeConfig == null) {
            serializeConfig = new Fastjson2Decl<>(new JSONWriter.Context(new ObjectWriterProvider()));
        }

        return serializeConfig;
    }

    /**
     * Gets the deserialization configuration.
     * Creates a default configuration if not already initialized.
     *
     * @return the deserialization configuration
     */
    public Fastjson2Decl<JSONReader.Context, JSONReader.Feature> getDeserializeConfig() {
        if (deserializeConfig == null) {
            deserializeConfig = new Fastjson2Decl<>(new JSONReader.Context(new ObjectReaderProvider()));
        }
        return deserializeConfig;
    }

    /**
     * Gets the MIME type supported by this serializer.
     *
     * @return "application/json"
     */
    @Override
    public String mimeType() {
        return "application/json";
    }

    /**
     * Gets the data type handled by this serializer.
     *
     * @return String.class
     */
    @Override
    public Class<String> dataType() {
        return String.class;
    }

    /**
     * Checks if this serializer matches the given MIME type.
     *
     * @param ctx the request context
     * @param mime the MIME type to check
     * @return true if this serializer can handle the MIME type
     */
    @Override
    public boolean matched(Context ctx, String mime) {
        if (mime == null) {
            return false;
        } else {
            return mime.contains(label) || mime.startsWith(MimeType.APPLICATION_X_NDJSON_VALUE);
        }
    }

    /**
     * Gets the name of this serializer.
     *
     * @return "fastjson2-json"
     */
    @Override
    public String name() {
        return "fastjson2-json";
    }

    /**
     * Serializes an object to JSON string.
     *
     * @param obj the object to serialize
     * @return the JSON string representation
     * @throws IOException if serialization fails
     */
    @Override
    public String serialize(Object obj) throws IOException {
        return JSON.toJSONString(obj, getSerializeConfig().getContext());
    }

    /**
     * Deserializes JSON string to an object.
     *
     * @param data the JSON string data
     * @param toType the target type
     * @return the deserialized object
     * @throws IOException if deserialization fails
     */
    @Override
    public Object deserialize(String data, Type toType) throws IOException {
        if (toType == null) {
            return JSON.parse(data, getDeserializeConfig().getContext());
        } else {
            return JSON.parseObject(data, toType, getDeserializeConfig().getContext());
        }
    }

    /**
     * Serializes data to the response body.
     *
     * @param ctx the request context
     * @param data the data to serialize
     * @throws IOException if serialization fails
     */
    @Override
    public void serializeToBody(Context ctx, Object data) throws IOException {
        //如果没有设置过，用默认的 //如 ndjson,sse 或故意改变 mime（可由外部控制）
        if (ctx.contentTypeNew() == null) {
            ctx.contentType(this.mimeType());
        }

        if (data instanceof ModelAndView) {
            ctx.output(serialize(((ModelAndView) data).model()));
        } else {
            ctx.output(serialize(data));
        }
    }

    /**
     * Deserializes data from the request body.
     *
     * @param ctx the request context
     * @param bodyType the expected body type (can be null)
     * @return the deserialized object
     * @throws IOException if deserialization fails
     */
    @Override
    public Object deserializeFromBody(Context ctx, @Nullable Type bodyType) throws IOException {
        String data = ctx.bodyNew();

        if (Utils.isNotEmpty(data)) {
            return JSON.parse(data, getDeserializeConfig().getContext());
        } else {
            return null;
        }
    }

    /**
     * Adds a custom encoder for the specified type.
     *
     * @param <T> the type to encode
     * @param clz the class type
     * @param encoder the object writer encoder
     */
    public <T> void addEncoder(Class<T> clz, ObjectWriter encoder) {
        getSerializeConfig().getContext().getProvider().register(clz, encoder);
    }

    /**
     * Adds a converter as a simplified encoder for the specified type.
     * The converter can transform objects to String, Number, or null.
     *
     * @param <T> the type to encode
     * @param clz the class type
     * @param converter the converter function
     * @throws IllegalArgumentException if the converter produces an unsupported type
     */
    @Override
    public <T> void addEncoder(Class<T> clz, Converter<T, Object> converter) {
        addEncoder(clz, (out, obj, fieldName, fieldType, features) -> {
            Object val = converter.convert((T) obj);
            if (val == null) {
                out.writeNull();
            } else if (val instanceof String) {
                out.writeString((String) val);
            } else if (val instanceof Number) {
                if (val instanceof Long) {
                    out.writeInt64(((Number) val).longValue());
                } else if (val instanceof Integer) {
                    out.writeInt32(((Number) val).intValue());
                } else if (val instanceof Float) {
                    out.writeDouble(((Number) val).floatValue());
                } else {
                    out.writeDouble(((Number) val).doubleValue());
                }
            } else {
                throw new IllegalArgumentException("The result type of the converter is not supported: " + val.getClass().getName());
            }
        });
    }

    /**
     * Loads JSON properties configuration and applies them to the serializer.
     * Configures date formatting, null handling, boolean/number formatting, etc.
     *
     * @param jsonProps the JSON properties to load
     */
    protected void loadJsonProps(JsonProps jsonProps) {
        if (jsonProps != null) {
            if (jsonProps.dateAsTicks) {
                jsonProps.dateAsTicks = false;
                getSerializeConfig().getContext().setDateFormat("millis");
            }

            if (Utils.isNotEmpty(jsonProps.dateAsFormat)) {
                //这个方案，可以支持全局配置，且个性注解不会失效；//用编码器会让个性注解失效
                getSerializeConfig().getContext().setDateFormat(jsonProps.dateAsFormat);
            }

            //JsonPropsUtil2.dateAsFormat(this, jsonProps);
            JsonPropsUtil2.dateAsTicks(this, jsonProps);
            JsonPropsUtil2.boolAsInt(this, jsonProps);

            boolean writeNulls = jsonProps.nullAsWriteable ||
                    jsonProps.nullNumberAsZero ||
                    jsonProps.nullArrayAsEmpty ||
                    jsonProps.nullBoolAsFalse ||
                    jsonProps.nullStringAsEmpty;

            if (jsonProps.nullStringAsEmpty) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteNullStringAsEmpty);
            }

            if (jsonProps.nullBoolAsFalse) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteNullBooleanAsFalse);
            }

            if (jsonProps.nullNumberAsZero) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteNullNumberAsZero);
            }

            if (jsonProps.boolAsInt) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteBooleanAsNumber);
            }

            if (jsonProps.longAsString) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteLongAsString);
            }

            if (jsonProps.nullArrayAsEmpty) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteNullListAsEmpty);
            }

            if (jsonProps.enumAsName) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteEnumsUsingName);
            }

            if (writeNulls) {
                getSerializeConfig().addFeatures(JSONWriter.Feature.WriteNulls);
            }
        }
    }

    /**
     * Configuration holder for Fastjson2 context and features.
     * Supports both serialization (JSONWriter) and deserialization (JSONReader) contexts.
     *
     * @param <C> the context type (JSONWriter.Context or JSONReader.Context)
     * @param <F> the feature type (JSONWriter.Feature or JSONReader.Feature)
     */
    public static class Fastjson2Decl<C, F> {
        private final boolean forSerialize;
        private C context;

        /**
         * Creates a new configuration holder with the specified context.
         *
         * @param context the JSON context
         */
        public Fastjson2Decl(C context) {
            this.context = context;

            if (context instanceof JSONWriter.Context) {
                forSerialize = true;
            } else {
                forSerialize = false;
            }
        }

        /**
         * Gets the JSON context.
         *
         * @return the context
         */
        public C getContext() {
            return context;
        }

        /**
         * Sets the JSON context.
         *
         * @param context the new context (must not be null)
         * @throws IllegalArgumentException if context is null
         */
        public void setContext(C context) {
            Assert.notNull(context, "context can not be null");
            this.context = context;
        }

        /**
         * Sets features, replacing all existing features with the defaults plus the specified ones.
         *
         * @param features the features to set
         */
        public void setFeatures(F... features) {
            if (forSerialize) {
                ((JSONWriter.Context) context).setFeatures(JSONFactory.getDefaultWriterFeatures());
            } else {
                ((JSONReader.Context) context).setFeatures(JSONFactory.getDefaultReaderFeatures());
            }

            addFeatures(features);
        }

        /**
         * Adds features to the context without removing existing ones.
         *
         * @param features the features to add
         */
        public void addFeatures(F... features) {
            if (forSerialize) {
                //序列化
                for (F f1 : features) {
                    JSONWriter.Feature feature = (JSONWriter.Feature) f1;
                    ((JSONWriter.Context) context).config(feature, true);
                }
            } else {
                for (F f1 : features) {
                    JSONReader.Feature feature = (JSONReader.Feature) f1;
                    ((JSONReader.Context) context).config(feature, true);
                }
            }
        }

        /**
         * Removes features from the context.
         *
         * @param features the features to remove
         */
        public void removeFeatures(F... features) {
            if (forSerialize) {
                //序列化
                for (F f1 : features) {
                    JSONWriter.Feature feature = (JSONWriter.Feature) f1;
                    ((JSONWriter.Context) context).config(feature, false);
                }
            } else {
                for (F f1 : features) {
                    JSONReader.Feature feature = (JSONReader.Feature) f1;
                    ((JSONReader.Context) context).config(feature, false);
                }
            }
        }
    }
}
