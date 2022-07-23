package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapTest {
    @Test
    public void test() {
        JSONObject object = JSONObject
                .of("", 0)
                .fluentPut("1", 1)
                .fluentPut("12", 12)
                .fluentPut("123", 123)
                .fluentPut("1234", 1234)
                .fluentPut("12345", 12345)
                .fluentPut("123456", 123456)
                .fluentPut("1234567", 1234567)
                .fluentPut("12345678", 12345678)
                .fluentPut("123456789", 123456789)
                .fluentPut("1234567890", 1234567890)
                .fluentPut("lastName", 1001)
                .fluentPut("UserName", 102);

        for (int i = 0; i < 3; ++i) {
            byte[] bytes = JSONB.toBytes(object);
            JSONObject object1 = JSONB.parseObject(bytes);
            assertEquals(object, object1);
        }
    }

    @Test
    public void test1() {
        JSONObject object = JSONObject
                .of("", 0)
                .fluentPut("1", 1)
                .fluentPut("12", 12)
                .fluentPut("123", 123)
                .fluentPut("1234", 1234)
                .fluentPut("12345", 12345)
                .fluentPut("123456", 123456)
                .fluentPut("1234567", 1234567)
                .fluentPut("12345678", 12345678)
                .fluentPut("123456789", 123456789)
                .fluentPut("1234567890", 1234567890)
                .fluentPut("lastName", 1001)
                .fluentPut("UserName", 102);
        for (int i = 0; i < 3; ++i) {
            Bean bean = new Bean();
            bean.values = object;
            byte[] bytes = JSONB.toBytes(bean);

            Bean bean1 = JSONB.parseObject(bytes, Bean.class);
            assertEquals(bean.values, bean1.values);
        }
    }

    @Test
    public void test2() {
        JSONObject object = new JSONObject();
        for (int i = 10000; i < 20000; i++) {
            object.put(Integer.toString(i), i);
        }
        for (int i = 100000; i < 110000; i++) {
            object.put(Integer.toString(i), i);
        }
        for (int i = 1000000; i < 1010000; i++) {
            object.put(Integer.toString(i), i);
        }
        for (int i = 10000000; i < 10010000; i++) {
            object.put(Integer.toString(i), i);
        }

        String str = JSON.toJSONString(object);
        JSONReader[] jsonReaders4 = TestUtils.createJSONReaders4(str);
        for (int i = 0; i < jsonReaders4.length; i++) {
            JSONReader jsonReader = jsonReaders4[i];
            Map<String, Object> map1 = jsonReader.readObject();
            assertEquals(object.size(), map1.size());
            assertEquals(object, map1);
        }

        {
            byte[] jsonbBytes = JSONB.toBytes(object);
            JSONObject map1 = JSONB.parseObject(jsonbBytes, JSONObject.class);
            assertEquals(object.size(), map1.size());
            assertEquals(object, map1);
        }
    }

    public static class Bean {
        public Map values;
    }
}
