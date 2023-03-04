package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.filter.Filter;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.SerializationException;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1190 {
    static final Filter autoTypeFilter = JSONReader.autoTypeFilter(B.class);

    @Test
    public void test6() {
        B b = new B.Builder()
                .withAge(123)
                .withUsername("yh")
                .build();
        byte[] bytes = serialize(b);
        assertEquals(
                "{\"@type\":\"com.alibaba.fastjson2.issues_1000.Issue1190$B\",\"age\":123,\"username\":\"yh\"}",
                new String(bytes, StandardCharsets.UTF_8)
        );

        B b1 = (B) deserialize(bytes);
        assertEquals(b.age, b1.age);
        assertEquals(b.username, b1.username);
    }

    public byte[] serialize(Object o) throws SerializationException {
        if (o == null) {
            return new byte[0];
        }
        return JSON.toJSONBytes(o, JSONWriter.Feature.WriteClassName);
    }

    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        return JSON.parseObject(bytes, Object.class, autoTypeFilter, JSONReader.Feature.SupportAutoType);
    }

    @Getter
    @ToString
    @JSONType(builder = B.Builder.class)
    public static class B
            implements Serializable {
        private String username;
        private int age;

        protected B() {
        }

        public static class Builder
                implements Serializable {
            private String username;
            private int age;

            public Builder withUsername(String username) {
                this.username = username;
                return this;
            }

            @JSONField
            public Builder withAge(int age) {
                this.age = age;
                return this;
            }

            public B build() {
                B b = new B();
                b.age = this.age;
                b.username = this.username;
                return b;
            }
        }
    }
}
