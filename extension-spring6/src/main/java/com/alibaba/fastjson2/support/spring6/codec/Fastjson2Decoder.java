package com.alibaba.fastjson2.support.spring6.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.AbstractDecoder;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * spring message codec decoder for Fastjson2.
 *
 * @author 张治保
 * @since 2025/8/6
 */
public final class Fastjson2Decoder
        extends AbstractDecoder<Object> {
    private static final int BUFFER_SIZE = 65536;

    private final FastJsonConfig config;

    public Fastjson2Decoder() {
        this(new FastJsonConfig(),
                MediaType.APPLICATION_JSON,
             new MediaType("application", "*+json"));
    }

    public Fastjson2Decoder(final FastJsonConfig config,
                            final MimeType... mimeTypes) {
        super(mimeTypes == null || mimeTypes.length == 0
                ? new MimeType[]{
                    MediaType.APPLICATION_JSON,
                    new MediaType("application", "*+json")}
                : mimeTypes);
        this.config = config;
    }

    @Override
    @NonNull
    public Flux<Object> decode(@NonNull final Publisher<DataBuffer> inputStream,
                               @NonNull final ResolvableType elementType,
                               @Nullable final MimeType mimeType,
                               @Nullable final Map<String, Object> hints) {
        return Flux.from(inputStream)
                .mapNotNull(dataBuffer ->
                    decode(dataBuffer, elementType, mimeType, hints));
    }

    @Override
    @Nullable
    public Object decode(@NonNull final DataBuffer buffer,
                         @NonNull final ResolvableType targetType,
                         final MimeType mimeType,
                         final Map<String, Object> hints)
            throws DecodingException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                InputStream in = buffer.asInputStream()) {
            byte[] buf = new byte[BUFFER_SIZE];
            for (;;) {
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
            throw new DecodingException("JSON parse error: " + ex.getMessage(),
                    ex);
        } catch (IOException ex) {
            throw new DecodingException(
                    "I/O error while reading input message", ex);
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    @Override
    @NonNull
    public Mono<Object> decodeToMono(
            @NonNull final Publisher<DataBuffer> inputStream,
            @NonNull final ResolvableType elementType,
            @Nullable final MimeType mimeType,
            @Nullable final Map<String, Object> hints) {
        return DataBufferUtils.join(inputStream)
                .flatMap(dataBuffer -> Mono.justOrEmpty(
                    decode(dataBuffer, elementType, mimeType, hints)));
    }

    private void logValue(@Nullable final Object value,
                          @Nullable final Map<String, Object> hints) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value,
                        !traceOn);
                return Hints.getLogPrefix(hints) + "Decoded [" + formatted
                        + "]";
            });
        }
    }
}
