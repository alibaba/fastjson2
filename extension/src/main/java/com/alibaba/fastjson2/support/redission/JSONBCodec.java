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

/**
 * Fastjson2 JSONB codec for Redisson.
 * Provides JSONB (binary JSON format) serialization and deserialization for Redisson Redis client.
 * JSONB offers better performance and smaller payload size compared to text JSON.
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * Config config = new Config();
 * config.useSingleServer()
 *     .setAddress("redis://127.0.0.1:6379")
 *     .setCodec(new JSONBCodec(MyObject.class));
 * RedissonClient redisson = Redisson.create(config);
 * }</pre>
 *
 * @since 2.0.0
 */
public class JSONBCodec
        extends BaseCodec {
    final JSONBEncoder encoder;
    final JSONBDecoder decoder;

    /**
     * Creates a JSONB codec for the specified type with FieldBased feature.
     *
     * @param valueType the type to encode/decode
     */
    public JSONBCodec(Type valueType) {
        this(
                JSONFactory.createWriteContext(JSONWriter.Feature.FieldBased),
                JSONFactory.createReadContext(JSONReader.Feature.FieldBased),
                valueType);
    }

    /**
     * Creates a JSONB codec with automatic type support for polymorphic deserialization.
     * Enables WriteClassName and FieldBased features for secure polymorphic handling.
     *
     * @param autoTypes support for automatically typed class name prefixes
     * @since 2.0.50
     */
    public JSONBCodec(String... autoTypes) {
        this(
                JSONFactory.createWriteContext(JSONWriter.Feature.FieldBased, JSONWriter.Feature.WriteClassName),
                JSONFactory.createReadContext(
                        JSONReader.autoTypeFilter(autoTypes),
                        JSONReader.Feature.FieldBased),
                null);
    }

    /**
     * Creates a JSONB codec with custom writer and reader contexts.
     *
     * @param writerContext the context for JSONB writing
     * @param readerContext the context for JSONB reading
     */
    public JSONBCodec(JSONWriter.Context writerContext, JSONReader.Context readerContext) {
        this(writerContext, readerContext, null);
    }

    /**
     * Creates a JSONB codec with custom contexts and type.
     *
     * @param writerContext the context for JSONB writing
     * @param readerContext the context for JSONB reading
     * @param valueType the type to encode/decode
     */
    public JSONBCodec(JSONWriter.Context writerContext, JSONReader.Context readerContext, Type valueType) {
        this(new JSONBEncoder(writerContext), new JSONBDecoder(readerContext, valueType));
    }

    /**
     * Creates a JSONB codec with custom encoder and decoder.
     *
     * @param encoder the JSONB encoder
     * @param decoder the JSONB decoder
     */
    protected JSONBCodec(JSONBEncoder encoder, JSONBDecoder decoder) {
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

    static final class JSONBEncoder
            implements Encoder {
        final JSONWriter.Context context;

        public JSONBEncoder(JSONWriter.Context context) {
            this.context = context;
        }

        @Override
        public ByteBuf encode(Object object) throws IOException {
            try (JSONWriter writer = JSONWriter.ofJSONB(context)) {
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
                ByteBuf out = ByteBufAllocator.DEFAULT.buffer(size);
                ByteBufOutputStream baos = new ByteBufOutputStream(out);
                writer.flushTo(baos);
                return baos.buffer();
            } catch (NullPointerException | NumberFormatException e) {
                throw new JSONException("JSON#toJSONString cannot serialize '" + object + "'", e);
            }
        }
    }

    protected static final class JSONBDecoder
            implements Decoder<Object> {
        final JSONReader.Context context;
        final Type valueType;

        public JSONBDecoder(JSONReader.Context context, Type valueType) {
            this.context = context;
            this.valueType = valueType;
        }

        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            Object value;
            try (JSONReader jsonReader = JSONReader.ofJSONB(new ByteBufInputStream(buf), context)) {
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
