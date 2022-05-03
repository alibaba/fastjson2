package com.alibaba.fastjson2.support.spring.data.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Fastjson for Spring Data Redis Serializer.
 *
 * @author lihengming
 * @author Victor.Zxy
 * @see RedisSerializer
 * @since 2.0.2
 */
public class FastJsonRedisSerializer<T> implements RedisSerializer<T> {

    private FastJsonConfig fastJsonConfig = new FastJsonConfig();
    private Class<T> type;

    public FastJsonRedisSerializer(Class<T> type) {
        this.type = type;
    }

    public FastJsonConfig getFastJsonConfig() {
        return fastJsonConfig;
    }

    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            return JSON.toJSONBytes(t, fastJsonConfig.getDateFormat(),
                    fastJsonConfig.getWriterFilters(), fastJsonConfig.getWriterFeatures());

        } catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return (T) JSON.parseObject(bytes, type,
                    fastJsonConfig.getDateFormat(), fastJsonConfig.getReaderFeatures());

        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
