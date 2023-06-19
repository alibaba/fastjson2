package com.alibaba.fastjson2.internal.processor.eishay;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class MediaContentTest {
    @Test
    public void test() {
        URL url = this.getClass().getClassLoader().getResource("data/eishay.json");
        MediaContent object = JSON.parseObject(url, MediaContent.class);
    }
}
