package com.alibaba.fastjson2.spring.issues.issue337;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.data.redis.FastJsonRedisSerializer;
import com.alibaba.fastjson2.support.spring.data.redis.GenericFastJsonRedisSerializer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue337 {
    @Test
    public void test_0() {
        User user1 = new User(1, "test_1", 15);
        User user2 = new User(2, "test_2", 25);
        User user3 = new User(3, "test_3", 35);
        List<User> listSer = Arrays.asList(user1, user2, user3);

        byte[] jsonBytes = JSON.toJSONBytes(listSer, JSONWriter.Feature.WriteClassName);
        List<Object> listDes = (List<Object>) JSON.parseObject(jsonBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertTrue(listDes instanceof JSONArray);
//        assertFalse(listDes.get(0) instanceof JSONObject); // Expected false
//        assertTrue(listDes.get(0) instanceof User); // Expected true
    }

    @Test
    public void test_1() {
        User user1 = new User(1, "test_1", 15);
        User user2 = new User(2, "test_2", 25);
        User user3 = new User(3, "test_3", 35);
        List<User> listSer = Arrays.asList(user1, user2, user3);

        byte[] jsonBytes = JSON.toJSONBytes(listSer, JSONWriter.Feature.WriteClassName);
        List<User> listDes = (List<User>) JSON.parseObject(jsonBytes, List.class, JSONReader.Feature.SupportAutoType);
        assertTrue(listDes.get(0) instanceof User); // Expected true
    }

    @Test
    public void test_2() {
        User user1 = new User(1, "test_1", 25);
        User user2 = new User(2, "test_2", 35);
        BaseResult<User> baseResult1 = new BaseResult<>();
        baseResult1.setData(user1);
        baseResult1.setMsg("msg001");
        baseResult1.setCode("001");
        BaseResult<User> baseResult2 = new BaseResult<>();
        baseResult2.setData(user2);
        baseResult2.setMsg("msg002");
        baseResult2.setCode("002");
        List<BaseResult<User>> listSer = new ArrayList<>();
        listSer.add(baseResult1);
        listSer.add(baseResult2);

        GenericFastJsonRedisSerializer serializer = new GenericFastJsonRedisSerializer();
        List<BaseResult<User>> listDes = (List<BaseResult<User>>) serializer.deserialize(serializer.serialize(listSer));
        assertTrue(listDes.size() == 2);
//        assertTrue(listDes.get(0) instanceof BaseResult); // Expected true
    }

    @Test
    public void test_3() {
        User user1 = new User(1, "test_1", 25);
        User user2 = new User(2, "test_2", 35);
        BaseResult<User> baseResult1 = new BaseResult<>();
        baseResult1.setData(user1);
        baseResult1.setMsg("msg001");
        baseResult1.setCode("001");
        BaseResult<User> baseResult2 = new BaseResult<>();
        baseResult2.setData(user2);
        baseResult2.setMsg("msg002");
        baseResult2.setCode("002");
        List<BaseResult<User>> listSer = new ArrayList<>();
        listSer.add(baseResult1);
        listSer.add(baseResult2);

        FastJsonRedisSerializer serializer = new FastJsonRedisSerializer(List.class);
        FastJsonConfig config = new FastJsonConfig();
        config.setReaderFeatures(JSONReader.Feature.SupportAutoType);
        config.setWriterFeatures(JSONWriter.Feature.WriteClassName);
        serializer.setFastJsonConfig(config);
        List<BaseResult<User>> listDes = (List<BaseResult<User>>) serializer.deserialize(serializer.serialize(listSer));
        assertTrue(listDes instanceof List);
        assertTrue(listDes.get(0) instanceof BaseResult); // Expected true
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

    static class BaseResult<T> {
        private String msg;
        private String code;
        private T data;

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
