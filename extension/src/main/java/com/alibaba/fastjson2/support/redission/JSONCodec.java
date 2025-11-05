package com.alibaba.fastjson2.support.redission;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.writer.ObjectWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Fastjson2 codec for Redisson.
 * Provides JSON serialization and deserialization for Redisson Redis client.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * Config config = new Config();
 * config.useSingleServer()
 *     .setAddress("redis://127.0.0.1:6379")
 *     .setCodec(new JSONCodec(MyObject.class));
 * RedissonClient redisson = Redisson.create(config);
 * }</pre>
 *
 * @since 2.0.0
 */
public class JSONCodec
        extends BaseCodec {
    final JSONEncoder encoder;
    final JSONDecoder decoder;

    /**
     * Creates a JSON codec for the specified type with UTF-8 charset.
     *
     * @param type the type to encode/decode
     */
    public JSONCodec(Type type) {
        this(JSONFactory.createWriteContext(), JSONFactory.createReadContext(), type, StandardCharsets.UTF_8);
    }

    /**
     * Creates a JSON codec for the specified type and charset.
     *
     * @param type the type to encode/decode
     * @param charset the charset to use for encoding/decoding
     */
    public JSONCodec(Type type, Charset charset) {
        this(JSONFactory.createWriteContext(), JSONFactory.createReadContext(), type, charset);
    }

    /**
     * Creates a JSON codec with custom writer and reader contexts.
     *
     * @param writerContext the context for JSON writing
     * @param readerContext the context for JSON reading
     */
    public JSONCodec(JSONWriter.Context writerContext, JSONReader.Context readerContext) {
        this(writerContext, readerContext, null, StandardCharsets.UTF_8);
    }

    /**
     * Creates a JSON codec with custom writer and reader contexts and charset.
     *
     * @param writerContext the context for JSON writing
     * @param readerContext the context for JSON reading
     * @param charset the charset to use for encoding/decoding
     */
    public JSONCodec(JSONWriter.Context writerContext, JSONReader.Context readerContext, Charset charset) {
        this(writerContext, readerContext, null, charset);
    }

    /**
     * Creates a JSON codec with custom contexts, type, and charset.
     *
     * @param writerContext the context for JSON writing
     * @param readerContext the context for JSON reading
     * @param valueType the type to encode/decode
     * @param charset the charset to use for encoding/decoding
     */
    public JSONCodec(JSONWriter.Context writerContext, JSONReader.Context readerContext, Type valueType, Charset charset) {
        this(new JSONEncoder(writerContext, charset), new JSONDecoder(valueType, charset, readerContext));
    }

    JSONCodec(JSONEncoder encoder, JSONDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Gets the decoder for deserializing values.
     *
     * @return the value decoder
     */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    /**
     * Gets the encoder for serializing values.
     *
     * @return the value encoder
     */
    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    static final class JSONEncoder
            implements Encoder {
        final JSONWriter.Context context;
        final Charset charset;

        public JSONEncoder() {
            this(new JSONWriter.Context());
        }

        public JSONEncoder(JSONWriter.Context context) {
            this(context, StandardCharsets.UTF_8);
        }

        public JSONEncoder(JSONWriter.Context context, Charset charset) {
            this.context = context;
            this.charset = charset;
        }

        @Override
        public ByteBuf encode(Object object) throws IOException {
            try (JSONWriter writer = JSONWriter.of(context)) {
                if (object == null) {
                    writer.writeNull();
                } else {
                    writer.setRootObject(object);

                    Class<?> valueClass = object.getClass();
                    if (valueClass == JSONObject.class && context.getFeatures() == 0) {
                        writer.write((JSONObject) object);
                    } else {
                        ObjectWriter objectWriter = context.getObjectWriter(valueClass, valueClass);
                        objectWriter.write(writer, object, null, null, 0);
                    }
                }

                int size = writer.size();
                if (!context.isEnabled(JSONWriter.Feature.OptimizedForAscii)) {
                    size <<= 2;
                }
                ByteBuf out = ByteBufAllocator.DEFAULT.buffer(size);
                ByteBufOutputStream baos = new ByteBufOutputStream(out);
                writer.flushTo(baos, charset);
                return baos.buffer();
            } catch (NullPointerException | NumberFormatException e) {
                throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
            }
        }
    }

    static final class JSONDecoder
            implements Decoder<Object> {
        final Type valueType;
        final Charset charset;
        final JSONReader.Context context;

        public JSONDecoder(Type valueType) {
            this(valueType, JSONFactory.createReadContext());
        }

        public JSONDecoder(Type valueType, JSONReader.Context context) {
            this(valueType, StandardCharsets.UTF_8, context);
        }

        public JSONDecoder(Type valueType, Charset charset, JSONReader.Context context) {
            this.valueType = valueType;
            this.charset = charset;
            this.context = context;
        }

        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            Object value;
            try (JSONReader jsonReader = JSONReader.of(new ByteBufInputStream(buf), charset, context)) {
                if (valueType == null || valueType == Object.class) {
                    value = jsonReader.readAny();
                } else {
                    value = jsonReader.read(valueType);
                }
            }
            return value;
        }
    }
}
