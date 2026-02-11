package com.alibaba.fastjson2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3984 {
    @Test
    public void test() {
        ValueFilter filter = (object, property, propertyValue) -> propertyValue;
        CustomMap msg = new CustomMap();
        msg.put("123", "456");
        CustomMap msg2 = new CustomMap();
        msg2.put("msg222", 123);
        msg.put("map", msg2);
        assertEquals(JSON.toJSONString(msg, SerializerFeature.WriteClassName), JSON.toJSONString(msg, filter, SerializerFeature.WriteClassName));
    }

    public static class CustomMap
            extends HashMap<String, Object> {}
}
