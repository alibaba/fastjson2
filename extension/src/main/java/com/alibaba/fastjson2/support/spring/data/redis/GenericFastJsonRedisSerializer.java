package com.alibaba.fastjson2.support.spring.data.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.filter.Filter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Fastjson for Spring Data Redis Serializer(Generic implement).
 *
 * @author lihengming
 * @author Victor.Zxy
 * @see RedisSerializer
 * @since 2.0.2
 */
public class GenericFastJsonRedisSerializer
        implements RedisSerializer<Object> {
    private final FastJsonConfig config = new FastJsonConfig();
    private Filter contextFilter;

    public GenericFastJsonRedisSerializer() {
        config.setReaderFeatures(JSONReader.Feature.SupportAutoType);
        config.setWriterFeatures(JSONWriter.Feature.WriteClassName);
        this.contextFilter = null;
    }

    public GenericFastJsonRedisSerializer(String[] acceptNames) {
        config.setReaderFeatures(JSONReader.Feature.SupportAutoType);
        config.setWriterFeatures(JSONWriter.Feature.WriteClassName);
        contextFilter = new ContextAutoTypeBeforeHandler(acceptNames);
    }

    public GenericFastJsonRedisSerializer(boolean jsonb) {
        this();
        config.setJSONB(jsonb);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            if (config.isJSONB()) {
                return JSONB.toBytes(object, config.getWriterFeatures());
            } else {
                return JSON.toJSONBytes(object, config.getWriterFeatures());
            }
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            if (config.isJSONB()) {
                return JSONB.parseObject(bytes, Object.class, contextFilter, config.getReaderFeatures());
            } else {
                return JSON.parseObject(bytes, Object.class, contextFilter, config.getReaderFeatures());
            }
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
