package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1040 {
    @Test
    public void test() {
        Path path = FileSystems.getDefault().getPath("/usr/bin");
        Map<String, Object> ext = new HashMap<>();
        ext.put("test1", path);

        byte[] fs = JSON.toJSONBytes(File.separator);
        String sep = new String(fs, 1, fs.length - 2);

        // Linux: {"test1":"/usr/bin"}
        // Windows: {"test1":"\\usr\\bin"}
        assertEquals("{\"test1\":\"" + sep + "usr" + sep + "bin\"}", JSONObject.toJSONString(ext));
    }
}
