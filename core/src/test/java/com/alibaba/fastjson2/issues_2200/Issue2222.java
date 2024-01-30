package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue2222 {
    @Test
    public void test() {
        String str = "{\"uid\":\"38316683\",\"roleid\":\"61dbb6306123a05f183b1e19\",\"d";
        String arrStr = "[{\"uid\":\"38316683\",\"roleid\":\"61dbb6306123a05f183b1e19\",\"d";
        assertFalse(JSON.isValid(str));
        assertFalse(JSON.isValid(str, JSONReader.Feature.AllowUnQuotedFieldNames));
        assertFalse(JSON.isValid(str.toCharArray()));
        assertFalse(JSON.isValid(str.getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValid(str.getBytes(), StandardCharsets.UTF_8));
        assertFalse(JSON.isValid(str.getBytes(), 0, str.length(), StandardCharsets.UTF_8));
        assertFalse(JSON.isValidObject(str));
        assertFalse(JSON.isValidObject(str.getBytes(StandardCharsets.UTF_8)));
        assertFalse(JSON.isValidArray(arrStr));
        assertFalse(JSON.isValidArray(arrStr.getBytes(StandardCharsets.UTF_8)));
    }
}
