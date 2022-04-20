package com.alibaba.fastjson2.support.spring.data.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
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
public class GenericFastJsonRedisSerializer implements RedisSerializer<Object> {

    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    public GenericFastJsonRedisSerializer() {
        fastJsonConfig.setReaderFeatures(JSONReader.Feature.SupportAutoType);
        fastJsonConfig.setWriterFeatures(JSONWriter.Feature.WriteClassName);
    }

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(object, fastJsonConfig.getWriterFeatures());
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
            return JSON.parseObject(bytes, Object.class, fastJsonConfig.getReaderFeatures());
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
