package com.alibaba.fastjson2.support.spring.http.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.*;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.codec.json.AbstractJackson2Decoder;
import org.springframework.http.codec.json.AbstractJackson2Encoder;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.util.Map;

/**
 * Fastjson2 for Spring WebFlux.
 *
 * @author Xi.Liu
 * @see AbstractJackson2Decoder
 */
public class Fastjson2Encoder
        extends AbstractJackson2Encoder {
    private final FastJsonConfig config;

    public Fastjson2Encoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
        this.config = new FastJsonConfig();
    }

    public Fastjson2Encoder(ObjectMapper mapper, FastJsonConfig config, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
        this.config = config;
    }

    @NonNull
    @Override
    public DataBuffer encodeValue(@Nullable Object value,
                                  @NonNull DataBufferFactory bufferFactory,
                                  @NonNull ResolvableType valueType,
                                  MimeType mimeType,
                                  Map<String, Object> hints) {
        try {
            logValue(hints, value);
            if (value instanceof String && JSON.isValidObject((String) value)) {
                byte[] strBytes = ((String) value).getBytes(this.config.getCharset());
                DataBuffer buffer = bufferFactory.allocateBuffer(strBytes.length);
                buffer.write(strBytes, 0, strBytes.length);
                Hints.touchDataBuffer(buffer, hints, logger);
                return buffer;
            }
            try (JSONWriter writer = JSONWriter.ofUTF8(this.config.getWriterFeatures())) {
                if (value == null) {
                    writer.writeNull();
                } else {
                    writer.setRootObject(value);
                    configFilter(writer.context, this.config.getWriterFilters());
                    Class<?> valueClass = value.getClass();
                    ObjectWriter<?> objectWriter = writer.getObjectWriter(valueClass, valueClass);
                    objectWriter.write(writer, value, null, null, 0);
                }
                DataBuffer buffer = bufferFactory.allocateBuffer(writer.size());
                writer.flushTo(buffer.asOutputStream());
                Hints.touchDataBuffer(buffer, hints, logger);
                return buffer;
            } catch (IOException e) {
                throw new JSONException("JSON#writeTo cannot serialize '" + value + "' to 'OutputStream'", e);
            }
        } catch (JSONException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

    private void configFilter(JSONWriter.Context context, Filter... filters) {
        if (filters == null || filters.length == 0) {
            return;
        }
        for (Filter filter : filters) {
            if (filter instanceof NameFilter) {
                NameFilter f = (NameFilter) filter;
                if (context.getNameFilter() == null) {
                    context.setNameFilter(f);
                } else {
                    context.setNameFilter(NameFilter.compose(context.getNameFilter(), f));
                }
            }

            if (filter instanceof ValueFilter) {
                ValueFilter f = (ValueFilter) filter;
                if (context.getValueFilter() == null) {
                    context.setValueFilter(f);
                } else {
                    context.setValueFilter(ValueFilter.compose(context.getValueFilter(), f));
                }
            }

            if (filter instanceof PropertyFilter) {
                PropertyFilter f = (PropertyFilter) filter;
                context.setPropertyFilter(f);
            }

            if (filter instanceof PropertyPreFilter) {
                PropertyPreFilter f = (PropertyPreFilter) filter;
                context.setPropertyPreFilter(f);
            }

            if (filter instanceof BeforeFilter) {
                BeforeFilter f = (BeforeFilter) filter;
                context.setBeforeFilter(f);
            }

            if (filter instanceof AfterFilter) {
                AfterFilter f = (AfterFilter) filter;
                context.setAfterFilter(f);
            }

            if (filter instanceof LabelFilter) {
                LabelFilter f = (LabelFilter) filter;
                context.setLabelFilter(f);
            }

            if (filter instanceof ContextValueFilter) {
                ContextValueFilter f = (ContextValueFilter) filter;
                context.setContextValueFilter(f);
            }

            if (filter instanceof ContextNameFilter) {
                ContextNameFilter f = (ContextNameFilter) filter;
                context.setContextNameFilter(f);
            }
        }
    }

    private void logValue(@Nullable Map<String, Object> hints, Object value) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Encoding [" + formatted + "]";
            });
        }
    }
}
