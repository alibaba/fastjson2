package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.reader.ObjectReader;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1168 {
    @Test
    void test01() {
        String json1 = "{\"inner\":{\"id\":\"1\"}}";
        Wrapper wrapper = JSON.parseObject(json1, Wrapper.class); // Type参数有值
        System.out.println(wrapper);

        String json2 = "{\"id\":\"1\"}";
        Inner inner = JSON.parseObject(json2, Inner.class); // Type为空
        assertNotNull(inner);
    }

    public static class InnerReader
            implements ObjectReader<Inner> {
        @Override
        public Inner readObject(JSONReader reader, Type type, Object name, long features) {
            if (type == null) {
                return null;
            }
            Map<String, Object> map = reader.readObject();
            Inner inner = new Inner();
            inner.setId(Objects.toString(map.get("id")));
            return inner;
        }
    }

    @Data
    public static class Wrapper {
        private Inner inner;
    }

    @Data
    @JSONType(deserializer = InnerReader.class)
    public static class Inner {
        private String id;
    }
}
