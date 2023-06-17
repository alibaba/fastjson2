package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue312 {
    @Test
    public void test() {
        JSONObject object = JSONObject.of("file", new File("/User/xxx/JsonTest.java"));
        String json = object.toJSONString();

        byte[] fs = JSON.toJSONBytes(File.separator);
        String sep = new String(fs, 1, fs.length - 2);

        // Linux: {"file":"/User/xxx/JsonTest.java"}
        // Windows: {"file":"\\User\\xxx\\JsonTest.java"}
        assertEquals("{\"file\":\"" + sep + "User" + sep + "xxx" + sep + "JsonTest.java\"}", json);
        File file = JSON.parseObject(json).getObject("file", File.class);

        // Linux: /User/xxx/JsonTest.java
        // Windows: \User\xxx\JsonTest.java
        assertEquals(File.separator + "User" + File.separator + "xxx" + File.separator + "JsonTest.java", file.toString());
    }

    @Test
    public void test1() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String str = JSON.toJSONString(format);
        assertEquals("\"yyyy-MM-dd\"", str);
        SimpleDateFormat format2 = JSON.parseObject(str, SimpleDateFormat.class);
        assertEquals(format.toPattern(), format2.toPattern());
    }
}
