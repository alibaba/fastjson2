package com.alibaba.fastjson2.support.odps;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.aliyun.odps.io.IntWritable;
import com.aliyun.odps.io.Text;
import com.aliyun.odps.udf.UDF;

import java.nio.charset.StandardCharsets;

public class JSONExtractInt32
        extends UDF {
    private final JSONPath path;
    private final IntWritable result = new IntWritable();

    public JSONExtractInt32(String path) {
        this.path = JSONPath.of(path);
    }

    public IntWritable eval(Text input) {
        JSONReader parser = JSONReader.of(input.getBytes(), 0, input.getLength(), StandardCharsets.UTF_8);
        int value = path.extractInt32Value(parser);
        if (parser.wasNull()) {
            return null;
        }
        result.set(value);
        return result;
    }
}
