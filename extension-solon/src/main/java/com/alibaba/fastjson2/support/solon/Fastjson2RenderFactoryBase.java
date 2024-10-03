package com.alibaba.fastjson2.support.solon;


import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.noear.solon.core.convert.Converter;
import org.noear.solon.serialization.JsonRenderFactory;

/**
 * Json 渲染器工厂基类
 *
 * @author noear
 * @author 暮城留风
 * @since 1.10
 * @since 2024-10-01
 */
public abstract class Fastjson2RenderFactoryBase implements JsonRenderFactory {
    protected Fastjson2StringSerializer serializer = new Fastjson2StringSerializer();

    /**
     * 获取序列化器
     */
    public Fastjson2StringSerializer getSerializer() {
        return serializer;
    }

    /**
     * 序列化配置
     */
    public ObjectWriterProvider config(){
        return serializer.getSerializeConfig().getProvider();
    }

    /**
     * 添加编码器
     *
     * @param clz     类型
     * @param encoder 编码器
     */
    public <T> void addEncoder(Class<T> clz, ObjectWriter encoder) {
        config().register(clz, encoder);
    }

    /**
     * 添加转换器（编码器的简化版）
     *
     * @param clz       类型
     * @param converter 转换器
     */
    @Override
    public <T> void addConvertor(Class<T> clz, Converter<T, Object> converter) {
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
}
