package com.alibaba.fastjson2.support.odps;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.aliyun.odps.io.LongWritable;
import com.aliyun.odps.io.Text;
import com.aliyun.odps.udf.UDF;

import java.nio.charset.StandardCharsets;

public class JSONExtractInt64
        extends UDF {
    private final JSONPath path;
    private final LongWritable result = new LongWritable();

    public JSONExtractInt64(String path) {
        this.path = JSONPath.of(path);
    }

    public LongWritable eval(Text input) {
        JSONReader parser = JSONReader.of(input.getBytes(), 0, input.getLength(), StandardCharsets.UTF_8);
        long value = path.extractInt64Value(parser);
        if (parser.wasNull()) {
            return null;
        }
        result.set(value);
        return result;
    }
}
