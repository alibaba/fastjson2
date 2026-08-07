package com.alibaba.fastjson2.support.airlift;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.IOUtils;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import java.util.List;
import java.util.Map;

public class SliceValueConsumer
        implements ValueConsumer {
    public Slice slice;

    @Override
    public void accept(byte[] bytes, int off, int len) {
        slice = Slices.wrappedBuffer(bytes, off, len);
    }

    @Override
    public void acceptNull() {
        slice = null;
    }

    @Override
    public void accept(int value) {
        int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
        byte[] bytes = new byte[size];
        IOUtils.getChars(value, bytes.length, bytes);
        slice = Slices.wrappedBuffer(bytes);
    }

    @Override
    public void accept(boolean val) {
        byte[] bytes = val
                ? new byte[]{'t', 'r', 'u', 'e'}
                : new byte[]{'f', 'a', 'l', 's', 'e'};
        slice = Slices.wrappedBuffer(bytes);
    }

    @Override
    public void accept(long value) {
        int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
        byte[] bytes = new byte[size];
        IOUtils.getChars(value, bytes.length, bytes);
        slice = Slices.wrappedBuffer(bytes);
    }

    @Override
    public void accept(Number val) {
        if (val == null) {
            slice = null;
            return;
        }

        if (val instanceof Long) {
            long value = val.longValue();
            int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
            byte[] bytes = new byte[size];
            IOUtils.getChars(value, bytes.length, bytes);
            slice = Slices.wrappedBuffer(bytes);
            return;
        }

        if (val instanceof Integer || val instanceof Short || val instanceof Byte) {
            int value = val.intValue();
            int size = (value < 0) ? IOUtils.stringSize(-value) + 1 : IOUtils.stringSize(value);
            byte[] bytes = new byte[size];
            IOUtils.getChars(value, bytes.length, bytes);
            slice = Slices.wrappedBuffer(bytes);
            return;
        }

        String str = val.toString();
        slice = Slices.utf8Slice(str);
    }

    @Override
    public void accept(String val) {
        slice = Slices.utf8Slice(val);
    }

    @Override
    public void accept(Map object) {
        if (object.isEmpty()) {
            slice = Slices.wrappedBuffer(new byte[]{'{', '}'});
            return;
        }

        try (JSONWriter jsonWriter = JSONWriter.ofUTF8()) {
            jsonWriter.write(object);
            byte[] bytes = jsonWriter.getBytes();
            slice = Slices.wrappedBuffer(bytes);
        }
    }

    @Override
    public void accept(List array) {
        if (array.isEmpty()) {
            slice = Slices.wrappedBuffer(new byte[]{'[', ']'});
            return;
        }

        try (JSONWriter jsonWriter = JSONWriter.ofUTF8()) {
            jsonWriter.write(array);
            byte[] bytes = jsonWriter.getBytes();
            slice = Slices.wrappedBuffer(bytes);
        }
    }
}
