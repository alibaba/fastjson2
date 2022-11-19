package com.alibaba.fastjson2.support.odps;

import com.alibaba.fastjson2.JSONReader;
import com.aliyun.odps.io.Text;
import com.aliyun.odps.io.Writable;

import java.nio.charset.StandardCharsets;

public class JSONExtract
        extends JSONExtractScalar {
    public JSONExtract(String path) {
        super(path);
        valueConsumer = new ExtractValueConsumer();
    }

    public Writable eval(Text input) {
        JSONReader parser = JSONReader.of(input.getBytes(),
                0,
                input.getLength(), StandardCharsets.UTF_8
        );

        path.extract(parser, valueConsumer);
        return text;
    }

    class ExtractValueConsumer
            extends JSONExtractScalar.ExtractValueConsumer {
        @Override
        public void accept(byte[] bytes, int off, int len) {
            if (off > 0) {
                int end = off + len;
                if (end < bytes.length
                        && bytes[off - 1] == bytes[end]
                ) {
                    byte quote = bytes[end];
                    if (quote == '"' || quote == '\'') {
                        text.bytes = bytes;
                        text.off = off - 1;
                        text.length = len + 2;
                        return;
                    }
                }
            }

            text.bytes = bytes;
            text.off = off;
            text.length = len;
        }

        @Override
        public void accept(String str) {
            int len = str.length() + 2;
            byte[] bytes = new byte[len];
            bytes[0] = '"';
            bytes[len - 1] = '"';
            str.getBytes(0, str.length(), bytes, 1);
            text.set(bytes);
        }
    }
}
