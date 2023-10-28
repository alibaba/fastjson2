package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.*;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1942 {
    @Test
    public void test() {
        for (int i = 0; i < 10; i++) {
            String json = "{\"id\":1,\"name\":\"joe\"}";
            JSONObject obj = JSON.parseObject(json);
            SerializeConfig config = new SerializeConfig();
            config.put(Integer.class, DecimalSerializer.instance);

            SysDic dic = obj.toJavaObject(SysDic.class);
            assertEquals("{\"id\":1,\"name\":\"joe\"}", JSON.toJSONString(dic));
        }
    }

    public static class DecimalSerializer
            implements ObjectSerializer {
        public static DecimalSerializer instance = new DecimalSerializer();
        private static final DecimalFormat format = new DecimalFormat("#.################");

        public void write(
                JSONSerializer serializer,
                Object object,
                Object fieldName,
                Type fieldType,
                int features
        ) throws IOException {
            SerializeWriter out = serializer.getWriter();
            String value;
            if (object instanceof Long || object instanceof Float) {
                value = format.format(object);
            } else {
                value = object.toString();
            }
            out.write(value);
        }
    }

    @Data
    public class SysDic
            implements Serializable {
        private Long id;
        private String name;
    }
}
