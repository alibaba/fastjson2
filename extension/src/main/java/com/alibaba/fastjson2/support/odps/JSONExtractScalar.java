package com.alibaba.fastjson2.support.odps;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.IOUtils;
import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.Writable;
import com.aliyun.odps.udf.UDF;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class JSONExtractScalar
        extends UDF {
    static final byte[] BYTES_TRUE = "true".getBytes(StandardCharsets.UTF_8);
    static final byte[] BYTES_FALSE = "false".getBytes(StandardCharsets.UTF_8);
    static final byte[] BYTES_NULL = "null".getBytes(StandardCharsets.UTF_8);

    final JSONPath path;
    JSONWritable text = new JSONWritable();

    ExtractValueConsumer valueConsumer = new ExtractValueConsumer();

    public JSONExtractScalar(String path) {
        this.path = JSONPath.of(path);
    }

    public Writable eval(Text input) {
        JSONReader parser = JSONReader.of(input.getBytes(),
                0,
                input.getLength(), StandardCharsets.UTF_8
        );

        path.extractScalar(parser, valueConsumer);
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
            text.set(BYTES_NULL);
        }

        @Override
        public void accept(boolean val) {
            text.set(val ? BYTES_TRUE : BYTES_FALSE);
        }

        @Override
        public void accept(int val) {
            int size = (val < 0) ? IOUtils.stringSize(-val) + 1 : IOUtils.stringSize(val);
            text.setCapacity(size, false);
            byte[] bytes = text.bytes;
            IOUtils.getChars(val, size, bytes);
            text.length = size;
        }

        @Override
        public void accept(long val) {
            int size = (val < 0) ? IOUtils.stringSize(-val) + 1 : IOUtils.stringSize(val);
            text.setCapacity(size, false);
            byte[] bytes = text.bytes;
            IOUtils.getChars(val, size, bytes);
            text.length = size;
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
            text.set(str);
        }

        @Override
        public void accept(String val) {
            text.set(val);
        }

        @Override
        public void accept(Map object) {
            text.set(JSON.toJSONBytes(object));
        }

        @Override
        public void accept(List array) {
            text.set(JSON.toJSONBytes(array));
        }
    }
}
