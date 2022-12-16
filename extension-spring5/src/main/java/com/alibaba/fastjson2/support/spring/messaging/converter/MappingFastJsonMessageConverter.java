package com.alibaba.fastjson2.support.spring.messaging.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeTypeUtils;

import java.lang.reflect.Type;

/**
 * Fastjson for Spring Messaging Json Converter.
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
    private FastJsonConfig config = new FastJsonConfig();

    /**
     * default support application/json
     */
    public MappingFastJsonMessageConverter() {
        super(MimeTypeUtils.APPLICATION_JSON);
    }

    /**
     * @return the fastJsonConfig.
     */
    public FastJsonConfig getFastJsonConfig() {
        return config;
    }

    /**
     * @param fastJsonConfig the fastJsonConfig to set.
     */
    public void setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.config = fastJsonConfig;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        // parse byte[] or String payload to Java Object
        Object obj = null;
        Type type = getResolvedType(targetClass, conversionHint);
        Object payload = message.getPayload();

        if (payload instanceof byte[] && config.isJSONB()) {
            obj = JSONB.parseObject((byte[]) payload, type, config.getSymbolTable(), config.getReaderFilters(), config.getReaderFeatures());
        } else if (payload instanceof byte[] && !config.isJSONB()) {
            obj = JSON.parseObject((byte[]) payload, type, config.getDateFormat(), config.getReaderFilters(), config.getReaderFeatures());
        } else if (payload instanceof String && JSON.isValid((String) payload)) {
            obj = JSON.parseObject((String) payload, type, config.getDateFormat(), config.getReaderFilters(), config.getReaderFeatures());
        }
        return obj;
    }

    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        // encode payload to json string or byte[]
        Object obj = null;

        if (byte[].class == getSerializedPayloadClass()) {
            if (payload instanceof String && config.isJSONB()) {
                obj = JSONB.fromJSONString((String) payload);
            } else if (payload instanceof String && !config.isJSONB()) {
                obj = ((String) payload).getBytes(config.getCharset());
            } else if (config.isJSONB()) {
                obj = JSONB.toBytes(payload, config.getSymbolTable(), config.getWriterFilters(), config.getWriterFeatures());
            } else {
                obj = JSON.toJSONBytes(payload, config.getDateFormat(), config.getWriterFilters(), config.getWriterFeatures());
            }
        } else if (String.class == getSerializedPayloadClass()) {
            if (payload instanceof String && JSON.isValid((String) payload)) {
                obj = payload;
            } else {
                obj = JSON.toJSONString(payload, config.getDateFormat(), config.getWriterFilters(), config.getWriterFeatures());
            }
        }
        return obj;
    }

    static Type getResolvedType(Class<?> targetClass, @Nullable Object conversionHint) {
        if (conversionHint instanceof MethodParameter) {
            MethodParameter param = (MethodParameter) conversionHint;
            param = param.nestedIfOptional();
            if (Message.class.isAssignableFrom(param.getParameterType())) {
                param = param.nested();
            }
            Type genericParameterType = param.getNestedGenericParameterType();
            Class<?> contextClass = param.getContainingClass();
            return GenericTypeResolver.resolveType(genericParameterType, contextClass);
        }
        return targetClass;
    }
}
