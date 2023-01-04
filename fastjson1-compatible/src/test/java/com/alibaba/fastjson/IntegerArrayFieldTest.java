package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteMapNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerArrayFieldTest {
    @Test
    public void test_codec() throws Exception {
        User user = new User();
        user.setValue(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)});

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);
        String text = JSON.toJSONString(user, mapping, WriteMapNullValue);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getValue()[0], user.getValue()[0]);
        assertEquals(user1.getValue()[1], user.getValue()[1]);
    }

    @Test
    public void test_codec_null() throws Exception {
        User user = new User();
        user.setValue(null);

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);
        String text = JSON.toJSONString(user, mapping, WriteMapNullValue);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getValue(), user.getValue());
    }

    @Test
    public void test_codec_null_1() throws Exception {
        User user = new User();
        user.setValue(null);

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);
        String text = JSON.toJSONString(user, mapping, WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(0, user1.getValue().length);
    }

    public static class User {
        private Integer[] value;

        public Integer[] getValue() {
            return value;
        }

        public void setValue(Integer[] value) {
            this.value = value;
        }
    }
}
