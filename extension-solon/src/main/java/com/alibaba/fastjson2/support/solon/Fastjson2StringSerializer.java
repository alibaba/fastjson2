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
import org.noear.solon.serialization.ContextSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Fastjson2 字符串序列化
 *
 * @author noear
 * @author 暮城留风
 * @since 1.10
 * @since 2.8
 * @since 2024-10-01
 */
public class Fastjson2StringSerializer implements ContextSerializer<String> {
    private static final String label = "/json";

    private JSONWriter.Context serializeConfig;
    private JSONReader.Context deserializeConfig;

    /**
     * 获取序列化配置
     */
    public JSONWriter.Context getSerializeConfig() {
        if (serializeConfig == null) {
            serializeConfig = new JSONWriter.Context(new ObjectWriterProvider());
        }

        return serializeConfig;
    }

    /**
     * 配置序列化特性
     *
     * @param isReset  是否重置
     * @param isAdd    是否添加
     * @param features 特性
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
     * 获取反序列化配置
     */
    public JSONReader.Context getDeserializeConfig() {
        if (deserializeConfig == null) {
            deserializeConfig = new JSONReader.Context(new ObjectReaderProvider());
        }
        return deserializeConfig;
    }

    /**
     * 配置反序列化特性
     *
     * @param isReset  是否重置
     * @param isAdd    是否添加
     * @param features 特性
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
     * 获取内容类型
     */
    @Override
    public String getContentType() {
        return "application/json";
    }

    /**
     * 是否匹配
     *
     * @param ctx  请求上下文
     * @param mime 内容类型
     */
    @Override
    public boolean matched(Context ctx, String mime) {
        if (mime == null) {
            return false;
        } else {
            return mime.contains(label);
        }
    }

    /**
     * 序列化器名字
     */
    @Override
    public String name() {
        return "fastjson2-json";
    }

    /**
     * 序列化
     *
     * @param obj 对象
     */
    @Override
    public String serialize(Object obj) throws IOException {
        return JSON.toJSONString(obj, getSerializeConfig());
    }

    /**
     * 反序列化
     *
     * @param data   数据
     * @param toType 目标类型
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
     * 序列化主体
     *
     * @param ctx  请求上下文
     * @param data 数据
     */
    @Override
    public void serializeToBody(Context ctx, Object data) throws IOException {
        ctx.contentType(getContentType());

        if (data instanceof ModelAndView) {
            ctx.output(serialize(((ModelAndView) data).model()));
        } else {
            ctx.output(serialize(data));
        }
    }

    /**
     * 反序列化主体
     *
     * @param ctx 请求上下文
     */
    @Override
    public Object deserializeFromBody(Context ctx) throws IOException {
        String data = ctx.bodyNew();

        if (Utils.isNotEmpty(data)) {
            return JSON.parse(data, getDeserializeConfig());
        } else {
            return null;
        }
    }
}
