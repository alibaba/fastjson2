package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

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
        assertEquals("{\"test1\":\"/usr/bin\"}", JSONObject.toJSONString(ext));
    }
}
