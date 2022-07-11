package com.alibaba.fastjson2.eishay;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.eishay.vo.MediaContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBArrayMapping {
    String str;
    MediaContent object;

    @BeforeEach
    public void init() {
        URL url = this.getClass().getClassLoader().getResource("data/eishay.json");
        object = JSON.parseObject(url, MediaContent.class);
        str = JSON.toJSONString(object);
    }

    @Test
    public void test() {
        byte[] bytes = JSONB.toBytes(object, JSONWriter.Feature.BeanToArray, JSONWriter.Feature.WriteClassName);
        Object object1 = JSONB.parseObject(bytes, Object.class, JSONReader.Feature.SupportAutoType, JSONReader.Feature.SupportArrayToBean);
        assertEquals(object, object1);
    }
}
