package com.alibaba.fastjson2.support.spring.messaging.converter;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.springframework.util.Assert;

/**
 * Fastjson(JSONB) for Spring Messaging Json Converter.
 *
 * @author Victor.Zxy
 * @see MappingFastJsonMessageConverter
 * @since 2.0.5
 */
@Deprecated
public class MappingFastJsonJSONBMessageConverter
        extends MappingFastJsonMessageConverter {
    public MappingFastJsonJSONBMessageConverter() {
        super.getFastJsonConfig().setJSONB(true);
    }

    @Override
    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        fastJsonConfig.setJSONB(true);
        super.setFastJsonConfig(fastJsonConfig);
    }

    @Override
    public void setSerializedPayloadClass(Class<?> payloadClass) {
        Assert.isTrue(byte[].class == payloadClass,
                () -> "Payload class must be byte[] : " + payloadClass);
    }
}
