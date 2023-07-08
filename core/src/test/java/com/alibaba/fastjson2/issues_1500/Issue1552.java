package com.alibaba.fastjson2.issues_1500;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1552 {
    @Test
    public void test() throws Exception {
        File tempFile = File.createTempFile("tmp", "json");

        Bean bean = new Bean();
        bean.in = new FileInputStream(tempFile);

        String str = JSON.toJSONString(bean);
        assertNotNull(str);
    }

    public static class Bean {
        public InputStream in;
    }
}
