package com.alibaba.fastjson2.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NonDefaulConstructorTestTest2 {
    @Test
    public void test_b() {
        JSONObject obj = JSONObject.of("id", 3);
        String str = obj.toString();
        assertEquals(3, JSON.parseObject(str, B.class).id);

        byte[] bytes = JSONB.toBytes(obj);
        assertEquals(3, JSONB.parseObject(bytes, B.class).id);
    }

    public static class B {
        public final long id;
        public final String name;

        public B(@JSONField(name = "id") long id) {
            this.id = id;
            this.name = null;
        }

        public B(long id, String name) {
            this.id = id;
            if (name == null) {
                throw new IllegalArgumentException("name is null");
            }
            this.name = name;
        }
    }
}
