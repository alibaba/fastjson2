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
 * Fastjson2 字符串序列化
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
     * 默认实例
     */
    public static Fastjson2StringSerializer getDefault() {
        return _default;
    }

    private Fastjson2Decl<JSONWriter.Context, JSONWriter.Feature> serializeConfig;
    private Fastjson2Decl<JSONReader.Context, JSONReader.Feature> deserializeConfig;

    public Fastjson2StringSerializer(JsonProps jsonProps) {
        loadJsonProps(jsonProps);
    }

    public Fastjson2StringSerializer() {

    }

    /**
     * 获取序列化配置
     */
    public Fastjson2Decl<JSONWriter.Context, JSONWriter.Feature> getSerializeConfig() {
        if (serializeConfig == null) {
            serializeConfig = new Fastjson2Decl<>(new JSONWriter.Context(new ObjectWriterProvider()));
        }

        return serializeConfig;
    }

    /**
     * 获取反序列化配置
     */
    public Fastjson2Decl<JSONReader.Context, JSONReader.Feature> getDeserializeConfig() {
        if (deserializeConfig == null) {
            deserializeConfig = new Fastjson2Decl<>(new JSONReader.Context(new ObjectReaderProvider()));
        }
        return deserializeConfig;
    }

    /**
     * 内容类型
     */
    @Override
    public String mimeType() {
        return "application/json";
    }

    /**
     * 数据类型
     *
     */
    @Override
    public Class<String> dataType() {
        return String.class;
    }

    /**
     * 是否匹配
     *
     * @param ctx 请求上下文
     * @param mime 内容类型
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
        return JSON.toJSONString(obj, getSerializeConfig().getContext());
    }

    /**
     * 反序列化
     *
     * @param data 数据
     * @param toType 目标类型
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
     * 序列化主体
     *
     * @param ctx 请求上下文
     * @param data 数据
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
     * 反序列化主体
     *
     * @param ctx 请求上下文
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
     * 添加编码器
     *
     * @param clz 类型
     * @param encoder 编码器
     */
    public <T> void addEncoder(Class<T> clz, ObjectWriter encoder) {
        getSerializeConfig().getContext().getProvider().register(clz, encoder);
    }

    /**
     * 添加转换器（编码器的简化版）
     *
     * @param clz 类型
     * @param converter 转换器
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

            //JsonPropsUtil.dateAsFormat(this, jsonProps);
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

    public static class Fastjson2Decl<C, F> {
        private final boolean forSerialize;
        private C context;

        public Fastjson2Decl(C context) {
            this.context = context;

            if (context instanceof JSONWriter.Context) {
                forSerialize = true;
            } else {
                forSerialize = false;
            }
        }

        /**
         * 获取配置
         */
        public C getContext() {
            return context;
        }

        /**
         * 重置配置
         */
        public void setContext(C context) {
            Assert.notNull(context, "context can not be null");
            this.context = context;
        }


        /**
         * 设置特性
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
         * 添加特性
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
         * 移除特性
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
