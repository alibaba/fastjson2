package com.alibaba.fastjson2.spring;

import com.alibaba.fastjson2.support.spring.data.redis.GenericFastJsonRedisSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericFastJsonRedisSerializerTest2 {
    @Test
    public void test() {
        SecurityContextImpl context = new SecurityContextImpl();

        GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer(
                new String[] {
                        // 这里可以配置多个前缀白名单
                        "org.springframework.security.core.context.SecurityContextImpl"
                }
        );

        byte[] bytes = serializer.serialize(context);

        Object deserialized = serializer.deserialize(bytes);

        assertEquals(context.getClass(), deserialized.getClass());
    }
}
