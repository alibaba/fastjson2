package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.io.Writer;

public class JSONWriter {
    private Writer out;
    private com.alibaba.fastjson2.JSONWriter raw;
    public JSONWriter(Writer out){
        this.out = out;
        raw = com.alibaba.fastjson2.JSONWriter.ofUTF8();
    }

    public void config(SerializerFeature feature, boolean state) {

    }

    public void writeObject(Object object) {
        raw.writeAny(object);
    }

    public void flush() throws IOException {
        raw.flushTo(out);
    }

    public void close() {
        raw.close();
        try {
            out.close();
        } catch (IOException ignored) {
            //
        }
    }
}
