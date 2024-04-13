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

public class JSONBCodec
        extends BaseCodec {
    final JSONEncoder encoder;
    final JSONDecoder decoder;

    public JSONBCodec(Type valueType) {
        this(JSONFactory.createWriteContext(), JSONFactory.createReadContext(), valueType);
    }

    public JSONBCodec(JSONWriter.Context writerContext, JSONReader.Context readerContext) {
        this(writerContext, readerContext, null);
    }

    public JSONBCodec(JSONWriter.Context writerContext, JSONReader.Context readerContext, Type valueType) {
        this(new JSONEncoder(writerContext), new JSONDecoder(readerContext, valueType));
    }

    public JSONBCodec(JSONEncoder encoder, JSONDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    public static class JSONEncoder
            implements Encoder {
        final JSONWriter.Context context;

        public JSONEncoder(JSONWriter.Context context) {
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

    public static class JSONDecoder
            implements Decoder<Object> {
        final JSONReader.Context context;
        final Type valueType;

        public JSONDecoder(JSONReader.Context context, Type valueType) {
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
