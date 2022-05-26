package com.alibaba.fastjson2.support.retrofit;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Fastjson for Retrofit Converter Json Factory.
 *
 * @author ligboy, wenshao
 * @author Victor.Zxy
 * @see Converter.Factory
 * @since 2.0.2
 */
public class Retrofit2ConverterFactory
        extends Converter.Factory {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    private FastJsonConfig config;

    public Retrofit2ConverterFactory() {
        this.config = new FastJsonConfig();
    }

    public Retrofit2ConverterFactory(FastJsonConfig fastJsonConfig) {
        this.config = fastJsonConfig;
    }

    public static Retrofit2ConverterFactory create() {
        return create(new FastJsonConfig());
    }

    public static Retrofit2ConverterFactory create(FastJsonConfig fastJsonConfig) {
        if (fastJsonConfig == null) {
            throw new NullPointerException("fastJsonConfig == null");
        }
        return new Retrofit2ConverterFactory(fastJsonConfig);
    }

    @Override
    public Converter<ResponseBody, Object> responseBodyConverter(Type type, //
                                                                 Annotation[] annotations, //
                                                                 Retrofit retrofit) {
        return new ResponseBodyConverter<Object>(type);
    }

    @Override
    public Converter<Object, RequestBody> requestBodyConverter(Type type, //
                                                               Annotation[] parameterAnnotations, //
                                                               Annotation[] methodAnnotations, //
                                                               Retrofit retrofit) {
        return new RequestBodyConverter<Object>();
    }

    public FastJsonConfig getFastJsonConfig() {
        return config;
    }

    public Retrofit2ConverterFactory setFastJsonConfig(FastJsonConfig fastJsonConfig) {
        this.config = fastJsonConfig;
        return this;
    }

    final class ResponseBodyConverter<T>
            implements Converter<ResponseBody, T> {
        private Type type;

        ResponseBodyConverter(Type type) {
            this.type = type;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            try {
                if (config.isJSONB()) {
                    return JSONB.parseObject(value.bytes(), type, config.getSymbolTable(), config.getReaderFilters(), config.getReaderFeatures());
                } else {
                    return JSON.parseObject(value.bytes(), type, config.getDateFormat(), config.getReaderFilters(), config.getReaderFeatures());
                }
            } catch (Exception e) {
                throw new IOException("JSON parse error: " + e.getMessage(), e);
            } finally {
                value.close();
            }
        }
    }

    final class RequestBodyConverter<T>
            implements Converter<T, RequestBody> {
        RequestBodyConverter() {
        }

        @Override
        public RequestBody convert(T value) throws IOException {
            try {
                byte[] content;
                if (config.isJSONB()) {
                    content = JSONB.toBytes(value, config.getSymbolTable(), config.getWriterFilters(), config.getWriterFeatures());
                } else {
                    content = JSON.toJSONBytes(value, config.getDateFormat(), config.getWriterFilters(), config.getWriterFeatures());
                }
                return RequestBody.create(MEDIA_TYPE, content);
            } catch (Exception e) {
                throw new IOException("Could not write JSON: " + e.getMessage(), e);
            }
        }
    }
}
