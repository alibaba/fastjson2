package com.alibaba.fastjson2.support.airlift;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import io.airlift.slice.Slice;

public class JSONFunctions {
    public static Slice jsonExtract(Slice json, JSONPath path) {
        byte[] bytes = json.byteArray();
        int off = json.byteArrayOffset();
        int length = json.length();
        JSONReader jsonReader = JSONReader.of(bytes, off, length);
        SliceValueConsumer sliceValueConsumer = new SliceValueConsumer();
        path.extract(jsonReader, sliceValueConsumer);
        return sliceValueConsumer.slice;
    }
}
