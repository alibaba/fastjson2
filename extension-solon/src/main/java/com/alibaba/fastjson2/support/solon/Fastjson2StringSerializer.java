package com.alibaba.fastjson2.support.solon;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.noear.solon.Utils;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.util.MimeType;
import org.noear.solon.lang.Nullable;
import org.noear.solon.serialization.ContextSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Fastjson2 string serialization
 *
 * @author noear
 * @author 暮城留风
 * @since 1.10
 * @since 2.8
 * @since 2024-10-01
 */
public class Fastjson2StringSerializer
        implements ContextSerializer<String> {
    private static final String label = "/json";

    private JSONWriter.Context serializeConfig;
    private JSONReader.Context deserializeConfig;

    /**
     * Get the serialization configuration
     */
    public JSONWriter.Context getSerializeConfig() {
        if (serializeConfig == null) {
            serializeConfig = new JSONWriter.Context(new ObjectWriterProvider());
        }

        return serializeConfig;
    }

    /**
     * Configure the serialization feature
     *
     * @param isReset Reset or not
     * @param isAdd Add or not
     * @param features Feature
     */
    public void cfgSerializeFeatures(boolean isReset, boolean isAdd, JSONWriter.Feature... features) {
        if (isReset) {
            getSerializeConfig().setFeatures(JSONFactory.getDefaultWriterFeatures());
        }

        for (JSONWriter.Feature feature : features) {
            getSerializeConfig().config(feature, isAdd);
        }
    }

    /**
     * Get the deserialized configuration
     */
    public JSONReader.Context getDeserializeConfig() {
        if (deserializeConfig == null) {
            deserializeConfig = new JSONReader.Context(new ObjectReaderProvider());
        }
        return deserializeConfig;
    }

    /**
     * Configure the deserialization feature
     *
     * @param isReset Reset or not
     * @param isAdd Add or not
     * @param features Feature
     */
    public void cfgDeserializeFeatures(boolean isReset, boolean isAdd, JSONReader.Feature... features) {
        if (isReset) {
            getDeserializeConfig().setFeatures(JSONFactory.getDefaultReaderFeatures());
        }

        for (JSONReader.Feature feature : features) {
            getDeserializeConfig().config(feature, isAdd);
        }
    }

    /**
     * Content type
     */
    @Override
    public String mimeType() {
        return "application/json";
    }

    /**
     * Data type
     */
    @Override
    public Class<String> dataType() {
        return String.class;
    }

    /**
     * Match or not
     *
     * @param ctx Handling context
     * @param mime content type
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
     * Serializer name
     */
    @Override
    public String name() {
        return "fastjson2-json";
    }

    /**
     * Serialize
     *
     * @param obj object
     */
    @Override
    public String serialize(Object obj) throws IOException {
        return JSON.toJSONString(obj, getSerializeConfig());
    }

    /**
     * Deserialize
     *
     * @param data data
     * @param toType Target type
     */
    @Override
    public Object deserialize(String data, Type toType) throws IOException {
        if (toType == null) {
            return JSON.parse(data, getDeserializeConfig());
        } else {
            return JSON.parseObject(data, toType, getDeserializeConfig());
        }
    }

    /**
     * Serialize the body
     *
     * @param ctx Handling context
     * @param data data
     */
    @Override
    public void serializeToBody(Context ctx, Object data) throws IOException {
        // If not set, use default // such as ndjson,sse or deliberately change mime (can be controlled externally)
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
     * Deserialize the body
     *
     * @param ctx Handling context
     */
    @Override
    public Object deserializeFromBody(Context ctx, @Nullable Type bodyType) throws IOException {
        String data = ctx.bodyNew();

        if (Utils.isNotEmpty(data)) {
            return JSON.parse(data, getDeserializeConfig());
        } else {
            return null;
        }
    }
}
