package com.alibaba.fastjson2.support.spring6.data.redis;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Fastjson2 serializer for Spring Data Redis (Spring 6 / Spring Boot 3).
 * Provides type-safe JSON serialization and deserialization for Redis values.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * RedisTemplate<String, User> template = new RedisTemplate<>();
 * template.setValueSerializer(new FastJsonRedisSerializer<>(User.class));
 * template.setConnectionFactory(connectionFactory);
 * template.afterPropertiesSet();
 * }</pre>
 *
 * @param <T> the type of objects to serialize/deserialize
 * @author lihengming
 * @author Victor.Zxy
 * @see RedisSerializer
 * @since 2.0.23
 */
public class FastJsonRedisSerializer<T>
        implements RedisSerializer<T> {
    private FastJsonConfig config = new FastJsonConfig();
    private final Class<T> type;

    /**
     * Creates a new serializer for the specified type.
     *
     * @param type the class type to serialize/deserialize
     */
    public FastJsonRedisSerializer(Class<T> type) {
        this.type = type;
    }

    /**
     * Gets the current FastJsonConfig.
     *
     * @return the FastJsonConfig instance
     */
    public FastJsonConfig getFastJsonConfig() {
        return config;
    }

    /**
     * Sets the FastJsonConfig for this serializer.
     *
     * @param fastJsonConfig the configuration to use
     */
    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.config = fastJsonConfig;
    }

    /**
     * Serializes the given object to JSON bytes.
     *
     * @param t the object to serialize
     * @return the JSON bytes, or empty array if object is null
     * @throws SerializationException if serialization fails
     */
    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            if (config.isJSONB()) {
                return JSONB.toBytes(t, config.getSymbolTable(), config.getWriterFilters(), config.getWriterFeatures());
            } else {
                return JSON.toJSONBytes(t, config.getDateFormat(), config.getWriterFilters(), config.getWriterFeatures());
            }
        } catch (Exception ex) {
            throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
        }
    }

    /**
     * Deserializes JSON bytes to an object.
     *
     * @param bytes the JSON bytes to deserialize
     * @return the deserialized object, or null if bytes is null or empty
     * @throws SerializationException if deserialization fails
     */
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            if (config.isJSONB()) {
                return JSONB.parseObject(bytes, type, config.getSymbolTable(), config.getReaderFilters(), config.getReaderFeatures());
            } else {
                return JSON.parseObject(bytes, type, config.getDateFormat(), config.getReaderFilters(), config.getReaderFeatures());
            }
        } catch (Exception ex) {
            throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
        }
    }
}
