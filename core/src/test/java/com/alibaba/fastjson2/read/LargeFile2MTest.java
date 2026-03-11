package com.alibaba.fastjson2.read;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

@Tag("reader")
public class LargeFile2MTest {
    String str;
    @BeforeEach
    public void init() throws Exception {
        try (
                InputStream fis = LargeFile2MTest.class.getClassLoader().getResourceAsStream("data/large-file-2m.json.zip");
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream zipIn = new ZipInputStream(bis)
        ) {
            zipIn.getNextEntry();
            str = IOUtils.toString(zipIn, "UTF-8");
        } catch (IOException ignored) {
            // ignored
        }
    }

    @Test
    public void test() {
        JSONReader.Context context = JSONFactory.createReadContext();
        JSON.parseObject(str, context);
    }

    @Test
    public void test1() {
        JSONReader.Context context = JSONFactory.createReadContext();

        JSON.parseObject(str, 0, str.length(), context);
    }

    @Test
    public void test2() {
        JSONReader.Context context = JSONFactory.createReadContext();
        JSON.parseObject(str, JSONObject.class, context);
    }
}
