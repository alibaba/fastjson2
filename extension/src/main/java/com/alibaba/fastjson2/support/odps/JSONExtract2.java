package com.alibaba.fastjson2.support.odps;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.aliyun.odps.udf.UDF;

public class JSONExtract2
        extends UDF {
    public String evaluate(String json, String path) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        Object result = null;
        try {
            result = JSONPath.of(path).extract(JSONReader.of(json));
        } catch (Throwable ignored) {
            // ignored
        }

        if (result == null) {
            return null;
        }
        return JSON.toJSONString(result);
    }
}
