package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Issue3932 {
    @Test
    public void test() {
        try {
            JSON.register(Long.class, ObjectWriters.ofToString(Object::toString));

            String str = "{\"id\":958829775217152}";
            JSONObject obj = (JSONObject) JSON.parse(str.getBytes());
            SerializeTest serializeTest = obj.toJavaObject(SerializeTest.class);
            assertEquals("958829775217152", serializeTest.getId());
        } finally {
            JSON.register(Long.class, (ObjectWriter) null);
        }
    }

    @Data
    public static class SerializeTest implements Serializable {
        private String id;
    }
}
