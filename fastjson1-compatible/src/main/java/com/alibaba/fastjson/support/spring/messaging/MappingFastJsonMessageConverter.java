package com.alibaba.fastjson.support.spring.messaging;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

import java.nio.charset.StandardCharsets;

/**
 * Fastjson for Spring Messaging Json Converter.
 * <p>
 * Compatible fastjson 1.2.x
 *
 * @author KimmKing
 * @author Victor.Zxy
 * @see AbstractMessageConverter
 * @since 2.0.2
 */
public class MappingFastJsonMessageConverter
        extends AbstractMessageConverter {
    /**
     * with fastJson config
     */
    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    public MappingFastJsonMessageConverter() {
        super(new MimeType("application", "json", StandardCharsets.UTF_8));
    }

    /**
     * @return the fastJsonConfig.
     */
    public FastJsonConfig getFastJsonConfig() {
        return fastJsonConfig;
    }

    /**
     * @param fastJsonConfig the fastJsonConfig to set.
     */
    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.fastJsonConfig = fastJsonConfig;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected boolean canConvertFrom(Message<?> message, Class<?> targetClass) {
        return supports(targetClass);
    }

    @Override
    protected boolean canConvertTo(Object payload, MessageHeaders headers) {
        return supports(payload.getClass());
    }

    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        // parse byte[] or String payload to Java Object
        Object payload = message.getPayload();
        Object obj = null;
        if (payload instanceof byte[]) {
            obj = JSON.parseObject((byte[]) payload, targetClass, fastJsonConfig.getFeatures());
        } else if (payload instanceof String) {
            obj = JSON.parseObject((String) payload, targetClass, fastJsonConfig.getFeatures());
        }

        return obj;
    }

    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        // encode payload to json string or byte[]
        Object obj;
        if (byte[].class == getSerializedPayloadClass()) {
            if (payload instanceof String && JSON.isValid((String) payload)) {
                obj = ((String) payload).getBytes(fastJsonConfig.getCharset());
            } else {
                obj = JSON.toJSONBytes(payload, fastJsonConfig.getSerializeFilters(), fastJsonConfig.getSerializerFeatures());
            }
        } else {
            if (payload instanceof String && JSON.isValid((String) payload)) {
                obj = payload;
            } else {
                obj = JSON.toJSONString(payload, fastJsonConfig.getSerializeFilters(), fastJsonConfig.getSerializerFeatures());
            }
        }

        return obj;
    }
}
