package com.alibaba.fastjson2.support.spring.data.redis;

/**
 * Fastjson(JSONB) for Spring Data Redis Serializer(Generic implement).
 *
 * @author Victor.Zxy
 * @see GenericFastJsonRedisSerializer
 * @since 2.0.3
 */
@Deprecated
public class GenericFastJsonJSONBRedisSerializer
        extends GenericFastJsonRedisSerializer {
    public GenericFastJsonJSONBRedisSerializer() {
        super(true);
    }
}
