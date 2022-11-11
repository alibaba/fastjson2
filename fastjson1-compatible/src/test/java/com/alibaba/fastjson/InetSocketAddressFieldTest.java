package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InetSocketAddressFieldTest {
    @Test
    public void test_codec() throws Exception {
        User user = new User();
        user.setValue(new InetSocketAddress(33));

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);
        String text = JSON.toJSONString(user, mapping, SerializerFeature.WriteMapNullValue);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getValue(), user.getValue());
    }

    @Test
    public void test_codec_null() throws Exception {
        User user = new User();
        user.setValue(null);

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);
        String text = JSON.toJSONString(user, mapping, SerializerFeature.WriteMapNullValue);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getValue(), user.getValue());
    }

    @Test
    public void test_codec_null_2() throws Exception {
        User user = JSON.parseObject("{\"value\":{\"address\":null,\"port\":33}}", User.class);

        assertEquals(33, user.getValue().getPort());
    }

    public static class User {
        private InetSocketAddress value;

        public InetSocketAddress getValue() {
            return value;
        }

        public void setValue(InetSocketAddress value) {
            this.value = value;
        }
    }
}
