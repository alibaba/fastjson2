package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.zip.GZIPOutputStream;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteByteArrayAsBase64;

final class ObjectWriterImplInt8ValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt8ValueArray INSTANCE = new ObjectWriterImplInt8ValueArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[B");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[B");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        byte[] bytes = (byte[]) object;
        jsonWriter.writeBinary(bytes);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeArrayNull();
            return;
        }

        byte[] array = (byte[]) object;

        String format = jsonWriter.context.getDateFormat();
        if ("millis".equals(format)) {
            format = null;
        }

        if ("gzip".equals(format) || "gzip,base64".equals(format)) {
            GZIPOutputStream gzipOut = null;
            try {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                if (array.length < 512) {
                    gzipOut = new GZIPOutputStream(byteOut, array.length);
                } else {
                    gzipOut = new GZIPOutputStream(byteOut);
                }
                gzipOut.write(array);
                gzipOut.finish();
                array = byteOut.toByteArray();
            } catch (IOException ex) {
                throw new JSONException("write gzipBytes error", ex);
            } finally {
                IOUtils.close(gzipOut);
            }
        }

        if ("base64".equals(format)
                || "gzip,base64".equals(format)
                || (format == null && (jsonWriter.getFeatures(features) & WriteByteArrayAsBase64.mask) != 0)) {
            jsonWriter.writeBase64(array);
            return;
        }

        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }
            jsonWriter.writeInt32(array[i]);
        }
        jsonWriter.endArray();
    }
}
