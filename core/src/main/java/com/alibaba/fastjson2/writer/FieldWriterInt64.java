package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

class FieldWriterInt64<T>
        extends FieldWriter<T> {
    final boolean browserCompatible;
    final boolean toString;
    final ObjLongConsumer<JSONWriterUTF8> utf8Impl;
    final ObjLongConsumer<JSONWriterUTF16> utf16Impl;

    FieldWriterInt64(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, null, label, fieldClass, fieldClass, field, method, function);
        browserCompatible = (features & JSONWriter.Feature.BrowserCompatible.mask) != 0;
        toString = (features & WriteNonStringValueAsString.mask) != 0
                || "string".equals(format);
        if (toString) {
            utf8Impl = JSONWriterUTF8::writeString;
            utf16Impl = JSONWriterUTF16::writeString;
        } else if (format != null) {
            utf8Impl = (w, v) -> w.writeString(String.format(format, v));
            utf16Impl = (w, v) -> w.writeString(String.format(format, v));
        } else {
            utf8Impl = JSONWriterUTF8::writeInt64;
            utf16Impl = JSONWriterUTF16::writeInt64;
        }
    }

    @Override
    public final void writeInt64JSONB(JSONWriterJSONB jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }
        boolean writeAsString = (features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
        writeFieldName(jsonWriter);
        if (!writeAsString) {
            writeAsString = browserCompatible && !TypeUtils.isJavaScriptSupport(value) && !jsonWriter.jsonb;
        }
        if (writeAsString) {
            jsonWriter.writeString(Long.toString(value));
        } else {
            jsonWriter.writeInt64(value);
        }
    }

    @Override
    public final void writeInt64UTF8(JSONWriterUTF8 jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return;
        }
        jsonWriter.writeNameRaw(fieldNameUTF8(features));
        utf8Impl.accept(jsonWriter, value);
    }

    @Override
    public final void writeInt64UTF16(JSONWriterUTF16 jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0 && defaultValue == null) {
            return;
        }
        jsonWriter.writeNameRaw(fieldNameUTF16(features));
        utf16Impl.accept(jsonWriter, value);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Long value = (Long) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        jsonWriter.writeInt64(value);
    }
}
