package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class JSONExtract {
    private static JSONWritable[] cache = new JSONWritable[512];

    static {
        for (int i = -1; i < 511; ++i) {
            int size = (i < 0) ? IOUtils.stringSize(-i) + 1 : IOUtils.stringSize(i);
            byte[] bytes = new byte[size + 2];
            bytes[0] = '"';
            bytes[size + 1] = '"';
            IOUtils.getChars(i, size + 1, bytes);
            cache[i + 1] = new JSONWritable(bytes);
        }
    }

    static final byte[] BYTES_TRUE = new byte[]{'"', 't', 'r', 'u', 'e', '"'};
    static final byte[] BYTES_FALSE = new byte[]{'"', 'f', 'a', 'l', 's', 'e', '"'};

    private final JSONPath path;
    private JSONWritable text = new JSONWritable();

    private ExtractValueConsumer valueConsumer = new ExtractValueConsumer();

    public JSONExtract(String path) {
        this.path = JSONPath.of(path);
    }

    public JSONWritable eval(byte[] input) {
        JSONReader parser = JSONReader.of(input,
                0,
                input.length, StandardCharsets.UTF_8
        );

        path.extract(parser, valueConsumer);
        return text;
    }

    class ExtractValueConsumer
            implements ValueConsumer {
        @Override
        public void accept(byte[] bytes, int off, int len) {
            text.bytes = bytes;
            text.off = off;
            text.length = len;
        }

        @Override
        public void acceptNull() {
            text = null;
        }

        @Override
        public void accept(boolean val) {
            text.set(val ? BYTES_TRUE : BYTES_FALSE);
        }

        public void accept(int val) {
            if (val >= -1 && val < 511) {
                text = cache[val + 1];
                return;
            }

            int size = (val < 0) ? IOUtils.stringSize(-val) + 1 : IOUtils.stringSize(val);
            text.setCapacity(size + 2, false);
            byte[] bytes = text.bytes;
            bytes[0] = '"';
            bytes[size + 1] = '"';
            IOUtils.getChars(val, size + 1, bytes);
            text.length = size + 2;
        }

        public void accept(long val) {
            int size = (val < 0) ? IOUtils.stringSize(-val) + 1 : IOUtils.stringSize(val);
            byte[] bytes = new byte[size + 2];
            bytes[0] = '"';
            bytes[size + 1] = '"';
            IOUtils.getChars(val, size + 1, bytes);
            text.set(bytes);
        }

        @Override
        public void accept(Number val) {
            if (val instanceof Integer) {
                accept(val.intValue());
                return;
            }

            if (val instanceof Long) {
                accept(val.longValue());
                return;
            }

            String str = val.toString();
            int len = str.length() + 2;
            byte[] bytes = new byte[len + 2];
            bytes[0] = '"';
            bytes[len + 1] = '"';
            str.getBytes(0, len, bytes, 1);
            text.set(bytes);
        }

        @Override
        public void accept(String val) {
            text.set(val);
        }

        @Override
        public void accept(Map object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void accept(List array) {
            throw new UnsupportedOperationException();
        }
    }
}
