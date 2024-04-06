package com.alibaba.fastjson2.issues_2300;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2391 {
    @Test
    public void test() {
        String str = "{\"vip\": \"7\\u5929VIP\\u4F53\\u9A8C\\u5361\"}";
        com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSON.parseObject(str);
        String str1 = object.toJSONString(object, SerializerFeature.BrowserCompatible);
        String str2 = JSON.toJSONString(object, JSONWriter.Feature.EscapeNoneAscii);
        System.out.println(str1);
        System.out.println(str2);
        assertEquals(str1, str2);
    }
}
