package com.alibaba.fastjson;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatternFieldTest {
    @Test
    public void test_codec() throws Exception {
        User user = new User();
        user.setValue(Pattern.compile("."));

        SerializeConfig mapping = new SerializeConfig();
        mapping.setAsmEnable(false);
        String text = JSON.toJSONString(user, mapping, SerializerFeature.WriteMapNullValue);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getValue().pattern(), user.getValue().pattern());
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

    public static class User {
        private Pattern value;

        public Pattern getValue() {
            return value;
        }

        public void setValue(Pattern value) {
            this.value = value;
        }
    }
}
