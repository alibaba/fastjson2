package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

final class FieldWriterInt32ValUF<T>
        extends FieldWriterInt32Val<T> {
    final long fieldOffset;

    FieldWriterInt32ValUF(String name, int ordinal, long features, String format, String label, Field field) {
        super(name, ordinal, features, format, label, field);
        fieldOffset = UnsafeUtils.objectFieldOffset(field);
    }

    @Override
    public Object getFieldValue(T object) {
        return UNSAFE.getInt(object, fieldOffset);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        int value = UNSAFE.getInt(object, fieldOffset);
        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        int value = UNSAFE.getInt(object, fieldOffset);
        jsonWriter.writeInt32(value);
    }
}
