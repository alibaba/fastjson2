package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Fastjson for Spring Data Redis Serializer(Generic implement).
 * <p>
 * Compatible fastjson 1.2.x
 *
 * @author lihengming
 * @author Victor.Zxy
 * @see RedisSerializer
 * @since 2.0.2
 */
public class GenericFastJsonRedisSerializer
        implements RedisSerializer<Object> {
    ContextAutoTypeBeforeHandler contextFilter;

    public GenericFastJsonRedisSerializer() {
        this.contextFilter = null;
    }

    public GenericFastJsonRedisSerializer(String[] acceptNames) {
        contextFilter = new ContextAutoTypeBeforeHandler(acceptNames);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object, SerializerFeature.WriteClassName);
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
            return JSON.parseObject(bytes, Object.class, contextFilter, Feature.SupportAutoType);
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
