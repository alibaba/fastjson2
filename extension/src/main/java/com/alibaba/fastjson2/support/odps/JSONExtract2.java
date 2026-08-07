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
        try (JSONReader jsonReader = JSONReader.of(json)) {
            result = JSONPath.of(path)
                    .extract(jsonReader);
        } catch (Throwable ignored) {
            // ignored
        }

        if (result == null) {
            return null;
        }
        try {
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return null;
        }
    }
}
