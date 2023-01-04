package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.filter.Filter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue389 {
    @Test
    public void test() {
        byte[] utf8Bytes = "".getBytes(StandardCharsets.UTF_8);
        assertNull(JSON.parseObject(""));
        assertNull(JSON.parseObject(utf8Bytes));

        assertNull(JSON.parse(null));
        assertNull(JSON.parse(""));
        assertNull(JSON.parse((String) null, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parse("", JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parse(null, (JSONReader.Context) null));
        assertNull(JSON.parse("", (JSONReader.Context) null));

        assertNull(JSON.parse((byte[]) null));
        assertNull(JSON.parse(new byte[0]));

        assertNull(JSON.parseObject((String) null));
        assertNull(JSON.parseObject((byte[]) null));
        assertNull(JSON.parseObject((InputStream) null));
        assertNull(JSON.parseObject((Reader) null));

        assertNull(JSON.parseObject((String) null, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject((byte[]) null, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject((InputStream) null, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject((Reader) null, JSONReader.Feature.SupportSmartMatch));

        assertNull(JSON.parseObject(null, (JSONReader.Context) null));
        assertNull(JSON.parseObject("", (JSONReader.Context) null));

        assertNull(JSON.parseObject("", Object.class));
        assertNull(JSON.parseObject(utf8Bytes, Object.class));

        assertNull(JSON.parseObject("", Object.class, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject("", (Type) Object.class, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject(utf8Bytes, Object.class, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject(utf8Bytes, (Type) Object.class, JSONReader.Feature.SupportSmartMatch));

        assertNull(JSON.parseObject((String) null, (Type) Object.class, (Filter) null, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject("", (Type) Object.class, (Filter) null, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject((byte[]) null, (Type) Object.class, (Filter) null, JSONReader.Feature.SupportSmartMatch));
        assertNull(JSON.parseObject(utf8Bytes, (Type) Object.class, (Filter) null, JSONReader.Feature.SupportSmartMatch));

        assertNull(JSON.parseObject((String) null, Object.class));
        assertNull(JSON.parseObject((byte[]) null, Object.class));
        assertNull(JSON.parseObject((InputStream) null, Object.class));
        assertNull(JSON.parseObject((Reader) null, Object.class));
        assertNull(JSON.parseObject((URL) null, Object.class));
        assertNull(JSON.parseObject((URL) null, (Type) Object.class));
        assertNull(JSON.parseObject((URL) null, (Function) null));

        assertNull(JSON.parseObject("", Object.class));
        assertNull(JSON.parseObject(new byte[0], Object.class));
        assertNull(JSON.parseObject(new ByteArrayInputStream(new byte[0]), Object.class));
        assertNull(JSON.parseObject(new StringReader(""), Object.class));

        assertNull(JSON.parseObject((String) null, Object.class, (JSONReader.Context) null));
        assertNull(JSON.parseObject("", Object.class, (JSONReader.Context) null));

        assertNull(JSON.parseObject(new ByteArrayInputStream(utf8Bytes)));
        assertNull(JSON.parseObject(new StringReader("")));
        assertTrue(JSON.parseObject(new StringReader("{}")).isEmpty());

        assertNull(JSON.parseObject(new ByteArrayInputStream(utf8Bytes), Object.class));
        assertNull(JSON.parseObject(new StringReader(""), Object.class));
        assertTrue(((JSONObject) JSON.parseObject(new StringReader("{}"), JSONObject.class)).isEmpty());
    }
}
