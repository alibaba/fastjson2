package com.alibaba.fastjson2;

public class JSONReaderUtils {
    public static JSONReader createJSONReader(String str) {
        return new JSONReaderStr(JSONFactory.createReadContext(), str, 0, str.length());
    }
}
