package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteArrayFieldTest_2 {
    @Test
    public void test_0() throws Exception {
        Entity entity = new Entity("中华人民共和国");
        String text = JSON.toJSONString(entity);
        JSONObject json = JSON.parseObject(text);
        assertEquals(TestUtils.encodeToBase64String(entity.getValue(), false), json.getString("value"));

        Entity entity2 = JSON.parseObject(text, Entity.class);
        assertEquals("中华人民共和国", new String(entity2.getValue(), "UTF-8"));
    }

    public static class Entity {
        private byte[] value;

        public Entity() {
        }

        public Entity(String value) throws UnsupportedEncodingException {
            this.value = value.getBytes("UTF-8");
        }

        public byte[] getValue() {
            return value;
        }

        public void setValue(byte[] value) {
            this.value = value;
        }
    }
}
