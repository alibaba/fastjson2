package com.alibaba.fastjson.support.spring;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.google.common.base.Objects;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FastJsonRedisSerializerTest {
    private FastJsonRedisSerializer<User> serializer;

    @BeforeEach
    public void setUp() {
        this.serializer = new FastJsonRedisSerializer<User>(User.class);
    }

    @Test
    public void test_1() {
        User user = serializer.deserialize(serializer.serialize(new User(1, "土豆", 25)));
        Assertions.assertTrue(Objects.equal(user.getId(), 1));
        Assertions.assertTrue(Objects.equal(user.getName(), "土豆"));
        Assertions.assertTrue(Objects.equal(user.getAge(), 25));
    }

    @Test
    public void test_2() {
        MatcherAssert.assertThat(serializer.serialize(null), Is.is(new byte[0]));
    }

    @Test
    public void test_3() {
        MatcherAssert.assertThat(serializer.deserialize(new byte[0]), IsNull.nullValue());
    }

    @Test
    public void test_4() {
        MatcherAssert.assertThat(serializer.deserialize(null), IsNull.nullValue());
    }

    @Test
    public void test_5() {
        User user = new User(1, "土豆", 25);
        byte[] serializedValue = serializer.serialize(user);
        Arrays.sort(serializedValue); // corrupt serialization result
        assertThrows(SerializationException.class, () -> serializer.deserialize(serializedValue));
    }

    /**
     * for issue #2147
     */
    @Test
    public void test_6() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();

        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteClassName);

        fastJsonConfig.setFeatures(Feature.SupportAutoType);

        FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
        Assertions.assertNotNull(fastJsonRedisSerializer.getFastJsonConfig());
        fastJsonRedisSerializer.setFastJsonConfig(fastJsonConfig);

        User userSer = new User(1, "土豆", 25);

        byte[] serializedValue = fastJsonRedisSerializer.serialize(userSer);
        User userDes = (User) fastJsonRedisSerializer.deserialize(serializedValue);

        Assertions.assertEquals(userDes.getName(), "土豆");
    }

    static class User {
        private Integer id;
        private String name;
        private Integer age;

        public User() {
        }

        public User(Integer id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }
}
