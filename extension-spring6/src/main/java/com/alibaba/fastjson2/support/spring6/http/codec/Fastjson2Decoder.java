package com.alibaba.fastjson2.support.spring6.http.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Fastjson2 for Spring WebFlux.
 *
 * @author Xi.Liu
 * @see AbstractJackson2Decoder
 */
public class Fastjson2Decoder
        extends AbstractJackson2Decoder {
    private final FastJsonConfig config;

    public Fastjson2Decoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
        this.config = new FastJsonConfig();
    }

    public Fastjson2Decoder(ObjectMapper mapper, FastJsonConfig config, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
        this.config = config;
    }

    @NonNull
    @Override
    public Flux<Object> decode(@NonNull Publisher<DataBuffer> input,
                               @NonNull ResolvableType elementType,
                               MimeType mimeType,
                               Map<String, Object> hints) {
        throw new UnsupportedOperationException("Does not support stream decoding yet");
    }

    @Override
    public Object decode(@NonNull DataBuffer dataBuffer,
                         @NonNull ResolvableType targetType,
                         MimeType mimeType,
                         Map<String, Object> hints) throws DecodingException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                InputStream in = dataBuffer.asInputStream()) {
            byte[] buf = new byte[1 << 16];
            for (; ; ) {
                int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                if (len > 0) {
                    os.write(buf, 0, len);
                }
            }
            Object value = JSON.parseObject(os.toByteArray(),
                    targetType.getType(),
                    this.config.getDateFormat(),
                    this.config.getReaderFilters(),
                    this.config.getReaderFeatures());
            logValue(value, hints);
            return value;
        } catch (JSONException ex) {
            throw new DecodingException("JSON parse error: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new DecodingException("I/O error while reading input message", ex);
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private void logValue(@Nullable Object value, @Nullable Map<String, Object> hints) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Decoded [" + formatted + "]";
            });
        }
    }
}
