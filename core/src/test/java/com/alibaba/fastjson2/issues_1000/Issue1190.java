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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1190 {
    static final Filter autoTypeFilter = JSONReader.autoTypeFilter(B.class, B1.class);

    @Test
    public void test() {
        B b = new B.Builder()
                .withAge(123)
                .withUsername("yh")
                .build();
        byte[] bytes = serialize(b);

        B b1 = (B) deserialize(bytes);
        assertEquals(b.age, b1.age);
        assertEquals(b.username, b1.username);
    }

    @Test
    public void test1() {
        B1 b = new B1.Builder()
                .age(123)
                .username("yh")
                .build();
        byte[] bytes = serialize(b);

        B1 b1 = (B1) deserialize(bytes);
        assertEquals(b.age, b1.age);
        assertEquals(b.username, b1.username);
    }

    @Test
    public void test2() {
        B2 b = new B2.Builder()
                .age(123)
                .username("yh")
                .build();
        byte[] bytes = serialize(b);

        B2 b1 = (B2) deserialize(bytes);
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

    @Getter
    @ToString
    @JSONType(builder = B1.Builder.class)
    public static class B1
            implements Serializable {
        private String username;
        private int age;

        protected B1() {
        }

        public static class Builder
                implements Serializable {
            private String username;
            private int age;

            @JSONField
            public Builder username(String username) {
                this.username = username;
                return this;
            }

            @JSONField
            public Builder age(int age) {
                this.age = age;
                return this;
            }

            public B1 build() {
                B1 b = new B1();
                b.age = this.age;
                b.username = this.username;
                return b;
            }
        }
    }

    @Getter
    @ToString
    @JSONType(builder = B2.Builder.class, deserializeFeatures = JSONReader.Feature.SupportSmartMatch)
    public static class B2
            implements Serializable {
        private String username;
        private int age;

        protected B2() {
        }

        public static class Builder
                implements Serializable {
            private String username;
            private int age;

            public Builder username(String username) {
                this.username = username;
                return this;
            }

            public Builder age(int age) {
                this.age = age;
                return this;
            }

            public B2 build() {
                B2 b = new B2();
                b.age = this.age;
                b.username = this.username;
                return b;
            }
        }
    }
}
