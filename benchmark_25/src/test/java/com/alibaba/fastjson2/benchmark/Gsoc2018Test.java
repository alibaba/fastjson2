package com.alibaba.fastjson2.benchmark;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class Gsoc2018Test {
    @Test
    public void test() throws Exception {
        String[] paths = new String[] {
                "data/simd-json/twitter.json", "data/simd-json/gsoc-2018.json", "data/simd-json/github_events.json"
        };
        for (String path : paths) {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
            File file = new File(resource.getFile());
            byte[] str = FileUtils.readFileToByteArray(file);
            JSON.parse(str);
        }
    }
}
