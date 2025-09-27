package com.alibaba.fastjson2.support.spring.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractEncoder;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * spring message codec encoder for Fastjson2.
 *
 * @author 张治保
 * @since 2025/8/6
 */

public final class Fastjson2Encoder
        extends AbstractEncoder<Object> {
    private final FastJsonConfig config;

    /**
     * default constructor
     */
    public Fastjson2Encoder() {
        this(new FastJsonConfig(), MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    /**
     * Constructor with custom configs
     *
     * @param config    FastJsonConfig
     * @param mimeTypes the mime types to support
     */
    public Fastjson2Encoder(FastJsonConfig config, MimeType... mimeTypes) {
        super(mimeTypes == null || mimeTypes.length == 0
                ? new MimeType[]{MediaType.APPLICATION_JSON, new MediaType("application", "*+json")}
                : mimeTypes);
        this.config = config;
    }

    @Override
    @NonNull
    public Flux<DataBuffer> encode(@NonNull Publisher<?> inputStream, @NonNull DataBufferFactory bufferFactory, @NonNull ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        return Flux.from(inputStream)
                .map(val -> encodeValue(val, bufferFactory, elementType, mimeType, hints));
    }

    @Override
    @NonNull
    public DataBuffer encodeValue(@Nullable Object value, @NonNull DataBufferFactory bufferFactory, @NonNull ResolvableType valueType, MimeType mimeType, Map<String, Object> hints) {
        byte[] bytes = null;
        // Check if the value is a valid JSON string or byte array
        if (value instanceof String) {
            String str = (String) value;
            if (JSON.isValidObject(str)) {
                bytes = str.getBytes(config.getCharset());
            }
        } else if (value instanceof byte[]) {
            byte[] curBytes = (byte[]) value;
            if (JSON.isValid(curBytes)) {
                bytes = (byte[]) value;
            }
        }
        // If bytes is not null, write it to a DataBuffer
        if (bytes != null) {
            int length = bytes.length;
            DataBuffer buffer = bufferFactory.allocateBuffer(length)
                    .write(bytes, 0, length);
            Hints.touchDataBuffer(buffer, hints, logger);
            return buffer;
        }
        //@see JSON.writeTo
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            JSON.writeTo(
                    baos,
                    value,
                    config.getDateFormat(),
                    config.getWriterFilters(),
                    config.getWriterFeatures()
            );
            DataBuffer buffer = bufferFactory.allocateBuffer(baos.size());
            baos.writeTo(buffer.asOutputStream());
            Hints.touchDataBuffer(buffer, hints, logger);
            return buffer;
        } catch (IOException e) {
            throw new EncodingException("JSON#writeTo cannot serialize '" + value + "' to 'OutputStream'", e);
        }
    }
}
