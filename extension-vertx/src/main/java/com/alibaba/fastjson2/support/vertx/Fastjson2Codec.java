package com.alibaba.fastjson2.support.vertx;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.json.JsonCodec;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Fastjson2Codec implements JsonCodec {
    private final JSONWriter.Context writerContext;
    private final JSONWriter.Context prettyWriterContext;
    private final JSONReader.Context readerContext;

    public Fastjson2Codec() {
        ObjectWriterProvider writerProvider = new ObjectWriterProvider();
        writerProvider.register(JsonObject.class, JsonObjectWriter.INSTANCE);
        writerProvider.register(JsonArray.class, JsonArrayWriter.INSTANCE);
        writerProvider.register(Buffer.class, BufferWriter.INSTANCE);
        writerProvider.register(byte[].class, ByteArrayWriter.INSTANCE);
        writerProvider.register(Instant.class, InstantWriter.INSTANCE);

        this.writerContext = new JSONWriter.Context(
                writerProvider,
                JSONWriter.Feature.WriteNulls, // 对齐 Jackson 默认输出 null
                JSONWriter.Feature.WriteByteArrayAsBase64
        );
        this.prettyWriterContext = new JSONWriter.Context(
                writerProvider,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.WriteByteArrayAsBase64,
                JSONWriter.Feature.PrettyFormat
        );

        ObjectReaderProvider readerProvider = new ObjectReaderProvider();
        readerProvider.register(JsonObject.class, JsonObjectReader.INSTANCE);
        readerProvider.register(JsonArray.class, JsonArrayReader.INSTANCE);
        readerProvider.register(Buffer.class, BufferReader.INSTANCE);
        readerProvider.register(byte[].class, ByteArrayReader.INSTANCE);
        readerProvider.register(Instant.class, InstantReader.INSTANCE);

        this.readerContext = new JSONReader.Context(
                readerProvider,
                JSONReader.Feature.UseNativeObject, // 对齐 Jackson，当目标类型为 Object.class 时，返回标准 JDK Map/List
                JSONReader.Feature.Base64StringAsByteArray
        );
    }

    @Override
    public <T> T fromString(String str, Class<T> clazz) throws DecodeException {
        try {
            T value = JSON.parseObject(str, clazz, readerContext);
            return clazz == Object.class ? (T) adapt(value) : value;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    public <T> T fromString(String str, TypeReference<T> type) throws DecodeException {
        try {
            T value = JSON.parseObject(str, type.getType(), readerContext);
            return type.getType() == Object.class ? (T) adapt(value) : value;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T fromBuffer(Buffer buf, Class<T> clazz) throws DecodeException {
        return parseFromBuffer(buf, clazz);
    }

    public <T> T fromBuffer(Buffer buf, TypeReference<T> type) throws DecodeException {
        return parseFromBuffer(buf, type.getType());
    }

    private <T> T parseFromBuffer(Buffer buf, Type type) throws DecodeException {
        try {
            ByteBuf byteBuf = ((BufferInternal) buf).getByteBuf();
            T value;

            if (byteBuf.hasArray()) {
                // 堆内内存：直接获取底层 byte[]，不拷贝
                int offset = byteBuf.arrayOffset() + byteBuf.readerIndex();
                int length = byteBuf.readableBytes();
                try (JSONReader reader = JSONReader.of(byteBuf.array(), offset, length, readerContext)) {
                    value = reader.read(type);
                }
            } else if (byteBuf.nioBufferCount() == 1) {
                // 堆外内存：转换为 NIO ByteBuffer 零拷贝解析
                ByteBuffer nioBuffer = byteBuf.nioBuffer(byteBuf.readerIndex(), byteBuf.readableBytes());
                try (JSONReader reader = JSONReader.of(nioBuffer, StandardCharsets.UTF_8, readerContext)) {
                    value = reader.read(type);
                }
            } else {
                // 不连续内存：使用 InputStream 流式解析
                try (InputStream is = new ByteBufInputStream(byteBuf);
                     JSONReader reader = JSONReader.of(is, StandardCharsets.UTF_8, readerContext)) {
                    value = reader.read(type);
                }
            }

            return type == Object.class ? (T) adapt(value) : value;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T fromValue(Object json, Class<T> clazz) throws DecodeException {
        try {
            T value = TypeUtils.cast(json, clazz);
            return clazz == Object.class ? (T) adapt(value) : value;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    public <T> T fromValue(Object json, TypeReference<T> type) throws DecodeException {
        try {
            T value = TypeUtils.cast(json, type.getType());
            return type.getType() == Object.class ? (T) adapt(value) : value;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString(Object object, boolean pretty) throws EncodeException {
        try {
            return pretty ?
                    JSON.toJSONString(object, prettyWriterContext) :
                    JSON.toJSONString(object, writerContext);
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Buffer toBuffer(Object object, boolean pretty) throws EncodeException {
        try {
            Buffer buffer = Buffer.buffer();
            ByteBuf byteBuf = ((BufferInternal) buffer).getByteBuf();

            try (ByteBufOutputStream os = new ByteBufOutputStream(byteBuf);
                 JSONWriter writer = JSONWriter.ofUTF8(pretty ? prettyWriterContext : writerContext)) {
                writer.writeAny(object);
                writer.flushTo(os);
            }
            return buffer;
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object adapt(Object o) {
        try {
            if (o == null) {
                return null;
            }

            if (o instanceof Map) {
                return new JsonObject((Map<String, Object>) o);
            } else if (o instanceof List) {
                return new JsonArray((List<Object>) o);
            }
            return o;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage(), e);
        }
    }
}
