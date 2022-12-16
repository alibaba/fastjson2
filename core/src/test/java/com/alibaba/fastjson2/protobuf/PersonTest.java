package com.alibaba.fastjson2.protobuf;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonTest {
    @Test
    public void test() {
        PersonOuterClass.Person john = PersonOuterClass.Person.newBuilder()
                .setId(1234)
                .setName("John Doe")
                .setEmail("jdoe@example.com")
                .build();

        String str = JSON.toJSONString(john);
        JSONObject jsonObject = JSON.parseObject(str);
        assertEquals(john.getEmail(), jsonObject.getString("email"));
        assertEquals(john.getId(), jsonObject.getIntValue("id"));
        assertEquals(john.getName(), jsonObject.getString("name"));
    }

    @Test
    public void test1() {
        PersonOuterClass.Person john = PersonOuterClass.Person.newBuilder()
                .setId(1234)
                .setName("John Doe")
                .setEmail("jdoe@example.com")
                .build();

        String str = JSON.toJSONString(john, JSONWriter.Feature.FieldBased);
        JSONObject jsonObject = JSON.parseObject(str);
        assertEquals(john.getEmail(), jsonObject.getString("email"));
        assertEquals(john.getId(), jsonObject.getIntValue("id"));
        assertEquals(john.getName(), jsonObject.getString("name"));
    }

    @Test
    public void test2() {
        PersonOuterClass.Person john = PersonOuterClass.Person.newBuilder()
                .setId(1234)
                .setName("John Doe")
                .setEmail("jdoe@example.com")
                .build();

        byte[] jsonbBytes = JSONB.toBytes(john, JSONWriter.Feature.FieldBased);
        JSONObject jsonObject = JSONB.parseObject(jsonbBytes);
        assertEquals(john.getEmail(), jsonObject.getString("email"));
        assertEquals(john.getId(), jsonObject.getIntValue("id"));
        assertEquals(john.getName(), jsonObject.getString("name"));
    }
}
